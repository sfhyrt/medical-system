<script setup>
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import SystemStatus from './components/SystemStatus.vue'
import { api } from './api'

const configs = {
  departments: { label: '科室管理', singular: '科室', subtitle: '维护科室资料、诊疗地点与联系信息。', fields: [
    { key: 'code', label: '科室编码', placeholder: '如 CARDIO', required: true }, { key: 'name', label: '科室名称', required: true },
    { key: 'location', label: '诊疗地点', required: true }, { key: 'phone', label: '联系电话', required: true }, { key: 'description', label: '科室介绍', area: true },
  ], filters: ['code', 'name', 'location'] },
  doctors: { label: '医生管理', singular: '医生', subtitle: '管理医生档案、所属科室与执业状态。', fields: [
    { key: 'code', label: '医生编码', required: true }, { key: 'name', label: '医生姓名', required: true },
    { key: 'departmentId', label: '所属科室', options: true, required: true }, { key: 'title', label: '职称', required: true },
    { key: 'phone', label: '联系电话', required: true }, { key: 'status', label: '执业状态', options: true, required: true },
  ], filters: ['name', 'departmentId', 'title', 'status'] },
  patients: { label: '患者管理', singular: '患者', subtitle: '维护患者基本信息，列表中的联系方式与证件号默认脱敏。', fields: [
    { key: 'name', label: '患者姓名', required: true }, { key: 'idCard', label: '身份证号', required: true },
    { key: 'phone', label: '联系电话', required: true }, { key: 'address', label: '联系地址', area: true },
  ], filters: ['name', 'phone', 'idCard'] },
}
const active = ref('departments')
const config = computed(() => configs[active.value])
const rows = ref([]), total = ref(0), page = ref(1), pageSize = ref(5), pages = ref(0)
const loading = ref(false), saving = ref(false), error = ref(''), formError = ref(''), notice = ref('')
const connected = ref(false), options = ref([]), counts = reactive({departments: 0, doctors: 0, patients: 0})
const filters = reactive({}), form = reactive({}), editingId = ref(null), deleting = ref(null)
const formDialog = ref(null), deleteDialog = ref(null)
let requestId = 0, noticeTimer
const columns = computed(() => config.value.fields.map(f => ({ ...f, key: f.key === 'departmentId' ? 'departmentName' : f.key })))
function showNotice(message) { notice.value = message; clearTimeout(noticeTimer); noticeTimer = setTimeout(() => notice.value = '', 3500) }
async function refreshCounts() {
  const results = await Promise.all(Object.keys(configs).map(async key => [key, await api(`/${key}?pageSize=1`)]))
  results.forEach(([key, data]) => counts[key] = data.total)
  options.value = await api('/departments/options')
}
async function load() {
  if (active.value === 'status') return
  const id = ++requestId
  loading.value = true; error.value = ''
  const query = new URLSearchParams({page: page.value, pageSize: pageSize.value})
  Object.entries(filters).forEach(([key,value]) => { if (value !== '' && value != null) query.set(key, value) })
  try {
    const result = await api(`/${active.value}?${query}`)
    if (id !== requestId) return
    rows.value = result.records; total.value = result.total; pages.value = result.pages; connected.value = true
  } catch (e) { if (id === requestId) { error.value = e.message; rows.value = []; connected.value = false } }
  finally { if (id === requestId) loading.value = false }
}
function navigate(key) {
  requestId++; active.value = key; page.value = 1
  Object.keys(filters).forEach(k => delete filters[k]); rows.value = []; error.value = ''; notice.value = ''
  if (key !== 'status') load()
}
function reset() { Object.keys(filters).forEach(k => delete filters[k]); page.value = 1; load() }
function fieldLabel(key) { return config.value.fields.find(f => f.key === key)?.label || key }
function selectOptions(key) { return key === 'departmentId' ? options.value.map(d => ({value:d.id,label:d.name})) : [{value:1,label:'在职'},{value:0,label:'停诊'}] }
async function openForm(row) {
  formError.value = ''; error.value = ''; editingId.value = row?.id ?? null
  Object.keys(form).forEach(k => delete form[k])
  try {
    if (active.value === 'doctors') options.value = await api('/departments/options')
    if (row) Object.assign(form, await api(`/${active.value}/${row.id}`))
    else config.value.fields.forEach(f => form[f.key] = f.key === 'status' ? 1 : '')
    await nextTick(); formDialog.value.showModal()
  } catch (e) { error.value = e.message }
}
async function save() {
  saving.value = true; formError.value = ''
  const payload = Object.fromEntries(config.value.fields.map(f => [f.key, typeof form[f.key] === 'string' ? form[f.key].trim() : form[f.key]]))
  if ('departmentId' in payload) payload.departmentId = payload.departmentId === '' ? null : Number(payload.departmentId)
  if ('status' in payload) payload.status = payload.status === '' ? null : Number(payload.status)
  try {
    await api(`/${active.value}${editingId.value ? '/' + editingId.value : ''}`, {method: editingId.value ? 'PUT' : 'POST', body:JSON.stringify(payload)})
    formDialog.value.close(); showNotice(`${config.value.singular}信息已保存`)
    await load(); await refreshCounts()
  } catch (e) { formError.value = e.message }
  finally { saving.value = false }
}
async function askDelete(row) { deleting.value = row; formError.value = ''; await nextTick(); deleteDialog.value.showModal() }
async function confirmDelete() {
  saving.value = true; formError.value = ''
  try {
    await api(`/${active.value}/${deleting.value.id}`, {method:'DELETE'})
    deleteDialog.value.close(); if (rows.value.length === 1 && page.value > 1) page.value--
    showNotice('记录已删除'); await load(); await refreshCounts()
  } catch(e) { formError.value = e.message }
  finally { saving.value = false }
}
onMounted(async () => {
  await load()
  try { await refreshCounts() } catch(e) { error.value = e.message }
})
</script>

