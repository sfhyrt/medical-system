<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { api } from '../api'

const props = defineProps({view:String})
const labels = {schedules:'医生排班',registrations:'预约挂号',prescriptions:'处方管理',medicines:'药品库存'}
const descriptions = {schedules:'按日期和时段管理接诊容量，号源随预约实时更新。',registrations:'选择排班为患者预约，取消预约后自动归还号源。',prescriptions:'关联预约记录开具处方，药品库存与就诊状态同步更新。',medicines:'查看当前药品可用数量，处方提交后由系统统一扣减。'}
const rows=ref([]), total=ref(0), page=ref(1), pages=ref(0), busy=ref(false), saving=ref(false), error=ref(''), message=ref(''), formError=ref('')
const doctors=ref([]), patients=ref([]), medicines=ref([]), bookings=ref([]), schedules=ref([])
const modal=ref(null), mode=ref(''), selected=ref(null), details=ref(null)
const form=reactive({}), filters=reactive({}), patientSearch=ref('')
const today=new Date().toLocaleDateString('en-CA')
const available=computed(()=>schedules.value.filter(s=>s.enabled===1&&s.doctorStatus===1&&s.remaining>0&&String(s.visitDate)>=today))
const statusLabel=s=>({BOOKED:'待就诊',COMPLETED:'已完成',CANCELLED:'已取消'}[s]||s)
const dateTime=s=>String(s||'').replace('T',' ').replace(/\.\d+$/,'')
const columns=computed(()=>({
 schedules:[['doctorName','医生'],['departmentName','科室'],['visitDate','日期'],['session','时段'],['capacity','总号源'],['remaining','剩余'],['enabled','状态']],
 registrations:[['patientName','患者'],['doctorName','医生'],['visitTime','就诊时间'],['registrationNo','预约编号'],['status','状态']],
 prescriptions:[['patientName','患者'],['doctorName','医生'],['diagnosis','诊断摘要'],['issuedAt','开具时间']],
 medicines:[['code','药品编码'],['name','药品名称'],['unit','单位'],['stock','可用库存']],
}[props.view]))
async function load(){
 busy.value=true;error.value=''
 const query=new URLSearchParams({page:page.value,pageSize:10})
 Object.entries(filters).forEach(([k,v])=>{if(v)query.set(k,v)})
 try{const data=await api(`/${props.view}?${query}`);rows.value=data.records;total.value=data.total;pages.value=data.pages}
 catch(e){error.value=e.message}finally{busy.value=false}
}
async function all(path){let results=[],current=1;do{const data=await api(`${path}${path.includes('?')?'&':'?'}page=${current}&pageSize=100`);results.push(...data.records);if(current>=data.pages)break;current++}while(current<=20);return results}
async function patientLookup(){try{patients.value=await all('/patients?name='+encodeURIComponent(patientSearch.value))}catch(e){formError.value=e.message}}
async function open(action,row){
 mode.value=action;selected.value=row||null;formError.value='';message.value='';details.value=null
 Object.keys(form).forEach(k=>delete form[k]);patientSearch.value=''
 try{
  if(action==='schedule'){doctors.value=await all('/doctors?status=1');Object.assign(form,{doctorId:'',visitDate:today,session:'AM',capacity:10})}
  if(action==='editSchedule')Object.assign(form,{capacity:row.capacity,enabled:row.enabled})
  if(action==='booking'){schedules.value=await all('/schedules');patients.value=await all('/patients');Object.assign(form,{scheduleId:row?.id||'',patientId:''})}
  if(action==='issue'){bookings.value=await all('/registrations?status=BOOKED');medicines.value=await all('/medicines');Object.assign(form,{registrationId:row?.id||'',diagnosis:'',items:[newItem()]})}
  if(action==='detail')details.value=await api('/prescriptions/'+row.id)
  await nextTick();modal.value.showModal()
 }catch(e){error.value=e.message}
}
function newItem(){return {medicineId:'',dosage:'',frequency:'',days:1,quantity:1}}
function medicine(id){return medicines.value.find(m=>m.id===Number(id))}
function clear(){Object.keys(filters).forEach(k=>delete filters[k]);page.value=1;load()}
async function submit(){
 saving.value=true;formError.value=''
 try{
  if(mode.value==='schedule')await api('/schedules',{method:'POST',body:JSON.stringify({...form,doctorId:form.doctorId||null})})
  if(mode.value==='editSchedule')await api('/schedules/'+selected.value.id,{method:'PUT',body:JSON.stringify(form)})
  if(mode.value==='booking')await api('/registrations',{method:'POST',body:JSON.stringify({patientId:form.patientId||null,scheduleId:form.scheduleId||null})})
  if(mode.value==='cancel')await api('/registrations/'+selected.value.id+'/cancel',{method:'POST'})
  if(mode.value==='issue')await api('/prescriptions',{method:'POST',body:JSON.stringify({registrationId:form.registrationId||null,diagnosis:form.diagnosis,items:form.items.map(i=>({...i,medicineId:i.medicineId||null}))})})
  modal.value.close();message.value=mode.value==='issue'?'处方已保存，库存已同步扣减':mode.value==='cancel'?'预约已取消，号源已归还':'信息已保存';await load()
 }catch(e){formError.value=e.message}finally{saving.value=false}
}
onMounted(load)
</script>

