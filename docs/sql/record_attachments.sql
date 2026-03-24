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

 Date: 24/03/2026 15:58:40
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for record_attachments
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
-- Records of record_attachments
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
