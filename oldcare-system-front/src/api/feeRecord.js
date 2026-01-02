import http from './http'

export function fetchFeeRecords(params) {
  return http.get('fee-record', { params })
}

export function createFeeRecord(data) {
  return http.post('fee-record', data)
}

export function getFeeRecordById(id) {
  return http.get(`/fee-record/${id}`)
}

export function getFeeRecordsByResident(residentId, params) {
  return http.get(`/fee-record/by-resident/${residentId}`, { params })
}

export function payFeeRecord(id) {
  return http.put(`/fee-record/${id}/pay`)
}

// 居民自助支付
export function paySelfFeeRecord(id) {
  return http.put(`/fee-record/${id}/pay-self`)
}

export function refundFeeRecord(id) {
  return http.put(`/fee-record/${id}/refund`)
}

export function approveRefund(id, approved) {
  return http.put(`/fee-record/${id}/approve-refund`, { approved })
}


