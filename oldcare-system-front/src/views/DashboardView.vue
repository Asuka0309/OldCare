<template>
  <div class="dashboard">
    <div class="welcome-card">
      <h2>欢迎使用 OldCare 智慧养老管理平台</h2>
      <p class="datetime">{{ currentDateTime }}</p>
    </div>

    <div class="grid">
      <!-- 天气信息 -->
      <el-card shadow="hover" class="card weather-card">
        <div class="title">今日天气</div>
        <div v-if="weatherLoading" class="loading">加载中...</div>
        <div v-else-if="weather" class="weather-content">
          <div class="weather-city">{{ weather.city || '—' }}</div>
          <div class="weather-main">
            <div class="weather-icon">{{ weatherIcon }}</div>
            <div class="weather-temp">{{ weather.temp }}°C</div>
          </div>
          <div class="weather-details">
            <div>天气：{{ weather.weather }}</div>
            <div>湿度：{{ weather.humidity }}%</div>
            <div>风向：{{ weather.windDir }}</div>
            <div>风速：{{ weather.windSpeed }} km/h</div>
          </div>
        </div>
        <div v-else class="error">天气信息加载失败</div>
      </el-card>

      <!-- 正在进行的活动 -->
      <el-card shadow="hover" class="card activities-card">
        <div class="title">正在进行的活动</div>
        <div v-if="activitiesLoading" class="loading">加载中...</div>
        <div v-else-if="ongoingActivities.length > 0" class="activities-list">
          <div v-for="activity in ongoingActivities" :key="activity.id" class="activity-item">
            <div class="activity-title">{{ activity.activityTitle }}</div>
            <div class="activity-meta">
              <span>{{ formatDateTime(activity.activityDate) }}</span>
              <span>{{ activity.location }}</span>
            </div>
          </div>
        </div>
        <div v-else class="empty">暂无正在进行的活动</div>
      </el-card>

      <!-- 今日新闻 -->
      <el-card shadow="hover" class="card news-card">
        <div class="title">今日新闻</div>
        <div v-if="newsLoading" class="loading">加载中...</div>
        <div v-else-if="news.length > 0" class="news-list">
          <div 
            v-for="(item, index) in news" 
            :key="index" 
            class="news-item"
            @click="openNews(item)"
          >
            <div class="news-title">{{ item.title }}</div>
            <div class="news-time">{{ item.time }}</div>
          </div>
        </div>
        <div v-else class="empty">暂无新闻</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useAuthStore } from '../store/auth'
import { getAllActivities } from '../api/communityActivity'
import { getNews } from '../api/news'
import { getWeather } from '../api/weather'
import { API_CONFIG } from '../config/api'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

// 扩展dayjs插件
dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const auth = useAuthStore()

// 当前日期时间
const currentDateTime = ref('')
let datetimeTimer = null

// 天气数据
const weather = ref(null)
const weatherLoading = ref(false)

// 活动数据
const ongoingActivities = ref([])
const activitiesLoading = ref(false)

// 新闻数据
const news = ref([])
const newsLoading = ref(false)

// 更新日期时间
const updateDateTime = () => {
  currentDateTime.value = dayjs().format('YYYY年MM月DD日 dddd HH:mm:ss')
}

// 获取天气信息（通过后端代理调用天行数据天气API）
const fetchWeather = async () => {
  weatherLoading.value = true
  try {
    const CITY = API_CONFIG.WEATHER.CITY || '北京'

    const parseTemp = (val) => {
      if (val === null || val === undefined) return null
      const num = parseFloat(String(val).replace('℃', '').trim())
      return Number.isNaN(num) ? null : num
    }
    
    // 通过后端代理调用天气API，避免CORS问题
    // 后端使用POST方法调用天行数据API，符合其API规范
    const result = await getWeather({ city: CITY })
    
    console.log('天气API返回:', result)
    
    // 天行数据API返回格式：{ code: 200, msg: 'success', newslist: [...] } 或 { code: 200, msg: 'success', result: {...} }
    if (result && result.code === 200 && result.msg === 'success') {
      // 尝试从newslist获取数据（旧格式）
      let weatherData = null
      if (result.newslist && result.newslist.length > 0) {
        weatherData = result.newslist[0]
      } else if (result.result) {
        // 新格式：数据在result字段中
        weatherData = result.result
      }
      
      if (weatherData) {
        const parsedTemp = parseTemp(weatherData.temp) ?? parseTemp(weatherData.real) ?? 0
        const parsedHumidity = parseInt(weatherData.humidity) || 0

        weather.value = {
          city: weatherData.city || weatherData.area || weatherData.province || CITY,
          temp: parsedTemp,
          weather: weatherData.weather || '未知',
          humidity: parsedHumidity,
          windDir: weatherData.wind || '未知',
          windSpeed: weatherData.windspeed || '0'
        }
      } else {
        // 数据为空，使用模拟数据
        console.warn('天气API返回成功但数据为空，使用模拟数据')
        throw new Error('天气数据为空')
      }
    } else {
      // API返回错误
      console.warn('天气API返回错误:', result)
      throw new Error(result?.msg || '获取天气数据失败')
    }
  } catch (error) {
    console.error('获取天气失败:', error)
    // 如果API失败，使用模拟数据
    weather.value = {
      city: CITY,
      temp: 22,
      weather: '晴',
      humidity: 65,
      windDir: '东北',
      windSpeed: '15'
    }
  } finally {
    weatherLoading.value = false
  }
}

