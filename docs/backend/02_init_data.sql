-- 事务管理系统 - 初始化数据
-- 执行顺序：先执行 01_tables.sql 建表，再执行本文件
-- 密码使用 BCrypt 加密（由 Spring Security BCryptPasswordEncoder 生成）

SET NAMES utf8mb4;

-- ============================================
-- 1. 部门数据（5个）
-- ============================================
INSERT INTO departments (id, name, parent_id, leader_id, sort_order, level, member_count, status) VALUES
('D001', 'xxx公司', NULL, NULL, 0, 0, 0, 1);

INSERT INTO departments (id, name, parent_id, leader_id, sort_order, level, member_count, status) VALUES
('D002', '总经办', 'D001', NULL, 1, 1, 0, 1);

INSERT INTO departments (id, name, parent_id, leader_id, sort_order, level, member_count, status) VALUES
('D003', '技术研发部', 'D002', NULL, 2, 2, 0, 1),
('D004', '财务部', 'D002', NULL, 3, 2, 0, 1),
('D005', '人力资源部', 'D002', NULL, 4, 2, 0, 1);

-- ============================================
-- 2. 用户数据（6个）
-- 密码说明：
--   zhangsan -> 123456     （技术研发部员工张三）
--   lisi     -> 123456     （技术研发部员工李四）
--   wangcheng -> 123456    （财务部经理王成）
--   zhaoyang  -> 123456    （系统管理员赵阳）
--   wangfang  -> 123456    （人力资源部经理王芳）
--   huangxl   -> 123456    （技术研发部员工黄晓龙）
--
-- BCrypt hash 由 Python bcrypt.hashpw 生成，Spring Security BCryptPasswordEncoder 兼容
-- 123456   -> $2a$10$0Le1umMEuXBlSeifDHXXsuK6TOXKJxHI5Y5jLzNryPlLns4zQV9j2
-- ============================================

INSERT INTO users (id, username, password_hash, name, avatar, phone, email, dept_id, position, role, status) VALUES
('U003', 'zhangsan', '$2a$10$0Le1umMEuXBlSeifDHXXsuK6TOXKJxHI5Y5jLzNryPlLns4zQV9j2', '张三', NULL, '13800000003', 'zhangsan@company.com', 'D003', '高级工程师', 'staff', 1),
('U004', 'lisi', '$2a$10$0Le1umMEuXBlSeifDHXXsuK6TOXKJxHI5Y5jLzNryPlLns4zQV9j2', '李四', NULL, '13800000004', 'lisi@company.com', 'D003', '工程师', 'staff', 1),
('U005', 'wangcheng', '$2a$10$0Le1umMEuXBlSeifDHXXsuK6TOXKJxHI5Y5jLzNryPlLns4zQV9j2', '王成', NULL, '13800000005', 'wangcheng@company.com', 'D004', '财务总监', 'manager', 1),
('U006', 'zhaoyang', '$2a$10$0Le1umMEuXBlSeifDHXXsuK6TOXKJxHI5Y5jLzNryPlLns4zQV9j2', '赵阳', NULL, '13800000006', 'zhaoyang@company.com', 'D002', '系统管理员', 'admin', 1),
('U007', 'wangfang', '$2a$10$0Le1umMEuXBlSeifDHXXsuK6TOXKJxHI5Y5jLzNryPlLns4zQV9j2', '王芳', NULL, '13800000007', 'wangfang@company.com', 'D005', '人事总监', 'manager', 1),
('U009', 'huangxl', '$2a$10$0Le1umMEuXBlSeifDHXXsuK6TOXKJxHI5Y5jLzNryPlLns4zQV9j2', '黄晓龙', NULL, '13800000009', 'huangxl@company.com', 'D003', '初级工程师', 'staff', 1);

-- ============================================
-- 3. 更新部门负责人
-- ============================================
UPDATE departments SET leader_id = 'U006' WHERE id = 'D001';
UPDATE departments SET leader_id = 'U006' WHERE id = 'D002';
UPDATE departments SET leader_id = 'U003' WHERE id = 'D003';
UPDATE departments SET leader_id = 'U005' WHERE id = 'D004';
UPDATE departments SET leader_id = 'U007' WHERE id = 'D005';

-- ============================================
-- 4. 更新部门成员数量
-- ============================================
UPDATE departments SET member_count = 0 WHERE id = 'D001';
UPDATE departments SET member_count = 1 WHERE id = 'D002';  -- 赵阳
UPDATE departments SET member_count = 3 WHERE id = 'D003';  -- 张三、李四、黄晓龙
UPDATE departments SET member_count = 1 WHERE id = 'D004';  -- 王成
UPDATE departments SET member_count = 1 WHERE id = 'D005';  -- 王芳

-- ============================================
-- 5. 用户可管理部门关系
-- ============================================
-- 王成（财务部经理）管理财务部
INSERT INTO user_managed_departments (user_id, dept_id) VALUES
('U005', 'D004');

-- 赵阳（系统管理员）管理所有部门
INSERT INTO user_managed_departments (user_id, dept_id) VALUES
('U006', 'D001'), ('U006', 'D002'), ('U006', 'D003'), ('U006', 'D004'), ('U006', 'D005');

-- 王芳（人力资源部经理）管理人力资源部
INSERT INTO user_managed_departments (user_id, dept_id) VALUES
('U007', 'D005');
