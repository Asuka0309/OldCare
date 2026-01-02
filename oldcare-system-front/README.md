# OldCare 智慧养老管理平台 - 前端

基于 Vue 3 + Element Plus 的智慧养老管理平台前端应用。为社区居民、管理员和服务提供商提供便捷的在线服务平台。

## 技术栈

- **框架**: Vue 3（Composition API）
- **UI 组件库**: Element Plus
- **HTTP 客户端**: Axios
- **状态管理**: Pinia
- **构建工具**: Vite
- **路由**: Vue Router
- **日期处理**: Day.js

## 项目结构

```
src/
├── api/                  # API 接口层
│   ├── auth.js          # 认证相关
│   ├── user.js          # 用户管理
│   ├── appointment.js   # 预约管理
│   ├── elderly.js       # 老人信息
│   ├── caregiver.js     # 护工管理
│   ├── service.js       # 服务项目
│   ├── healthRecord.js  # 健康记录
│   ├── healthCheck.js   # 健康检查
│   ├── emergencyHelp.js # 紧急求助
│   ├── evaluation.js    # 服务评价
│   ├── feedback.js      # 反馈管理
│   ├── communityActivity.js  # 社区活动
│   ├── feeRecord.js     # 费用记录
│   ├── income.js        # 收入统计
│   ├── notification.js  # 通知
│   ├── weather.js       # 天气
│   ├── news.js          # 新闻资讯
│   ├── quotation.js     # 报价
│   ├── serviceNeed.js   # 服务需求
│   └── http.js          # HTTP 拦截器
├── assets/              # 静态资源
│   ├── base.css
│   └── main.css
├── components/          # 通用组件
├── config/              # 配置文件
│   └── api.js          # API 配置
├── router/              # 路由配置
│   └── index.js
├── store/               # Pinia 状态管理
│   └── auth.js         # 认证状态
├── views/               # 页面组件
│   ├── LoginView.vue           # 登录
│   ├── RegisterView.vue        # 注册
│   ├── DashboardView.vue       # 首页仪表板
│   ├── LayoutShell.vue         # 布局壳
│   ├── UserView.vue            # 用户管理
│   ├── ElderlyView.vue         # 老人管理
│   ├── CaregiverView.vue       # 护工管理
│   ├── ServiceView.vue         # 服务项目
│   ├── AppointmentView.vue     # 预约管理
│   ├── HealthRecordView.vue    # 健康档案
│   ├── HealthCheckView.vue     # 健康检查
│   ├── EmergencyHelpView.vue   # 紧急求助
│   ├── EvaluationView.vue      # 服务评价
│   ├── FeedbackView.vue        # 反馈管理
│   ├── CommunityActivityView.vue # 社区活动
│   ├── FeeRecordView.vue       # 费用记录
│   ├── IncomeView.vue          # 收入统计
│   ├── QuotationView.vue       # 报价管理
│   ├── ServiceNeedView.vue     # 服务需求
│   ├── NotificationView.vue    # 通知
│   └── TestView.vue            # 测试页面
├── App.vue              # 根组件
└── main.js              # 入口文件
```

## 快速开始

### 环境要求

- Node.js 14+
- npm 或 yarn

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

访问 http://localhost:5173

### 生产构建

```bash
npm run build
```

### 预览生产构建

```bash
npm run preview
```

## 测试账户

系统已预设以下测试账户，可直接登录测试各功能模块：

| 账户名 | 角色 | 说明 |
|--------|------|------|
| 赵太太 | resident | 社区居民 |
| 吴太太 | resident | 社区居民 |
| 员工钱八 | caregiver | 服务护工 |
| 员工徐七 | caregiver | 服务护工 |
| admin | admin | 系统管理员 |

> 注：登录密码与账户名相同（初始密码），首次登录请修改密码

## 功能模块

### 用户与角色管理
- 用户注册与登录
- 用户信息管理
- 角色权限控制（admin、resident、caregiver）
- 用户列表查看和编辑

### 老人信息管理
- 老人基本信息录入
- 身份证、电话等必填项验证
- 老人列表查看和编辑
- 老人信息的增删改查

### 护工管理
- 护工信息注册与编辑
- 护工列表管理
- 护工资质和服务能力配置

### 服务项目管理
- 服务项目发布和编辑
- 服务项目分类与搜索
- 服务定价和时长设置
- 服务项目禁用与删除

### 预约管理
- 查看可用服务和护工
- 在线预约服务
- 预约状态跟踪（待确认/已确认/进行中/已完成/已取消/已退款）
- 预约记录查询

### 健康档案
- 健康档案创建与管理
- 就诊历史记录
- 健康指标追踪
- 医院与医生信息记录

### 健康检查
- 定期健康检查记录
- 体检数据输入和查询
- 检查结果分析

### 紧急求助
- 一键紧急求助发起
- 求助位置查看
- 求助状态跟踪（待响应/已响应/已解决）
- 紧急联系人管理

### 服务评价与反馈
- 服务完成后的评价
- 满意度评分
- 反馈管理和审核
- 评价列表展示

### 社区活动与资讯
- 社区活动通知发布
- 活动报名与参加
- 养老资讯和健康知识分享
- 活动列表查询

### 费用与收入管理
- 费用记录查询
- 支付状态管理
- 退款申请与审批
- 收入统计与分析报表

### 通知系统
- 实时通知接收
- 未读通知管理
- 通知类型区分

## 配置说明

### API 地址配置

编辑 `src/config/api.js`：

```javascript
export const API_CONFIG = {
  BASE_URL: 'http://localhost:8080/api'
}
```

### 环境变量

根目录 `.env.local` 文件：

```
VITE_API_BASE_URL=http://localhost:8080/api
```

## 权限控制

系统采用基于角色的访问控制（RBAC）：

- **admin（管理员）**: 完全访问所有功能
- **resident（居民）**: 查看和管理自己的信息、预约、健康档案等
- **caregiver（护工）**: 查看分配给自己的预约和任务，可处理紧急求助

## 页面路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/login` | 登录 | 用户登录 |
| `/register` | 注册 | 新用户注册 |
| `/` | 首页 | 仪表板 |
| `/user` | 用户管理 | 管理员功能 |
| `/elderly` | 老人管理 | 老人信息管理 |
| `/caregiver` | 护工管理 | 护工信息管理 |
| `/service` | 服务项目 | 服务项目列表 |
| `/appointment` | 预约管理 | 预约相关功能 |
| `/health-record` | 健康档案 | 健康档案管理 |
| `/health-check` | 健康检查 | 定期检查记录 |
| `/emergency-help` | 紧急求助 | 应急救助功能 |
| `/evaluation` | 服务评价 | 服务评价查看 |
| `/feedback` | 反馈管理 | 用户反馈处理 |
| `/community-activity` | 社区活动 | 活动与资讯 |
| `/fee-record` | 费用记录 | 费用管理 |
| `/income` | 收入统计 | 收入分析报表 |
| `/notification` | 通知 | 消息通知 |

## 常见问题

### CORS 跨域问题

确保后端已配置 CORS，在 `SecurityConfig` 中：

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:5173")
                .allowedMethods("*")
                .allowedHeaders("*");
        }
    };
}
```

### 登录状态丢失

检查 localStorage 中是否保存了 token，确认 `store/auth.js` 中的状态持久化配置。

## 开发建议

1. 遵循 Vue 3 Composition API 规范
2. 组件名使用 PascalCase
3. 使用 scoped CSS 避免样式冲突
4. API 调用统一使用 `src/api/` 下的函数
5. 状态管理使用 Pinia，避免直接修改

## 许可证

MIT
