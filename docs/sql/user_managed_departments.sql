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

 Date: 24/03/2026 15:59:02
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user_managed_departments
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

-- ----------------------------
-- Records of user_managed_departments
-- ----------------------------
INSERT INTO `user_managed_departments` VALUES (1, 'U001', 'D001', '2026-03-19 22:15:09');
INSERT INTO `user_managed_departments` VALUES (16, 'U012', 'D015', '2026-03-23 15:40:18');
INSERT INTO `user_managed_departments` VALUES (17, 'U013', 'D016', '2026-03-23 15:45:20');

SET FOREIGN_KEY_CHECKS = 1;
