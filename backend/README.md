# 新疆数字文化平台 — 后端 (backend)

## 📘 项目简介

本项目为新疆数字文化平台的后端服务，基于 **Spring Boot 3.2.0** 开发，提供文化内容管理、用户认证、活动管理、社区互动、文件上传、数据统计等完整的 RESTful API。

系统采用分层架构设计，使用 JWT 实现安全认证，支持多语言内容管理与第三方对象存储集成。应用上下文路径：`/api`（见 `server.servlet.context-path`）。

---

## ⚙️ 技术栈

| 模块 | 技术 |
|------|------|
| 核心框架 | Spring Boot 3.2.0 |
| Java 版本 | JDK 17+ |
| ORM 框架 | Spring Data JPA (Hibernate) |
| 数据库 | PostgreSQL（推荐）/ MySQL |
| 缓存 | Redis |
| 安全认证 | Spring Security + JWT (io.jsonwebtoken:jjwt 0.12.3) |
| 文件存储 | 本地存储（支持扩展至阿里云 OSS / 七牛 / AWS S3） |
| API 文档 | Springdoc OpenAPI / Swagger UI |
| 构建工具 | Maven |
| 部署方式 | Docker + docker-compose |
| 日志框架 | Logback |

---

## 🧭 核心功能模块

### 1. 认证与用户管理
- **用户注册**：支持用户名、邮箱注册
- **用户登录**：JWT Token 认证
- **用户信息**：获取/更新个人信息
- **密码管理**：修改密码功能
- **用户状态**：启用/禁用用户（管理员）
- **用户列表**：分页查询、关键词搜索

### 2. 文化资源管理
- **资源搜索**：支持关键词、类型、地区、标签多维度筛选
- **资源详情**：获取文化资源详细信息（文章、展品、视频等）
- **热门资源**：基于浏览量排序的热门资源推荐
- **精选推荐**：管理员配置的精选资源推荐
- **收藏功能**：收藏/取消收藏文化资源
- **资源类型**：支持 ARTICLE（文章）、EXHIBIT（展品）、VIDEO（视频）等类型
- **多媒体支持**：图片、视频、音频内容展示

### 3. 活动管理
- **活动列表**：分页查询，支持状态、类型、月份筛选
- **活动详情**：获取活动完整信息
- **活动报名**：用户报名/取消报名活动
- **我的报名**：查看已报名的活动列表
- **最新活动**：获取最新发布的活动
- **活动状态**：UPCOMING（即将开始）、ONGOING（进行中）、ENDED（已结束）、CANCELLED（已取消）
- **活动类型**：EXHIBITION（展览）、WORKSHOP（工作坊）、PERFORMANCE（演出）、LECTURE（讲座）等
- **报名审核**：管理员审核活动报名（通过/驳回）

### 4. 路线推荐
- **路线列表**：分页查询，支持主题筛选
- **路线详情**：获取路线详细信息
- **自定义路线**：根据用户偏好生成个性化路线
- **主题分类**：支持不同主题的路线推荐

### 5. 社区互动
- **帖子列表**：分页查询，支持排序（最新、热门等）
- **帖子详情**：获取帖子完整信息及评论
- **创建帖子**：用户发布社区投稿（需审核）
- **编辑/删除**：用户管理自己的帖子
- **点赞功能**：点赞/取消点赞帖子
- **评论功能**：对帖子进行评论
- **收藏帖子**：收藏/取消收藏社区帖子
- **我的帖子**：查看自己发布的帖子
- **点赞/评论/收藏记录**：查看相关互动记录
- **投稿审核**：管理员审核社区投稿（通过/驳回）

### 6. 轮播图管理
- **轮播图列表**：获取启用的轮播图（公开）或全部轮播图（管理员）
- **创建/更新/删除**：管理员管理轮播图
- **图片上传**：支持轮播图图片上传
- **排序控制**：支持轮播图显示顺序配置
- **启用/禁用**：控制轮播图显示状态

### 7. 首页推荐配置
- **推荐类型**：FEATURED（精选推荐）、HOT（热门推荐）
- **资源来源**：支持文化资源（CULTURE_RESOURCE）和社区投稿（COMMUNITY_POST）
- **推荐管理**：添加、更新、删除推荐配置
- **显示顺序**：支持自定义推荐资源的显示顺序
- **启用/禁用**：灵活控制推荐资源的显示状态
- **当前显示**：查看当前首页实际显示的资源及配置信息

### 8. 后台管理
- **仪表盘**：统计数据概览（用户数、资源数、活动数、帖子数等）
- **用户管理**：用户列表、编辑、删除、启用/禁用
- **文化资源管理**：资源的增删改查、图片/视频上传
- **活动管理**：活动的创建、编辑、删除、报名审核
- **社区投稿审核**：审核帖子、通过/驳回、删除违规内容
- **轮播图管理**：轮播图的完整管理功能
- **首页推荐配置**：管理首页推荐和热门资源

### 9. 文件上传
- **图片上传**：支持文化资源、活动、轮播图等图片上传
- **视频上传**：支持视频文件上传
- **文件存储**：本地文件系统存储（可扩展至云存储）
- **访问路径**：`/api/uploads/**` 静态资源访问

