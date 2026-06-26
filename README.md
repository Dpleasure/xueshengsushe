# 学生宿舍管理系统

基于SpringBoot + Vue3的前后端分离学生宿舍管理系统。

## 🏗️ 项目结构

```
xueshengsushe/
├── backend/                 # 后端模块 (SpringBoot)
│   ├── src/
│   ├── pom.xml
│   └── README.md
├── src/                    # 前端模块 (Vue3)
│   ├── views/
│   ├── components/
│   └── ...
├── package.json            # 前端依赖
├── vite.config.js         # Vite配置
├── run-backend.bat        # 后端启动脚本(Windows)
├── run-backend.sh         # 后端启动脚本(Mac/Linux)
├── run-frontend.bat       # 前端启动脚本(Windows)
├── run-frontend.sh        # 前端启动脚本(Mac/Linux)
└── README.md              # 项目说明
```

## 🚀 快速开始

### 环境要求
- JDK 23+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+
- IntelliJ IDEA 2023+

### 1. 数据库准备
```sql
-- 创建数据库
CREATE DATABASE XueShengSuShe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
mysql -u root -p XueShengSuShe < backend/src/main/resources/sql/init.sql
```

### 2. 使用IntelliJ IDEA运行（推荐）

#### 方法1：自动识别（推荐）
1. **打开项目**：
   - File → Open → 选择项目根目录 `C:\Users\17336\Desktop\xueshengsushe`
   - IDEA会自动识别Maven模块（backend）和前端模块

2. **运行后端**：
   - 右键点击 `DormitoryManagementApplication.java` → Run
   - 或选择Run Configuration: "Backend SpringBoot"
   - 访问: http://localhost:8080/api

3. **运行前端**：
   - 选择Run Configuration: "Frontend Dev"
   - 点击运行按钮 ▶️
   - 访问: http://localhost:5173

#### 方法2：手动配置
如果自动识别失败，请参考 [IDEA设置指南.md](IDEA设置指南.md)

### 3. 使用脚本运行

```bash
# Windows
run-backend.bat    # 启动后端
run-frontend.bat   # 启动前端

# Mac/Linux
chmod +x run-backend.sh run-frontend.sh
./run-backend.sh   # 启动后端
./run-frontend.sh  # 启动前端
```

### 4. 使用命令行运行

```bash
# 后端
cd backend
mvn spring-boot:run

# 前端（新开终端）
npm install
npm run dev
```

## 🔧 技术栈

### 后端
- Spring Boot 2.7.14
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- Maven

### 前端
- Vue 3
- Vue Router 4
- Vite
- 原生CSS

## 📋 功能模块

- ✅ 用户认证（登录、注册）
- ✅ 宿舍楼管理
- ✅ 宿舍管理
- ✅ 学生管理
- ✅ 住宿信息管理
- ✅ 报修管理
- ✅ 换寝管理
- ✅ 来访登记管理
- ✅ 管理员管理
- ✅ 系统统计

## 🔑 默认账号

- **管理员**: `admin` / `123456`
- **学生**: `zhangsan` / `123456`

## 📚 文档

- [后端API文档](backend/API文档.md)
- [后端README](backend/README.md)

## 🛠️ 开发说明

### 后端开发
- 使用SpringBoot + JPA
- 支持JWT认证
- 完整的CRUD操作
- 全局异常处理

### 前端开发
- 使用Vue3 Composition API
- 响应式设计
- 模块化组件
- 路由管理

## 🚨 注意事项

1. 确保MySQL服务已启动
2. 确保数据库连接信息正确
3. 首次运行会自动创建表结构
4. 建议在生产环境中修改默认密码

## 📞 联系方式

如有问题，请联系开发团队。