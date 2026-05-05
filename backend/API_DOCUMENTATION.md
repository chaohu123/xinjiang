# 新疆数字文化平台后端 API 文档

> **后端技术栈**：Spring Boot 3.2 · Spring Security · JWT · Spring Data JPA · PostgreSQL/MySQL · Redis
> **基础地址**：`https://{host}:{port}/api`（默认 `http://localhost:8080/api`）
> **统一响应**：
> ```json
> {
>   "code": 200,
>   "message": "成功",
>   "data": { ... } // 或数组
> }
> ```
> 发生错误时 `code` 为业务/HTTP 状态码，`message` 给出原因。

## 1. 认证与账号

### 1.1 注册
- `POST /auth/register`
- 请求体：
```json
{
  "username": "demo",
  "email": "demo@example.com",
  "password": "P@ssw0rd",
  "role": "USER"
}
```
- 响应：返回 `token` 与 `userInfo`。

### 1.2 登录
- `POST /auth/login`
- 请求体：`{ "username": "...", "password": "..." }`
- 响应：
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs...",
    "userInfo": {
      "id": 12,
      "username": "demo",
      "email": "demo@example.com",
      "role": "USER",
      "avatar": "https://..."
    }
  }
}
```
- 将 `token` 写入 `Authorization: Bearer <token>` 访问受保护接口。

### 1.3 刷新/注销
- Token 续签通过再次登录实现；服务端支持 Token 黑名单（如配置 Redis）。

## 2. 用户服务

| 接口 | 方法 | 描述 | 参数/体 |
| --- | --- | --- | --- |
| `/user/info` | GET | 获取当前用户信息 | Header 携带 Token |
| `/user/info` | PUT | 更新昵称、头像、简介、偏好等 | `{"nickname":"","avatar":"","bio":"","preferences":[]}` |
| `/user/change-password` | POST | 修改密码 | `{"oldPassword":"","newPassword":""}` |
| `/user/favorites` | GET | 收藏列表（文化资源或帖子） | `page` `size` |

## 3. 文化资源模块

### 3.1 搜索资源
- `GET /culture/search`
- 查询参数：`keyword`, `type`, `region`, `tags`, `heritageOnly`, `page`, `size`
- 响应：
```json
{
  "code": 200,
  "data": {
    "list": [
      {
        "id": 101,
        "type": "article",
        "title": "十二木卡姆",
        "description": "...",
        "cover": "https://...",
        "tags": ["音乐","非遗"],
        "region": "喀什",
        "views": 2389,
        "favorites": 120
      }
    ],
    "page": 1,
    "size": 10,
    "total": 120,
    "extra": {
      "heritage": [],
      "heritageOnly": false
    }
  }
}
```

### 3.2 资源详情
- `GET /culture/{type}/{id}`
- 返回 `CultureResource`，包含 `content`, `images`, `videoUrl`, `location`, `heritageLevel` 等。

### 3.3 热门与推荐
- `GET /culture/hot?limit=8`
- `GET /culture/recommended?limit=8`
- 返回 `HomeResource` 列表，用于首页展示。

### 3.4 收藏/取消收藏
- `POST /culture/{type}/{id}/favorite`
- `DELETE /culture/{type}/{id}/favorite`
- 成功响应 `204` 或 `{code:200}`。

## 4. 活动管理

| 接口 | 方法 | 描述 | 参数 |
| --- | --- | --- | --- |
| `/events` | GET | 活动列表 | `status`(UPCOMING/ONGOING/ENDED/CANCELLED) `type`(EXHIBITION/WORKSHOP/...) `month` `page` `size` |
| `/events/{id}` | GET | 活动详情 | |
| `/events/latest` | GET | 最新活动 | 默认 4 条 |
| `/events/{id}/register` | POST | 报名活动 | 请求体 `{"remark":"","participants":2}` |
| `/events/{id}/register` | DELETE | 取消报名 | |
| `/events/my-registrations` | GET | 我的报名 |  |

报名响应示例：
```json
{
  "code": 200,
  "data": {
    "registrationId": 88,
    "status": "PENDING",
    "eventId": 6,
    "userId": 12
  }
}
```

## 5. 路线推荐

| 接口 | 方法 | 描述 |
| --- | --- | --- |
| `/routes` | GET | 获取路线列表，支持 `theme`, `days`, `region`, `page`, `size` |
| `/routes/{id}` | GET | 路线详情（日程、地点、交通、预算） |
| `/routes/generate` | POST | AI 生成路线 |
| `/routes/my` | GET | 我的路线 |
| `/routes/{id}` | DELETE | 删除自定义路线 |

AI 生成请求体示例：
```json
{
  "destination": "伊犁",
  "days": 5,
  "budget": "medium",
  "interests": ["自然","民族音乐"],
  "travelers": 2,
  "season": "summer",
  "language": "zh-CN"
}
```
响应将包含 `itinerary`（每日数组）、`highlights`、`mapPoints`（含经纬度）以及 `provider`（deepseek/openai）。

## 6. 社区互动

### 6.1 帖子

| 接口 | 方法 | 描述 |
| --- | --- | --- |
| `/community/posts` | GET | 帖子列表，支持 `sort=latest/hot`, `tag`, `page`, `size` |
| `/community/posts/{id}` | GET | 帖子详情（含评论） |
| `/community/posts` | POST | 创建帖子：`{title, content, cover, tags}` |
| `/community/posts/{id}` | PUT | 更新帖子 |
| `/community/posts/{id}` | DELETE | 删除帖子 |

### 6.2 互动
- 点赞：`POST /community/posts/{id}/like`
- 取消点赞：`DELETE /community/posts/{id}/like`
- 收藏：`POST /community/posts/{id}/favorite`
- 取消收藏：`DELETE /community/posts/{id}/favorite`
- 评论：`POST /community/posts/{id}/comments`，体 `{content:"...", parentId?:number}`
- 我的帖子/点赞/评论/收藏：
  - `/community/posts/my`
  - `/community/posts/liked`
  - `/community/posts/commented`
  - `/community/posts/favorites`

## 7. 媒体与轮播

| 接口 | 方法 | 描述 |
| --- | --- | --- |
| `/carousel` | GET | 获取启用的轮播图（首页） |
| `/carousel/all` | GET | 管理员获取全部轮播图 |
| `/carousel` | POST | 管理员创建轮播 | `title`, `imageUrl`, `link`, `order`, `enabled` |
| `/carousel/{id}` | PUT | 更新轮播 |
| `/carousel/{id}` | DELETE | 删除 |
| `/carousel/upload` | POST | 上传轮播图片（`multipart/form-data`, 字段 `file`） |

### 数字图片/上传
- `GET /digital-images/{filename}`：访问 `digital-images/` 目录资源。
- 上传接口通常在文化资源、活动、社区等模块的 `/upload` 路径，返回文件访问 URL。

## 8. 管理端（需 `ADMIN` 角色）

### 8.1 仪表盘
- `GET /admin/dashboard/stats`：返回用户数、资源数、活动数、帖子数、待审核等。
- `GET /admin/dashboard/pending-posts`
- `GET /admin/dashboard/ongoing-events`

### 8.2 用户管理

| 接口 | 方法 | 描述 |
| --- | --- | --- |
| `/admin/users` | GET | 用户列表，支持 `keyword`, `status`, `role`, `page`, `size` |
| `/admin/users/{id}` | PUT | 更新用户（昵称、角色、状态） |
| `/admin/users/{id}` | DELETE | 删除用户 |
| `/admin/users/{id}/status` | PUT | 启用/禁用用户：`{"enabled":true}` |

### 8.3 文化资源管理
- `GET /admin/culture`：列表 + 筛选
- `POST /admin/culture`：创建资源（含多语言字段、媒体 URL）
- `PUT /admin/culture/{id}`：更新
- `DELETE /admin/culture/{id}`：删除
- `POST /admin/culture/upload`：上传图片/视频

### 8.4 活动管理
- `GET /admin/events`、`POST /admin/events`、`PUT /admin/events/{id}`、`DELETE /admin/events/{id}`
- 报名审核：
  - `GET /admin/events/{eventId}/registrations`
  - `PUT /admin/events/{eventId}/registrations/{registrationId}/approve`
  - `PUT /admin/events/{eventId}/registrations/{registrationId}/reject`
- 上传：`POST /admin/events/upload`

### 8.5 社区审核
- `GET /admin/posts`：查看待审核/全部帖子
- `PUT /admin/posts/{id}/approve`
- `PUT /admin.posts/{id}/reject`
- `DELETE /admin/posts/{id}`

### 8.6 首页推荐配置
- `GET /admin/home-recommendations`
- `POST /admin/home-recommendations`（`type`: FEATURED/HOT, `sourceType`: CULTURE_RESOURCE/COMMUNITY_POST, `resourceId`, `order`, `enabled`）
- `PUT /admin/home-recommendations/{id}`
- `DELETE /admin/home-recommendations/{id}`
- `GET /admin/home-recommendations/current`

## 9. 错误码示例

| code | message | 说明 |
| --- | --- | --- |
| 200 | 成功 | 请求成功 |
| 201 | Created | 成功创建资源 |
| 204 | No Content | 操作成功无返回 |
| 400 | 参数错误 | 参数缺失/格式不正确 |
| 401 | 未认证 | Token 缺失或无效 |
| 403 | 无权限 | 当前用户角色不足 |
| 404 | 资源不存在 | ID 未找到 |
| 409 | 业务冲突 | 例如重复收藏、重复报名 |
| 429 | 请求过于频繁 | 限流触发 |
| 500 | 服务器错误 | 未捕获异常 |

## 10. 调用建议
1. **认证**：登录后保存 Token；使用 `axios` 拦截器自动注入；处理 401 重定向登录。
2. **分页**：所有列表接口默认 `page=1`、`size=10`，支持最大 100；返回 `total` 便于前端分页。
3. **时间格式**：统一 ISO 8601（`YYYY-MM-DDTHH:mm:ssZ`），部分接口提供 `startDate`/`endDate` 字段。
4. **多语言**：部分字段（如 `title`, `description`) 可扩展 i18n 结构，当前默认中文；若要扩展，请携带 `Accept-Language`。
5. **AI 配置**：在 `application.yml` 中设置 `ai.provider`、`ai.deepseek/api-key` 或 `ai.openai/api-key`；前端调用 `/routes/generate` 时可通过 `language` 指定输出语言。
6. **安全**：上传接口需校验文件类型；对敏感操作开启 CSRF 保护或在前端二次确认；日志中避免输出敏感参数。

---
如需更详细字段/示例，可直接访问运行中的 Swagger UI：`/api/swagger-ui.html` 或 `http://localhost:8080/api/v3/api-docs`。


