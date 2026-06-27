# 学生宿舍管理系统

这是一个前后端分离的学生宿舍管理系统，包含 Spring Boot 后端、Vue 3 管理端和微信小程序端。

## 项目结构

```text
xueshengsushe-master/
├─ backend/                 # 后端服务，Spring Boot + Maven
│  ├─ src/main/java/         # 控制器、服务、实体、仓库、配置
│  ├─ src/main/resources/    # application.yml、SQL 初始化脚本
│  └─ pom.xml
├─ frontend/                # Web 前端，Vue 3 + Vite
│  ├─ api/                  # 接口封装
│  ├─ assets/               # 样式和图片资源
│  ├─ layout/               # 页面布局
│  ├─ router/               # 路由配置
│  ├─ store/                # 前端状态
│  ├─ views/                # 页面组件
│  ├─ package.json
│  └─ vite.config.js
├─ miniprogram/             # 微信小程序端
│  ├─ pages/                # 小程序页面
│  ├─ utils/                # 请求、格式化等工具
│  ├─ custom-tab-bar/       # 自定义底部导航
│  └─ config.js
├─ uploads/                 # 运行时上传文件目录
├─ run-backend.bat          # Windows 后端启动脚本
├─ run-frontend.bat         # Windows 前端启动脚本
└─ .gitignore
```

## 技术栈

- 后端：Java 17、Spring Boot 3.3.0、Spring Security、Spring Data JPA、JWT、MySQL、Maven
- 前端：Vue 3、Vue Router、Element Plus、Axios、Vite
- 小程序：微信小程序原生框架

## 主要功能

- 用户登录、注册和 JWT 鉴权
- 管理员后台首页与统计
- 宿舍楼管理
- 宿舍管理
- 学生管理
- 住宿信息管理
- 报修申请与处理
- 换宿申请与审批
- 来访登记与审批
- AI 助手接口
- 微信小程序学生端操作入口

## 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+
- 微信开发者工具

## 数据库配置

后端默认连接本机 MySQL：

```yaml
url: jdbc:mysql://localhost:3306/XueShengSuShe
username: root
password: ttcc7798521
```

如需修改数据库账号密码，编辑：

```text
backend/src/main/resources/application.yml
```

项目也支持通过本地环境文件覆盖配置：

```text
.env.local
backend/.env.local
```

示例：

```properties
spring.datasource.username=root
spring.datasource.password=你的密码
```

## 启动后端

```powershell
cd C:\Users\17336\Desktop\xueshengsushe-master\backend
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

## 启动前端

首次运行需要安装依赖：

```powershell
cd C:\Users\17336\Desktop\xueshengsushe-master\frontend
npm install
npm run dev
```

Vite 配置的前端端口是：

```text
http://localhost:3000
```

如果出现 `'vite' 不是内部或外部命令`，说明前端依赖还没有安装，先执行 `npm install`。

## 启动小程序

1. 打开微信开发者工具。
2. 导入 `miniprogram` 目录。
3. 确认后端服务已经启动。
4. 如需真机调试，修改 `miniprogram/config.js` 中的局域网地址为电脑当前 IPv4 地址。

## 常用脚本

Windows 下可以直接双击：

```text
run-backend.bat
run-frontend.bat
```

命令行方式更推荐，方便查看报错日志。

## 默认账号

初始化数据中包含示例账号：

```text
管理员：admin / 123456
学生：zhangsan / 123456
```

## AI 助手配置

AI 助手由前端页面调用后端接口，再由后端转发到 DeepSeek API。不要把真实 API Key 写进源码或提交到 Git。

### 1. 准备 DeepSeek API Key

到 DeepSeek 平台创建 API Key。拿到 Key 后，在后端目录新建本地配置文件：

```text
backend/.env.local
```

文件内容：

```properties
DEEPSEEK_API_KEY=你的DeepSeek API Key
```

也可以使用 Spring 配置名：

```properties
deepseek.api-key=你的DeepSeek API Key
```

项目里提供了示例文件：

```text
backend/.env.example
```

复制示例文件时，记得把文件名改成 `.env.local`，并替换成真实 Key。

### 2. 确认后端配置

AI 助手默认配置在：

```text
backend/src/main/java/com/dormitory/config/DeepSeekProperties.java
```

默认值：

```text
deepseek.base-url=https://api.deepseek.com
deepseek.model=deepseek-v4-flash
deepseek.timeout-seconds=30
```

一般只需要配置 `DEEPSEEK_API_KEY`，不用修改代码。

### 3. 重启后端

修改 `.env.local` 后必须重启后端：

```powershell
cd C:\Users\17336\Desktop\xueshengsushe-master\backend
mvn spring-boot:run
```

启动日志中如果看到 `DeepSeek API Key loaded`，说明 Key 已加载。

### 4. 启动前端并测试

```powershell
cd C:\Users\17336\Desktop\xueshengsushe-master\frontend
npm run dev
```

登录系统后进入“智能助手”页面，发送一句宿舍管理相关问题，例如：

```text
我想提交报修申请，应该怎么操作？
```

### 5. 常见问题

- 提示 `DeepSeek API Key 未配置`：检查 `backend/.env.local` 是否存在，文件名不要写成 `.env.local.txt`，并重启后端。
- 提示 `DeepSeek API Key 无效或未授权`：检查 Key 是否复制完整、账号是否可用。
- 提示 `请求过于频繁或额度不足`：检查 DeepSeek 账号额度，稍后重试。
- 提示 `连接 DeepSeek 超时或网络不可用`：检查电脑网络、代理、防火墙，确认后端机器能访问 `https://api.deepseek.com`。
- 前端提示请求失败：先确认后端 `http://localhost:8080` 已启动，再查看后端控制台日志。

## 注意事项

- `backend/target/` 是 Maven 编译产物，不需要提交。
- `frontend/node_modules/` 是前端依赖目录，不需要提交。
- `uploads/` 是运行时上传文件目录，通常不提交。
- `.env.local` 用于本地敏感配置，不要提交到 Git。
- 如果端口被占用，修改 `backend/src/main/resources/application.yml` 或 `frontend/vite.config.js`。
