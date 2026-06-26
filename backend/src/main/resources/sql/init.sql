-- 学生宿舍管理系统 - 完整初始化脚本
-- 数据库: XueShengSuShe
-- 说明:
-- - 使用 utf8mb4 字符集
-- - 启用严格模式避免隐式截断
-- - 完整外键依赖和触发器
-- - 保障宿舍容量与床位唯一性

-- 基础设置
SET NAMES utf8mb4;
SET time_zone = '+08:00';
SET sql_safe_updates = 0;
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- 建库并切库
CREATE DATABASE IF NOT EXISTS XueShengSuShe
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE XueShengSuShe;

-- 为了可重复执行，按外键顺序删表
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS visits;
DROP TABLE IF EXISTS repairs;
DROP TABLE IF EXISTS room_changes;
DROP TABLE IF EXISTS accommodations;
DROP TABLE IF EXISTS dormitories;
DROP TABLE IF EXISTS buildings;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

-- 用户表（管理员/学生）
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL,
  password VARCHAR(100) NOT NULL,
  name VARCHAR(50) NOT NULL,
  role ENUM('ADMIN','STUDENT') NOT NULL,
  phone VARCHAR(20),
  email VARCHAR(100),
  avatar VARCHAR(255) DEFAULT '👤',
  created_at DATETIME,
  updated_at DATETIME,
  UNIQUE KEY uk_users_username (username),
  KEY idx_users_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 宿舍楼
