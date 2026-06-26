# API 集成说明

## 修改概述

已成功将前端从使用硬编码数据改为从后端 API 获取数据库实时数据。

## 主要修改

### 1. 安装依赖
- 安装了 `axios` 用于 HTTP 请求

### 2. 创建 API 服务层 (`frontend/api/`)
- `axiosConfig.js` - axios 配置，包含请求/响应拦截器
- `studentApi.js` - 学生相关 API
- `buildingApi.js` - 宿舍楼相关 API
- `dormitoryApi.js` - 宿舍相关 API
- `accommodationApi.js` - 住宿信息相关 API
- `repairApi.js` - 报修相关 API
- `roomChangeApi.js` - 换寝相关 API
- `visitApi.js` - 来访登记相关 API
- `adminApi.js` - 管理员相关 API

### 3. 修改 Store (`frontend/store/index.js`)
- 将硬编码数据改为空数组
- 添加 `loadData()` 函数，用于从 API 加载所有数据
- 保留兼容性的工具函数（addItem, updateItem, deleteItem 等）

### 4. 修改组件
- **layout/index.vue** - 添加页面加载时自动获取所有数据
- **views/Student.vue** - 修改 CRUD 操作调用 API
- **views/Building.vue** - 修改 CRUD 操作调用 API
- **views/Accommodation.vue** - 修改删除操作调用 API

## 数据流程

1. **页面加载**: layout/index.vue 调用 `loadData()` 从 API 获取所有数据
2. **数据显示**: 各个组件使用 store 中的数据，不再硬编码
3. **数据操作**: 增删改操作调用相应 API，然后重新加载数据以更新界面

## 如何测试

1. **启动后端**（确保在 8080 端口）:
   ```bash
   cd backend
   mvn spring-boot:run
   ```

2. **启动前端**:
   ```bash
   cd frontend
   npm run dev
   ```

3. **测试功能**:
   - 登录系统
   - 查看数据是否正确显示（应该显示数据库中的数据）
   - 尝试添加、修改、删除操作
   - 刷新页面，验证数据是否持久化

## 注意事项

1. **API 地址**: 默认配置为 `http://localhost:8080`，如需修改请编辑 `frontend/api/axiosConfig.js`

2. **Token 认证**: 系统会自动从 localStorage 获取 token 并添加到请求头

3. **错误处理**: 所有 API 调用都包含错误处理，失败时会显示 alert 提示

4. **加载状态**: 各个组件都有 loading 状态显示，操作时会显示"加载中..."

## 后续改进建议

1. 可以添加更友好的加载动画
2. 可以使用更优雅的通知组件替换 alert
3. 可以添加数据缓存机制，减少重复请求
4. 可以优化批量删除功能

