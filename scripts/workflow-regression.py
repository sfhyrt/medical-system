"""Verify real workflow HTTP requests and clean only this run's synthetic fixture."""
import json
import os
import shutil
import subprocess
import sys
import urllib.error
import urllib.parse
import urllib.request
import uuid
from datetime import date, timedelta
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
BASE = (sys.argv[1] if len(sys.argv)>1 else 'http://127.0.0.1:8080').rstrip('/')+'/api'
OUTPUT = Path(sys.argv[2] if len(sys.argv)>2 else ROOT/'target/workflow-regression.json')
PREFIX = 'WF_'+uuid.uuid4().hex[:12].upper()
JAVA = shutil.which('java')
DRIVERS = sorted((Path.home()/'.m2/repository/com/mysql/mysql-connector-j').glob('*/mysql-connector-j-*.jar'))
if not JAVA or not DRIVERS:
    raise SystemExit('JDK 21 and the Maven MySQL JDBC driver are required')
def fixture(operation):
    return subprocess.check_output([JAVA,'-cp',str(DRIVERS[-1]),str(ROOT/'scripts/WorkflowFixtures.java'),operation,PREFIX],cwd=ROOT,text=True,encoding='utf-8')
def request(method,path,payload=None):
    data=None if payload is None else json.dumps(payload).encode()
    req=urllib.request.Request(BASE+path,data=data,method=method,headers={'Content-Type':'application/json'})
    try:
        with urllib.request.urlopen(req,timeout=20) as response:return response.status,json.loads(response.read())
    except urllib.error.HTTPError as error:return error.code,json.loads(error.read())
cases=[]
def check(name,method,path,payload=None,status=200,verify=lambda value:True):
    actual,value=request(method,path,payload)
    assert actual==status and verify(value),(name,actual,value)
    cases.append({'case':name,'method':method,'path':path,'expected':status,'actual':actual,'passed':True})
    return value
