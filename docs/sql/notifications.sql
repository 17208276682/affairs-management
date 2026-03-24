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

 Date: 24/03/2026 15:58:30
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知ID，如N001',
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
  CONSTRAINT `chk_notifications_type` CHECK (`type` in (_utf8mb4'task_assigned',_utf8mb4'task_accepted',_utf8mb4'task_responded',_utf8mb4'task_completed',_utf8mb4'task_submitted',_utf8mb4'task_approved',_utf8mb4'task_rejected',_utf8mb4'system'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
