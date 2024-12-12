# 课程资源共享系统

一个用于教师和学生之间共享和管理课程资源、作业和提交内容的全栈网络应用程序。

## 功能特点

- 用户认证和授权（教师/学生）
- 课程资源管理
- 作业创建和提交
- 文件上传和下载
- 资源共享和管理
- 用户档案管理

## 技术栈

### 后端

- Java 11
- Spring Boot 2.7.6
- Spring Security
- MyBatis
- MySQL 8.0
- JWT认证

### 前端

- Vue.js 3
- Pinia（状态管理）
- Element Plus UI
- Axios
- Vite

## 前置条件

开始之前，请确保安装以下软件：

- JDK 11
- Node.js（最新LTS版本）
- MySQL 8.0
- Maven

## 安装步骤

### 数据库设置

1. 创建一个名为`sharing_system`的MySQL数据库
2. 导入数据库架构：

```bash
mysql -u your_username -p sharing_system < sharing_system.sql
```

### 后端设置

1. 进入后端目录：

```bash
cd backend
```

2. 在`src/main/resources/application.properties`中更新数据库配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/sharing_system
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. 构建并运行后端：

```bash
mvn clean install
mvn spring-boot:run
```

后端服务器将在 [http://localhost:8080](http://localhost:8080/) 启动

### 前端设置

1. 进入前端目录：

```bash
cd frontend
```

2. 安装依赖：

```bash
npm install
```

3. 启动开发服务器：

```bash
npm run dev
```

前端应用将在 [http://localhost:5173](http://localhost:5173/) 可用

## API文档

API文档可在以下位置找到：

- Postman测试集合：`/backend/docs/postman-testing-guide.md`
- API端点文档：查看后端的控制器文件

## 项目结构



```
├── backend/                 # Spring Boot后端
│   ├── src/                # 源文件
│   ├── pom.xml            # Maven依赖
│   └── docs/              # API文档
├── frontend/               # Vue.js前端
│   ├── src/               # 源文件
│   ├── public/            # 静态文件
│   └── package.json       # npm依赖
├── sharing_system.sql     # 数据库架构
└── README.md              # 项目文档
```

## 贡献指南

1. Fork本仓库
2. 为您的功能创建新分支
3. 提交您的更改
4. 推送到分支
5. 创建新的Pull Request

## 版权声明
本项目由[Kit-xiekaitong]开发和维护，根据 MIT 许可证发布。未经许可，禁止用于商业用途。

Copyright (c) 2024 [Kit]. All rights reserved.

## 支持与打赏
如需支持，请在仓库中创建issue或联系开发团队。
如果这个项目对你有帮助，欢迎打赏支持作者持续更新！


## 联系方式
- 邮箱：xiekaitong@gmail.com
- 微信：kt960200
- GitHub：https://github.com/traisvt

## 免责声明
1. 本项目是开源项目，使用时请遵守相关开源协议
2. 本项目仅供学习交流使用，严禁用于任何商业用途
3. 使用本项目所造成的任何损失，作者不承担任何责任
