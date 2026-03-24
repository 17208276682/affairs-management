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

 Date: 24/03/2026 15:58:51
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for task_process_records
-- ----------------------------
DROP TABLE IF EXISTS `task_process_records`;
CREATE TABLE `task_process_records`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '流程记录ID，如R001',
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
  CONSTRAINT `chk_records_action` CHECK (`action` in (_utf8mb4'create',_utf8mb4'assign',_utf8mb4'accept',_utf8mb4'process',_utf8mb4'submit',_utf8mb4'approve',_utf8mb4'reject',_utf8mb4'reassign',_utf8mb4'cancel',_utf8mb4'comment'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务流程记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_process_records
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
