# 新疆数字文化平台

## 📘 项目简介

新疆数字文化平台是一个面向游客、研究者及文化机构的数字文化展示与交互平台，提供新疆地区的文化资源展示、活动管理、社区互动、路线推荐等功能。

项目采用前后端分离架构：
- **前端**：Vue 3 + Vite + TypeScript + Element Plus
- **后端**：Spring Boot 3.2.0 + PostgreSQL/MySQL + Redis

## 🚀 快速开始

### 前端
详细文档请查看 [前端 README](./frontend/README.md)

```bash
cd frontend
npm install
npm run dev
```

### 后端
详细文档请查看 [后端 README](./backend/README.md)

```bash
cd backend
mvn spring-boot:run
```

## 📁 项目结构

```
xinjiang/
├── frontend/          # 前端项目
│   ├── src/          # 源代码
│   ├── public/       # 静态资源
│   └── README.md     # 前端文档
├── backend/          # 后端项目
│   ├── src/          # 源代码
│   └── README.md     # 后端文档
└── README.md         # 项目主文档
```

## ✨ 核心功能

### 用户端功能
- 🏠 **首页展示**：轮播图、精选推荐、热门资源、最新活动
- 🔍 **搜索功能**：多维度筛选文化资源
- 📖 **资源详情**：文章、展品、视频等多媒体展示
- 🗺️ **地图探索**：文化地标展示和路线可视化
- 🛣️ **路线推荐**：AI智能生成个性化旅游路线
- 📅 **活动管理**：活动列表、详情、报名
- 💬 **社区互动**：发帖、评论、点赞、收藏
- 👤 **个人中心**：个人信息、收藏、投稿、报名记录

### 管理端功能
- 📊 **数据仪表盘**：统计概览、待审核内容
- 👥 **用户管理**：用户列表、编辑、启用/禁用
- 📚 **文化资源管理**：资源的增删改查、文件上传
- 🎪 **活动管理**：活动创建、编辑、报名审核
- 📝 **社区审核**：帖子审核、通过/驳回
- 🎠 **轮播图管理**：轮播图配置和管理
- ⭐ **首页推荐**：精选推荐和热门资源配置

## 🛠️ 技术栈

### 前端
- Vue 3 (Composition API)
- Vite 5.2.0
- TypeScript 5.5.4
- Element Plus 2.6.3
- Pinia 2.1.7
- Vue Router 4.3.0
- Axios 1.6.7
- Mapbox GL 3.1.2 (地图)
- Tailwind CSS 3.4.1

### 后端
- Spring Boot 3.2.0
- Spring Security + JWT
- Spring Data JPA
- PostgreSQL / MySQL
- Redis
- DeepSeek / OpenAI API (AI路线生成)
- Maven

## 📚 文档

- [前端详细文档](./frontend/README.md)
- [后端详细文档](./backend/README.md)

## 📄 许可证

MIT License

## 👥 贡献

欢迎提交 Issue 和 Pull Request！

