// API配置
export const API_CONFIG = {
  // 天行数据天气API配置
  // 获取API Key：访问 https://www.tianapi.com/ 注册登录后，在控制台创建应用获取
  WEATHER: {
    BASE_URL: 'https://apis.tianapi.com/tianqi/index',
    KEY: '0bdd9006e7e21b1724a76645ced0edf1', // 天行数据API Key
    CITY: '北京' // 默认城市，可以根据需要修改
  },
  // NewsAPI新闻API配置
  // 获取API Key：访问 https://newsapi.org/ 注册登录后获取
  NEWS: {
    BASE_URL: 'https://newsapi.org/v2/top-headlines',
    KEY: '3a8892dca9a64e55a63a9718fe28b780', // NewsAPI Key
    COUNTRY: 'cn' // 国家代码，cn表示中国
  }
}

