import request from './request'

/**
 * 下载后端返回的 Excel 文件
 * @param {string} url 接口地址（不含 /api）
 * @param {object} params 查询参数
 * @param {string} fallbackName 拿不到文件名时使用
 */
export async function downloadFile(url, params = {}, fallbackName = '导出文件.xlsx') {
  const response = await request.get(url, { params, responseType: 'blob' })

  let fileName = fallbackName
  const disposition = response.headers['content-disposition'] || ''
  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  const normalMatch = disposition.match(/filename="?([^";]+)"?/i)
  if (utf8Match) {
    fileName = decodeURIComponent(utf8Match[1])
  } else if (normalMatch) {
    fileName = normalMatch[1]
  }

  const blob = new Blob([response.data], {
    type: response.headers['content-type'] || 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
  })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  URL.revokeObjectURL(link.href)
  document.body.removeChild(link)
}
