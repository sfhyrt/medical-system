<script setup>
import { computed, onMounted, ref, version as vueVersion } from 'vue'

const loading = ref(false)
const report = ref(null)
const hello = ref('')
const helloOk = ref(false)
const error = ref('')
const apiStatus = ref(null)
const connected = computed(() => !loading.value && !error.value && helloOk.value && report.value?.databaseStatus === 'connected')
const status = computed(() => loading.value ? '正在检测' : connected.value ? '全部连通' : '需要检查')
const checkedTime = computed(() => report.value?.checkedAt ? new Date(report.value.checkedAt).toLocaleString('zh-CN', { hour12: false }) : '尚未完成检测')
const jsonResult = computed(() => report.value ? JSON.stringify(report.value, null, 2) : '等待后端响应…')

async function inspect() {
  if (loading.value) return
  loading.value = true
  error.value = ''
  helloOk.value = false
  apiStatus.value = null
  report.value = null
  hello.value = ''
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), 8000)
  try {
    const [helloResponse, environmentResponse] = await Promise.all([
      fetch('/api/hello', { signal: controller.signal, cache: 'no-store' }),
      fetch('/api/environment', { signal: controller.signal, cache: 'no-store' }),
    ])
    hello.value = await helloResponse.text()
    helloOk.value = helloResponse.ok && hello.value === 'Hello World! Medical System'
    apiStatus.value = environmentResponse.status
    report.value = await environmentResponse.json()
    if (!environmentResponse.ok || report.value.databaseStatus !== 'connected') {
      error.value = report.value.message || '数据库检测未通过，请检查本机连接配置。'
    } else if (!helloOk.value) {
      error.value = 'Hello World 接口响应不符合预期，请检查后端服务。'
    }
  } catch (cause) {
    error.value = cause.name === 'AbortError' ? '请求超时，请检查后端与 MySQL 服务。' : '无法获取接口结果，请检查后端服务与前端代理配置。'
  } finally {
    clearTimeout(timeout)
    loading.value = false
  }
}
onMounted(inspect)
</script>

<template>
  <div class="app-shell">
    <header class="topbar">
      <a class="brand" href="/" aria-label="医疗系统首页">
        <span class="brand-mark" aria-hidden="true"><svg viewBox="0 0 24 24"><path d="M10 3h4v7h7v4h-7v7h-4v-7H3v-4h7z" /></svg></span>
        <span>医疗系统<span class="brand-caption">MEDICAL SYSTEM</span></span>
      </a>
      <div class="topbar-right"><span class="course">Java Web 开发</span><span class="lab-badge">实验 01</span></div>
    </header>

    <main>
      <section class="intro">
        <div><p class="eyebrow">开发环境验证</p><h1>{{ connected ? '从页面到数据库，连接已就绪。' : '检查开发环境的每一段连接。' }}</h1><p class="intro-copy">通过一次真实请求，验证前端页面、后端接口与数据库访问。</p></div>
        <button class="refresh" type="button" :disabled="loading" @click="inspect"><svg :class="{ spinning: loading }" viewBox="0 0 24 24" aria-hidden="true"><path d="M20 7v5h-5M4 17v-5h5M6.1 7a7 7 0 0 1 11.5-1L20 9M4 15l2.4 3A7 7 0 0 0 18 17" /></svg>{{ loading ? '检测中…' : '重新检测' }}</button>
      </section>

      <div v-if="error" class="error-banner" role="alert">{{ error }}</div>

      <div class="workspace">
        <div class="main-column">
          <section class="panel connection-panel">
            <div class="panel-heading"><div><p class="section-kicker">CONNECTION</p><h2>完整连接链路</h2></div><span class="status" :class="{ success: connected, pending: loading }" role="status" aria-live="polite"><i></i>{{ status }}</span></div>
            <div class="pipeline">
              <div class="pipeline-node"><span class="node-icon">V</span><strong>Vue</strong><span>前端页面</span></div><span class="connector" aria-hidden="true">→</span>
              <div class="pipeline-node"><span class="node-icon">S</span><strong>Spring Boot</strong><span>接口服务</span></div><span class="connector" aria-hidden="true">→</span>
              <div class="pipeline-node"><span class="node-icon">M</span><strong>MyBatis</strong><span>数据访问</span></div><span class="connector" aria-hidden="true">→</span>
              <div class="pipeline-node"><span class="node-icon">DB</span><strong>MySQL</strong><span>数据库</span></div>
            </div>
            <div class="hello-result"><span class="small-label">GET /api/hello</span><code>{{ hello || (loading ? '等待响应…' : '未获取到响应') }}</code></div>
          </section>

          <section class="panel database-panel">
            <div class="panel-heading"><div><p class="section-kicker">DATABASE</p><h2>数据库实测结果</h2></div><span class="read-only">只读查询</span></div>
            <div class="database-grid"><div><span>MySQL 版本</span><strong data-testid="database-version">{{ report?.databaseVersion || '—' }}</strong></div><div><span>数据库当前时间</span><strong class="time-value">{{ report?.databaseTime || '—' }}</strong></div><div><span>查询验证值</span><strong>{{ report?.probe ?? '—' }}<small v-if="report?.probe === 1">查询通过</small></strong></div></div>
            <p class="query-note">通过 MyBatis 执行 SELECT 查询获取，不修改已有数据库和业务表。</p>
          </section>

          <details class="panel response-panel"><summary>查看接口原始响应 <span>GET /api/environment<span v-if="apiStatus"> · HTTP {{ apiStatus }}</span></span></summary><pre>{{ jsonResult }}</pre></details>
        </div>

        <aside class="side-column">
          <section class="panel checklist-panel"><p class="section-kicker">VERIFICATION</p><h2>本次检测</h2><ul class="checklist">
            <li><span class="check-dot pass">✓</span><div><strong>前端已加载</strong><span>Vue {{ vueVersion }}</span></div></li>
            <li><span class="check-dot" :class="{ pass: helloOk && !loading }">{{ helloOk && !loading ? '✓' : '·' }}</span><div><strong>后端接口响应</strong><span>{{ helloOk && !loading ? 'Hello World 返回正常' : loading ? '正在请求接口' : '等待有效响应' }}</span></div></li>
            <li><span class="check-dot" :class="{ pass: connected }">{{ connected ? '✓' : '·' }}</span><div><strong>MyBatis 查询完成</strong><span>{{ connected ? 'MySQL 实际连接成功' : loading ? '正在查询数据库' : '等待查询通过' }}</span></div></li>
          </ul><div class="last-check"><span>最近检测时间</span><time>{{ checkedTime }}</time></div></section>
          <section class="panel runtime-panel"><p class="section-kicker">RUNTIME</p><h2>运行环境</h2><dl><div><dt>Java</dt><dd>{{ report?.javaVersion || '—' }}</dd></div><div><dt>Spring Boot</dt><dd>{{ report?.springBootVersion || '—' }}</dd></div><div><dt>MyBatis</dt><dd>{{ report?.mybatisVersion || '—' }}</dd></div><div><dt>开发工具</dt><dd>VS Code</dd></div></dl></section>
        </aside>
      </div>
      <footer><span>医疗系统初始化 · Java Web 实验一</span><span>Vue / Spring Boot / MyBatis / MySQL</span></footer>
    </main>
  </div>
</template>
