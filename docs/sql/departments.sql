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

 Date: 24/03/2026 15:58:24
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for departments
-- ----------------------------
DROP TABLE IF EXISTS `departments`;
CREATE TABLE `departments`  (
  `id` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门ID，如D001',
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

-- ----------------------------
-- Records of departments
-- ----------------------------
INSERT INTO `departments` VALUES ('D001', '总经办', NULL, NULL, 0, 0, 2, 1, '2026-03-19 22:15:09', '2026-03-23 15:32:37');
INSERT INTO `departments` VALUES ('D013', '财务部', 'D001', NULL, 0, 1, 3, 1, '2026-03-23 15:30:21', '2026-03-23 15:34:23');
INSERT INTO `departments` VALUES ('D014', '市场部', 'D001', NULL, 1, 1, 3, 1, '2026-03-23 15:30:33', '2026-03-23 15:35:50');
INSERT INTO `departments` VALUES ('D015', '企划部', 'D001', NULL, 2, 1, 2, 1, '2026-03-23 15:30:44', '2026-03-23 15:36:48');
INSERT INTO `departments` VALUES ('D016', '研发部', 'D001', NULL, 3, 1, 0, 1, '2026-03-23 15:30:56', '2026-03-23 15:30:56');
INSERT INTO `departments` VALUES ('D017', '研发一组', 'D016', NULL, 0, 2, 3, 1, '2026-03-23 15:37:02', '2026-03-23 15:38:24');
INSERT INTO `departments` VALUES ('D018', '研发二组', 'D016', NULL, 1, 2, 3, 1, '2026-03-23 15:37:13', '2026-03-23 15:39:27');

SET FOREIGN_KEY_CHECKS = 1;
