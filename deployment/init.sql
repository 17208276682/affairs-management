-- ============================================================
-- 事务管理系统 - 数据库初始化脚本
-- 按依赖顺序：先建无外键依赖的表，再建有外键的表
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 1. 用户表（先建，departments 外键依赖它）
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '姓名',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `dept_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属部门ID',
  `position` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '岗位',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色：admin/ceo/director/manager/staff',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=离职 1=在职',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_users_username`(`username` ASC) USING BTREE,
  INDEX `idx_users_dept_id`(`dept_id` ASC) USING BTREE,
  INDEX `idx_users_role`(`role` ASC) USING BTREE,
  CONSTRAINT `chk_users_role` CHECK (`role` in ('admin','ceo','director','manager','staff'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

INSERT INTO `users` VALUES ('U001', 'admin', 'Admin123', '管理员', NULL, '17208276600', NULL, NULL, '系统管理员', 'admin', 1, '2026-03-19 22:15:09', '2026-03-22 15:40:39');
INSERT INTO `users` VALUES ('U012', '17208276680', 's8Qdhr&4oSW!', '吴春江', NULL, '17208276680', '', 'D001', '总经办', 'ceo', 1, '2026-03-23 15:31:40', '2026-03-24 09:25:41');
INSERT INTO `users` VALUES ('U013', '17208276681', '!4N&BpvWG!5!', '吴林林', NULL, '17208276681', '', 'D001', '副总经理', 'director', 1, '2026-03-23 15:32:37', '2026-03-23 15:32:37');
INSERT INTO `users` VALUES ('U014', '17208276682', 'YFjb9*Q*V%2T', '秦晓龙', NULL, '17208276682', '', 'D013', '暂无', 'manager', 1, '2026-03-23 15:33:26', '2026-03-23 15:33:26');
INSERT INTO `users` VALUES ('U015', '17208276683', 'Uxd5vi$dLpD7', '张三', NULL, '17208276683', '', 'D013', '暂无', 'staff', 1, '2026-03-23 15:33:59', '2026-03-23 15:33:59');
INSERT INTO `users` VALUES ('U016', '17208276684', 'SFt#G2PMmc9K', '赵五', NULL, '17208276684', '', 'D013', '暂无', 'staff', 1, '2026-03-23 15:34:23', '2026-03-23 15:34:23');
INSERT INTO `users` VALUES ('U017', '17208276685', '$^Ma8bK3u@Bw', '樊伊宁', NULL, '17208276685', '', 'D014', '暂无', 'manager', 1, '2026-03-23 15:35:13', '2026-03-23 15:35:13');
INSERT INTO `users` VALUES ('U018', '17208276686', 'pis^c6*5Kzcm', '哈哈哈', NULL, '17208276686', '', 'D014', '暂无', 'staff', 1, '2026-03-23 15:35:33', '2026-03-23 15:35:33');
INSERT INTO `users` VALUES ('U019', '17208276687', 'g*sHSWxqwd4Y', '嘿嘿嘿', NULL, '17208276687', '', 'D014', '暂无', 'staff', 1, '2026-03-23 15:35:50', '2026-03-23 15:35:50');
INSERT INTO `users` VALUES ('U020', '17208276688', 'Wygr5$M#!6yn', '嘻嘻嘻', NULL, '17208276688', '', 'D015', '暂无', 'staff', 1, '2026-03-23 15:36:33', '2026-03-23 15:36:33');
INSERT INTO `users` VALUES ('U021', '17208276689', 'Z@yC96sJSZ$t', '李四', NULL, '17208276689', '', 'D015', '暂无', 'staff', 1, '2026-03-23 15:36:48', '2026-03-23 15:36:48');
INSERT INTO `users` VALUES ('U022', '17208276690', 'tyxU4bRe&L%f', '李志', NULL, '17208276690', '', 'D017', '暂无', 'manager', 1, '2026-03-23 15:37:48', '2026-03-23 15:37:48');
INSERT INTO `users` VALUES ('U023', '17208276691', 'S644JAwp!6Dg', '王晨', NULL, '17208276691', '', 'D017', '暂无', 'staff', 1, '2026-03-23 15:38:04', '2026-03-23 15:38:04');
INSERT INTO `users` VALUES ('U024', '17208276692', '6EP7v$QAV2j4', '刘佳炜', NULL, '17208276692', '', 'D017', '暂无', 'staff', 1, '2026-03-23 15:38:24', '2026-03-23 15:38:24');
INSERT INTO `users` VALUES ('U025', '17208276693', 'h@8Z&ispEDr6', '易豪宇', NULL, '17208276693', '', 'D018', '暂无', 'manager', 1, '2026-03-23 15:38:42', '2026-03-23 15:38:42');
INSERT INTO `users` VALUES ('U026', '17208276694', 'w@Q3dmD$b&%H', '陈琳', NULL, '17208276694', '', 'D018', '暂无', 'staff', 1, '2026-03-23 15:39:04', '2026-03-23 15:39:04');
INSERT INTO `users` VALUES ('U027', '17208276695', 'h6f^7uosb7Q*', '邓文丹', NULL, '17208276695', '', 'D018', '暂无', 'staff', 1, '2026-03-23 15:39:27', '2026-03-23 15:39:27');

-- ----------------------------
-- 2. 部门表
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门ID',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `parent_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上级部门ID',
  `leader_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门负责人用户ID',
  `sort_order` int NOT NULL DEFAULT 0 COMMENT '排序',
  `level` int NOT NULL DEFAULT 0 COMMENT '层级',
  `member_count` int NOT NULL DEFAULT 0 COMMENT '成员数量',
  `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：0=停用 1=启用',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_departments_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `fk_departments_leader`(`leader_id` ASC) USING BTREE,
  CONSTRAINT `fk_departments_leader` FOREIGN KEY (`leader_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_departments_parent` FOREIGN KEY (`parent_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

INSERT INTO `departments` VALUES ('D001', '总经办', NULL, NULL, 0, 0, 2, 1, '2026-03-19 22:15:09', '2026-03-23 15:32:37');
INSERT INTO `departments` VALUES ('D013', '财务部', 'D001', NULL, 0, 1, 3, 1, '2026-03-23 15:30:21', '2026-03-23 15:34:23');
INSERT INTO `departments` VALUES ('D014', '市场部', 'D001', NULL, 1, 1, 3, 1, '2026-03-23 15:30:33', '2026-03-23 15:35:50');
INSERT INTO `departments` VALUES ('D015', '企划部', 'D001', NULL, 2, 1, 2, 1, '2026-03-23 15:30:44', '2026-03-23 15:36:48');
INSERT INTO `departments` VALUES ('D016', '研发部', 'D001', NULL, 3, 1, 0, 1, '2026-03-23 15:30:56', '2026-03-23 15:30:56');
INSERT INTO `departments` VALUES ('D017', '研发一组', 'D016', NULL, 0, 2, 3, 1, '2026-03-23 15:37:02', '2026-03-23 15:38:24');
INSERT INTO `departments` VALUES ('D018', '研发二组', 'D016', NULL, 1, 2, 3, 1, '2026-03-23 15:37:13', '2026-03-23 15:39:27');

-- 添加 users → departments 的外键（延迟到 departments 建好后）
ALTER TABLE `users` ADD CONSTRAINT `fk_users_dept` FOREIGN KEY (`dept_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE;

-- ----------------------------
-- 3. 用户管理部门关系表
-- ----------------------------
DROP TABLE IF EXISTS `user_managed_departments`;
CREATE TABLE `user_managed_departments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `dept_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_managed_dept`(`user_id` ASC, `dept_id` ASC) USING BTREE,
  INDEX `fk_umd_dept`(`dept_id` ASC) USING BTREE,
  CONSTRAINT `fk_umd_dept` FOREIGN KEY (`dept_id`) REFERENCES `departments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_umd_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户可管理部门关系表' ROW_FORMAT = Dynamic;

INSERT INTO `user_managed_departments` VALUES (1, 'U001', 'D001', '2026-03-19 22:15:09');
INSERT INTO `user_managed_departments` VALUES (16, 'U012', 'D015', '2026-03-23 15:40:18');
INSERT INTO `user_managed_departments` VALUES (17, 'U013', 'D016', '2026-03-23 15:45:20');

-- ----------------------------
-- 4. 事务表
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事务ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事务标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '事务描述',
  `assigner_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '下达人ID',
  `executor_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '执行人ID',
  `importance` tinyint NOT NULL COMMENT '重要性：1-3',
  `urgency` tinyint NOT NULL COMMENT '紧急度：1-3',
  `level` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '级别：A/B/C/D',
  `response_deadline` datetime NOT NULL COMMENT '响应截止时间',
  `completion_deadline` datetime NOT NULL COMMENT '完成截止时间',
  `response_time` datetime NULL DEFAULT NULL COMMENT '实际响应时间',
  `completion_time` datetime NULL DEFAULT NULL COMMENT '实际完成时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '状态',
  `parent_task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父任务ID',
  `assigner_role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下达人当时的角色上下文',
  `assigner_dept_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下达人当时的上下文部门ID',
  `executor_context_dept_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '执行人应在哪个部门上下文下看到此任务',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_tasks_assigner_id`(`assigner_id` ASC) USING BTREE,
  INDEX `idx_tasks_executor_id`(`executor_id` ASC) USING BTREE,
  INDEX `idx_tasks_parent_task_id`(`parent_task_id` ASC) USING BTREE,
  INDEX `idx_tasks_status`(`status` ASC) USING BTREE,
  INDEX `idx_tasks_level`(`level` ASC) USING BTREE,
  INDEX `idx_tasks_completion_deadline`(`completion_deadline` ASC) USING BTREE,
  INDEX `idx_tasks_assigner_role`(`assigner_role` ASC) USING BTREE,
  INDEX `idx_tasks_assigner_dept_id`(`assigner_dept_id` ASC) USING BTREE,
  INDEX `idx_tasks_executor_context_dept_id`(`executor_context_dept_id` ASC) USING BTREE,
  CONSTRAINT `fk_tasks_assigner` FOREIGN KEY (`assigner_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_tasks_executor` FOREIGN KEY (`executor_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_tasks_parent` FOREIGN KEY (`parent_task_id`) REFERENCES `tasks` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_tasks_importance` CHECK (`importance` in (1,2,3)),
  CONSTRAINT `chk_tasks_level` CHECK (`level` in ('A','B','C','D')),
  CONSTRAINT `chk_tasks_status` CHECK (`status` in ('pending','accepted','in_progress','submitted','approved','rejected','completed','overdue','cancelled')),
  CONSTRAINT `chk_tasks_urgency` CHECK (`urgency` in (1,2,3))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '事务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 5. 附件元数据表
-- ----------------------------
DROP TABLE IF EXISTS `attachments`;
CREATE TABLE `attachments`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附件ID',
  `file_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件名',
  `file_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '文件访问地址',
  `file_size` bigint NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `mime_type` varchar(120) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MIME类型',
  `uploaded_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传人ID',
  `uploaded_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_attachments_uploaded_by`(`uploaded_by` ASC) USING BTREE,
  CONSTRAINT `fk_attachments_uploaded_by` FOREIGN KEY (`uploaded_by`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '附件元数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 6. 任务-附件关系表
-- ----------------------------
DROP TABLE IF EXISTS `task_attachments`;
CREATE TABLE `task_attachments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务ID',
  `attachment_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附件ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_task_attachment`(`task_id` ASC, `attachment_id` ASC) USING BTREE,
  INDEX `idx_task_attachments_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_task_attachments_attachment_id`(`attachment_id` ASC) USING BTREE,
  CONSTRAINT `fk_task_attachments_attachment` FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_task_attachments_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 85 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务-附件关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 7. 任务流程记录表
-- ----------------------------
DROP TABLE IF EXISTS `task_process_records`;
CREATE TABLE `task_process_records`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程记录ID',
  `task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '关联任务ID',
  `operator_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作人ID',
  `action` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作内容',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_records_task_id`(`task_id` ASC) USING BTREE,
  INDEX `idx_records_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_records_action`(`action` ASC) USING BTREE,
  INDEX `idx_records_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_records_operator` FOREIGN KEY (`operator_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_records_task` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_records_action` CHECK (`action` in ('create','assign','accept','process','submit','approve','reject','reassign','cancel','comment'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务流程记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 8. 流程记录-附件关系表
-- ----------------------------
DROP TABLE IF EXISTS `record_attachments`;
CREATE TABLE `record_attachments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `record_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程记录ID',
  `attachment_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '附件ID',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_record_attachment`(`record_id` ASC, `attachment_id` ASC) USING BTREE,
  INDEX `idx_record_attachments_record_id`(`record_id` ASC) USING BTREE,
  INDEX `idx_record_attachments_attachment_id`(`attachment_id` ASC) USING BTREE,
  CONSTRAINT `fk_record_attachments_attachment` FOREIGN KEY (`attachment_id`) REFERENCES `attachments` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_record_attachments_record` FOREIGN KEY (`record_id`) REFERENCES `task_process_records` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 42 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '流程记录-附件关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- 9. 通知表
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知ID',
  `user_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接收用户ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知类型',
  `related_task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联任务ID',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读：0否 1是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notifications_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_notifications_related_task_id`(`related_task_id` ASC) USING BTREE,
  INDEX `idx_notifications_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_notifications_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `fk_notifications_task` FOREIGN KEY (`related_task_id`) REFERENCES `tasks` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_notifications_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `chk_notifications_is_read` CHECK (`is_read` in (0,1)),
  CONSTRAINT `chk_notifications_type` CHECK (`type` in ('task_assigned','task_accepted','task_responded','task_completed','task_submitted','task_approved','task_rejected','system'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