<template>
  <div class="directory-shell">
    <aside class="directory-sidebar">
      <a class="directory-brand" href="/" aria-label="医疗系统首页"><span class="directory-logo">+</span><span>医疗系统<small>MEDICAL SYSTEM</small></span></a>
      <p class="nav-caption">工作空间</p>
      <nav aria-label="主导航"><button v-for="(item,key) in configs" :key="key" :class="{selected:active===key}" @click="navigate(key)"><span class="nav-symbol">{{key==='departments'?'▦':key==='doctors'?'✚':'◎'}}</span>{{item.label}}<span class="nav-count">{{counts[key]}}</span></button><button :class="{selected:active==='status'}" @click="navigate('status')"><span class="nav-symbol">◉</span>服务状态</button></nav>
      <div class="sidebar-note"><span class="sidebar-dot"></span>本地工作区<small>数据与服务运行在本机</small></div>
    </aside>
    <main class="workspace-main">
      <header class="directory-topbar"><span>医疗信息管理<span class="breadcrumb"> / {{config?.label || '服务状态'}}</span></span><span class="connection-label" :class="{online:connected}"><i></i>{{connected?'数据服务已连接':'正在检查服务'}}</span></header>
      <div v-if="active!=='status'" class="directory-content">
        <section class="directory-heading"><div><p class="directory-eyebrow">MEDICAL DIRECTORY</p><h1>{{config.label}}</h1><p>{{config.subtitle}}</p></div><button class="primary-action" @click="openForm(null)"><span>+</span>新增{{config.singular}}</button></section>
        <div v-if="notice" class="notice-banner" role="status">✓ {{notice}}</div>
        <div v-if="error" class="error-banner" role="alert">{{error}} <button @click="load">重新加载</button></div>
        <section class="directory-card">
          <form class="filter-bar" @submit.prevent="page=1;load()"><div v-for="key in config.filters" :key="key" class="filter-field"><label :for="'search-'+key">{{fieldLabel(key)}}</label><select v-if="key==='departmentId'||key==='status'" :id="'search-'+key" v-model="filters[key]"><option value="">全部</option><option v-for="option in selectOptions(key)" :key="option.value" :value="option.value">{{option.label}}</option></select><input v-else :id="'search-'+key" v-model="filters[key]" :placeholder="'搜索'+fieldLabel(key)" maxlength="100" /></div><div class="filter-actions"><button class="search-action" type="submit">查询</button><button class="secondary-action" type="button" @click="reset">重置</button></div></form>
          <div class="table-summary"><strong>{{config.singular}}列表 <span>{{total}}</span></strong><span>共 {{total}} 条记录</span></div>
          <div class="table-scroll" :aria-busy="loading"><table class="directory-table"><thead><tr><th v-for="column in columns" :key="column.key">{{column.label}}</th><th class="operations">操作</th></tr></thead><tbody><tr v-if="loading"><td :colspan="columns.length+1" class="empty-state">正在加载数据…</td></tr><tr v-else-if="!rows.length"><td :colspan="columns.length+1" class="empty-state"><strong>暂无匹配记录</strong><span>调整查询条件，或新增{{config.singular}}信息。</span></td></tr><template v-else><tr v-for="row in rows" :key="row.id"><td v-for="column in columns" :key="column.key" :class="{'name-cell':column.key==='name','code-cell':column.key==='code'}"><span v-if="column.key==='status'" class="status-tag" :class="{inactive:row.status===0}">{{row.status===1?'在职':'停诊'}}</span><span v-else>{{row[column.key] || '—'}}</span></td><td class="operations"><button @click="openForm(row)" :aria-label="'编辑'+row.name">编辑</button><button class="delete-action" @click="askDelete(row)" :aria-label="'删除'+row.name">删除</button></td></tr></template></tbody></table></div>
          <div class="pagination"><span>第 {{page}} 页 · 共 {{pages || 1}} 页</span><div><label for="page-size">每页</label><select id="page-size" v-model.number="pageSize" @change="page=1;load()"><option :value="5">5 条</option><option :value="10">10 条</option><option :value="20">20 条</option></select><button :disabled="page<=1||loading" @click="page--;load()" aria-label="上一页">‹</button><span class="current-page">{{page}}</span><button :disabled="page>=pages||loading" @click="page++;load()" aria-label="下一页">›</button></div></div>
        </section><p class="directory-footer">Medical System<span>信息维护 · 数据一致性 · 清晰可追溯</span></p>
      </div>
      <section v-else class="status-content"><SystemStatus /></section>
    </main>
    <dialog ref="formDialog" class="record-dialog" aria-labelledby="record-title"><form @submit.prevent="save" novalidate><div class="dialog-heading"><div><p>信息维护</p><h2 id="record-title">{{editingId?'编辑':'新增'}}{{config?.singular}}</h2></div><button type="button" class="dialog-close" @click="formDialog.close()" :disabled="saving" aria-label="关闭">×</button></div><div class="dialog-fields"><div v-for="field in config?.fields" :key="field.key" class="record-field" :class="{wide:field.area}"><label :for="'form-'+field.key">{{field.label}} <span v-if="field.required">*</span></label><select v-if="field.options" :id="'form-'+field.key" v-model="form[field.key]"><option value="">请选择</option><option v-for="option in selectOptions(field.key)" :key="option.value" :value="option.value">{{option.label}}</option></select><textarea v-else-if="field.area" :id="'form-'+field.key" v-model="form[field.key]" rows="3" maxlength="500"/><input v-else :id="'form-'+field.key" v-model="form[field.key]" :placeholder="field.placeholder || '请输入'+field.label" :maxlength="field.key==='idCard'?18:field.key==='phone'?11:120" autocomplete="off"/></div></div><p v-if="active==='patients'" class="field-hint">身份证将核验18位格式、出生日期和校验码。</p><div v-if="formError" class="dialog-error" role="alert">{{formError}}</div><div class="dialog-footer"><button type="button" class="secondary-action" @click="formDialog.close()" :disabled="saving">取消</button><button type="submit" class="primary-action" :disabled="saving">{{saving?'保存中…':'保存信息'}}</button></div></form></dialog>
    <dialog ref="deleteDialog" class="record-dialog delete-dialog" aria-labelledby="delete-title"><h2 id="delete-title">删除{{config?.singular}}</h2><p>确认删除“{{deleting?.name}}”？已被其他数据引用的记录将受到保护。</p><div v-if="formError" class="dialog-error" role="alert">{{formError}}</div><div class="dialog-footer"><button class="secondary-action" @click="deleteDialog.close()" :disabled="saving">取消</button><button class="danger-action" @click="confirmDelete" :disabled="saving">确认删除</button></div></dialog>
  </div>
</template>
