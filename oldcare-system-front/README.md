# OldCare System 前端

基于 Vite + Vue 3 + Element Plus 的养老关怀管理前端，配合后端 `oldcare-system` 提供的 REST API，实现老人档案、预约服务、健康记录、通知提醒、财务记录等可视化与操作界面。

## 技术栈
- Node 20.19+（或 22.12+）
- Vite 7
- Vue 3、Vue Router、Pinia
- Element Plus、Element Plus Icons
- Axios、ECharts、Day.js

## 目录结构
```
oldcare-system-front/
├─ src/
│  ├─ api/        # 各业务 API 封装（axios）
│  ├─ assets/     # 样式与静态资源
│  ├─ components/ # 通用组件
│  ├─ config/     # 配置（如第三方接口）
│  ├─ router/     # 路由配置
│  ├─ store/      # Pinia 状态管理
│  └─ views/      # 业务页面
├─ public/        # 静态文件
├─ index.html
├─ package.json
└─ vite.config.js
```

## 环境准备
1. 安装 Node（建议使用与 `package.json` engines 匹配的版本）。
2. 安装 pnpm 或 npm：
   - `npm install -g pnpm`（推荐）
3. 安装依赖：
   - `pnpm install` 或 `npm install`

## 开发与构建
- 本地开发：`pnpm dev`（或 `npm run dev`），默认端口 5173。
- 预览构建：`pnpm preview`（或 `npm run preview`）。
- 生产构建：`pnpm build`（或 `npm run build`），产物位于 `dist/`。

## 后端接口地址
- 默认在 `src/api/http.js` 中配置基础地址（如使用 axios 实例）。若后端域名/端口有变更，请同步修改该文件或相关常量。
- 与后端 `oldcare-system` 保持一致的跨域与鉴权设置（通常为 JWT Bearer Token）。

## 第三方 API 配置
- 天气与新闻配置位于 `src/config/api.js`：
  - WEATHER：`BASE_URL`、`KEY`、`CITY`
  - NEWS：`BASE_URL`、`KEY`、`COUNTRY`
- 请将示例 Key 替换为你自己的 Key，并避免将真实 Key 提交到公共仓库。

## 常见问题
- 依赖安装失败：确认 Node 版本符合要求；如使用国内环境，可配置镜像源。
- 接口 401/403：检查前端存储的 Token 是否过期，或后端 JWT 密钥是否与前端期望一致。
- 跨域问题：本地开发可在后端开启 CORS；生产环境建议通过网关或反向代理统一域名。

## 前后端协同
- 后端仓库：`oldcare-system`，默认端口 8080。
- 若修改后端路径或鉴权策略，请同步更新前端 API 封装（位于 `src/api/*.js`）。

## 许可证与贡献
- 若需贡献，请在提交前运行 `pnpm build` 或 `npm run build` 确认无构建错误，并附必要说明。
