# 新疆数字文化平台 — 后端 (backend)

## 📘 项目简介

本项目为新疆数字文化平台的后端服务，基于 **Spring Boot 3.x** 开发，提供文化内容管理、用户认证、活动管理、文件上传、数据统计等 RESTful API。

系统采用分层架构设计，使用 JWT 实现安全认证，支持多语言内容管理与第三方对象存储集成。应用上下文路径：`/api`（见 `server.servlet.context-path`）。

---

## ⚙️ 技术栈

| 模块 | 技术 |
|------|------|
| 核心框架 | Spring Boot 3.x |
| ORM 框架 | Spring Data JPA (Hibernate) |
| 数据库 | PostgreSQL（推荐）/ MySQL |
| 缓存 | Redis |
| 安全认证 | Spring Security + JWT |
| 文件存储 | 阿里云 OSS / 七牛 / AWS S3 |
| 搜索功能 | Elasticsearch（可选） |
| API 文档 | Springdoc OpenAPI / Swagger UI |
| 测试框架 | JUnit 5 + Mockito |
| 构建工具 | Maven |
| 部署方式 | Docker + docker-compose |
| 消息队列（可选） | RabbitMQ / Kafka |

---

## 🧭 模块功能

1. **认证与用户** — 注册、登录、修改密码、用户信息、收藏  
2. **文化资源** — 搜索、详情、热门/推荐、收藏与取消收藏  
3. **活动与报名** — 列表、详情、报名/取消报名（支持状态/类型/月筛选）  
4. **路线** — 列表、详情、自定义路线生成（按主题筛选）  
5. **社区** — 发帖、详情、点赞/取消、评论  
6. **后台管理** — 用户、轮播、文化资源、活动、社区投稿审核  
7. **多媒体存储** — 文件上传，对外暴露 `/uploads/**` 静态资源  
8. **API 文档** — Springdoc OpenAPI（Swagger UI）  
9. **日志与监控** — Actuator + Logback  

---

## 🧩 项目结构

```
backend/
│
├─ src/main/java/com/example/culturalxinjiang/
│  ├─ controller/          # 控制层（REST API）
│  │  ├─ AuthController.java
│  │  ├─ UserController.java
│  │  ├─ CultureController.java
│  │  ├─ EventController.java
│  │  ├─ RouteController.java
│  │  ├─ CommunityController.java
│  │  ├─ AdminController.java
│  │  └─ AdminEventController.java
│  │
│  ├─ service/             # 业务逻辑层
│  │  ├─ AuthService.java
│  │  ├─ UserService.java
│  │  ├─ CultureResourceService.java
│  │  ├─ EventService.java
│  │  ├─ RouteService.java
│  │  ├─ CommunityService.java
│  │  └─ FavoriteService.java
│  │
│  ├─ repository/          # 数据访问层（JPA接口）
│  │  ├─ UserRepository.java
│  │  ├─ CultureResourceRepository.java
│  │  ├─ EventRepository.java
│  │  ├─ RouteRepository.java
│  │  ├─ CommunityPostRepository.java
│  │  └─ ...
│  │
│  ├─ entity/              # 实体类
│  │  ├─ User.java
│  │  ├─ CultureResource.java
│  │  ├─ Event.java
│  │  ├─ Route.java
│  │  ├─ CommunityPost.java
│  │  └─ ...
│  │
│  ├─ dto/                 # 数据传输对象
│  │  ├─ request/          # 请求DTO
│  │  └─ response/         # 响应DTO
│  │
│  ├─ security/            # JWT、用户认证逻辑
│  │  ├─ JwtTokenProvider.java
│  │  ├─ JwtAuthenticationFilter.java
│  │  ├─ CustomUserDetailsService.java
│  │  └─ SecurityConfig.java
│  │
│  ├─ config/              # 配置文件（CORS、安全、Swagger 等）
│  │  ├─ OpenApiConfig.java
│  │  └─ WebConfig.java
│  │
│  ├─ exception/           # 异常处理
│  │  └─ GlobalExceptionHandler.java
│  │
│  └─ CulturalXinjiangApplication.java
│
├─ src/main/resources/
│  ├─ application.yml
│  ├─ application-dev.yml
│  ├─ application-prod.yml
│  └─ static/
│
├─ Dockerfile
├─ docker-compose.yml
├─ pom.xml
└─ README.md
```

---

## 🚀 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 12+ (或 MySQL 8+)
- Redis 6+
- Docker (可选)

### 2. 本地开发

#### 2.1 克隆项目

```bash
git clone <repository-url>
cd xinjiang/backend
```

#### 2.2 配置数据库

创建 PostgreSQL 数据库：

```sql
CREATE DATABASE cultural_xinjiang;
```

#### 2.3 配置文件

