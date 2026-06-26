# IntelliJ IDEA 项目设置指南

## 🚀 快速设置步骤

### 1. 打开项目
1. 启动 IntelliJ IDEA
2. 选择 `File` → `Open`
3. 选择项目根目录：`C:\Users\17336\Desktop\xueshengsushe`
4. 点击 `OK`

### 2. 等待项目加载
- IDEA 会自动识别 Maven 模块（backend）
- IDEA 会自动识别前端模块（根目录的 package.json）
- 等待依赖下载完成

### 3. 配置运行环境

#### 后端配置（SpringBoot）
1. 右键点击 `backend/src/main/java/com/dormitory/DormitoryManagementApplication.java`
2. 选择 `Run 'DormitoryManagementApplication'`
3. 或者使用预配置的运行配置：`Backend SpringBoot`

#### 前端配置（Vue3）
1. 在右上角运行配置下拉框中选择 `Frontend Dev`
2. 点击运行按钮 ▶️
3. 或者右键点击 `package.json` → `Run 'npm'`

### 4. 验证运行
- **后端**: http://localhost:8080/api
- **前端**: http://localhost:5173

## 🔧 手动配置（如果自动识别失败）

### 后端模块配置
1. `File` → `Project Structure` → `Modules`
2. 点击 `+` → `Import Module`
3. 选择 `backend/pom.xml`
4. 选择 `Import module from external model` → `Maven`
5. 点击 `Next` → `Finish`

### 前端模块配置
1. `File` → `Project Structure` → `Modules`
2. 点击 `+` → `New Module`
3. 选择 `Static Web` → `Static Web`
4. 设置模块名：`frontend`
5. 设置内容根目录：项目根目录
6. 点击 `Finish`

### 运行配置
1. `Run` → `Edit Configurations`
2. 点击 `+` → `Spring Boot`
3. 设置名称：`Backend SpringBoot`
4. 设置主类：`com.dormitory.DormitoryManagementApplication`
5. 点击 `OK`

## 🐛 常见问题解决

### 问题1：Maven依赖下载失败
**解决方案**：
1. `File` → `Settings` → `Build` → `Build Tools` → `Maven`
2. 设置 `Maven home directory` 为本地Maven路径
3. 设置 `User settings file` 为本地settings.xml
4. 点击 `Apply` → `OK`

### 问题2：Node.js未识别
**解决方案**：
1. `File` → `Settings` → `Languages & Frameworks` → `Node.js and NPM`
2. 设置 `Node interpreter` 为本地Node.js路径
3. 点击 `Apply` → `OK`

### 问题3：JDK版本不匹配
**解决方案**：
1. `File` → `Project Structure` → `Project`
2. 设置 `Project SDK` 为 JDK 23
3. 设置 `Project language level` 为 23
4. 点击 `Apply` → `OK`

### 问题4：端口被占用
**解决方案**：
1. 修改 `backend/src/main/resources/application.yml` 中的端口
2. 或者关闭占用端口的程序

## 📋 项目结构说明

```
xueshengsushe/
├── backend/                 # SpringBoot后端模块
│   ├── src/main/java/      # Java源码
│   ├── src/main/resources/ # 配置文件
│   └── pom.xml            # Maven配置
├── src/                    # Vue3前端源码
├── package.json           # 前端依赖
├── vite.config.js         # Vite配置
└── .idea/                 # IDEA配置
```

## 🎯 运行顺序

1. **先启动后端**：确保数据库连接正常
2. **再启动前端**：前端会调用后端API
3. **访问系统**：http://localhost:5173

## 📞 技术支持

如果遇到问题，请检查：
1. JDK 23 是否正确安装
2. Maven 是否正确配置
3. Node.js 是否正确安装
4. MySQL 是否正在运行
5. 数据库是否已创建并初始化





