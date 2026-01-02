/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.75.129
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : oldcare_system

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 03/01/2026 01:04:44
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for activity_registrations
-- ----------------------------
DROP TABLE IF EXISTS `activity_registrations`;
CREATE TABLE `activity_registrations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activity_id` bigint NOT NULL COMMENT '活动ID',
  `resident_id` bigint NOT NULL COMMENT '居民ID',
  `registration_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'registered' COMMENT '报名状态：registered(已报名)、attended(已参加)、cancelled(已取消)',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_registration`(`activity_id` ASC, `resident_id` ASC) USING BTREE,
  INDEX `idx_activity_id`(`activity_id` ASC) USING BTREE,
  INDEX `idx_resident_id`(`resident_id` ASC) USING BTREE,
  CONSTRAINT `activity_registrations_ibfk_1` FOREIGN KEY (`activity_id`) REFERENCES `community_activities` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `activity_registrations_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '活动报名表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of activity_registrations
-- ----------------------------
INSERT INTO `activity_registrations` VALUES (2, 9, 130, 'registered', '2025-12-29 10:36:40', '2025-12-29 10:36:40');
INSERT INTO `activity_registrations` VALUES (3, 9, 131, 'registered', '2025-12-29 10:36:42', '2025-12-29 10:36:42');
INSERT INTO `activity_registrations` VALUES (4, 10, 131, 'registered', '2025-12-29 10:47:39', '2025-12-29 10:47:39');
INSERT INTO `activity_registrations` VALUES (5, 12, 135, 'registered', '2026-01-02 22:23:02', '2026-01-02 22:23:02');
INSERT INTO `activity_registrations` VALUES (6, 12, 136, 'registered', '2026-01-02 22:23:08', '2026-01-02 22:23:08');
INSERT INTO `activity_registrations` VALUES (7, 11, 135, 'registered', '2026-01-03 00:28:25', '2026-01-03 00:28:25');

-- ----------------------------
-- Table structure for appointments
-- ----------------------------
DROP TABLE IF EXISTS `appointments`;
CREATE TABLE `appointments`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resident_id` bigint NOT NULL COMMENT '居民ID',
  `provider_id` bigint NULL DEFAULT NULL COMMENT '服务提供商ID',
  `service_id` bigint NULL DEFAULT NULL COMMENT '服务ID',
  `need_id` bigint NULL DEFAULT NULL COMMENT '需求ID',
  `appointment_date` datetime NOT NULL COMMENT '预约日期时间',
  `appointment_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务地址',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预约状态：pending(待确认)、confirmed(已确认)、in_progress(进行中)、completed(已完成)、cancelled(已取消)',
  `total_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '总费用',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `service_id`(`service_id` ASC) USING BTREE,
  INDEX `need_id`(`need_id` ASC) USING BTREE,
  INDEX `idx_resident_id`(`resident_id` ASC) USING BTREE,
  INDEX `idx_provider_id`(`provider_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `appointments_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointments_ibfk_2` FOREIGN KEY (`provider_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointments_ibfk_3` FOREIGN KEY (`service_id`) REFERENCES `services` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `appointments_ibfk_4` FOREIGN KEY (`need_id`) REFERENCES `service_needs` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of appointments
-- ----------------------------
INSERT INTO `appointments` VALUES (31, 130, 131, 9, NULL, '2026-01-07 03:03:00', NULL, '已完成', 50.00, '', '2025-12-29 10:05:18', '2025-12-29 19:36:31');
INSERT INTO `appointments` VALUES (32, 130, 131, 3, NULL, '2025-12-31 00:00:00', NULL, '已完成', 150.00, '', '2025-12-29 10:54:50', '2025-12-29 19:36:25');
INSERT INTO `appointments` VALUES (34, 130, 131, 5, NULL, '2026-01-09 00:00:00', NULL, '已完成', 80.00, '', '2025-12-29 19:36:15', '2025-12-29 19:36:38');
INSERT INTO `appointments` VALUES (35, 133, 134, 8, NULL, '2026-01-01 00:00:00', NULL, '已完成', 50.00, '', '2025-12-31 08:46:55', '2025-12-31 08:47:01');
INSERT INTO `appointments` VALUES (36, 135, 136, 11, NULL, '2026-01-03 09:05:08', NULL, '已完成', 150.00, '', '2026-01-02 22:20:35', '2026-01-02 22:20:40');
INSERT INTO `appointments` VALUES (39, 135, 136, 10, NULL, '2026-01-09 00:00:00', NULL, '已完成', 50.00, '', '2026-01-03 00:20:34', '2026-01-03 00:20:53');

-- ----------------------------
-- Table structure for caregivers
-- ----------------------------
DROP TABLE IF EXISTS `caregivers`;
CREATE TABLE `caregivers`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '护理员姓名',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话号码',
  `company_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属机构或公司名称',
  `certification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '护理证书',
  `experience_years` int NULL DEFAULT NULL COMMENT '工作经验年限',
  `rating` decimal(3, 2) NULL DEFAULT NULL COMMENT '评分（5分制）',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'active' COMMENT '状态：active-在职、inactive-离职',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_card`(`id_card` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '护理人员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of caregivers
-- ----------------------------
INSERT INTO `caregivers` VALUES (5, 124, '员工李四', 30, '女', '411322200500001234', '13271385770', NULL, '康复训练师傅', 5, 4.50, '2025-12-27 15:55:11', '2025-12-27 15:55:11', 'active');
INSERT INTO `caregivers` VALUES (6, 126, '员工王五', 30, '女', '123456789012345', '12345612345', NULL, '清洁师', 5, 5.00, '2025-12-27 20:29:44', '2025-12-27 23:14:36', 'active');
INSERT INTO `caregivers` VALUES (7, 127, '员工赵六', 30, '女', '123456789', '19561998178', NULL, '退休安全员', 5, 5.00, '2025-12-27 23:12:40', '2025-12-27 23:12:40', 'active');
INSERT INTO `caregivers` VALUES (8, 131, '员工徐七', 30, '男', '411322200659584546', '19372475603', NULL, '营养师', 5, 4.50, '2025-12-29 10:03:20', '2025-12-29 10:03:20', 'active');
INSERT INTO `caregivers` VALUES (9, 134, '员工钱八', 30, '男', '123456789745896123', '12345678902', '', '电气工程师', 5, 4.50, '2025-12-31 08:44:51', '2025-12-31 08:46:24', '1');
INSERT INTO `caregivers` VALUES (10, 136, '员工陈八', 30, '女', '', '12345678991', '', '美容师', 5, 4.50, '2026-01-02 22:12:25', '2026-01-02 22:16:02', '1');

-- ----------------------------
-- Table structure for community_activities
-- ----------------------------
DROP TABLE IF EXISTS `community_activities`;
CREATE TABLE `community_activities`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL COMMENT '发布管理员ID',
  `activity_title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '活动标题',
  `activity_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '活动描述',
  `activity_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动类型：education(老年大学)、interest(兴趣小组)、festival(节日活动)、other(其他)',
  `activity_date` datetime NOT NULL COMMENT '活动日期时间',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '活动地点',
  `max_participants` int NULL DEFAULT NULL COMMENT '最多参与人数',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'published' COMMENT '状态：draft(草稿)、published(已发布)、closed(已关闭)',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_activity_date`(`activity_date` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `community_activities_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '社区活动表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of community_activities
-- ----------------------------
INSERT INTO `community_activities` VALUES (9, 1, '广场舞大赛', '一起进行一场广场舞大赛吧', 'entertainment', '2026-01-09 16:00:00', '社区广场', 50, 'published', '2025-12-27 23:20:43', '2025-12-27 23:20:43');
INSERT INTO `community_activities` VALUES (10, 1, '象棋大赛', '象棋博弈大赛', 'entertainment', '2026-01-07 16:00:00', '社区公园亭子 ', 10, 'published', '2025-12-29 10:36:31', '2025-12-29 10:36:31');
INSERT INTO `community_activities` VALUES (11, 1, '羽毛球赛', '酣畅淋漓的羽毛球大赛', 'entertainment', '2026-01-07 16:00:00', '社区羽毛球场', 50, 'published', '2025-12-31 08:51:21', '2026-01-02 23:29:36');
INSERT INTO `community_activities` VALUES (12, 1, '元旦庆祝晚会', '共庆元旦，新年新气象', 'entertainment', '2026-01-09 16:00:00', '社区会议厅', 50, 'published', '2026-01-02 22:22:06', '2026-01-02 23:31:41');

-- ----------------------------
-- Table structure for community_news
-- ----------------------------
DROP TABLE IF EXISTS `community_news`;
CREATE TABLE `community_news`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `admin_id` bigint NOT NULL COMMENT '发布管理员ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资讯标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '资讯内容',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '类别：policy(政策)、health(健康知识)、news(社区新闻)',
  `cover_image` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'published' COMMENT '状态：draft(草稿)、published(已发布)、archived(已归档)',
  `view_count` int NULL DEFAULT 0 COMMENT '浏览次数',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `admin_id`(`admin_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `community_news_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '社区资讯表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of community_news
-- ----------------------------

-- ----------------------------
-- Table structure for elderly_info
-- ----------------------------
DROP TABLE IF EXISTS `elderly_info`;
CREATE TABLE `elderly_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '老人姓名',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `gender` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '性别',
  `id_card` varchar(18) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '身份证号',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话号码',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '住址',
  `emergency_contact` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系人',
  `emergency_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系电话',
  `health_status` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '健康状况',
  `medical_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '病史',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_card`(`id_card` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `elderly_info_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '老人信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of elderly_info
-- ----------------------------
INSERT INTO `elderly_info` VALUES (4, 125, '刘姥姥', 70, '女', '12345678974125896', '14563278974', '河南省郑州市', '无', '13800138002', '良好', '无', '2025-12-27 20:28:49', '2025-12-27 20:28:49');
INSERT INTO `elderly_info` VALUES (5, 128, '牛爷爷', 70, '男', '123456789145236', '12345678905', '河南省新乡市', '', '13800138002', '患病', '左腿骨折', '2025-12-27 23:16:06', '2025-12-27 23:16:06');
INSERT INTO `elderly_info` VALUES (6, 129, '徐太太', 85, '女', '411322000000000000', '18203862557', '河南省郑州市', '', '', '良好', '无', '2025-12-29 08:55:54', '2025-12-29 08:55:54');
INSERT INTO `elderly_info` VALUES (7, 130, '吴太太', 70, '女', '411322232345641234', '13271385771', '北京市海淀区', '', '', '良好', '无', '2025-12-29 10:00:38', '2025-12-29 11:36:49');
INSERT INTO `elderly_info` VALUES (13, 132, '王老三', 74, '男', '411322200605041234', '12345678963', '北京市海淀区', '', '', '良好', '高血压', '2025-12-29 20:27:39', '2025-12-29 20:27:39');
INSERT INTO `elderly_info` VALUES (14, 133, '张姥姥', 70, '女', '123456789745896132', '12345678901', '北京市海淀区', '无', '无', '良好', '脑血栓', '2025-12-31 08:43:28', '2025-12-31 08:47:58');
INSERT INTO `elderly_info` VALUES (15, 135, '赵太太', 72, '女', '', '12345678985', '北京市海淀区', '', '', '良好', '无', '2026-01-02 22:11:22', '2026-01-02 22:15:40');
INSERT INTO `elderly_info` VALUES (18, 140, '陈太太', 70, '女', '411322222222000222', '12378945674', '北京市朝阳区', '', '', '良好', '无', '2026-01-02 23:44:29', '2026-01-02 23:48:51');

-- ----------------------------
-- Table structure for emergency_contacts
-- ----------------------------
DROP TABLE IF EXISTS `emergency_contacts`;
CREATE TABLE `emergency_contacts`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人名称',
  `contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '联系人电话',
  `relation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关系：子女、朋友、邻居等',
  `priority` int NULL DEFAULT 1 COMMENT '优先级：1最高',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_priority`(`priority` ASC) USING BTREE,
  CONSTRAINT `emergency_contacts_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '紧急联系人表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of emergency_contacts
-- ----------------------------
INSERT INTO `emergency_contacts` VALUES (1, 2, '王子女', '13800138002', '子女', 1, '2025-12-26 15:31:32', '2025-12-26 15:31:32');
INSERT INTO `emergency_contacts` VALUES (2, 2, '邻居李', '13800138003', '邻居', 2, '2025-12-26 15:31:32', '2025-12-26 15:31:32');

-- ----------------------------
-- Table structure for emergency_helps
-- ----------------------------
DROP TABLE IF EXISTS `emergency_helps`;
CREATE TABLE `emergency_helps`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resident_id` bigint NOT NULL COMMENT '居民ID',
  `help_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '求助类型：accident(意外)、medical(医疗)、other(其他)',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '求助描述',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '当前位置（GPS坐标或地址）',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '状态：pending(待处理)、in_progress(处理中)、resolved(已解决)',
  `assigned_admin_id` bigint NULL DEFAULT NULL COMMENT '负责管理员ID',
  `response_time` datetime NULL DEFAULT NULL COMMENT '响应时间',
  `response_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '响应备注',
  `resolved_time` datetime NULL DEFAULT NULL COMMENT '解决时间',
  `notified_contacts` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '已通知的联系人',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `assigned_admin_id`(`assigned_admin_id` ASC) USING BTREE,
  INDEX `idx_resident_id`(`resident_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `emergency_helps_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `emergency_helps_ibfk_2` FOREIGN KEY (`assigned_admin_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '紧急求助表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of emergency_helps
-- ----------------------------
INSERT INTO `emergency_helps` VALUES (12, 128, 'other', '牛爷爷测试求助', '公园', 'responding', 126, '2025-12-29 09:22:19', NULL, NULL, '', '2025-12-29 08:52:40', '2025-12-29 09:22:19');
INSERT INTO `emergency_helps` VALUES (13, 129, 'fall', '测试徐太太的求助', '家里卫生间', 'responding', 126, '2025-12-29 09:22:19', NULL, NULL, '', '2025-12-29 09:22:12', '2025-12-29 09:22:19');
INSERT INTO `emergency_helps` VALUES (14, 129, 'fall', '徐太太测试响应时间', '家里厨房', 'responding', 126, '2025-12-29 09:30:58', NULL, NULL, '', '2025-12-29 09:30:45', '2025-12-29 09:30:58');
INSERT INTO `emergency_helps` VALUES (15, 129, 'medical', '测试响应时间', '不知道', 'responding', 126, '2025-12-29 09:32:51', NULL, NULL, '', '2025-12-29 09:32:44', '2025-12-29 09:32:51');
INSERT INTO `emergency_helps` VALUES (16, 129, 'other', '徐太太响应时间测试', '学校', 'responding', 126, '2025-12-29 09:36:55', NULL, NULL, '', '2025-12-29 09:36:51', '2025-12-29 09:36:55');
INSERT INTO `emergency_helps` VALUES (17, 129, 'fall', '徐太太优化操作按钮', '按钮', 'responding', 126, '2025-12-29 09:56:54', NULL, NULL, '', '2025-12-29 09:49:06', '2025-12-29 09:56:54');
INSERT INTO `emergency_helps` VALUES (18, 129, 'fall', '徐太太测试全部响应按钮', '不知道', 'responding', 126, '2025-12-29 09:58:11', NULL, NULL, '', '2025-12-29 09:57:37', '2025-12-29 09:58:11');
INSERT INTO `emergency_helps` VALUES (19, 133, 'fall', '跌倒了', '广场', 'responding', 134, '2025-12-31 08:52:36', NULL, NULL, '', '2025-12-31 08:52:31', '2025-12-31 08:52:36');
INSERT INTO `emergency_helps` VALUES (21, 135, 'medical', '心口疼', '操场', 'responding', 136, '2026-01-03 00:23:20', NULL, NULL, '', '2026-01-03 00:23:15', '2026-01-03 00:23:20');

-- ----------------------------
-- Table structure for evaluations
-- ----------------------------
DROP TABLE IF EXISTS `evaluations`;
CREATE TABLE `evaluations`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint NOT NULL COMMENT '预约ID',
  `resident_id` bigint NOT NULL COMMENT '居民ID',
  `provider_id` bigint NOT NULL COMMENT '服务提供商ID',
  `service_quality_rating` decimal(3, 1) NULL DEFAULT NULL COMMENT '服务质量评分（1-5分）',
  `attitude_rating` decimal(3, 1) NULL DEFAULT NULL COMMENT '服务态度评分（1-5分）',
  `effect_rating` decimal(3, 1) NULL DEFAULT NULL COMMENT '服务效果评分（1-5分）',
  `overall_rating` decimal(3, 1) NULL DEFAULT NULL COMMENT '总体评分（1-5分）',
  `comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '评价内容',
  `is_complaint` int NULL DEFAULT 0 COMMENT '是否投诉：0-否、1-是',
  `complaint_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '投诉原因',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '状态：evaluated(已评价)、replied(已回复)、resolved(已解决)',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `resident_id`(`resident_id` ASC) USING BTREE,
  INDEX `idx_appointment_id`(`appointment_id` ASC) USING BTREE,
  INDEX `idx_provider_id`(`provider_id` ASC) USING BTREE,
  INDEX `idx_overall_rating`(`overall_rating` ASC) USING BTREE,
  CONSTRAINT `evaluations_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `evaluations_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `evaluations_ibfk_3` FOREIGN KEY (`provider_id`) REFERENCES `caregivers` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务评价表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluations
-- ----------------------------
INSERT INTO `evaluations` VALUES (7, 32, 130, 8, 3.0, 3.0, 3.0, 3.0, '好', 0, NULL, '已评价', '2025-12-30 11:27:18', '2025-12-30 11:27:18');
INSERT INTO `evaluations` VALUES (8, 35, 133, 9, 3.0, 3.0, 3.0, 3.0, '好', 0, NULL, '已评价', '2025-12-31 08:51:49', '2025-12-31 08:51:49');
INSERT INTO `evaluations` VALUES (11, 39, 135, 10, 3.0, 3.0, 3.0, 3.0, 'ok', 0, NULL, '已评价', '2026-01-03 00:21:23', '2026-01-03 00:21:23');

-- ----------------------------
-- Table structure for fee_records
-- ----------------------------
DROP TABLE IF EXISTS `fee_records`;
CREATE TABLE `fee_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint NULL DEFAULT NULL COMMENT '预约ID',
  `resident_id` bigint NOT NULL COMMENT '居民ID',
  `service_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务名称',
  `amount` decimal(10, 2) NOT NULL COMMENT '费用金额',
  `fee_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '费用类型：service(服务费)、materials(材料费)、other(其他)',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT '未支付' COMMENT '支付状态：未支付、已支付、已退款',
  `due_date` date NULL DEFAULT NULL COMMENT '应付日期',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resident_id`(`resident_id` ASC) USING BTREE,
  INDEX `idx_appointment_id`(`appointment_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_due_date`(`due_date` ASC) USING BTREE,
  CONSTRAINT `fee_records_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fee_records_ibfk_2` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '费用记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of fee_records
-- ----------------------------
INSERT INTO `fee_records` VALUES (16, 30, 129, '修理电器', 50.00, NULL, '已支付', NULL, '2025-12-29 09:00:33', NULL, NULL, '2025-12-29 09:00:22', '2025-12-29 09:00:33');
INSERT INTO `fee_records` VALUES (17, 31, 130, '康复训练', 50.00, NULL, '已支付', NULL, '2025-12-29 10:08:45', NULL, NULL, '2025-12-29 10:05:26', '2025-12-29 10:08:45');
INSERT INTO `fee_records` VALUES (18, 32, 130, '健康体检', 150.00, NULL, '已支付', NULL, '2025-12-29 10:55:14', NULL, NULL, '2025-12-29 10:55:04', '2025-12-29 10:55:14');
INSERT INTO `fee_records` VALUES (19, 34, 130, '陪同就医', 80.00, NULL, '已支付', NULL, '2025-12-29 19:36:47', NULL, NULL, '2025-12-29 19:36:38', '2025-12-29 19:36:47');
INSERT INTO `fee_records` VALUES (20, 35, 133, '修理电器', 50.00, NULL, '已支付', NULL, '2025-12-31 08:47:07', NULL, NULL, '2025-12-31 08:47:01', '2025-12-31 08:47:07');
INSERT INTO `fee_records` VALUES (21, 36, 135, '知识讲解', 150.00, NULL, '已支付', NULL, '2026-01-02 22:20:51', NULL, NULL, '2026-01-02 22:20:40', '2026-01-02 22:20:51');
INSERT INTO `fee_records` VALUES (24, 39, 135, '种菜帮助', 50.00, NULL, '已支付', NULL, '2026-01-03 00:21:02', NULL, NULL, '2026-01-03 00:20:53', '2026-01-03 00:21:02');

-- ----------------------------
-- Table structure for feedbacks
-- ----------------------------
DROP TABLE IF EXISTS `feedbacks`;
CREATE TABLE `feedbacks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '提交人 users.id',
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提交人角色：admin/resident/caregiver',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈标题',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'general' COMMENT '类别: suggestion/issue/complaint/general',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '反馈内容',
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'pending' COMMENT 'pending/processing/resolved',
  `handler_id` bigint NULL DEFAULT NULL COMMENT '处理人 users.id（管理员）',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '处理备注',
  `created_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE,
  INDEX `fk_feedback_handler`(`handler_id` ASC) USING BTREE,
  CONSTRAINT `fk_feedback_handler` FOREIGN KEY (`handler_id`) REFERENCES `users` (`id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_feedback_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '反馈与建议' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of feedbacks
-- ----------------------------
INSERT INTO `feedbacks` VALUES (1, 135, 'resident', '加一个知识学习部分', 'suggestion', '加一个可以读书部分', 'pending', NULL, NULL, '2026-01-02 22:13:59', '2026-01-02 22:13:59');
INSERT INTO `feedbacks` VALUES (2, 136, 'caregiver', '工资有点低了', 'issue', '涨工资', 'resolved', 1, '', '2026-01-02 22:14:50', '2026-01-02 22:15:03');
INSERT INTO `feedbacks` VALUES (3, 135, 'resident', '花坛修理一下', 'general', '花坛的草长的很乱了，休整一下', 'pending', NULL, NULL, '2026-01-02 23:25:31', '2026-01-02 23:25:31');

-- ----------------------------
-- Table structure for health_checks
-- ----------------------------
DROP TABLE IF EXISTS `health_checks`;
CREATE TABLE `health_checks`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resident_id` bigint NOT NULL COMMENT '居民ID',
  `check_date` datetime NOT NULL COMMENT '检查日期',
  `check_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '检查类型：blood_pressure(血压)、blood_sugar(血糖)、weight(体重)、other(其他)',
  `check_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '检查结果',
  `normal_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '是否正常：normal(正常)、abnormal(异常)',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resident_id`(`resident_id` ASC) USING BTREE,
  INDEX `idx_check_date`(`check_date` ASC) USING BTREE,
  CONSTRAINT `health_checks_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '健康检查记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_checks
-- ----------------------------
INSERT INTO `health_checks` VALUES (3, 1, '2025-12-27 01:30:06', '血压', '120', 'normal', '注意休息', '2025-12-27 01:30:06', '2025-12-27 03:16:57');

-- ----------------------------
-- Table structure for health_records
-- ----------------------------
DROP TABLE IF EXISTS `health_records`;
CREATE TABLE `health_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `record_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '记录类型：checkup(体检)、diagnosis(诊断)、medication(用药)、other(其他)',
  `record_date` date NULL DEFAULT NULL COMMENT '记录日期',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '记录标题',
  `details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '记录详情',
  `hospital` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '医院/诊所',
  `doctor_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '医生姓名',
  `attachment_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '附件URL',
  `notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '其他备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `health_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '健康档案表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_records
-- ----------------------------
INSERT INTO `health_records` VALUES (9, 130, 'medication', '2025-12-28', '吴太太用药记录', '吃了一片药', '', '', NULL, '', '2025-12-29 10:25:29', '2025-12-29 10:25:29');
INSERT INTO `health_records` VALUES (10, 125, 'checkup', '2025-12-31', '刘姥姥检查记录', '身体健康，轻微骨质疏松', '', '', NULL, '无', '2025-12-31 08:50:27', '2025-12-31 09:07:09');
INSERT INTO `health_records` VALUES (11, 133, 'treatment', '2026-01-01', '张姥姥', '普通感冒', '', '', NULL, '', '2025-12-31 09:07:53', '2025-12-31 09:07:53');
INSERT INTO `health_records` VALUES (12, 135, 'medication', '2026-01-29', '赵太太用药记录', '吃了一个阿司匹林', '', '', NULL, '', '2026-01-02 22:20:06', '2026-01-02 22:20:06');

-- ----------------------------
-- Table structure for health_reminders
-- ----------------------------
DROP TABLE IF EXISTS `health_reminders`;
CREATE TABLE `health_reminders`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `reminder_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提醒类型：medication(用药)、checkup(体检)、custom(自定义)',
  `reminder_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提醒内容',
  `reminder_date` date NOT NULL COMMENT '提醒日期',
  `reminder_time` time NULL DEFAULT NULL COMMENT '提醒时间',
  `is_completed` int NULL DEFAULT 0 COMMENT '是否完成：0-未完成、1-已完成',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_reminder_date`(`reminder_date` ASC) USING BTREE,
  CONSTRAINT `health_reminders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '健康提醒表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of health_reminders
-- ----------------------------

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `receiver_id` bigint NOT NULL COMMENT '接收者ID（护工或管理员）',
  `sender_id` bigint NULL DEFAULT NULL COMMENT '发送者ID（居民或系统）',
  `notification_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知类型：emergency_help(紧急求助)、appointment(预约)、evaluation(评价)',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '通知内容',
  `related_id` bigint NULL DEFAULT NULL COMMENT '相关的业务ID（如紧急求助ID）',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读',
  `created_at` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `read_at` datetime NULL DEFAULT NULL COMMENT '读取时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_receiver_id`(`receiver_id` ASC) USING BTREE,
  INDEX `idx_is_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `notifications_ibfk_2` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 82 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消息通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notifications
-- ----------------------------
INSERT INTO `notifications` VALUES (29, 8, 7, 'emergency_help', '紧急求助通知', '居民【居民张三】发起紧急求助：测试', 10, 1, '2025-12-27 19:07:31', '2025-12-27 19:31:12');
INSERT INTO `notifications` VALUES (30, 124, 7, 'emergency_help', '紧急求助通知', '居民【居民张三】发起紧急求助：测试', 10, 1, '2025-12-27 19:07:31', '2025-12-27 19:07:45');
INSERT INTO `notifications` VALUES (31, 8, 7, 'emergency_help', '紧急求助通知', '居民【居民张三】发起紧急求助：测试', 11, 1, '2025-12-27 19:11:17', '2025-12-27 19:31:12');
INSERT INTO `notifications` VALUES (32, 124, 7, 'emergency_help', '紧急求助通知', '居民【居民张三】发起紧急求助：测试', 11, 1, '2025-12-27 19:11:17', '2025-12-27 19:11:22');
INSERT INTO `notifications` VALUES (33, 8, 128, 'emergency_help', '紧急求助通知', '居民【牛爷爷】发起紧急求助：牛爷爷测试求助', 12, 0, '2025-12-29 08:52:40', NULL);
INSERT INTO `notifications` VALUES (34, 124, 128, 'emergency_help', '紧急求助通知', '居民【牛爷爷】发起紧急求助：牛爷爷测试求助', 12, 0, '2025-12-29 08:52:40', NULL);
INSERT INTO `notifications` VALUES (35, 126, 128, 'emergency_help', '紧急求助通知', '居民【牛爷爷】发起紧急求助：牛爷爷测试求助', 12, 1, '2025-12-29 08:52:40', '2025-12-29 09:22:19');
INSERT INTO `notifications` VALUES (36, 127, 128, 'emergency_help', '紧急求助通知', '居民【牛爷爷】发起紧急求助：牛爷爷测试求助', 12, 0, '2025-12-29 08:52:40', NULL);
INSERT INTO `notifications` VALUES (37, 8, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：测试徐太太的求助', 13, 0, '2025-12-29 09:22:12', NULL);
INSERT INTO `notifications` VALUES (38, 124, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：测试徐太太的求助', 13, 0, '2025-12-29 09:22:12', NULL);
INSERT INTO `notifications` VALUES (39, 126, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：测试徐太太的求助', 13, 1, '2025-12-29 09:22:12', '2025-12-29 09:22:19');
INSERT INTO `notifications` VALUES (40, 127, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：测试徐太太的求助', 13, 0, '2025-12-29 09:22:12', NULL);
INSERT INTO `notifications` VALUES (41, 8, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太测试响应时间', 14, 0, '2025-12-29 09:30:45', NULL);
INSERT INTO `notifications` VALUES (42, 124, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太测试响应时间', 14, 0, '2025-12-29 09:30:45', NULL);
INSERT INTO `notifications` VALUES (43, 126, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太测试响应时间', 14, 1, '2025-12-29 09:30:45', '2025-12-29 09:30:58');
INSERT INTO `notifications` VALUES (44, 127, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太测试响应时间', 14, 0, '2025-12-29 09:30:45', NULL);
INSERT INTO `notifications` VALUES (45, 8, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：测试响应时间', 15, 0, '2025-12-29 09:32:44', NULL);
INSERT INTO `notifications` VALUES (46, 124, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：测试响应时间', 15, 0, '2025-12-29 09:32:44', NULL);
INSERT INTO `notifications` VALUES (47, 126, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：测试响应时间', 15, 1, '2025-12-29 09:32:44', '2025-12-29 09:32:51');
INSERT INTO `notifications` VALUES (48, 127, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：测试响应时间', 15, 0, '2025-12-29 09:32:44', NULL);
INSERT INTO `notifications` VALUES (49, 8, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太响应时间测试', 16, 0, '2025-12-29 09:36:51', NULL);
INSERT INTO `notifications` VALUES (50, 124, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太响应时间测试', 16, 0, '2025-12-29 09:36:51', NULL);
INSERT INTO `notifications` VALUES (51, 126, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太响应时间测试', 16, 1, '2025-12-29 09:36:51', '2025-12-29 09:36:55');
INSERT INTO `notifications` VALUES (52, 127, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太响应时间测试', 16, 0, '2025-12-29 09:36:51', '2025-12-31 09:41:31');
INSERT INTO `notifications` VALUES (53, 8, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太优化操作按钮', 17, 0, '2025-12-29 09:49:06', NULL);
INSERT INTO `notifications` VALUES (54, 124, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太优化操作按钮', 17, 0, '2025-12-29 09:49:06', NULL);
INSERT INTO `notifications` VALUES (55, 126, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太优化操作按钮', 17, 1, '2025-12-29 09:49:06', '2025-12-29 09:56:54');
INSERT INTO `notifications` VALUES (56, 127, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太优化操作按钮', 17, 0, '2025-12-29 09:49:06', NULL);
INSERT INTO `notifications` VALUES (57, 8, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太测试全部响应按钮', 18, 0, '2025-12-29 09:57:37', NULL);
INSERT INTO `notifications` VALUES (58, 124, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太测试全部响应按钮', 18, 0, '2025-12-29 09:57:37', NULL);
INSERT INTO `notifications` VALUES (59, 126, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太测试全部响应按钮', 18, 1, '2025-12-29 09:57:37', '2025-12-29 09:58:11');
INSERT INTO `notifications` VALUES (60, 127, 129, 'emergency_help', '紧急求助通知', '居民【徐太太】发起紧急求助：徐太太测试全部响应按钮', 18, 0, '2025-12-29 09:57:37', NULL);
INSERT INTO `notifications` VALUES (61, 8, 133, 'emergency_help', '紧急求助通知', '居民【张姥姥】发起紧急求助：跌倒了', 19, 0, '2025-12-31 08:52:31', NULL);
INSERT INTO `notifications` VALUES (62, 124, 133, 'emergency_help', '紧急求助通知', '居民【张姥姥】发起紧急求助：跌倒了', 19, 0, '2025-12-31 08:52:31', NULL);
INSERT INTO `notifications` VALUES (63, 126, 133, 'emergency_help', '紧急求助通知', '居民【张姥姥】发起紧急求助：跌倒了', 19, 0, '2025-12-31 08:52:31', NULL);
INSERT INTO `notifications` VALUES (64, 127, 133, 'emergency_help', '紧急求助通知', '居民【张姥姥】发起紧急求助：跌倒了', 19, 0, '2025-12-31 08:52:31', NULL);
INSERT INTO `notifications` VALUES (65, 131, 133, 'emergency_help', '紧急求助通知', '居民【张姥姥】发起紧急求助：跌倒了', 19, 0, '2025-12-31 08:52:31', NULL);
INSERT INTO `notifications` VALUES (66, 134, 133, 'emergency_help', '紧急求助通知', '居民【张姥姥】发起紧急求助：跌倒了', 19, 1, '2025-12-31 08:52:31', '2025-12-31 08:52:36');
INSERT INTO `notifications` VALUES (67, 8, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心脏疼', 20, 0, '2026-01-02 22:22:46', NULL);
INSERT INTO `notifications` VALUES (68, 124, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心脏疼', 20, 0, '2026-01-02 22:22:46', NULL);
INSERT INTO `notifications` VALUES (69, 126, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心脏疼', 20, 0, '2026-01-02 22:22:46', NULL);
INSERT INTO `notifications` VALUES (70, 127, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心脏疼', 20, 0, '2026-01-02 22:22:46', NULL);
INSERT INTO `notifications` VALUES (71, 131, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心脏疼', 20, 0, '2026-01-02 22:22:46', NULL);
INSERT INTO `notifications` VALUES (72, 134, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心脏疼', 20, 0, '2026-01-02 22:22:46', NULL);
INSERT INTO `notifications` VALUES (73, 136, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心脏疼', 20, 1, '2026-01-02 22:22:46', '2026-01-02 22:22:53');
INSERT INTO `notifications` VALUES (74, 8, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心口疼', 21, 0, '2026-01-03 00:23:15', NULL);
INSERT INTO `notifications` VALUES (75, 124, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心口疼', 21, 0, '2026-01-03 00:23:15', NULL);
INSERT INTO `notifications` VALUES (76, 126, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心口疼', 21, 0, '2026-01-03 00:23:15', NULL);
INSERT INTO `notifications` VALUES (77, 127, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心口疼', 21, 0, '2026-01-03 00:23:15', NULL);
INSERT INTO `notifications` VALUES (78, 131, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心口疼', 21, 0, '2026-01-03 00:23:15', NULL);
INSERT INTO `notifications` VALUES (79, 134, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心口疼', 21, 0, '2026-01-03 00:23:15', NULL);
INSERT INTO `notifications` VALUES (80, 136, 135, 'emergency_help', '紧急求助通知', '居民【赵太太】发起紧急求助：心口疼', 21, 1, '2026-01-03 00:23:15', '2026-01-03 00:23:20');

-- ----------------------------
-- Table structure for payment_records
-- ----------------------------
DROP TABLE IF EXISTS `payment_records`;
CREATE TABLE `payment_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `appointment_id` bigint NOT NULL COMMENT '预约ID',
  `resident_id` bigint NOT NULL COMMENT '居民ID',
  `amount` decimal(10, 2) NOT NULL COMMENT '支付金额',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式：online(在线)、offline(线下)',
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付状态：pending(待支付)、completed(已支付)、refunded(已退款)',
  `transaction_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '交易ID',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `remark` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `appointment_id`(`appointment_id` ASC) USING BTREE,
  INDEX `idx_resident_id`(`resident_id` ASC) USING BTREE,
  INDEX `idx_payment_status`(`payment_status` ASC) USING BTREE,
  CONSTRAINT `payment_records_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `appointments` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `payment_records_ibfk_2` FOREIGN KEY (`resident_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '支付记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of payment_records
-- ----------------------------

-- ----------------------------
-- Table structure for service_needs
-- ----------------------------
DROP TABLE IF EXISTS `service_needs`;
CREATE TABLE `service_needs`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `resident_id` bigint NOT NULL COMMENT '居民用户ID',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '需求标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '需求描述',
  `service_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务类型',
  `budget` decimal(10, 2) NULL DEFAULT NULL COMMENT '预算',
  `required_date` date NULL DEFAULT NULL COMMENT '需求完成日期',
  `location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务地点',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'published' COMMENT '状态：published(已发布)、accepted(已接单)、completed(已完成)、cancelled(已取消)',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  `accepted_quotation_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_resident_id`(`resident_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  CONSTRAINT `service_needs_ibfk_1` FOREIGN KEY (`resident_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 26 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务需求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_needs
-- ----------------------------
INSERT INTO `service_needs` VALUES (25, 7, '帮忙打扫卫生', '把厨房卫生打扫了', 'repair', 0.00, '2025-12-30', '居民楼张三家', 'pending', '2025-12-27 19:33:13', '2025-12-27 19:33:13', NULL);

-- ----------------------------
-- Table structure for service_provider_info
-- ----------------------------
DROP TABLE IF EXISTS `service_provider_info`;
CREATE TABLE `service_provider_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `company_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公司/门店名称',
  `business_license` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业执照',
  `certification` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资质认证',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '服务提供商描述',
  `rating` decimal(3, 2) NULL DEFAULT NULL COMMENT '评分（5分制）',
  `completed_orders` int NULL DEFAULT 0 COMMENT '完成订单数',
  `approval_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'pending' COMMENT '审核状态：pending(待审核)、approved(已批准)、rejected(已拒绝)',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_approval_status`(`approval_status` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE,
  CONSTRAINT `service_provider_info_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务提供商信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of service_provider_info
-- ----------------------------

-- ----------------------------
-- Table structure for services
-- ----------------------------
DROP TABLE IF EXISTS `services`;
CREATE TABLE `services`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `provider_id` bigint NULL DEFAULT NULL COMMENT '服务提供商ID（如果为NULL则为平台标准服务）',
  `service_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务项目名称',
  `service_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务类型',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '服务描述',
  `service_process` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '服务流程',
  `price` decimal(10, 2) NULL DEFAULT NULL COMMENT '服务价格',
  `duration_minutes` int NULL DEFAULT NULL COMMENT '服务时长（分钟）',
  `provider_qualifications` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务人员资质',
  `service_area` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '服务地区',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：1-可用、0-禁用',
  `approval_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'approved' COMMENT '审核状态：pending(待审核)、approved(已批准)、rejected(已拒绝)',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_service_name`(`service_name` ASC) USING BTREE,
  INDEX `idx_service_type`(`service_type` ASC) USING BTREE,
  INDEX `idx_provider_id`(`provider_id` ASC) USING BTREE,
  INDEX `idx_approval_status`(`approval_status` ASC) USING BTREE,
  CONSTRAINT `services_ibfk_1` FOREIGN KEY (`provider_id`) REFERENCES `service_provider_info` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '服务项目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of services
-- ----------------------------
INSERT INTO `services` VALUES (1, NULL, '上门理发', '生活护理', '专业理发师上门提供理发和修剪服务', '预约时间->上门服务->支付结算', 50.00, 60, '持证理发师', NULL, 1, 'approved', '2025-12-26 15:31:32', '2025-12-26 15:31:32');
INSERT INTO `services` VALUES (3, NULL, '健康体检', '医疗护理', '定期健康检查和健康评估', '预约检查->专业检查->报告反馈', 150.00, 45, '医护人员', NULL, 1, 'approved', '2025-12-26 15:31:32', '2025-12-26 15:31:32');
INSERT INTO `services` VALUES (5, NULL, '陪同就医', '生活护理', '陪同老年人就医挂号、看病、取药', '确认时间->陪同就医->反馈结果', 80.00, 120, '专业护工', NULL, 1, 'approved', '2025-12-26 15:31:32', '2025-12-26 15:31:32');
INSERT INTO `services` VALUES (6, NULL, '心理关怀', '心理咨询', '心理疏导、精神慰藉、倾听陪伴', '预约咨询->心理疏导->效果评估', 40.00, 90, '心理咨询师', NULL, 1, 'approved', '2025-12-26 15:31:32', '2025-12-26 15:31:32');
INSERT INTO `services` VALUES (7, NULL, '做饭', '家务协助', '帮你做美味可口的饭菜', NULL, 80.00, 60, NULL, NULL, 1, 'approved', '2025-12-27 20:30:55', '2025-12-27 20:30:55');
INSERT INTO `services` VALUES (8, NULL, '修理电器', '维修工具', '维修电视等电器', NULL, 50.00, 60, NULL, NULL, 1, 'approved', '2025-12-27 23:16:50', '2025-12-27 23:17:11');
INSERT INTO `services` VALUES (9, NULL, '康复训练', '医疗帮助', '帮你正骨等', NULL, 50.00, 60, NULL, NULL, 1, 'approved', '2025-12-29 10:04:49', '2025-12-29 10:04:49');
INSERT INTO `services` VALUES (10, NULL, '种菜帮助', '生活帮助', '帮忙进行一些种菜等活动', NULL, 50.00, 60, NULL, NULL, 1, 'approved', '2025-12-31 08:49:28', '2025-12-31 08:49:28');
INSERT INTO `services` VALUES (11, NULL, '知识讲解', '知识讲解', '讲授一些知识', NULL, 150.00, 45, NULL, NULL, 1, 'approved', '2026-01-02 22:17:48', '2026-01-02 22:18:04');
INSERT INTO `services` VALUES (12, NULL, '测试服务', '测试类型', '测试', NULL, 50.00, 60, NULL, NULL, 0, 'approved', '2026-01-02 23:35:48', '2026-01-03 00:04:40');

-- ----------------------------
-- Table structure for statistics
-- ----------------------------
DROP TABLE IF EXISTS `statistics`;
CREATE TABLE `statistics`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `stat_date` date NOT NULL COMMENT '统计日期',
  `stat_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '统计类型：orders(订单)、users(用户)、revenue(收入)、satisfaction(满意度)',
  `total_count` int NULL DEFAULT NULL COMMENT '总数',
  `detail` json NULL COMMENT '详细数据',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `unique_stat`(`stat_date` ASC, `stat_type` ASC) USING BTREE,
  INDEX `idx_stat_date`(`stat_date` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '统计数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of statistics
-- ----------------------------
INSERT INTO `statistics` VALUES (1, '2025-12-25', 'orders', 120, '{\"pending\": 20, \"completed\": 100}', '2025-12-26 17:53:49');
INSERT INTO `statistics` VALUES (2, '2025-12-25', 'users', 200, '{\"new\": 50, \"active\": 150}', '2025-12-26 17:53:49');
INSERT INTO `statistics` VALUES (3, '2025-12-25', 'revenue', 5000, '{\"total\": 5000, \"refunded\": 200}', '2025-12-26 17:53:49');
INSERT INTO `statistics` VALUES (4, '2025-12-25', 'satisfaction', 85, '{\"rating\": 4.5, \"feedback_count\": 100}', '2025-12-26 17:53:49');
INSERT INTO `statistics` VALUES (5, '2025-12-26', 'orders', 130, '{\"pending\": 20, \"completed\": 110}', '2025-12-26 17:53:49');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（MD5加密）',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '电话号码',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'resident' COMMENT '角色：admin(管理员)、resident(居民)、service_provider(服务提供商)',
  `status` int NOT NULL DEFAULT 1 COMMENT '状态：1-正常、0-禁用',
  `created_time` datetime NULL DEFAULT NULL COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 143 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin', '0192023a7bbd73250516f069df18b500', '系统管理员', NULL, NULL, NULL, 'admin', 1, '2025-12-26 15:31:32', '2025-12-26 15:31:32');
INSERT INTO `users` VALUES (4, 'provider1', '0192023a7bbd73250516f069df18b500', '护理服务公司', '13800138010', NULL, NULL, 'service_provider', 1, '2025-12-26 15:31:32', '2025-12-26 15:31:32');
INSERT INTO `users` VALUES (5, 'provider2', '0192023a7bbd73250516f069df18b500', '居家保洁服务', '13800138011', NULL, NULL, 'service_provider', 1, '2025-12-26 15:31:32', '2025-12-26 15:31:32');
INSERT INTO `users` VALUES (8, 'hugong', 'e10adc3949ba59abbe56e057f20f883e', '员工张三', '123456', NULL, NULL, 'caregiver', 1, '2025-12-26 17:01:24', '2025-12-26 17:01:24');
INSERT INTO `users` VALUES (124, '13271385770', 'e10adc3949ba59abbe56e057f20f883e', '员工李四', '13271385770', NULL, NULL, 'caregiver', 1, '2025-12-27 15:55:11', '2025-12-27 15:55:11');
INSERT INTO `users` VALUES (125, '14563278974', 'e10adc3949ba59abbe56e057f20f883e', '刘姥姥', '14563278974', NULL, NULL, 'resident', 1, '2025-12-27 20:28:49', '2025-12-27 20:28:49');
INSERT INTO `users` VALUES (126, '12345612345', 'e10adc3949ba59abbe56e057f20f883e', '员工王五', '12345612345', NULL, NULL, 'caregiver', 1, '2025-12-27 20:29:44', '2025-12-27 20:29:44');
INSERT INTO `users` VALUES (127, '19561998178', 'e10adc3949ba59abbe56e057f20f883e', '员工赵六', '19561998178', NULL, NULL, 'caregiver', 1, '2025-12-27 23:12:40', '2025-12-27 23:12:40');
INSERT INTO `users` VALUES (128, '12345678905', 'e10adc3949ba59abbe56e057f20f883e', '牛爷爷', '12345678905', NULL, NULL, 'resident', 1, '2025-12-27 23:16:06', '2025-12-27 23:16:06');
INSERT INTO `users` VALUES (129, '18203862557', 'e10adc3949ba59abbe56e057f20f883e', '徐太太', '18203862557', NULL, NULL, 'resident', 1, '2025-12-29 08:55:54', '2025-12-29 08:55:54');
INSERT INTO `users` VALUES (130, 'wutaitai', 'e10adc3949ba59abbe56e057f20f883e', '吴太太', '13271385771', NULL, NULL, 'resident', 1, '2025-12-29 10:00:38', '2025-12-29 11:36:49');
INSERT INTO `users` VALUES (131, '19372475603', 'e10adc3949ba59abbe56e057f20f883e', '员工徐七', '19372475603', NULL, NULL, 'caregiver', 1, '2025-12-29 10:03:20', '2025-12-29 10:03:20');
INSERT INTO `users` VALUES (132, '12345678963', 'e10adc3949ba59abbe56e057f20f883e', '王老三', '12345678963', NULL, NULL, 'resident', 1, '2025-12-29 20:27:39', '2025-12-29 20:27:39');
INSERT INTO `users` VALUES (133, '12345678901', 'e10adc3949ba59abbe56e057f20f883e', '张姥姥', '12345678901', NULL, NULL, 'resident', 1, '2025-12-31 08:43:28', '2025-12-31 08:47:58');
INSERT INTO `users` VALUES (134, '12345678902', 'e10adc3949ba59abbe56e057f20f883e', '员工钱八', '12345678902', NULL, NULL, 'caregiver', 1, '2025-12-31 08:44:51', '2025-12-31 08:46:24');
INSERT INTO `users` VALUES (135, 'zhaotaitai', 'e10adc3949ba59abbe56e057f20f883e', '赵太太', '12345678985', NULL, NULL, 'resident', 1, '2026-01-02 22:11:22', '2026-01-02 22:15:40');
INSERT INTO `users` VALUES (136, 'yuangongchen', 'e10adc3949ba59abbe56e057f20f883e', '员工陈八', '12345678991', NULL, NULL, 'caregiver', 1, '2026-01-02 22:12:25', '2026-01-02 22:16:02');
INSERT INTO `users` VALUES (140, '12378945674', 'e10adc3949ba59abbe56e057f20f883e', '陈太太', '12378945674', NULL, NULL, 'resident', 1, '2026-01-02 23:44:29', '2026-01-02 23:48:51');

SET FOREIGN_KEY_CHECKS = 1;
