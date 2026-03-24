/*
 Navicat Premium Data Transfer

 Source Server         : 888888888
 Source Server Type    : MySQL
 Source Server Version : 80034
 Source Host           : localhost:3306
 Source Schema         : affairs_management

 Target Server Type    : MySQL
 Target Server Version : 80034
 File Encoding         : 65001

 Date: 24/03/2026 15:58:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for tasks
-- ----------------------------
DROP TABLE IF EXISTS `tasks`;
CREATE TABLE `tasks`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '事务ID，如T20260201001',
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
  `parent_task_id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父任务ID（向下分派）',
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
  CONSTRAINT `chk_tasks_level` CHECK (`level` in (_utf8mb4'A',_utf8mb4'B',_utf8mb4'C',_utf8mb4'D')),
  CONSTRAINT `chk_tasks_status` CHECK (`status` in (_utf8mb4'pending',_utf8mb4'accepted',_utf8mb4'in_progress',_utf8mb4'submitted',_utf8mb4'approved',_utf8mb4'rejected',_utf8mb4'completed',_utf8mb4'overdue',_utf8mb4'cancelled')),
  CONSTRAINT `chk_tasks_urgency` CHECK (`urgency` in (1,2,3))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '事务表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tasks
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