---

## 🧩 项目结构

```
backend/
│
├─ src/main/java/com/example/culturalxinjiang/
│  ├─ controller/          # 控制层（REST API）
│  │  ├─ AuthController.java              # 认证相关
│  │  ├─ UserController.java              # 用户相关
│  │  ├─ CultureController.java           # 文化资源
│  │  ├─ EventController.java             # 活动管理
│  │  ├─ RouteController.java             # 路线推荐
│  │  ├─ CommunityController.java          # 社区互动
│  │  ├─ CarouselController.java          # 轮播图
│  │  ├─ AdminController.java             # 后台管理（用户、资源、投稿、推荐）
│  │  └─ AdminEventController.java        # 后台活动管理
│  │
│  ├─ service/             # 业务逻辑层
│  │  ├─ AuthService.java
│  │  ├─ UserService.java
│  │  ├─ CultureResourceService.java
│  │  ├─ EventService.java
│  │  ├─ RouteService.java
│  │  ├─ CommunityService.java
│  │  ├─ CarouselService.java
│  │  ├─ FavoriteService.java
│  │  ├─ AdminService.java
│  │  ├─ AdminEventService.java
│  │  └─ HomeRecommendationService.java   # 首页推荐服务
│  │
│  ├─ repository/          # 数据访问层（JPA接口）
│  │  ├─ UserRepository.java
│  │  ├─ CultureResourceRepository.java
│  │  ├─ EventRepository.java
│  │  ├─ RouteRepository.java
│  │  ├─ CommunityPostRepository.java
│  │  ├─ CarouselRepository.java
│  │  ├─ FavoriteRepository.java
│  │  ├─ CommentRepository.java
│  │  ├─ PostLikeRepository.java
│  │  ├─ EventRegistrationRepository.java
│  │  └─ HomeRecommendationRepository.java
│  │
│  ├─ entity/              # 实体类
│  │  ├─ User.java
│  │  ├─ CultureResource.java
│  │  ├─ Event.java
│  │  ├─ Route.java
│  │  ├─ CommunityPost.java
│  │  ├─ Carousel.java
│  │  ├─ Favorite.java
│  │  ├─ Comment.java
│  │  ├─ PostLike.java
│  │  ├─ EventRegistration.java
│  │  └─ HomeRecommendation.java
│  │
│  ├─ dto/                 # 数据传输对象
│  │  ├─ request/          # 请求DTO
│  │  └─ response/         # 响应DTO
│  │
│  ├─ security/            # JWT、用户认证逻辑
│  │  ├─ JwtTokenProvider.java
│  │  ├─ JwtAuthenticationFilter.java
│  │  ├─ CustomUserDetailsService.java
│  │  ├─ PlainTextPasswordEncoder.java
│  │  └─ SecurityConfig.java
│  │
│  ├─ config/              # 配置文件（CORS、安全、Swagger 等）
│  │  ├─ OpenApiConfig.java
│  │  ├─ WebConfig.java
│  │  ├─ RedisConfig.java
│  │  └─ PasswordMigrationRunner.java
│  │
│  ├─ exception/           # 异常处理
│  │  └─ GlobalExceptionHandler.java
│  │
│  ├─ util/                # 工具类
│  │
│  └─ CulturalXinjiangApplication.java
│
├─ src/main/resources/
│  ├─ application.yml
│  ├─ application-dev.yml
│  ├─ application-prod.yml
│  └─ db/                  # 数据库脚本
│     ├─ schema.sql
│     ├─ init-data.sql
│     └─ ...
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

#### 2.4 AI 密钥配置（必须）

1. 复制 `src/main/resources/application-local.yml.example`，并重命名为 `application-local.yml`。
2. 在新文件中把 `sk-your-deepseek-api-key` 替换为你的真实 DeepSeek Key（例如 `sk-0d2fc9960f654d7db1ef5bf8c7ef6642`）。该文件已在 `.gitignore` 中，不会被提交。
3. 或者直接在终端设置环境变量：
   - Windows PowerShell：`setx DEEPSEEK_API_KEY "sk-0d2fc9960f654d7db1ef5bf8c7ef6642"`
   - macOS/Linux：`export DEEPSEEK_API_KEY="sk-0d2fc9960f654d7db1ef5bf8c7ef6642"`
4. 启动本地时激活 `local` 配置：`mvn spring-boot:run -Dspring.profiles.active=local`

> **安全提交建议**
> - 提交前执行 `git status` 与 `git diff`，确认没有 `.env`、`application-local.yml` 等敏感文件。
> - 使用 `git grep -n "sk-"` 检查仓库中是否残留密钥。
> - 在 GitHub 仓库的 *Settings → Secrets and variables → Actions* 中配置 `DEEPSEEK_API_KEY`，CI/CD 或部署流程统一从 Secrets 读取。

#### 2.5 运行应用

```bash
# 使用 Maven
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/backend-1.0.0.jar
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
      "email": "user@example.com",
      "role": "USER"
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
- `GET /api/culture/search` - 搜索文化资源（支持 keyword、type、region、tags 筛选）
- `GET /api/culture/{type}/{id}` - 获取资源详情
- `GET /api/culture/hot` - 获取热门资源
- `GET /api/culture/recommended` - 获取精选推荐资源
- `POST /api/culture/{type}/{id}/favorite` - 收藏资源
- `DELETE /api/culture/{type}/{id}/favorite` - 取消收藏

