import http from './http'

/**
 * 获取收入统计
 */
export function getIncomeStatistics(period) {
  return http.get('income/statistics', { params: { period } })
}

