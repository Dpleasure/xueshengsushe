import axios from "axios";
import {ElMessage} from "element-plus";
import { withApiBaseUrl } from "@/api/baseUrl";
const defaultError=(err)=> {
  console.error('请求错误:', err)
  let errorMessage = '你的post或者get请求有一些错误请联系管理员'
  if (err.response) {
    // 服务器返回了错误状态码
    errorMessage = `请求失败: ${err.response.status} - ${err.response.data?.message || err.response.statusText}`
  } else if (err.request) {
    // 请求已发出但没有收到响应
    errorMessage = '无法连接到服务器，请检查网络连接或确认后端服务是否启动'
  } else {
    // 其他错误
    errorMessage = err.message || errorMessage
  }
  ElMessage.error(errorMessage)
}
const defaultFailure=(message)=>ElMessage.warning(message)
const authHeaders = (headers = {}) => {
  const token = localStorage.getItem('token')
  return token ? { ...headers, Authorization: `Bearer ${token}` } : headers
}

function post(url, data,success,failure=defaultFailure,error=defaultError) {
  axios.post(withApiBaseUrl(url), data,{
      headers: authHeaders({
          'Content-Type': 'application/json'
      })
  }).then(({data})=> {
      if (data.success)
          success(data.message,data.status);
      else
          failure(data.message,data.status);
  }).catch(error)

}
function get(url, success,failure=defaultFailure,error=defaultError) {
    axios.get(withApiBaseUrl(url),{
      headers: authHeaders()
    }).then(({data})=> {
        if (data.success)
            success(data.message,data.status);
        else
            failure(data.message,data.status);
    }).catch(error)
}
export {get,post}