### 活动管理
- `GET /api/events` - 获取活动列表（支持 `status`、`type`、`month` 筛选）
- `GET /api/events/{id}` - 获取活动详情
- `GET /api/events/latest` - 获取最新活动
- `GET /api/events/my-registrations` - 获取我的报名活动
- `POST /api/events/{id}/register` - 报名活动
- `DELETE /api/events/{id}/register` - 取消报名

### 路线管理
- `GET /api/routes` - 获取路线列表（支持 `theme` 筛选）
- `GET /api/routes/{id}` - 获取路线详情
- `POST /api/routes/generate` - 生成自定义路线

### 社区功能
- `GET /api/community/posts` - 获取帖子列表（支持 `sort` 排序）
- `GET /api/community/posts/{id}` - 获取帖子详情
- `POST /api/community/posts` - 创建帖子
- `PUT /api/community/posts/{id}` - 更新帖子
- `DELETE /api/community/posts/{id}` - 删除帖子
- `POST /api/community/posts/{id}/like` - 点赞帖子
- `DELETE /api/community/posts/{id}/like` - 取消点赞
- `POST /api/community/posts/{id}/comments` - 评论帖子
- `POST /api/community/posts/{id}/favorite` - 收藏帖子
- `DELETE /api/community/posts/{id}/favorite` - 取消收藏
- `GET /api/community/posts/my` - 获取我的帖子
- `GET /api/community/posts/liked` - 获取点赞的帖子
- `GET /api/community/posts/commented` - 获取评论的帖子
- `GET /api/community/posts/favorites` - 获取收藏的帖子

### 轮播图
- `GET /api/carousel` - 获取启用的轮播图（公开）
- `GET /api/carousel/all` - 获取所有轮播图（管理员）
- `POST /api/carousel` - 创建轮播图（管理员）
- `PUT /api/carousel/{id}` - 更新轮播图（管理员）
- `DELETE /api/carousel/{id}` - 删除轮播图（管理员）
- `POST /api/carousel/upload` - 上传轮播图图片（管理员）

### 管理端（需管理员权限）
#### 用户管理
- `GET /api/admin/users` - 获取用户列表
- `PUT /api/admin/users/{id}` - 更新用户信息
- `DELETE /api/admin/users/{id}` - 删除用户
- `PUT /api/admin/users/{id}/status` - 启用/禁用用户

#### 文化资源管理
- `GET /api/admin/culture` - 获取文化资源列表
- `POST /api/admin/culture` - 创建文化资源
- `PUT /api/admin/culture/{id}` - 更新文化资源
- `DELETE /api/admin/culture/{id}` - 删除文化资源
- `POST /api/admin/culture/upload` - 上传文化资源图片/视频

#### 活动管理
- `GET /api/admin/events` - 获取活动列表
- `POST /api/admin/events` - 创建活动
- `PUT /api/admin/events/{id}` - 更新活动
- `DELETE /api/admin/events/{id}` - 删除活动
- `GET /api/admin/events/{id}/registrations` - 获取活动报名列表
- `PUT /api/admin/events/{eventId}/registrations/{registrationId}/approve` - 审核通过报名
- `PUT /api/admin/events/{eventId}/registrations/{registrationId}/reject` - 审核驳回报名
- `POST /api/admin/events/upload` - 上传活动图片/视频

#### 社区投稿管理
- `GET /api/admin/posts` - 获取帖子列表
- `PUT /api/admin/posts/{id}/approve` - 审核通过帖子
- `PUT /api/admin/posts/{id}/reject` - 审核驳回帖子
- `DELETE /api/admin/posts/{id}` - 删除帖子

#### 首页推荐配置
- `GET /api/admin/home-recommendations` - 获取推荐配置列表
- `POST /api/admin/home-recommendations` - 添加推荐配置
- `PUT /api/admin/home-recommendations/{id}` - 更新推荐配置
- `DELETE /api/admin/home-recommendations/{id}` - 删除推荐配置
- `GET /api/admin/home-recommendations/current` - 获取当前首页显示的资源

#### 仪表盘
- `GET /api/admin/dashboard/stats` - 获取统计数据
- `GET /api/admin/dashboard/pending-posts` - 获取待审核帖子
- `GET /api/admin/dashboard/ongoing-events` - 获取进行中的活动

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

# 文件上传
FILE_UPLOAD_DIR=uploads

# 阿里云OSS（可选）
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

4. **文件上传失败**
   - 检查上传目录权限
   - 检查文件大小限制配置

---

## 📄 许可证

本项目采用 Apache 2.0 许可证。

---

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

---

## 📞 联系方式

如有问题，请联系项目维护者。
