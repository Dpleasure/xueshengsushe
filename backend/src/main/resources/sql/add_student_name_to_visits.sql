-- 为 visits 表添加 student_name 字段
-- 执行日期: 2025-01-10

-- 添加 student_name 列
ALTER TABLE visits ADD COLUMN IF NOT EXISTS student_name VARCHAR(50);

-- 从 accommodations 表中更新现有记录的 student_name
UPDATE visits v
SET student_name = (
    SELECT a.student_name 
    FROM accommodations a 
    WHERE a.student_id = v.student_id 
    LIMIT 1
)
WHERE v.student_id IS NOT NULL AND v.student_name IS NULL;

-- 添加注释
COMMENT ON COLUMN visits.student_name IS '申请学生姓名';