<template>
 <div class="directory-content care-content">
  <section class="directory-heading"><div><p class="directory-eyebrow">CARE OPERATIONS</p><h1>{{labels[view]}}</h1><p>{{descriptions[view]}}</p></div><button v-if="view!=='medicines'" class="primary-action" @click="open(view==='schedules'?'schedule':view==='registrations'?'booking':'issue')">+ {{view==='schedules'?'新增排班':view==='registrations'?'新增预约':'开具处方'}}</button></section>
  <div v-if="message" class="notice-banner" role="status">✓ {{message}}</div>
  <div v-if="error" class="error-banner" role="alert">{{error}} <button @click="load">重新加载</button></div>
  <div v-if="view==='medicines'" class="workflow-note">模拟药品仅用于本地功能预览。库存不足时，整张处方不会保存。</div>
  <section class="directory-card">
   <form class="filter-bar" @submit.prevent="page=1;load()">
    <div v-if="view==='schedules'" class="filter-field"><label for="visit-date">排班日期</label><input id="visit-date" v-model="filters.visitDate" type="date" /></div>
    <div v-if="view==='registrations'" class="filter-field"><label for="booking-status">预约状态</label><select id="booking-status" v-model="filters.status"><option value="">全部</option><option value="BOOKED">待就诊</option><option value="COMPLETED">已完成</option><option value="CANCELLED">已取消</option></select></div>
    <div class="filter-actions"><button type="submit" class="search-action">{{view==='medicines'||view==='prescriptions'?'刷新列表':'查询'}}</button><button v-if="view==='schedules'||view==='registrations'" class="secondary-action" type="button" @click="clear">重置</button></div>
   </form>
   <div class="table-summary"><strong>{{labels[view]}} <span>{{total}}</span></strong><span>共 {{total}} 条记录</span></div>
   <div class="table-scroll" :aria-busy="busy"><table class="directory-table"><thead><tr><th v-for="[key,label] in columns" :key="key">{{label}}</th><th v-if="view!=='medicines'">操作</th></tr></thead><tbody>
    <tr v-if="!rows.length"><td :colspan="columns.length+1" class="empty-state">{{busy?'正在加载数据…':'暂无记录'}}</td></tr>
    <tr v-for="row in rows" :key="row.id"><td v-for="[key] in columns" :key="key" :class="{'workflow-number':key==='remaining'||key==='stock','code-cell':key==='registrationNo'}">
     <span v-if="key==='session'">{{row[key]==='AM'?'上午':'下午'}}</span>
     <span v-else-if="key==='enabled'" class="status-tag" :class="{inactive:row.enabled!==1}">{{row.enabled===1?'开放':'暂停'}}</span>
     <span v-else-if="key==='status'" class="status-tag" :class="{inactive:row.status==='CANCELLED'}">{{statusLabel(row.status)}}</span>
     <span v-else-if="key==='visitTime'||key==='issuedAt'">{{dateTime(row[key])}}</span>
     <span v-else>{{row[key]??'—'}}</span>
    </td><td v-if="view!=='medicines'" class="operations">
     <template v-if="view==='schedules'"><button @click="open('editSchedule',row)">调整</button><button :disabled="row.enabled!==1||row.remaining===0||row.doctorStatus!==1||String(row.visitDate)<today" @click="open('booking',row)">预约</button></template>
     <template v-if="view==='registrations'&&row.status==='BOOKED'"><button @click="open('issue',row)">开方</button><button class="delete-action" @click="open('cancel',row)">取消预约</button></template>
     <span v-if="view==='registrations'&&row.status!=='BOOKED'">—</span><button v-if="view==='prescriptions'" @click="open('detail',row)">查看明细</button>
    </td></tr>
   </tbody></table></div>
   <div class="pagination"><span>第 {{page}} 页 · 共 {{pages||1}} 页</span><div><button :disabled="page<=1||busy" @click="page--;load()" aria-label="上一页">‹</button><span>{{page}}</span><button :disabled="page>=pages||busy" @click="page++;load()" aria-label="下一页">›</button></div></div>
  </section><p class="directory-footer">Medical System<span>排班预约 · 处方流转 · 库存同步</span></p>
  <dialog ref="modal" class="record-dialog workflow-dialog" aria-labelledby="workflow-title"><form @submit.prevent="submit">
   <div class="dialog-heading"><div><p>诊疗工作台</p><h2 id="workflow-title">{{{schedule:'新增排班',editSchedule:'调整排班',booking:'预约挂号',issue:'开具处方',cancel:'取消预约',detail:'处方明细'}[mode]}}</h2></div><button type="button" class="dialog-close" :disabled="saving" @click="modal.close()" aria-label="关闭">×</button></div>
   <div class="dialog-fields">
    <template v-if="mode==='schedule'"><div class="record-field"><label for="schedule-doctor">接诊医生 *</label><select id="schedule-doctor" v-model.number="form.doctorId" required><option value="">请选择医生</option><option v-for="d in doctors" :key="d.id" :value="d.id">{{d.name}} · {{d.departmentName}}</option></select></div><div class="record-field"><label for="schedule-date">接诊日期 *</label><input id="schedule-date" v-model="form.visitDate" type="date" :min="today" required /></div><div class="record-field"><label for="schedule-session">接诊时段</label><select id="schedule-session" v-model="form.session"><option value="AM">上午 09:00</option><option value="PM">下午 14:00</option></select></div></template>
    <div v-if="mode==='schedule'||mode==='editSchedule'" class="record-field"><label for="schedule-capacity">总号源 *</label><input id="schedule-capacity" v-model.number="form.capacity" type="number" min="1" max="1000" required /><small>调整容量会保留已占用的号源。</small></div>
    <div v-if="mode==='editSchedule'" class="record-field"><label for="schedule-enabled">挂号状态</label><select id="schedule-enabled" v-model.number="form.enabled"><option :value="1">开放挂号</option><option :value="0">暂停挂号</option></select></div>
    <template v-if="mode==='booking'"><div class="record-field wide"><label for="booking-schedule">可预约排班 *</label><select id="booking-schedule" v-model.number="form.scheduleId" required><option value="">请选择排班</option><option v-for="s in available" :key="s.id" :value="s.id">{{s.doctorName}} · {{s.visitDate}} {{s.session==='AM'?'上午':'下午'}} · 余 {{s.remaining}} 号</option></select></div><div class="record-field wide"><label for="patient-search">查找患者</label><div class="patient-lookup"><input id="patient-search" v-model="patientSearch" placeholder="按姓名搜索" /><button class="secondary-action" type="button" @click="patientLookup">查找</button></div></div><div class="record-field wide"><label for="booking-patient">患者 *</label><select id="booking-patient" v-model.number="form.patientId" required><option value="">请选择患者</option><option v-for="p in patients" :key="p.id" :value="p.id">{{p.name}} · {{p.phone}}</option></select></div></template>
    <template v-if="mode==='issue'"><div class="record-field wide"><label for="issue-registration">待就诊预约 *</label><select id="issue-registration" v-model.number="form.registrationId" required><option value="">请选择预约</option><option v-for="b in bookings" :key="b.id" :value="b.id">{{b.patientName}} · {{b.doctorName}} · {{dateTime(b.visitTime)}}</option></select></div><div class="record-field wide"><label for="issue-diagnosis">诊断摘要 *</label><textarea id="issue-diagnosis" v-model="form.diagnosis" rows="2" maxlength="500" required /></div><div class="record-field wide prescription-items"><div v-for="(item,index) in form.items" :key="index" class="prescription-line"><div class="line-heading"><strong>药品 {{index+1}}</strong><button v-if="form.items.length>1" type="button" class="delete-action" @click="form.items.splice(index,1)">移除</button></div><label :for="'medicine-'+index">药品 *</label><select :id="'medicine-'+index" v-model.number="item.medicineId" required><option value="">请选择药品</option><option v-for="m in medicines" :key="m.id" :value="m.id">{{m.name}} · 库存 {{m.stock}} {{m.unit}}</option></select><div class="line-grid"><div><label :for="'dosage-'+index">用量 *</label><input :id="'dosage-'+index" v-model="item.dosage" maxlength="100" required /></div><div><label :for="'frequency-'+index">频次 *</label><input :id="'frequency-'+index" v-model="item.frequency" maxlength="100" required /></div><div><label :for="'days-'+index">天数 *</label><input :id="'days-'+index" v-model.number="item.days" type="number" min="1" max="365" required /></div><div><label :for="'quantity-'+index">扣库存数量 *</label><input :id="'quantity-'+index" v-model.number="item.quantity" type="number" min="0.01" max="99999999.99" step="0.01" required /></div></div><small v-if="medicine(item.medicineId)">当前库存 {{medicine(item.medicineId).stock}} {{medicine(item.medicineId).unit}}，提交时会再次校验。</small></div><button v-if="form.items.length<20" type="button" class="secondary-action" @click="form.items.push(newItem())">+ 添加药品</button></div></template>
    <p v-if="mode==='cancel'" class="wide">确认取消 {{selected?.patientName}} 的预约？取消后号源将自动归还。</p>
    <div v-if="mode==='detail'&&details" class="wide"><p class="detail-diagnosis">{{details.diagnosis}}</p><div v-for="item in details.items" :key="item.lineNo" class="prescription-line"><strong>{{item.medicineName}}</strong><p>{{item.dosage}} · {{item.frequency}} · {{item.days}} 天</p><span>数量 {{item.quantity}} {{item.unit}}</span></div></div>
   </div>
   <div v-if="formError" class="dialog-error" role="alert">{{formError}}</div>
   <div class="dialog-footer"><button type="button" class="secondary-action" :disabled="saving" @click="modal.close()">{{mode==='detail'?'关闭':'取消'}}</button><button v-if="mode!=='detail'" type="submit" class="primary-action" :disabled="saving">{{saving?'提交中…':mode==='issue'?'保存处方并扣减库存':mode==='cancel'?'确认取消':'确认保存'}}</button></div>
  </form></dialog>
 </div>
