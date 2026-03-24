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

 Date: 24/03/2026 15:59:07
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for users
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
  CONSTRAINT `fk_users_dept` FOREIGN KEY (`dept_id`) REFERENCES `departments` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `chk_users_role` CHECK (`role` in (_utf8mb4'admin',_utf8mb4'ceo',_utf8mb4'director',_utf8mb4'manager',_utf8mb4'staff'))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
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

SET FOREIGN_KEY_CHECKS = 1;