编辑 `src/main/resources/application.yml`，配置数据库连接和 Redis：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/cultural_xinjiang
    username: postgres
    password: postgres
  redis:
    host: localhost
    port: 6379
```

#### 2.4 运行应用

```bash
# 使用 Maven
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/cultural-xinjiang-1.0.0.jar
```

应用将在 `http://localhost:8080/api` 启动。

### 3. Docker 部署

#### 3.1 使用 docker-compose

```bash
docker-compose up -d
```

这将启动：
- PostgreSQL 数据库 (端口 5432)
- Redis 缓存 (端口 6379)
- 后端应用 (端口 8080)

#### 3.2 单独构建 Docker 镜像

```bash
docker build -t cultural-xinjiang-backend .
docker run -p 8080:8080 cultural-xinjiang-backend
```

---

## 📡 API 文档

启动应用后，访问 Swagger UI：

```
http://localhost:8080/api/swagger-ui.html
```

API 文档地址：

```
http://localhost:8080/api/v3/api-docs
```

---

## 🔐 认证说明

### JWT Token 使用

1. **注册/登录** 获取 Token：

```bash
POST /api/auth/register
POST /api/auth/login
```

响应示例：
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "user",
      "email": "user@example.com"
    }
  }
}
```

2. **使用 Token** 访问受保护接口：

```bash
Authorization: Bearer <token>
```

---

## 📝 API 端点

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 用户相关
- `GET /api/user/info` - 获取用户信息
- `PUT /api/user/info` - 更新用户信息
- `POST /api/user/change-password` - 修改密码
- `GET /api/user/favorites` - 获取收藏列表

### 文化资源
- `GET /api/culture/search` - 搜索文化资源
- `GET /api/culture/{type}/{id}` - 获取资源详情
- `GET /api/culture/hot` - 获取热门资源
- `GET /api/culture/recommended` - 获取推荐资源
- `POST /api/culture/{type}/{id}/favorite` - 收藏资源
- `DELETE /api/culture/{type}/{id}/favorite` - 取消收藏
- `GET /api/user/favorites` - 获取收藏列表

### 活动管理
- `GET /api/events` - 获取活动列表（支持 `status`、`type`、`month`）
- `GET /api/events/{id}` - 获取活动详情
- `POST /api/events/{id}/register` - 报名活动
- `DELETE /api/events/{id}/register` - 取消报名

### 路线管理
- `GET /api/routes` - 获取路线列表（支持 `theme`）
- `GET /api/routes/{id}` - 获取路线详情
- `POST /api/routes/generate` - 生成自定义路线

### 社区功能
- `GET /api/community/posts` - 获取帖子列表
- `GET /api/community/posts/{id}` - 获取帖子详情
- `POST /api/community/posts` - 创建帖子
- `POST /api/community/posts/{id}/like` - 点赞帖子
- `DELETE /api/community/posts/{id}/like` - 取消点赞
- `POST /api/community/posts/{id}/comments` - 评论帖子

### 管理端（需管理员权限，以 Swagger 为准）
- `POST /api/admin/users/toggle` - 启用/禁用用户
- `POST /api/admin/culture` - 新增或编辑文化资源
- `POST /api/admin/carousels` - 轮播图增删改
- `POST /api/admin/events` - 创建/更新活动
- `POST /api/admin/posts/{id}/reject` - 驳回社区投稿

---

## 🔧 配置说明

### 环境变量

生产环境建议使用环境变量配置：

```bash
# 数据库
DB_URL=jdbc:postgresql://db:5432/cultural_xinjiang
DB_USERNAME=postgres
DB_PASSWORD=postgres

# Redis
REDIS_HOST=redis
REDIS_PORT=6379

# JWT
JWT_SECRET=your-secret-key
JWT_EXPIRATION=86400000

# 阿里云OSS
ALIYUN_ACCESS_KEY_ID=your-access-key-id
ALIYUN_ACCESS_KEY_SECRET=your-access-key-secret
ALIYUN_BUCKET_NAME=your-bucket-name
```

### 配置文件

- `application.yml` - 主配置文件
- `application-dev.yml` - 开发环境配置
- `application-prod.yml` - 生产环境配置

---

## 🧪 测试

运行测试：

```bash
mvn test
```

---

## 📦 构建

构建项目：

```bash
mvn clean package
```

构建产物位于 `target/` 目录。

---

## 🐛 故障排查

### 常见问题

1. **数据库连接失败**
   - 检查数据库服务是否启动
   - 检查数据库连接配置是否正确

2. **Redis 连接失败**
   - 检查 Redis 服务是否启动
   - 检查 Redis 配置是否正确

3. **JWT Token 无效**
   - 检查 Token 是否过期
   - 检查 JWT Secret 配置是否正确

---

## 📄 许可证

本项目采用 Apache 2.0 许可证。

---

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📞 联系方式

如有问题，请联系项目维护者。






