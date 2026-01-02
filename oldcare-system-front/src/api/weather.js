import http from './http'

/**
 * 获取天气信息（通过后端代理）
 */
export function getWeather(params) {
  return http.get('weather', { params })
}