def err(code):return lambda v:v.get('code')==code and not any(s in v.get('message','') for s in ['SELECT ','jdbc:mysql','SQLException'])
f=json.loads(fixture('setup'))
cleaned=False
try:
    tomorrow=(date.today()+timedelta(days=1)).isoformat()
    schedule_body={'doctorId':f['doctor'],'visitDate':tomorrow,'session':'AM','capacity':2}
    s=check('create schedule','POST','/schedules',schedule_body,201);sid=s['id']
    check('duplicate schedule','POST','/schedules',schedule_body,409,err('SCHEDULE_EXISTS'))
    check('past date rejected','POST','/schedules',{**schedule_body,'visitDate':'2020-01-01'},400)
    check('invalid session','POST','/schedules',{**schedule_body,'session':'XX'},400)
    check('invalid capacity','POST','/schedules',{**schedule_body,'capacity':0},400)
    check('schedule filter and local date','GET',f"/schedules?doctorId={f['doctor']}&visitDate={tomorrow}",verify=lambda v:v['total']==1 and v['records'][0]['remaining']==2 and v['records'][0]['visitDate']==tomorrow)
    check('schedule missing','PUT','/schedules/999999999',{'capacity':2,'enabled':1},404)
    booking={'patientId':f['patient1'],'scheduleId':sid}
    reg=check('book patient','POST','/registrations',booking,201)
    check('repeat booking','POST','/registrations',booking,409,err('DUPLICATE_BOOKING'))
    check('quota after duplicate','GET',f"/schedules?doctorId={f['doctor']}",verify=lambda v:v['records'][0]['remaining']==1)
    check('missing patient','POST','/registrations',{**booking,'patientId':999999999},404)
    check('null schedule','POST','/registrations',{**booking,'scheduleId':None},400)
    reg2=check('book second patient','POST','/registrations',{**booking,'patientId':f['patient2']},201)
    check('shrink below used','PUT',f'/schedules/{sid}',{'capacity':1,'enabled':1},409,err('CAPACITY_TOO_SMALL'))
    check('cancel second','POST',f"/registrations/{reg2['id']}/cancel",status=200,verify=lambda v:v['status']=='CANCELLED')
    check('repeat cancel idempotent','POST',f"/registrations/{reg2['id']}/cancel",verify=lambda v:v['status']=='CANCELLED')
    check('quota restored once','GET',f"/schedules?doctorId={f['doctor']}",verify=lambda v:v['records'][0]['remaining']==1)
    check('pause schedule','PUT',f'/schedules/{sid}',{'capacity':2,'enabled':0})
    check('disabled booking','POST','/registrations',{**booking,'patientId':f['patient2']},409,err('SCHEDULE_DISABLED'))
    check('reopen and shrink','PUT',f'/schedules/{sid}',{'capacity':1,'enabled':1})
    check('full schedule','POST','/registrations',{**booking,'patientId':f['patient2']},409,err('SCHEDULE_FULL'))
    check('expand schedule','PUT',f'/schedules/{sid}',{'capacity':2,'enabled':1})
    reg3=check('rebook after cancellation','POST','/registrations',{**booking,'patientId':f['patient2']},201)
    def item(id,quantity):return {'medicineId':id,'dosage':'模拟用量','frequency':'模拟频次','days':1,'quantity':quantity}
    body={'registrationId':reg['id'],'diagnosis':'模拟诊断摘要','items':[item(f['medicine1'],2),item(f['medicine2'],2)]}
    check('prescription validation','POST','/prescriptions',{**body,'items':[]},400)
    check('null item rejected','POST','/prescriptions',{**body,'items':[None]},400)
    check('zero quantity','POST','/prescriptions',{**body,'items':[item(f['medicine1'],0)]},400)
    check('three decimal places','POST','/prescriptions',{**body,'items':[item(f['medicine1'],0.123)]},400)
    check('duplicate medicines','POST','/prescriptions',{**body,'items':[item(f['medicine1'],1),item(f['medicine1'],1)]},409,err('DUPLICATE_MEDICINE'))
    check('second medicine insufficient','POST','/prescriptions',body,409,err('INSUFFICIENT_STOCK'))
    def unchanged(v):
        values={row['id']:row['stock'] for row in v['records']}
        return values[f['medicine1']]==10 and values[f['medicine2']]==1
    check('inventory restored after partial failure','GET','/medicines?pageSize=100',verify=unchanged)
    check('failed prescription not saved','GET','/prescriptions?pageSize=100',verify=lambda v:all(row['registrationId']!=reg['id'] for row in v['records']))
    check('failed prescription keeps booking','GET',f"/registrations?patientId={f['patient1']}",verify=lambda v:v['records'][0]['status']=='BOOKED')
    check('unknown medicine rollback','POST','/prescriptions',{**body,'items':[item(f['medicine1'],2),item(999999999,1)]},404)
    check('inventory restored after unknown medicine','GET','/medicines?pageSize=100',verify=unchanged)
    rx=check('issue prescription successfully','POST','/prescriptions',{**body,'items':[item(f['medicine1'],2),item(f['medicine2'],1)]},201)
    check('prescription details','GET',f"/prescriptions/{rx['id']}",verify=lambda v:len(v['items'])==2)
    check('completed booking','GET',f"/registrations?patientId={f['patient1']}",verify=lambda v:v['records'][0]['status']=='COMPLETED')
    check('repeat prescription rejected','POST','/prescriptions',body,409,err('REGISTRATION_NOT_BOOKED'))
    check('completed booking cannot cancel','POST',f"/registrations/{reg['id']}/cancel",status=409,verify=err('REGISTRATION_COMPLETED'))
    check('completed booking cannot rebook','POST','/registrations',booking,409,err('DUPLICATE_BOOKING'))
    check('cancel remaining booking','POST',f"/registrations/{reg3['id']}/cancel")
    check('cancelled booking cannot issue','POST','/prescriptions',{**body,'registrationId':reg3['id']},409,err('REGISTRATION_NOT_BOOKED'))
    for kind in ['schedules','registrations','prescriptions','medicines']:
        check(kind+' invalid page','GET',f'/{kind}?page=0',status=400)
        check(kind+' oversized page','GET',f'/{kind}?pageSize=101',status=400)
    check('invalid status','GET','/registrations?status=OTHER',status=400)
    check('missing prescription','GET','/prescriptions/999999999',status=404)
finally:
    fixture('cleanup');cleaned=True
    OUTPUT.parent.mkdir(parents=True,exist_ok=True)
    OUTPUT.write_text(json.dumps({'passed':len(cases),'cases':cases,'syntheticRowsCleaned':cleaned},ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps({'passed':len(cases),'syntheticRowsCleaned':cleaned}))