// 获取风向
const getWindDirection = (deg) => {
  const directions = ['北', '东北', '东', '东南', '南', '西南', '西', '西北']
  return directions[Math.round(deg / 45) % 8]
}

// 天气图标
const weatherIcon = computed(() => {
  if (!weather.value) return '☀️'
  const w = weather.value.weather.toLowerCase()
  if (w.includes('晴')) return '☀️'
  if (w.includes('云') || w.includes('阴')) return '☁️'
  if (w.includes('雨')) return '🌧️'
  if (w.includes('雪')) return '❄️'
  return '🌤️'
})

// 获取正在进行的活动
const fetchOngoingActivities = async () => {
  activitiesLoading.value = true
  try {
    const today = new Date()
    today.setHours(0, 0, 0, 0)
    
    const response = await getAllActivities({ 
      status: 'published', 
      size: 100 
    })
    const activities = response.records || response.data?.records || []
    
    // 筛选今天及以后的活动（活动日期>=今天0点，且状态为已发布）
    ongoingActivities.value = activities.filter(activity => {
      if (!activity.activityDate) return false
      const activityDate = new Date(activity.activityDate)
      // 活动日期在今天或以后
      return activityDate >= today && activity.status === 'published'
    }).slice(0, 8) // 只显示前8个
  } catch (error) {
    console.error('获取活动失败:', error)
    ongoingActivities.value = []
  } finally {
    activitiesLoading.value = false
  }
}

// 获取新闻（通过后端代理调用新的新闻API）
const fetchNews = async () => {
  newsLoading.value = true
  try {
    // 通过后端代理调用新闻API
    const result = await getNews()
    
    console.log('新闻API返回:', result)
    
    // 聚合数据API返回格式：{ reason: "Success", result: { data: [...] } }
    // 或者：{ error_code: 0, reason: "Success", result: { data: [...] } }
    if (result && (result.reason === "Success" || result.error_code === 0)) {
      // 检查result字段中的data数组
      let newsList = []
      
      if (result.result && result.result.data && Array.isArray(result.result.data)) {
        newsList = result.result.data
      } else if (result.data && Array.isArray(result.data)) {
        newsList = result.data
      } else if (result.list && Array.isArray(result.list)) {
        newsList = result.list
      } else if (result.items && Array.isArray(result.items)) {
        newsList = result.items
      } else if (result.newslist && Array.isArray(result.newslist)) {
        newsList = result.newslist
      } else if (Array.isArray(result)) {
        // 如果result本身就是数组
        newsList = result
      }
      
      if (newsList.length > 0) {
        // 解析新闻列表，聚合数据API通常返回的字段：title, date, author_name, url等
        news.value = newsList.slice(0, 8).map(item => {
          // 聚合数据API的字段：title, date, author_name, url, thumbnail_pic_s等
          const title = item.title || item.name || item.content || item.text || '无标题'
          const time = item.date || item.time || item.publishTime || item.createdAt || '未知时间'
          const url = item.url || item.link || item.href || null
          
          return {
            title: title,
            time: time ? (time.includes('前') || time.includes('小时') || time.includes('分钟') ? time : dayjs(time).fromNow()) : '未知时间',
            url: url
          }
        })
        newsLoading.value = false
        return
      } else {
        // 如果没有找到列表，可能是单个对象或不同结构
        console.warn('新闻API返回成功但未找到新闻列表，返回数据:', result)
        throw new Error('新闻数据格式不正确')
      }
    } else if (result && result.error_code) {
      // 聚合数据API返回错误
      console.warn('新闻API返回错误:', result)
      throw new Error(result.reason || result.message || '获取新闻失败')
    } else {
      // 其他错误格式
      console.warn('新闻API返回错误:', result)
      throw new Error(result?.message || result?.msg || result?.reason || '获取新闻失败')
    }
  } catch (error) {
    console.error('获取新闻失败:', error)
    
    // 如果是连接错误，给出更友好的提示
    if (error.message && error.message.includes('无法连接到新闻API服务')) {
      console.warn('新闻API服务连接失败，使用模拟数据。')
      console.warn('请检查网络连接或后端配置（application.properties 中的 news.api.url 和 news.api.key）')
    }
    
    // 使用模拟数据作为后备（模拟数据没有链接）
    news.value = [
      { title: '社区开展健康知识讲座', time: '2小时前', url: null },
      { title: '智慧养老服务平台正式上线', time: '5小时前', url: null },
      { title: '社区举办老年人书法比赛', time: '1天前', url: null },
      { title: '健康体检活动开始报名', time: '2天前', url: null },
      { title: '冬季养生小常识分享', time: '3天前', url: null },
      { title: '社区志愿者招募通知', time: '4天前', url: null },
      { title: '老年大学新学期课程表发布', time: '5天前', url: null },
      { title: '防诈骗知识宣传活动', time: '1周前', url: null }
    ]
  } finally {
    newsLoading.value = false
  }
}

