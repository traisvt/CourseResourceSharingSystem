/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : sharing_system

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 12/12/2024 15:55:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for assignments
-- ----------------------------
DROP TABLE IF EXISTS `assignments`;
CREATE TABLE `assignments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `due_date` datetime(6) NOT NULL,
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `teacher_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK67msc7a52b0l2pttoq5bhm6bk`(`teacher_id` ASC) USING BTREE,
  CONSTRAINT `FK67msc7a52b0l2pttoq5bhm6bk` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of assignments
-- ----------------------------
INSERT INTO `assignments` VALUES (2, NULL, '开发一个简单的命令', '2024-12-13 22:26:22.000000', 'Python项目实践', 2);
INSERT INTO `assignments` VALUES (12, '2024-12-12 13:53:55.000000', '235', '2024-12-20 00:00:00.000000', '235', 13);
INSERT INTO `assignments` VALUES (15, '2024-12-12 14:09:44.000000', '124', '2024-12-14 00:00:00.000000', '5他34', 17);
INSERT INTO `assignments` VALUES (16, '2024-12-12 14:30:42.000000', '124', '2024-12-21 00:00:00.000000', '124', 13);
INSERT INTO `assignments` VALUES (17, '2024-12-12 14:31:02.000000', '124', '2024-12-21 00:00:00.000000', '412', 13);
INSERT INTO `assignments` VALUES (18, '2024-12-12 14:33:09.000000', '1241', '2024-12-21 00:00:00.000000', '412', 13);

-- ----------------------------
-- Table structure for download_records
-- ----------------------------
DROP TABLE IF EXISTS `download_records`;
CREATE TABLE `download_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `downloaded_at` datetime(6) NULL DEFAULT NULL,
  `ip_address` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `resource_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKtcks0lhnooksfn2rwqyecvtp`(`resource_id` ASC) USING BTREE,
  INDEX `FK6yuy4ljp1qghmxu4qxhkdkuio`(`user_id` ASC) USING BTREE,
  CONSTRAINT `FK6yuy4ljp1qghmxu4qxhkdkuio` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKtcks0lhnooksfn2rwqyecvtp` FOREIGN KEY (`resource_id`) REFERENCES `resources` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 103 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of download_records
-- ----------------------------
INSERT INTO `download_records` VALUES (73, '2024-12-01 03:44:09.264597', '0:0:0:0:0:0:0:1', 29, 17);
INSERT INTO `download_records` VALUES (79, '2024-12-12 13:59:19.726206', '0:0:0:0:0:0:0:1', 29, 12);
INSERT INTO `download_records` VALUES (80, '2024-12-12 13:59:27.162668', '0:0:0:0:0:0:0:1', 29, 12);
INSERT INTO `download_records` VALUES (88, '2024-12-12 14:00:24.966264', '0:0:0:0:0:0:0:1', 29, 12);
INSERT INTO `download_records` VALUES (89, '2024-12-12 14:09:30.220525', '0:0:0:0:0:0:0:1', 29, 17);
INSERT INTO `download_records` VALUES (90, '2024-12-12 14:18:29.679867', '0:0:0:0:0:0:0:1', 29, 17);
INSERT INTO `download_records` VALUES (91, '2024-12-12 14:24:32.457477', '0:0:0:0:0:0:0:1', 29, 13);
INSERT INTO `download_records` VALUES (92, '2024-12-12 14:25:28.515192', '0:0:0:0:0:0:0:1', 29, 13);
INSERT INTO `download_records` VALUES (93, '2024-12-12 14:32:04.807383', '0:0:0:0:0:0:0:1', 29, 12);
INSERT INTO `download_records` VALUES (94, '2024-12-12 14:32:22.190062', '0:0:0:0:0:0:0:1', 29, 12);
INSERT INTO `download_records` VALUES (95, '2024-12-12 14:32:22.191064', '0:0:0:0:0:0:0:1', 33, 12);
INSERT INTO `download_records` VALUES (96, '2024-12-12 14:53:41.984921', '0:0:0:0:0:0:0:1', 41, 1);
INSERT INTO `download_records` VALUES (97, '2024-12-12 14:53:44.137365', '0:0:0:0:0:0:0:1', 29, 1);
INSERT INTO `download_records` VALUES (98, '2024-12-12 15:14:38.074719', '0:0:0:0:0:0:0:1', 33, 17);
INSERT INTO `download_records` VALUES (99, '2024-12-12 15:14:38.073720', '0:0:0:0:0:0:0:1', 29, 17);
INSERT INTO `download_records` VALUES (101, '2024-12-12 15:14:38.076717', '0:0:0:0:0:0:0:1', 36, 17);
INSERT INTO `download_records` VALUES (102, '2024-12-12 15:14:38.077724', '0:0:0:0:0:0:0:1', 39, 17);

-- ----------------------------
-- Table structure for resources
-- ----------------------------
DROP TABLE IF EXISTS `resources`;
CREATE TABLE `resources`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `download_count` int NULL DEFAULT NULL,
  `file_path` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `file_size` bigint NULL DEFAULT NULL,
  `file_type` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `title` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `uploader_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FK3spm9n2ecfr0rooymfoq5sy2s`(`uploader_id` ASC) USING BTREE,
  CONSTRAINT `FK3spm9n2ecfr0rooymfoq5sy2s` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 48 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of resources
-- ----------------------------
INSERT INTO `resources` VALUES (29, '2024-12-01 03:35:31.491491', '同学们注意看', 12, 'uploads/c20d8d40-cde4-4748-99f7-7b3b0ec996ec.png', 3165022, 'image/png', '期末大作业', '2024-12-12 15:14:38.071719', 1);
INSERT INTO `resources` VALUES (33, '2024-12-01 04:05:31.711635', '152', 2, 'uploads/3cce4159-b6a8-40ca-8500-51a70a276296.txt', 92, 'text/plain', '2154', '2024-12-12 15:14:38.073720', 17);
INSERT INTO `resources` VALUES (36, '2024-12-01 04:07:00.547328', '阿桑的歌绿卡就是的轮番攻击力量开通目录', 1, 'uploads/bbff53a1-8dd1-4782-8182-01d61640705a.jpg', 142520, 'image/jpeg', '阿桑的歌乔任梁皮套就按实际OK的风景', '2024-12-12 15:14:38.075718', 17);
INSERT INTO `resources` VALUES (39, '2024-12-01 05:46:52.378418', '恢复速度还是的风格', 1, 'uploads/06e5663c-ef28-4c10-8a7d-df3dab3008db.png', 118881, 'image/png', '阿斯顿噶啥的', '2024-12-12 15:14:38.076717', 17);
INSERT INTO `resources` VALUES (41, '2024-12-12 14:18:26.567932', '5235', 1, 'uploads/8a863bae-40c9-45e6-b150-4238a3f68ba4.png', 10151, 'image/png', '23', '2024-12-12 14:53:41.982920', 17);
INSERT INTO `resources` VALUES (42, '2024-12-12 14:26:13.024144', '132', 0, 'uploads/6205c7b7-ea9d-42fd-8e34-fb76768d99a2.png', 10151, 'image/png', '13', '2024-12-12 14:26:13.024144', 17);
INSERT INTO `resources` VALUES (44, '2024-12-12 15:01:58.544204', '364346', 0, 'uploads/9a3fff0a-534a-4617-aa17-832f78af33a7.png', 10151, 'image/png', '376634', '2024-12-12 15:01:58.544204', 1);
INSERT INTO `resources` VALUES (45, '2024-12-12 15:02:05.476522', '3636', 0, 'uploads/b566a79f-00bb-4c37-8c72-af57f655ddda.png', 10151, 'image/png', '3636', '2024-12-12 15:02:05.476522', 1);
INSERT INTO `resources` VALUES (46, '2024-12-12 15:02:24.087917', '四级英语', 0, 'uploads/2701b90b-4a9b-4334-8fd1-068ae67d1232.pdf', 370820, 'application/pdf', '四级阅读', '2024-12-12 15:02:24.087917', 1);
INSERT INTO `resources` VALUES (47, '2024-12-12 15:23:11.413900', '测试', 0, 'uploads/14cf66ae-c130-4538-8309-68be6bf2e90d.png', 10151, 'image/png', '测试', '2024-12-12 15:23:11.413900', 17);

-- ----------------------------
-- Table structure for submissions
-- ----------------------------
DROP TABLE IF EXISTS `submissions`;
CREATE TABLE `submissions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `feedback` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `file_path` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `score` float NULL DEFAULT NULL,
  `submitted_at` datetime(6) NULL DEFAULT NULL,
  `assignment_id` bigint NOT NULL,
  `student_id` bigint NOT NULL,
  `filename` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL DEFAULT 'submission.file',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `FKrirbb44savy2g7nws0hoxs949`(`assignment_id` ASC) USING BTREE,
  INDEX `FK3p6y8mnhpwusdgqrdl4hcl72m`(`student_id` ASC) USING BTREE,
  CONSTRAINT `FK3p6y8mnhpwusdgqrdl4hcl72m` FOREIGN KEY (`student_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `FKrirbb44savy2g7nws0hoxs949` FOREIGN KEY (`assignment_id`) REFERENCES `assignments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of submissions
-- ----------------------------
INSERT INTO `submissions` VALUES (2, '非常不错', '/uploads/submissions/python-project.zip', 89.5, NULL, 2, 3, 'submission.file');
INSERT INTO `submissions` VALUES (6, '4', '29d8d08b-87e5-4c63-8acf-98baaf94417d.png', 100, '2024-12-01 01:41:25.904727', 2, 12, '屏幕截图 2023-11-23 224601.png');
INSERT INTO `submissions` VALUES (8, '好', '8ce4c273-e0fa-4c8f-9b3e-2ef4373f8703.docx', 100, '2024-12-12 14:08:48.839698', 12, 12, '832578294eff5e671439342b59f8b662_e61595b07873636d078abe3ed057ed1d_8.docx');
INSERT INTO `submissions` VALUES (9, '非常好', '4cb539e0-54ec-47a9-8a98-b2c61fe6637c.png', 100, '2024-12-12 14:32:40.072002', 17, 12, '微信图片_20241210182914.png');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `email` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `name` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `password` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NULL DEFAULT NULL,
  `role` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  `updated_at` datetime(6) NULL DEFAULT NULL,
  `username` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_general_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_r43af9ap4edm43mmtq01oddj6`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8mb3 COLLATE = utf8mb3_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, '2024-11-29 22:21:53.889487', 'admin@example.com', '系统管理员', '$2a$10$Jl85c1a1Q97mswbGLoD6DeGGiQfN9Ed6tjUnYd5EwGH1q3Z20Dv1C', NULL, 'ADMIN', '2024-11-29 22:21:53.889487', 'admin');
INSERT INTO `users` VALUES (2, '2024-11-29 22:21:53.992661', 'teacher@example.com', '张老师', '$2a$10$LBjN/ToA3F8sZvHyexfSre1BnjyxpbHJx7yYXgxz7uQx9Tej9OLqe', NULL, 'TEACHER', '2024-11-29 22:21:53.992661', 'teacher');
INSERT INTO `users` VALUES (3, '2024-11-29 22:21:54.072674', 'student@example.com', '李同学', '$2a$10$7ilJ4wZTyK4WUOU7/SKFyOkWZ18nOalxW0GzZp6o84ve4j6a4mhbe', NULL, 'STUDENT', '2024-11-29 22:21:54.072674', 'student');
INSERT INTO `users` VALUES (10, '2024-11-29 22:34:07.674306', 'teacher@163.com', '李老师', '$2a$10$9qPrj7wVoJ/UBYjMraOcRuWpQkzpzShoWCpd/E1cILKR1nDuTlbCe', NULL, 'TEACHER', '2024-11-29 22:34:07.674306', 'teacher2');
INSERT INTO `users` VALUES (11, '2024-11-29 22:37:59.282704', 'student1@163.com', '张三', '$2a$10$INWMLz0/EFp23fsvxFETf.4erjjByJkKZVj6DphDGjQFZFTZ/Q5.u', NULL, 'STUDENT', '2024-11-29 22:37:59.282704', 'student2');
INSERT INTO `users` VALUES (12, '2024-11-29 22:41:15.568423', 'student3@163.com', '张三三', '$2a$10$Q5fHNWTs03Gz3w.ReGvyZ.aV0Xaufnym5MA7VoMONv2NnXn9tdGsK', NULL, 'STUDENT', '2024-11-29 22:41:15.568423', 'student3');
INSERT INTO `users` VALUES (13, '2024-11-30 17:09:59.244968', 'wls@qq.com', '王老师', '$2a$10$/3GBt4obhimQ8rNw9yBy8u2Egng/2tvbKz5gpt4JKq7r2ogDarGXO', NULL, 'TEACHER', '2024-11-30 17:09:59.244968', 'teacher1');
INSERT INTO `users` VALUES (15, '2024-11-30 22:29:35.374092', 'lisi@qq.com', '李四', '$2a$10$sG7Lu2z.7UcLq44XjGdcd.HBJ02cCuaRcF3TUDrhxFMFS7pFgTsrC', NULL, 'STUDENT', '2024-11-30 22:29:35.374092', 'lisi');
INSERT INTO `users` VALUES (16, '2024-11-30 22:30:48.723854', 'wlsasldkf@qq.com', '王老师', '$2a$10$wHFLnpSRR3Iy/5eEz6ti9OcrWHZAZf6gx9QidcZwhikKwuiXjjsda', NULL, 'TEACHER', '2024-11-30 22:30:48.723854', 'teacher3');
INSERT INTO `users` VALUES (17, '2024-12-01 00:14:34.208613', 'dada@qq.com', '大老师', '$2a$10$ox6gN7AEczTon3Mk6Kjt2./bsZgThuP9dIVEni4DA2HgEDisAO3lG', NULL, 'TEACHER', '2024-12-01 00:14:34.208613', 'Dteacher');

SET FOREIGN_KEY_CHECKS = 1;
