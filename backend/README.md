# 学生宿舍管理系统后端

基于SpringBoot + MySQL的学生宿舍管理系统后端服务。

## 技术栈

- **框架**: Spring Boot 2.7.14
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA
- **安全**: Spring Security + JWT
- **构建工具**: Maven
- **Java版本**: JDK 8

## 功能模块

- 用户认证（登录、注册）
- 宿舍楼管理
- 宿舍管理
- 学生管理
- 住宿信息管理
- 报修管理
- 换寝管理
- 来访登记管理
- 管理员管理
- 系统统计

## 快速开始

### 1. 环境要求

- JDK 23+
- Maven 3.6+
- MySQL 8.0+
- Node.js 16+ (前端)

### 2. 数据库配置

1. 创建数据库：
```sql
CREATE DATABASE XueShengSuShe CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
mysql -u root -p XueShengSuShe < src/main/resources/sql/init.sql
```

3. 修改数据库连接配置（如需要）：
```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/XueShengSuShe?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: ttcc7798521
```

### 3. 运行项目

#### 方法1：使用IntelliJ IDEA（推荐）

1. **导入项目**：
   - 打开IntelliJ IDEA
   - File → Open → 选择项目根目录
   - IDEA会自动识别Maven模块和前端模块

2. **运行后端**：
   - 在Run Configuration中选择 "Backend SpringBoot"
   - 点击运行按钮 ▶️
   - 或者直接运行 `DormitoryManagementApplication.java`

3. **运行前端**：
   - 在Run Configuration中选择 "Frontend Dev"
   - 点击运行按钮 ▶️
   - 或者双击 `run-frontend.bat` (Windows) / `run-frontend.sh` (Mac/Linux)

#### 方法2：使用命令行

```bash
# 后端
cd backend
mvn clean compile
mvn spring-boot:run

# 前端（新开终端）
npm install
npm run dev
```

#### 方法3：使用脚本

```bash
# Windows
run-backend.bat    # 启动后端
run-frontend.bat   # 启动前端

# Mac/Linux
chmod +x run-backend.sh run-frontend.sh
./run-backend.sh   # 启动后端
./run-frontend.sh  # 启动前端
```

### 4. 访问接口

- 服务地址: http://localhost:8080/api
- API文档: 查看 `API文档.md`

## 项目结构

```
backend/
├── src/main/java/com/dormitory/
│   ├── DormitoryManagementApplication.java    # 启动类
│   ├── config/                                # 配置类
│   │   ├── CorsConfig.java                   # CORS配置
│   │   ├── JwtAuthenticationFilter.java      # JWT过滤器
│   │   └── SecurityConfig.java               # 安全配置
│   ├── controller/                           # 控制器层
│   │   ├── AccommodationController.java      # 住宿信息控制器
│   │   ├── AdminController.java              # 管理员控制器
│   │   ├── AuthController.java               # 认证控制器
│   │   ├── BuildingController.java           # 宿舍楼控制器
│   │   ├── DormitoryController.java          # 宿舍控制器
│   │   ├── RepairController.java             # 报修控制器
│   │   ├── RoomChangeController.java         # 换寝控制器
│   │   ├── StatsController.java              # 统计控制器
│   │   ├── StudentController.java            # 学生控制器
│   │   └── VisitController.java              # 来访登记控制器
│   ├── dto/                                  # 数据传输对象
│   │   ├── ApiResponse.java                  # 统一响应格式
│   │   ├── LoginRequest.java                 # 登录请求
│   │   └── LoginResponse.java                # 登录响应
│   ├── entity/                               # 实体类
│   │   ├── Accommodation.java                # 住宿信息实体
│   │   ├── Building.java                     # 宿舍楼实体
│   │   ├── Dormitory.java                    # 宿舍实体
│   │   ├── Repair.java                       # 报修实体
│   │   ├── RoomChange.java                   # 换寝实体
│   │   ├── User.java                         # 用户实体
│   │   └── Visit.java                        # 来访登记实体
│   ├── exception/                            # 异常处理
│   │   └── GlobalExceptionHandler.java       # 全局异常处理器
│   ├── repository/                           # 数据访问层
│   │   ├── AccommodationRepository.java      # 住宿信息仓库
│   │   ├── BuildingRepository.java           # 宿舍楼仓库
│   │   ├── DormitoryRepository.java          # 宿舍仓库
│   │   ├── RepairRepository.java             # 报修仓库
│   │   ├── RoomChangeRepository.java         # 换寝仓库
│   │   ├── UserRepository.java               # 用户仓库
│   │   └── VisitRepository.java              # 来访登记仓库
│   └── service/                              # 服务层
│       ├── AccommodationService.java         # 住宿信息服务
│       ├── AuthService.java                  # 认证服务
│       ├── BuildingService.java              # 宿舍楼服务
│       ├── DormitoryService.java             # 宿舍服务
│       ├── RepairService.java                # 报修服务
│       ├── RoomChangeService.java            # 换寝服务
│       ├── StudentService.java               # 学生服务
│       └── VisitService.java                 # 来访登记服务
├── src/main/resources/
│   ├── application.yml                       # 应用配置
│   └── sql/
│       └── init.sql                          # 数据库初始化脚本
├── pom.xml                                   # Maven配置
├── API文档.md                                # API接口文档
└── README.md                                 # 项目说明
```

## 默认数据

系统初始化时会自动创建以下测试数据：

### 用户账号
- 管理员: `admin` / `123456`
- 学生: `zhangsan` / `123456`

### 宿舍楼
- A栋、B栋、C栋、D栋、E栋

### 宿舍
- A-101 到 A-206 等宿舍

## API接口

详细的API接口文档请查看 `API文档.md` 文件。

## 开发说明

### 1. 数据库设计
- 使用JPA自动建表，表结构由实体类定义
- 支持MySQL数据库
- 使用utf8mb4字符集支持emoji

### 2. 安全认证
- 使用JWT进行用户认证
- 支持管理员和学生两种角色
- 登录接口不需要认证，其他接口需要携带token

### 3. 跨域配置
- 已配置CORS支持前端跨域访问
- 允许所有来源的请求

### 4. 异常处理
- 全局异常处理器统一处理异常
- 返回统一的错误响应格式

## 部署说明

### 1. 打包应用
```bash
mvn clean package
```

### 2. 运行jar包
```bash
java -jar target/dormitory-management-1.0.0.jar
```

### 3. 生产环境配置
- 修改数据库连接信息
- 修改JWT密钥
- 配置日志级别
- 配置服务器端口

## 注意事项

1. 确保MySQL服务已启动
2. 确保数据库连接信息正确
3. 首次运行会自动创建表结构
4. 建议在生产环境中修改默认密码和JWT密钥

## 联系方式

如有问题，请联系开发团队。