</template>

<style scoped>
.workflow-note{padding:14px 18px;background:#edf4f1;color:#426256;border:1px solid #d5e7de;border-radius:10px;margin-bottom:20px;font-size:13px}.workflow-number{font-variant-numeric:tabular-nums;font-weight:650}.workflow-dialog{width:min(760px,94vw);max-height:88vh;overflow:auto}.workflow-dialog small{display:block;font-size:12px;color:#76817c;margin-top:7px}.patient-lookup{display:flex;gap:8px}.patient-lookup input{flex:1;min-width:0}.line-heading{display:flex;justify-content:space-between;align-items:center;margin-bottom:12px}.prescription-line{padding:16px;border:1px solid #e0e8e3;background:#f8faf9;border-radius:10px;margin-bottom:12px}.line-grid{display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-top:12px}.line-grid label{display:block;margin-bottom:7px;font-size:13px}.prescription-items label{font-size:13px}.prescription-line select,.prescription-line input{width:100%;box-sizing:border-box}.prescription-line p{font-size:14px;color:#59665e}.operations button:disabled{opacity:.35;cursor:not-allowed}.detail-diagnosis{line-height:1.7;white-space:pre-wrap}.directory-table .code-cell{font-size:11px;max-width:230px;overflow-wrap:anywhere;white-space:normal}@media(max-width:600px){.line-grid{grid-template-columns:1fr}.care-content{padding:22px 14px}.directory-heading{gap:18px;align-items:flex-start;flex-direction:column}.directory-heading h1{font-size:26px}}
</style>
