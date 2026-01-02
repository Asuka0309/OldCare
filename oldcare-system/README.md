# OldCare System 后端

一个基于 Spring Boot 3 的养老关怀管理后端，提供用户与权限、老人档案、照护预约、健康检查、社区活动、费用与收入、反馈与通知等核心服务，为前端（`oldcare-system-front`）提供 REST API 支撑。

## 技术栈
- JDK 17、Maven 3.9+
- Spring Boot 3.2.8、Spring Security、JWT
- MyBatis-Plus、MySQL（HikariCP 连接池）
- Lombok、Jackson
- 单元测试：Spring Boot Starter Test、Spring Security Test

## 功能模块（按业务）
- 用户与认证：登录、注册、基于 JWT 的身份校验与权限控制。
- 老人信息：老人基本资料、健康档案与体检记录管理。
- 照护与服务：照护人员、服务需求、服务项目、预约管理。
- 应急与通知：紧急求助、通知提醒、消息推送。
- 活动与资讯：社区活动、新闻资讯聚合（聚合数据头条 API）。
- 财务记录：费用记录、收入记录、报价管理。
- 评价与反馈：服务评价、意见反馈收集。

## 目录结构
```
oldcare-system/
├─ pom.xml                 # Maven 项目定义（Spring Boot 3、Java 17）
├─ src/main/java/com/example/oldcaresystem/
│  ├─ controller/          # 控制器，暴露 REST API
│  ├─ service/             # 业务服务
│  ├─ entity/              # 实体与数据模型
│  ├─ mapper/              # MyBatis-Plus Mapper 接口
│  ├─ security/            # 安全与认证配置
│  └─ ...
├─ src/main/resources/
│  ├─ application.properties  # 核心配置（数据库、JWT、日志等）
│  ├─ mapper/                 # MyBatis-Plus XML 映射
│  └─ db/oldcare_system.sql   # 初始化 SQL 脚本
└─ target/                  # 构建输出目录
```

## 环境要求
- JDK 17+
- Maven 3.9+（或兼容版本）
- MySQL 8.x（默认库名：`oldcare_system`）

## 快速开始
1. 克隆或下载项目源码，进入 `oldcare-system` 目录。
2. 创建数据库并导入初始化脚本：`src/main/resources/db/oldcare_system.sql`。
3. 配置 `src/main/resources/application.properties` 中的数据库账号和口令（`spring.datasource.*`）。
4. 启动方式（任选其一）：
   - 开发调试：`mvn spring-boot:run`
   - 打包运行：
     ```bash
     mvn clean package -DskipTests
     java -jar target/oldcare-system-0.0.1-SNAPSHOT.jar
     ```
5. 启动后默认端口：`http://localhost:8080`。

## 配置要点
- 数据源：`spring.datasource.url` 指向 `oldcare_system` 库，可根据环境调整主机、端口、库名与凭据。
- MyBatis-Plus：`mybatis-plus.mapper-locations=classpath*:/mapper/**/*.xml`，别名包为 `com.example.oldcaresystem.entity`。
- JWT：`jwt.secret` 为签名密钥，`jwt.expiration` 为过期秒数（默认 86400 秒）。请在生产环境替换为更安全的密钥，并与前端保持一致。
- 日志：`logging.level.com.example.oldcaresystem=DEBUG`，可在生产环境下调为 INFO。

## 常用命令
- 构建并跳过测试：`mvn clean package -DskipTests`
- 运行单元测试：`mvn test`
- 本地启动：`mvn spring-boot:run`

## 部署建议
- 将环境配置（数据库、JWT 密钥、日志级别）通过环境变量或外部化配置管理。
- 数据库账户使用最小权限原则，开启备份与审计。
- 如置于公网，请在网关或负载均衡层启用 HTTPS 与限流，必要时接入 WAF。

## 前后端协同
- 前端工程位于同级目录 `oldcare-system-front`，基于 Vite/Vue。调整接口域名或端口时，请同步修改前端的 API 基础地址（见 `oldcare-system-front/src/config/api.js`）。

## 许可与贡献
- 如需贡献代码，请在提交前运行格式检查与测试，并附上必要的说明。