CREATE TABLE buildings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL,
  location VARCHAR(100) NOT NULL,
  capacity INT NOT NULL,
  created_at DATETIME,
  updated_at DATETIME,
  UNIQUE KEY uk_buildings_name (name),
  CHECK (capacity >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 宿舍
-- 注意：与代码模型对齐，按名称外键；并保证寝室号唯一
CREATE TABLE dormitories (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  number VARCHAR(20) NOT NULL,
  building VARCHAR(50) NOT NULL,
  capacity INT NOT NULL,
  occupied INT NOT NULL DEFAULT 0,
  created_at DATETIME,
  updated_at DATETIME,
  UNIQUE KEY uk_dormitories_number (number),
  KEY idx_dormitories_building (building),
  CONSTRAINT fk_dormitories_building
    FOREIGN KEY (building) REFERENCES buildings(name)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CHECK (capacity >= 0),
  CHECK (occupied >= 0),
  CHECK (occupied <= capacity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 住宿信息
-- 采用寝室号/宿舍楼名外键；寝室+床位唯一，确保不重复分配
CREATE TABLE accommodations (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_name VARCHAR(50) NOT NULL,
  student_id VARCHAR(20),
  dormitory VARCHAR(20) NOT NULL,
  bed VARCHAR(20) NOT NULL,
  building VARCHAR(50),
  check_in_date DATE,
  created_at DATETIME,
  updated_at DATETIME,
  UNIQUE KEY uk_accommodations_dorm_bed (dormitory, bed),
  KEY idx_accommodations_student_name (student_name),
  KEY idx_accommodations_dormitory (dormitory),
  KEY idx_accommodations_building (building),
  CONSTRAINT fk_accommodations_dormitory
    FOREIGN KEY (dormitory) REFERENCES dormitories(number)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_accommodations_building
    FOREIGN KEY (building) REFERENCES buildings(name)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 报修记录
CREATE TABLE repairs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  reporter VARCHAR(50) NOT NULL,
  dormitory VARCHAR(20) NOT NULL,
  description TEXT NOT NULL,
  status ENUM('PENDING','PROCESSING','COMPLETED') NOT NULL,
  repair_time DATETIME,
  images TEXT,
  created_at DATETIME,
  updated_at DATETIME,
  KEY idx_repairs_reporter (reporter),
  KEY idx_repairs_dormitory (dormitory),
  KEY idx_repairs_status (status),
  CONSTRAINT fk_repairs_dormitory
    FOREIGN KEY (dormitory) REFERENCES dormitories(number)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 换寝记录
CREATE TABLE room_changes (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  student_a VARCHAR(50) NOT NULL,
  student_b VARCHAR(50) NOT NULL,
  old_dormitory_a VARCHAR(20) NOT NULL,
  old_bed_a VARCHAR(20) NOT NULL,
  new_dormitory_a VARCHAR(20) NOT NULL,
  new_bed_a VARCHAR(20) NOT NULL,
  old_dormitory_b VARCHAR(20) NOT NULL,
  old_bed_b VARCHAR(20) NOT NULL,
  new_dormitory_b VARCHAR(20) NOT NULL,
  new_bed_b VARCHAR(20) NOT NULL,
  change_time DATETIME,
  created_at DATETIME,
  updated_at DATETIME,
  KEY idx_room_changes_student_a (student_a),
  KEY idx_room_changes_student_b (student_b),
  CONSTRAINT fk_room_changes_old_da FOREIGN KEY (old_dormitory_a) REFERENCES dormitories(number)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_room_changes_new_da FOREIGN KEY (new_dormitory_a) REFERENCES dormitories(number)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_room_changes_old_db FOREIGN KEY (old_dormitory_b) REFERENCES dormitories(number)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_room_changes_new_db FOREIGN KEY (new_dormitory_b) REFERENCES dormitories(number)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 来访登记
CREATE TABLE visits (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  dormitory VARCHAR(20) NOT NULL,
  description TEXT NOT NULL,
  visit_time DATETIME,
  created_at DATETIME,
  updated_at DATETIME,
  KEY idx_visits_dormitory (dormitory),
  CONSTRAINT fk_visits_dormitory
    FOREIGN KEY (dormitory) REFERENCES dormitories(number)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 触发器：维护 dormitories.occupied 并防止超容量/重复床位
DELIMITER $$

-- 插入住宿：检查容量，插入后占用+1
CREATE TRIGGER trg_accommodations_bi
BEFORE INSERT ON accommodations
FOR EACH ROW
BEGIN
  DECLARE cap INT;
  DECLARE occ INT;
  -- 检查床位是否重复由唯一键保障，这里先检查容量
  SELECT capacity, occupied INTO cap, occ FROM dormitories WHERE number = NEW.dormitory FOR UPDATE;
  IF cap IS NULL THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '宿舍不存在';
  END IF;
  IF occ >= cap THEN
    SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '宿舍容量已满，无法分配';
  END IF;
END$$

CREATE TRIGGER trg_accommodations_ai
AFTER INSERT ON accommodations
FOR EACH ROW
BEGIN
  UPDATE dormitories
    SET occupied = occupied + 1, updated_at = NOW()
  WHERE number = NEW.dormitory;
END$$

-- 更新住宿：如寝室变化，旧寝-1，新寝检查容量后+1；如床位变化仍由唯一键保障
CREATE TRIGGER trg_accommodations_bu
BEFORE UPDATE ON accommodations
FOR EACH ROW
BEGIN
  DECLARE cap INT;
  DECLARE occ INT;
  IF NEW.dormitory <> OLD.dormitory THEN
    -- 检查新宿舍容量
    SELECT capacity, occupied INTO cap, occ FROM dormitories WHERE number = NEW.dormitory FOR UPDATE;
    IF cap IS NULL THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '新宿舍不存在';
    END IF;
    IF occ >= cap THEN
      SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '新宿舍容量已满，无法更换';
    END IF;
  END IF;
END$$

CREATE TRIGGER trg_accommodations_au
AFTER UPDATE ON accommodations
FOR EACH ROW
BEGIN
  IF NEW.dormitory <> OLD.dormitory THEN
    UPDATE dormitories SET occupied = occupied - 1, updated_at = NOW()
    WHERE number = OLD.dormitory;

    UPDATE dormitories SET occupied = occupied + 1, updated_at = NOW()
    WHERE number = NEW.dormitory;
  END IF;
END$$

-- 删除住宿：占用-1
CREATE TRIGGER trg_accommodations_ad
AFTER DELETE ON accommodations
FOR EACH ROW
BEGIN
  UPDATE dormitories
    SET occupied = GREATEST(occupied - 1, 0), updated_at = NOW()
  WHERE number = OLD.dormitory;
END$$

DELIMITER ;

-- 初始化数据（顺序严格，避免外键错误）
-- 1) 宿舍楼
INSERT INTO buildings (name, location, capacity, created_at, updated_at) VALUES
('A栋', '南区生活区', 10, NOW(), NOW()),
('B栋', '南区生活区', 10, NOW(), NOW()),
('C栋', '北区生活区', 10, NOW(), NOW()),
('D栋', '东区生活区', 10, NOW(), NOW()),
('E栋', '东区生活区', 10, NOW(), NOW());

-- 2) 宿舍（occupied 先置 0，后续插入住宿记录由触发器累加）
INSERT INTO dormitories (number, building, capacity, occupied, created_at, updated_at) VALUES
('A-101', 'A栋', 4, 0, NOW(), NOW()),
('A-102', 'A栋', 4, 0, NOW(), NOW()),
('A-103', 'A栋', 4, 0, NOW(), NOW()),
('A-104', 'A栋', 4, 0, NOW(), NOW()),
('A-105', 'A栋', 4, 0, NOW(), NOW()),
('A-201', 'A栋', 4, 0, NOW(), NOW()),
('A-202', 'A栋', 4, 0, NOW(), NOW()),
('A-203', 'A栋', 4, 0, NOW(), NOW()),
('A-204', 'A栋', 4, 0, NOW(), NOW()),
('A-206', 'A栋', 4, 0, NOW(), NOW());

-- 3) 用户
INSERT INTO users (username, password, name, role, phone, email, avatar, created_at, updated_at) VALUES
('admin',    '123456', '管理员', 'ADMIN',   '18888888888', 'admin@xm.com',    '👤', NOW(), NOW()),
('zhangsan', '123456', '张三',   'STUDENT', '18800009999', 'zhangsan@xm.com', '👤', NOW(), NOW()),
('lisi',     '123456', '李四',   'STUDENT', '18877776666', 'lisi@xm.com',     '👤', NOW(), NOW()),
('wangwu',   '123456', '王五',   'STUDENT', '18822223333', 'wangwu@xm.com',   '👤', NOW(), NOW()),
('zhaoliu',  '123456', '赵六',   'STUDENT', '18877776666', 'zhaoliu@xm.com',  '👤', NOW(), NOW()),
('sunqi',    '123456', '孙七',   'STUDENT', '18800002222', 'liqi@xm.com',     '👤', NOW(), NOW());

-- 4) 住宿信息（触发器将自动维护 occupied）
INSERT INTO accommodations (student_name, student_id, dormitory, bed, building, check_in_date, created_at, updated_at) VALUES
('张三', '2021001', 'A-101', '1号床', 'A栋', '2024-09-01', NOW(), NOW()),
('李四', '2021002', 'A-101', '2号床', 'A栋', '2024-09-01', NOW(), NOW()),
('王五', '2021003', 'A-101', '3号床', 'A栋', '2024-09-02', NOW(), NOW()),
('赵六', '2021004', 'A-101', '4号床', 'A栋', '2024-09-02', NOW(), NOW()),
('孙七', '2021005', 'A-102', '1号床', 'A栋', '2024-09-03', NOW(), NOW());

-- 5) 换寝记录
INSERT INTO room_changes (student_a, student_b, old_dormitory_a, old_bed_a, new_dormitory_a, new_bed_a, old_dormitory_b, old_bed_b, new_dormitory_b, new_bed_b, change_time, created_at, updated_at) VALUES
('张三', '李四', 'A-101', '1号', 'A-102', '2号', 'A-102', '2号', 'A-101', '1号', '2024-03-25 14:30:00', NOW(), NOW()),
('王五', '赵六', 'A-103', '1号', 'A-104', '1号', 'A-104', '1号', 'A-103', '1号', '2024-03-26 09:15:30', NOW(), NOW()),
('张三', '李四', 'A-101', '2号', 'A-101', '3号', 'A-101', '3号', 'A-101', '3号', '2024-03-28 11:27:13', NOW(), NOW());

-- 6) 报修记录
INSERT INTO repairs (reporter, dormitory, description, status, repair_time, images, created_at, updated_at) VALUES
('张三', 'A-101', '水龙头漏水，需要尽快维修', 'PENDING',    '2024-03-20 10:30:00', '[\"https://images.unsplash.com/photo-1585704032915-c3400ca199e7?w=400\",\"https://images.unsplash.com/photo-1607472586893-edb57bdc0e39?w=400\"]', NOW(), NOW()),
('李四', 'A-102', '灯管损坏，晚上无法照明',   'PROCESSING', '2024-03-21 14:20:00', '[\"https://images.unsplash.com/photo-1513506003901-1e6a229e2d15?w=400\"]', NOW(), NOW()),
('王五', 'A-103', '门锁故障，无法正常开关',   'COMPLETED',  '2024-03-22 09:15:00', '[\"https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400\",\"https://images.unsplash.com/photo-1582139329536-e7284fece509?w=400\"]', NOW(), NOW()),
('赵六', 'A-104', '窗户玻璃破损，存在安全隐患', 'PENDING',    '2024-03-23 16:45:00', '[\"https://images.unsplash.com/photo-1497366216548-37526070297c?w=400\"]', NOW(), NOW()),
('孙七', 'A-105', '空调不制冷，天气太热',     'PROCESSING', '2024-03-24 11:00:00', '[]', NOW(), NOW()),
('张三', 'A-201', '热水器故障，无法加热',     'PENDING',    '2024-03-25 08:30:00', '[\"https://images.unsplash.com/photo-1620626011761-996317b8d101?w=400\"]', NOW(), NOW()),
('李四', 'A-202', '插座损坏，无法充电',       'COMPLETED',  '2024-03-26 13:20:00', '[\"https://images.unsplash.com/photo-1621905251918-48416bd8575a?w=400\",\"https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400\"]', NOW(), NOW()),
('王五', 'A-203', '床板松动，睡觉有异响',     'PENDING',    '2024-03-27 15:10:00', '[]', NOW(), NOW()),
('赵六', 'A-204', '柜门脱落，需要重新安装',   'PROCESSING', '2024-03-28 10:05:00', '[\"https://images.unsplash.com/photo-1595428774223-ef52624120d2?w=400\"]', NOW(), NOW()),
('孙七', 'A-206', '网络故障，无法上网',       'PENDING',    '2024-03-29 14:50:00', '[]', NOW(), NOW());

-- 7) 来访登记
INSERT INTO visits (dormitory, description, visit_time, created_at, updated_at) VALUES
('A-101', '家长来访',   '2024-03-20 14:30:00', NOW(), NOW()),
('A-102', '朋友来访',   '2024-03-21 16:20:00', NOW(), NOW()),
('A-103', '快递员送货', '2024-03-22 10:15:00', NOW(), NOW());
