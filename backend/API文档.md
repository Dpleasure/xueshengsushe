# 学生宿舍管理系统 API 文档

## 基础信息
- 基础URL: `http://localhost:8080/api`
- 认证方式: JWT Token
- 请求头: `Authorization: Bearer <token>`

## 认证模块

### 1. 用户登录
- **URL**: `/auth/login`
- **方法**: POST
- **请求体**:
```json
{
  "username": "admin",
  "password": "123456",
  "role": "admin"
}
```
- **响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "token": "eyJhbGciOiJIUzUxMiJ9...",
    "username": "admin",
    "name": "管理员",
    "role": "ADMIN",
    "message": "登录成功"
  }
}
```

### 2. 用户注册
- **URL**: `/auth/register`
- **方法**: POST
- **请求体**:
```json
{
  "username": "newuser",
  "password": "123456",
  "name": "新用户",
  "phone": "18888888888",
  "email": "newuser@example.com"
}
```

## 宿舍楼管理

### 1. 获取所有宿舍楼
- **URL**: `/buildings`
- **方法**: GET

### 2. 根据ID获取宿舍楼
- **URL**: `/buildings/{id}`
- **方法**: GET

### 3. 搜索宿舍楼
- **URL**: `/buildings/search?keyword=关键词`
- **方法**: GET

### 4. 添加宿舍楼
- **URL**: `/buildings`
- **方法**: POST
- **请求体**:
```json
{
  "name": "F栋",
  "location": "西区生活区",
  "capacity": 12
}
```

### 5. 更新宿舍楼
- **URL**: `/buildings/{id}`
- **方法**: PUT

### 6. 删除宿舍楼
- **URL**: `/buildings/{id}`
- **方法**: DELETE

### 7. 批量删除宿舍楼
- **URL**: `/buildings/batch`
- **方法**: DELETE
- **请求体**: `[1, 2, 3]`

## 宿舍管理

### 1. 获取所有宿舍
- **URL**: `/dormitories`
- **方法**: GET

### 2. 根据ID获取宿舍
- **URL**: `/dormitories/{id}`
- **方法**: GET

### 3. 搜索宿舍
- **URL**: `/dormitories/search?keyword=关键词`
- **方法**: GET

### 4. 根据宿舍楼获取宿舍
- **URL**: `/dormitories/building/{building}`
- **方法**: GET

### 5. 添加宿舍
- **URL**: `/dormitories`
- **方法**: POST
- **请求体**:
```json
{
  "number": "A-301",
  "building": "A栋",
  "capacity": 4,
  "occupied": 0
}
```

### 6. 更新宿舍
- **URL**: `/dormitories/{id}`
- **方法**: PUT

### 7. 删除宿舍
- **URL**: `/dormitories/{id}`
- **方法**: DELETE

### 8. 批量删除宿舍
- **URL**: `/dormitories/batch`
- **方法**: DELETE

## 学生管理

### 1. 获取所有学生
- **URL**: `/students`
- **方法**: GET

### 2. 根据ID获取学生
- **URL**: `/students/{id}`
- **方法**: GET

### 3. 搜索学生
- **URL**: `/students/search?keyword=关键词`
- **方法**: GET

### 4. 添加学生
- **URL**: `/students`
- **方法**: POST
- **请求体**:
```json
{
  "username": "student001",
  "password": "123456",
  "name": "学生姓名",
  "phone": "18888888888",
  "email": "student@example.com"
}
```

### 5. 更新学生
- **URL**: `/students/{id}`
- **方法**: PUT

### 6. 删除学生
- **URL**: `/students/{id}`
- **方法**: DELETE

### 7. 批量删除学生
- **URL**: `/students/batch`
- **方法**: DELETE

## 住宿信息管理

### 1. 获取所有住宿信息
- **URL**: `/accommodations`
- **方法**: GET

### 2. 根据ID获取住宿信息
- **URL**: `/accommodations/{id}`
- **方法**: GET

### 3. 搜索住宿信息
- **URL**: `/accommodations/search?studentName=学生姓名&dormitory=宿舍号`
- **方法**: GET

### 4. 添加住宿信息
- **URL**: `/accommodations`
- **方法**: POST
- **请求体**:
```json
{
  "studentName": "张三",
  "studentId": "2021001",
  "dormitory": "A-101",
  "bed": "1号床",
  "building": "A栋",
  "checkInDate": "2024-09-01"
}
```

### 5. 更新住宿信息
- **URL**: `/accommodations/{id}`
- **方法**: PUT

### 6. 删除住宿信息
- **URL**: `/accommodations/{id}`
- **方法**: DELETE

## 报修管理

### 1. 获取所有报修记录
- **URL**: `/repairs`
- **方法**: GET

### 2. 根据ID获取报修记录
- **URL**: `/repairs/{id}`
- **方法**: GET

### 3. 搜索报修记录
- **URL**: `/repairs/search?keyword=关键词`
- **方法**: GET

### 4. 根据状态获取报修记录
- **URL**: `/repairs/status/{status}`
- **方法**: GET
- **状态值**: PENDING(待处理), PROCESSING(处理中), COMPLETED(已完成)

### 5. 添加报修记录
- **URL**: `/repairs`
- **方法**: POST
- **请求体**:
```json
{
  "reporter": "张三",
  "dormitory": "A-101",
  "description": "水龙头漏水，需要尽快维修",
  "status": "PENDING",
  "images": "[\"image1.jpg\", \"image2.jpg\"]"
}
```

### 6. 更新报修记录
- **URL**: `/repairs/{id}`
- **方法**: PUT

### 7. 更新报修状态
- **URL**: `/repairs/{id}/status?status=PENDING`
- **方法**: PUT

## 换寝管理

### 1. 获取所有换寝记录
- **URL**: `/room-changes`
- **方法**: GET

### 2. 根据ID获取换寝记录
- **URL**: `/room-changes/{id}`
- **方法**: GET

### 3. 搜索换寝记录
- **URL**: `/room-changes/search?keyword=关键词`
- **方法**: GET

### 4. 添加换寝记录
- **URL**: `/room-changes`
- **方法**: POST
- **请求体**:
```json
{
  "studentA": "张三",
  "studentB": "李四",
  "oldDormitoryA": "A-101",
  "oldBedA": "1号",
  "newDormitoryA": "A-102",
  "newBedA": "2号",
  "oldDormitoryB": "A-102",
  "oldBedB": "2号",
  "newDormitoryB": "A-101",
  "newBedB": "1号"
}
```

### 5. 更新换寝记录
- **URL**: `/room-changes/{id}`
- **方法**: PUT

### 6. 删除换寝记录
- **URL**: `/room-changes/{id}`
- **方法**: DELETE

## 来访登记管理

### 1. 获取所有来访记录
- **URL**: `/visits`
- **方法**: GET

### 2. 根据ID获取来访记录
- **URL**: `/visits/{id}`
- **方法**: GET

### 3. 搜索来访记录
- **URL**: `/visits/search?keyword=关键词`
- **方法**: GET

### 4. 添加来访记录
- **URL**: `/visits`
- **方法**: POST
- **请求体**:
```json
{
  "dormitory": "A-101",
  "description": "家长来访"
}
```

### 5. 更新来访记录
- **URL**: `/visits/{id}`
- **方法**: PUT

### 6. 删除来访记录
- **URL**: `/visits/{id}`
- **方法**: DELETE

## 管理员管理

### 1. 获取所有管理员
- **URL**: `/admins`
- **方法**: GET

### 2. 根据ID获取管理员
- **URL**: `/admins/{id}`
- **方法**: GET

### 3. 搜索管理员
- **URL**: `/admins/search?keyword=关键词`
- **方法**: GET

### 4. 添加管理员
- **URL**: `/admins`
- **方法**: POST

### 5. 更新管理员
- **URL**: `/admins/{id}`
- **方法**: PUT

### 6. 删除管理员
- **URL**: `/admins/{id}`
- **方法**: DELETE

## 统计信息

### 1. 获取系统统计数据
- **URL**: `/stats`
- **方法**: GET
- **响应**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "buildingCount": 5,
    "dormitoryCount": 10,
    "studentCount": 5,
    "repairCount": 10
  }
}
```

## 统一响应格式

所有API响应都遵循以下格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

- `code`: 状态码，200表示成功，其他表示失败
- `message`: 响应消息
- `data`: 响应数据，可能为null

## 错误处理

当请求失败时，会返回相应的错误信息：

```json
{
  "code": 500,
  "message": "错误信息",
  "data": null
}
```

## 注意事项

1. 所有需要认证的接口都需要在请求头中携带JWT token
2. 时间格式统一使用 `yyyy-MM-dd HH:mm:ss`
3. 分页参数暂未实现，后续可扩展
4. 文件上传功能暂未实现，报修图片使用URL字符串存储






