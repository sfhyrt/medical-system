export async function api(path, options = {}) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 12000)
  try {
    const response = await fetch('/api' + path, { ...options, headers: { 'Content-Type': 'application/json', ...options.headers }, signal: controller.signal, cache: 'no-store' })
    if (response.status === 204) return null
    const data = await response.json()
    if (!response.ok) throw new Error(data.message || `请求失败（${response.status}）`)
    return data
  } catch (error) {
    if (error.name === 'AbortError') throw new Error('请求超时，请检查服务后重试')
    throw error
  } finally { clearTimeout(timer) }
}