// 打开新闻链接
const openNews = (item) => {
  if (item.url) {
    // 在新标签页打开新闻链接
    window.open(item.url, '_blank')
  } else {
    console.warn('该新闻没有链接地址')
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return ''
  return dayjs(dateTime).format('MM月DD日 HH:mm')
}

onMounted(() => {
  updateDateTime()
  datetimeTimer = setInterval(updateDateTime, 1000)
  fetchWeather()
  fetchOngoingActivities()
  fetchNews()
})

onUnmounted(() => {
  if (datetimeTimer) {
    clearInterval(datetimeTimer)
  }
})
</script>

<style scoped>
.dashboard {
  max-width: 1200px;
}

.welcome-card {
  background: #ffffff;
  color: #1f2937;
  padding: 32px;
  border-radius: 12px;
  margin-bottom: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.welcome-card h2 {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: 600;
  color: #111827;
}

.welcome-card p {
  margin: 8px 0 0 0;
  font-size: 16px;
  color: #4b5563;
}

.welcome-card .datetime {
  font-size: 14px;
  color: #6b7280;
  margin-top: 8px;
}

.grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.card {
  min-height: 400px;
  display: flex;
  flex-direction: column;
}

.card :deep(.el-card__body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.title {
  font-weight: 600;
  margin-bottom: 16px;
  color: #0f172a;
  font-size: 18px;
}

.links {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.tips {
  margin: 0;
  padding-left: 18px;
  color: #475569;
  line-height: 1.8;
}

/* 天气卡片 */
.weather-card :deep(.el-card__body) {
  position: relative;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.weather-card .weather-content {
  flex: 1;
  display: flex;
  gap: 30px;
  align-items: center;
  justify-content: center;
}

.weather-city {
  position: absolute;
  top: 16px;
  right: 16px;
  font-size: 14px;
  color: #475569;
}

.weather-main {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  transform: scale(1.1);
}

.weather-icon {
  font-size: 56px;
}

.weather-temp {
  font-size: 36px;
  font-weight: 600;
  color: #000000;
}

.weather-details {
  display: flex;
  flex-direction: column;
  gap: 12px;
  font-size: 15px;
  color: #64748b;
}

/* 活动卡片 */
.activities-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  padding: 12px;
  background: #E4EFF1;
  border-radius: 8px;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.activity-item:hover {
  background: #cfdfe2;
  border-color: #cfdfe2;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.activity-title {
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 6px;
}

.activity-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: #64748b;
}

/* 新闻卡片 */
.news-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.news-item {
  padding: 12px;
  background: #E4EFF1;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid transparent;
}

.news-item:hover {
  background: #cfdfe2;
  border-color: #cfdfe2;
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.news-title {
  font-weight: 500;
  color: #1e293b;
  margin-bottom: 6px;
  font-size: 14px;
  line-height: 1.5;
}

.news-title:hover {
  color: #409eff;
}

.news-time {
  font-size: 12px;
  color: #94a3b8;
}

.loading, .empty, .error {
  text-align: center;
  padding: 20px;
  color: #94a3b8;
  font-size: 14px;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.error {
  color: #ef4444;
}
</style>
