import http from './http'

/**
 * 获取新闻列表（通过后端代理）
 * 新的API无需参数
 */
export function getNews() {
  return http.get('news')
}

