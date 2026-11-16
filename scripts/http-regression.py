"""Run CRUD checks against a local service; only this run's synthetic rows are removed."""
import json
import sys
import time
import uuid
import urllib.request
import urllib.error
import urllib.parse
from pathlib import Path

BASE=(sys.argv[1] if len(sys.argv)>1 else 'http://127.0.0.1:8080').rstrip('/')+'/api'
OUTPUT=Path(sys.argv[2] if len(sys.argv)>2 else 'target/http-regression.json')
results=[]; created=[]
prefix='QA_'+uuid.uuid4().hex[:8].upper()
def identity(body):
    weights=[7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2]
    return body+'10X98765432'[sum(int(a)*b for a,b in zip(body,weights))%11]
def request(method,path,payload=None):
    data=None if payload is None else json.dumps(payload,ensure_ascii=False).encode('utf-8')
    req=urllib.request.Request(BASE+path,data=data,method=method,headers={'Content-Type':'application/json'})
    try:
        with urllib.request.urlopen(req,timeout=15) as response:
            raw=response.read(); return response.status,json.loads(raw) if raw else None
    except urllib.error.HTTPError as error:
        raw=error.read();return error.code,json.loads(raw) if raw else None
def check(name,method,path,payload,status,verify=lambda value:True):
    actual,value=request(method,path,payload)
    assert actual==status and verify(value),(name,actual,value)
    results.append({'case':name,'method':method,'path':path,'expected':status,'actual':actual,'passed':True})
    return value
def create(kind,payload):
    value=check(kind+' create','POST','/'+kind,payload,201)
    created.append((kind,value['id']));return value
try:
    dep_payload={'code':prefix,'name':prefix+'科室','location':'QA诊室','phone':'13900009901','description':'Synthetic regression data'}
    dep=create('departments',dep_payload)
    check('department duplicate','POST','/departments',dep_payload,409)
    check('department invalid phone','POST','/departments',{**dep_payload,'code':prefix+'P','name':prefix+'P','phone':'123'},400)
    check('department combined filters','GET','/departments?'+urllib.parse.urlencode({'code':prefix,'name':prefix,'location':'QA','pageSize':1}),None,200,lambda v:v['total']==1 and len(v['records'])==1)
    check('department pagination','GET','/departments?pageSize=1&page=2',None,200,lambda v:len(v['records'])==1 and v['page']==2)
    check('department update','PUT',f"/departments/{dep['id']}",{**dep_payload,'location':'QA更新诊室'},200,lambda v:v['location']=='QA更新诊室')
    doctor_payload={'code':prefix,'name':prefix+'医生','departmentId':dep['id'],'title':'主治医师','phone':'13900009902','status':1}
    doctor=create('doctors',doctor_payload)
    check('doctor duplicate','POST','/doctors',doctor_payload,409)
    check('doctor missing department','POST','/doctors',{**doctor_payload,'code':prefix+'N','departmentId':999999999},400)
    check('doctor invalid phone','POST','/doctors',{**doctor_payload,'phone':'123'},400)
    check('doctor invalid status','PUT',f"/doctors/{doctor['id']}",{**doctor_payload,'status':2},400)
    check('doctor combined filters','GET','/doctors?'+urllib.parse.urlencode({'departmentId':dep['id'],'name':prefix,'title':'主治','status':1,'pageSize':1}),None,200,lambda v:v['total']==1 and v['records'][0]['departmentName']==dep_payload['name'])
    check('doctor update','PUT',f"/doctors/{doctor['id']}",{**doctor_payload,'status':0},200,lambda v:v['status']==0)
    check('foreign key protects department','DELETE',f"/departments/{dep['id']}",None,409)
    sequence=str(time.time_ns()%997+1).zfill(3)
    identity_value=identity('11010119900515'+sequence)
    patient_payload={'name':prefix+'患者','idCard':identity_value,'phone':'13900009903','address':'Synthetic address'}
    patient=create('patients',patient_payload)
    check('patient duplicate identity','POST','/patients',patient_payload,409)
    check('patient bad checksum','POST','/patients',{**patient_payload,'idCard':identity_value[:17]+('1' if identity_value[-1]!='1' else '2')},400)
    check('patient impossible birth date','POST','/patients',{**patient_payload,'idCard':identity('11010120010230001')},400)
    check('patient invalid phone','POST','/patients',{**patient_payload,'phone':'10000000000'},400)
    check('patient blank name','POST','/patients',{**patient_payload,'name':'  '},400)
    check('patient combined filters and masking','GET','/patients?'+urllib.parse.urlencode({'name':prefix,'phone':patient_payload['phone'],'idCard':identity_value,'pageSize':1}),None,200,lambda v:v['total']==1 and '*' in v['records'][0]['idCard'] and '*' in v['records'][0]['phone'])
    check('patient edit detail','GET',f"/patients/{patient['id']}",None,200,lambda v:v['idCard']==identity_value)
    check('patient update','PUT',f"/patients/{patient['id']}",{**patient_payload,'address':'QA updated'},200,lambda v:v['address']=='QA updated')
    for kind in ['departments','doctors','patients']:
        check(kind+' invalid page','GET',f'/{kind}?page=0',None,400)
        check(kind+' oversized page','GET',f'/{kind}?pageSize=101',None,400)
        check(kind+' missing row','GET',f'/{kind}/999999999',None,404)
        check(kind+' injection filter','GET','/'+kind+'?'+urllib.parse.urlencode({'name':"%' OR 1=1 --"}),None,200,lambda v:v['total']==0)
    attack="QA'); DROP TABLE patient; --"
    hostile=create('departments',{**dep_payload,'code':prefix+'SQL','name':attack})
    check('SQL-looking name round trip','GET',f"/departments/{hostile['id']}",None,200,lambda v:v['name']==attack)
    check('patient table remains accessible','GET','/patients?pageSize=1',None,200,lambda v:v['total']>=1)
    protected_doctor=check('find referenced doctor','GET','/doctors?code=DOC001',None,200)['records'][0]
    check('registration protects doctor','DELETE',f"/doctors/{protected_doctor['id']}",None,409)
    # Only rows recorded in created are deleted, in reverse foreign-key order.
finally:
    for kind,key in reversed(created):
        code,value=request('DELETE',f'/{kind}/{key}')
        if code not in (204,404):raise RuntimeError(('Cleanup failed',kind,key,code))
    OUTPUT.parent.mkdir(parents=True,exist_ok=True)
    OUTPUT.write_text(json.dumps({'baseUrl':BASE,'cases':results,'passed':len(results),'syntheticRowsCleaned':True},ensure_ascii=False,indent=2),'utf-8')
print(json.dumps({'passed':len(results),'syntheticRowsCleaned':True}))
