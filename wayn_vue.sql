/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.31.49
 Source Server Type    : MySQL
 Source Server Version : 80019
 Source Host           : 192.168.31.49:3306
 Source Schema         : wayn_vue

 Target Server Type    : MySQL
 Target Server Version : 80019
 File Encoding         : 65001

 Date: 26/06/2020 22:32:57
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for qiniu_config
-- ----------------------------
DROP TABLE IF EXISTS `qiniu_config`;
CREATE TABLE `qiniu_config`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `access_key` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'accessKey',
  `bucket` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT 'Bucket 识别符',
  `host` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT '外链域名',
  `secret_key` text CHARACTER SET utf8 COLLATE utf8_general_ci NULL COMMENT 'secretKey',
  `type` tinyint(0) NULL DEFAULT NULL COMMENT '空间类型 0 公开 1 私有',
  `region` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '存储区域',
  `enable` tinyint(0) NULL DEFAULT NULL COMMENT '是否启用七牛云存储 0 启用 1 禁用',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci COMMENT = '七牛云配置' ROW_FORMAT = Compact;

-- ----------------------------
-- Records of qiniu_config
-- ----------------------------
INSERT INTO `qiniu_config` VALUES (1, NULL, NULL, 'http://cdn.wayn.xin', NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for shop_banner
-- ----------------------------
DROP TABLE IF EXISTS `shop_banner`;
CREATE TABLE `shop_banner`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间/注册时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '最后更新时间',
  `sort` int(0) NULL DEFAULT NULL COMMENT '显示顺序',
  `img_url` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'banner图url',
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '栏目类型',
  `jump_url` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点击banner跳转到url',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  `status` tinyint(0) NULL DEFAULT 0 COMMENT 'banner状态（0启用 1禁用）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 12 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'banner' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_banner
-- ----------------------------
INSERT INTO `shop_banner` VALUES (14, 'admin', '2020-06-26 19:56:03', NULL, NULL, NULL, 'http://cdn.wayn.xin/0295dc8f9fc9edff45bd902623279604.png', 'hh', NULL, 'rrr', NULL, 0, 0);

-- ----------------------------
-- Table structure for shop_category
-- ----------------------------
DROP TABLE IF EXISTS `shop_category`;
CREATE TABLE `shop_category`  (
  `id` int(0) NOT NULL AUTO_INCREMENT,
  `name` varchar(63) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '类目名称',
  `keywords` varchar(1023) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '' COMMENT '类目关键字，以JSON数组格式',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '类目广告语介绍',
  `pid` int(0) NOT NULL DEFAULT 0 COMMENT '父类目ID',
  `icon_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '类目图标',
  `pic_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '类目图片',
  `level` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'L1',
  `sort_order` tinyint(0) NULL DEFAULT 50 COMMENT '排序',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT 'banner状态（0启用 1禁用）',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_id`(`pid`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1036007 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '类目表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_category
-- ----------------------------
INSERT INTO `shop_category` VALUES (1005000, '居家', '', '回家，放松身心', 0, 'http://yanxuan.nosdn.127.net/a45c2c262a476fea0b9fc684fed91ef5.png', 'http://yanxuan.nosdn.127.net/e8bf0cf08cf7eda21606ab191762e35c.png', 'L1', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2020-06-26 15:50:50', NULL);
INSERT INTO `shop_category` VALUES (1005001, '餐厨', '', '爱，囿于厨房', 0, 'http://yanxuan.nosdn.127.net/ad8b00d084cb7d0958998edb5fee9c0a.png', 'http://yanxuan.nosdn.127.net/3708dbcb35ad5abf9e001500f73db615.png', 'L1', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1005002, '饮食', '', '好吃，高颜值美食', 0, 'http://yanxuan.nosdn.127.net/c9280327a3fd2374c000f6bf52dff6eb.png', 'http://yanxuan.nosdn.127.net/fb670ff3511182833e5b035275e4ac09.png', 'L1', 9, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1005007, '锅具', '', '一口好锅，炖煮生活一日三餐', 1005001, 'http://yanxuan.nosdn.127.net/4aab4598017b5749e3b63309d25e9f6b.png', 'http://yanxuan.nosdn.127.net/d2db0d1d0622c621a8aa5a7c06b0fc6d.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1005008, '餐具', '', '餐桌上的舞蹈', 1005001, 'http://yanxuan.nosdn.127.net/f109afbb7e7a00c243c1da29991a5aa3.png', 'http://yanxuan.nosdn.127.net/695ed861a63d8c0fc51a51f42a5a993b.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1005009, '清洁', '', '环保便利，聪明之选', 1005001, 'http://yanxuan.nosdn.127.net/e8b67fe8b8db2ecc2e126a0aa631def0.png', 'http://yanxuan.nosdn.127.net/3a40faaef0a52627357d98ceed7a3c45.png', 'L2', 9, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1005010, '炒货', '', '精选原产地，美味加营养', 1005002, 'http://yanxuan.nosdn.127.net/6c43063003207168c1d8e83a923e8515.png', 'http://yanxuan.nosdn.127.net/3972963a4b6f9588262d2a667f4c1c73.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1005011, '小食', '', '原香鲜材，以小食之味，带来味蕾惊喜', 1005002, 'http://yanxuan.nosdn.127.net/663f568475c994358bf31bcb67d122fe.png', 'http://yanxuan.nosdn.127.net/418f86049f957108a31ad55cec42c349.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1005012, '食材', '', '天时地利人和，寻找这个时节这个地点的味道', 1005002, 'http://yanxuan.nosdn.127.net/e050980992725b7932bb3645fe5aec08.png', 'http://yanxuan.nosdn.127.net/80db363e0687b1a65edc6e75c1b99726.png', 'L2', 10, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1005013, '冲饮', '', '以用料天然之美，尽享闲雅之意', 1005002, 'http://yanxuan.nosdn.127.net/2919b0d6eec79182cca31dc827f4d00a.png', 'http://yanxuan.nosdn.127.net/1e3d8f65c7c7811baccdfda6711cbfd5.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1007000, '杯壶', '', '精工生产制作，匠人手艺', 1005001, 'http://yanxuan.nosdn.127.net/0b244d3575b737c8f0ed7e84c5c4abd2.png', 'http://yanxuan.nosdn.127.net/ec53828a3814171079178a59fb2593da.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008000, '配件', '', '配角，亦是主角', 0, 'http://yanxuan.nosdn.127.net/11abb11c4cfdee59abfb6d16caca4c6a.png', 'http://yanxuan.nosdn.127.net/02f9a44d05c05c0dd439a5eb674570a2.png', 'L1', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008001, '毛巾', '', '日本皇室专供，内野制造商出品', 1013001, 'http://yanxuan.nosdn.127.net/44ad9a739380aa6b7cf956fb2a06e7a7.png', 'http://yanxuan.nosdn.127.net/c53d2dd5ba6b1cfb55bd42ea0783f051.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008002, '布艺软装', '', '各种风格软装装点你的家', 1005000, 'http://yanxuan.nosdn.127.net/8bbcd7de60a678846664af998f57e71c.png', 'http://yanxuan.nosdn.127.net/2e2fb4f2856a021bbcd1b4c8400f2b06.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008003, '鞋', '', '一双好鞋，才能带你到远方', 1008000, 'http://yanxuan.nosdn.127.net/4316c2d05745bc90d1f333e363e571bd.png', 'http://yanxuan.nosdn.127.net/85566d138ea55e6aaeda2cda02df66f8.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008004, '袜子', '', '新百伦、阿迪达斯等制造商出品', 1010000, 'http://yanxuan.nosdn.127.net/f123c74f54d9acff0bd1546c60034814.png', 'http://yanxuan.nosdn.127.net/13f256bac02bb27d74e035ad25cbd375.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008005, '户外', '', 'MUJI、Nike等制造商出品', 1012000, 'http://yanxuan.nosdn.127.net/83d22ca3d1c8f94ee23ca96de489864c.png', 'http://yanxuan.nosdn.127.net/833476fc3ecc30a7446279b787328775.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008006, '口罩', '', '为你遮挡雾霾', 1012000, 'http://yanxuan.nosdn.127.net/9b93e661ff59cbda6094e8b30a63724e.png', 'http://yanxuan.nosdn.127.net/11d9700da759f2c962c2f6d9412ac2a1.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008007, '围巾件套', '', '围上它，你的造型才完整', 1008000, 'http://yanxuan.nosdn.127.net/3a8c7ae5b9dc5c1c4b7f2b656abb0279.png', 'http://yanxuan.nosdn.127.net/6beb3fd67106e42dc0f026b173373d16.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008008, '被枕', '', '守护你的睡眠时光', 1005000, 'http://yanxuan.nosdn.127.net/927bc33f7ae2895dd6c11cf91f5e3228.png', 'http://yanxuan.nosdn.127.net/b43ef7cececebe6292d2f7f590522e05.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008009, '床品件套', '', 'MUJI等品牌制造商出品', 1005000, 'http://yanxuan.nosdn.127.net/243e5bf327a87217ad1f54592f0176ec.png', 'http://yanxuan.nosdn.127.net/81f671bd36bce05d5f57827e5c88dd1b.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008010, '拖鞋', '', '穿上拖鞋，回到自我', 1008000, 'http://yanxuan.nosdn.127.net/1121696544ed9b0c2a70e82f1088fa0e.png', 'http://yanxuan.nosdn.127.net/984ddb9671aab41651784ba55b2cbdcf.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008011, '清洁保鲜', '', '真空保鲜，美味不限时', 1005001, 'http://yanxuan.nosdn.127.net/dc4d6c35b9f4abb42d2eeaf345710589.png', 'http://yanxuan.nosdn.127.net/04cd632e1589adcc4345e40e8ad75d2b.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008012, '功能厨具', '', '下厨省力小帮手', 1005001, 'http://yanxuan.nosdn.127.net/22db4ccbf52dc62c723ac83aa587812a.png', 'http://yanxuan.nosdn.127.net/5b94463017437467a93ae4af17c2ba4f.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008013, '茶具咖啡具', '', '先进工艺制造，功夫体验', 1005001, 'http://yanxuan.nosdn.127.net/9ea192cd2719c8348f42ec17842ba763.png', 'http://yanxuan.nosdn.127.net/be3ba4056e274e311d1c23bd2931018d.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008014, '糖巧', '', '糖心蜜意，甜而不腻', 1005002, 'http://yanxuan.nosdn.127.net/db48a1db4daab74233656caaea4a06f3.png', 'http://yanxuan.nosdn.127.net/c12cf29b574c7e9d1fcff6a57a12eea2.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008015, '糕点', '', '四季糕点，用心烘焙', 1005002, 'http://yanxuan.nosdn.127.net/93168242df456b5f7bf3c89653b3db76.png', 'http://yanxuan.nosdn.127.net/66ea1d6ad602a8e441af7cada93bdc7a.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008016, '灯具', '', '一盏灯，温暖一个家', 1005000, 'http://yanxuan.nosdn.127.net/c48e0d9dcfac01499a437774a915842b.png', 'http://yanxuan.nosdn.127.net/f702dc399d14d4e1509d5ed6e57acd19.png', 'L2', 8, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008017, '收纳', '', '选自古驰竹柄原料供应商', 1012000, 'http://yanxuan.nosdn.127.net/fdc048e1bf4f04d1c20b32eda5d1dc6e.png', 'http://yanxuan.nosdn.127.net/2a62f6c53f4ff089fa6a210c7a0c2e63.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1008018, '单肩包', '', '单肩装上惬意心情', 1008000, 'http://yanxuan.nosdn.127.net/2f71c7710f0bf857e787e1adb449c8a2.png', 'http://yanxuan.nosdn.127.net/55f34f23ed31f31e1313ff33602f90cc.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1009000, '日用清洁', '', '洁净才能带来清爽心情', 1013001, 'http://yanxuan.nosdn.127.net/e071686c212e93aa2fcafd0062a9c613.png', 'http://yanxuan.nosdn.127.net/729638bb13997f9c4c435b41ce6ed910.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1010000, '服装', '', '贴身的，要亲肤', 0, 'http://yanxuan.nosdn.127.net/28a685c96f91584e7e4876f1397767db.png', 'http://yanxuan.nosdn.127.net/622c8d79292154017b0cbda97588a0d7.png', 'L1', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1010001, '内衣', '', '给你贴身的关怀', 1010000, 'http://yanxuan.nosdn.127.net/20279e1753e4eedc6e347857acda9681.png', 'http://yanxuan.nosdn.127.net/02fede55aba1bc6c9d7f7c01682f9e2d.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1010002, '内裤', '', '来自李维斯、爱慕等制造商', 1010000, 'http://yanxuan.nosdn.127.net/364269344ed69adafe1b70ab7998fc50.png', 'http://yanxuan.nosdn.127.net/0a7fe0a08c195ca2cf55d12cd3c30f09.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1010003, '地垫', '', '家里的第“五”面墙', 1005000, 'http://yanxuan.nosdn.127.net/83d4c87f28c993af1aa8d3e4d30a2fa2.png', 'http://yanxuan.nosdn.127.net/1611ef6458e244d1909218becfe87c4d.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1010004, '双肩包', '', '背上的时髦', 1008000, 'http://yanxuan.nosdn.127.net/5197c44b610d786796f955334b55c7a5.png', 'http://yanxuan.nosdn.127.net/506d19510c967ba137283035a93738a1.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1011000, '婴童', '', '爱，从心开始', 0, 'http://yanxuan.nosdn.127.net/1ba9967b8de1ac50fad21774a4494f5d.png', 'http://yanxuan.nosdn.127.net/9cc0b3e0d5a4f4a22134c170f10b70f2.png', 'L1', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1011001, '妈咪', '', '犬印、Harvest Hills制造商', 1011000, 'http://yanxuan.nosdn.127.net/720aebaa529df9391b95a078dfb2fd5c.png', 'http://yanxuan.nosdn.127.net/844e2f4dce94f71283840c141d4ca71b.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1011002, '海外', '', '来自海外制造商的好物', 1012000, 'http://yanxuan.nosdn.127.net/da884ff3b9e9d5276986c99e85722461.png', 'http://yanxuan.nosdn.127.net/fd1de05d274222f1e56d057d2f2c20c6.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1011003, '床垫', '', '承托你的好时光', 1005000, 'http://yanxuan.nosdn.127.net/316afeb3948b295dfe073e4c51f77a42.png', 'http://yanxuan.nosdn.127.net/d6e0e84961032fc70fd52a8d4d0fb514.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1011004, '家饰', '', '装饰你的家', 1005000, 'http://yanxuan.nosdn.127.net/ab0df9445d985bf6719ac415313a8e88.png', 'http://yanxuan.nosdn.127.net/79275db76b5865e6167b0fbd141f2d7e.png', 'L2', 9, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1012000, '杂货', '', '解忧，每个烦恼', 0, 'http://yanxuan.nosdn.127.net/c2a3d6349e72c35931fe3b5bcd0966be.png', 'http://yanxuan.nosdn.127.net/547853361d29a37282f377b9a755dd37.png', 'L1', 8, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1012001, '功能箱包', '', '范思哲、Coach等品牌制造商出品', 1008000, 'http://yanxuan.nosdn.127.net/3050a2b3052d766c4b460d4b766353a3.png', 'http://yanxuan.nosdn.127.net/0645dcda6172118f9295630c2a6f234f.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1012002, '雨具', '', 'WPC制作商出品', 1012000, 'http://yanxuan.nosdn.127.net/4e929a21baebdb1200361d8097e35e45.png', 'http://yanxuan.nosdn.127.net/589da0f02917b8393197a43175764381.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1012003, '文具', '', '找回书写的力量', 1012000, 'http://yanxuan.nosdn.127.net/e1743239e41ca9af76875aedc73be7f0.png', 'http://yanxuan.nosdn.127.net/e074795f61a83292d0f20eb7d124e2ac.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1013000, '靴', '', '经典的温暖', 1008000, 'http://yanxuan.nosdn.127.net/868c2a976719cd508e9ddf807167a446.png', 'http://yanxuan.nosdn.127.net/59485f1aa100e4210e16175f3412fa41.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1013001, '洗护', '', '亲肤之物，严选天然', 0, 'http://yanxuan.nosdn.127.net/9fe068776b6b1fca13053d68e9c0a83f.png', 'http://yanxuan.nosdn.127.net/1526ab0f5982722adbc8726f9f2a338c.png', 'L1', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1013002, '美妆', '', '为你的面容添色', 1013001, 'http://yanxuan.nosdn.127.net/aa49c088f74a1c318f1765cc2703495a.png', 'http://yanxuan.nosdn.127.net/d6a7b9a2eb6af92d709429798a4ca3ea.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1013003, '护发', '', '呵护秀发，柔顺不同发质', 1013001, 'http://yanxuan.nosdn.127.net/672ddbed88d9762d2be789080880b16a.png', 'http://yanxuan.nosdn.127.net/398375d0e39574c6e87273d328316186.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1013004, '香薰', '', '爱马仕集团制造商出品', 1013001, 'http://yanxuan.nosdn.127.net/d43e7af0a6a9385d88be2ca1df679158.png', 'http://yanxuan.nosdn.127.net/fc7764ff8e12d18f6c5881a32318ed16.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1013005, '刀剪砧板', '', '传统工艺 源自中国刀城', 1005001, 'http://yanxuan.nosdn.127.net/9d481ea4c2e9e6eda35aa720d407332e.png', 'http://yanxuan.nosdn.127.net/555afbfe05dab48c1a3b90dcaf89b4f2.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1013006, '家居服', '', '舒适亲肤', 1010000, 'http://yanxuan.nosdn.127.net/71f391af17fce739a6a57a1eeadbcbf0.png', 'http://yanxuan.nosdn.127.net/5da102ea4c64081ce3a05a91c855fbc9.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1015000, '家具', '', '大师级工艺', 1005000, 'http://yanxuan.nosdn.127.net/4f00675caefd0d4177892ad18bfc2df6.png', 'http://yanxuan.nosdn.127.net/d5d41841136182bf49c1f99f5c452dd6.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1015001, 'T恤', '', '自在而潇洒的穿着感', 1010000, 'http://yanxuan.nosdn.127.net/24a7a33cfeac0bb87a737480db79e053.png', 'http://yanxuan.nosdn.127.net/505c9a5a794b79e85fef4654722b3447.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1017000, '宠物', '', '抑菌除味，打造宠物舒适空间', 1005000, 'http://yanxuan.nosdn.127.net/a0352c57c60ce4f68370ecdab6a30857.png', 'http://yanxuan.nosdn.127.net/dae4d6e89ab8a0cd3e8da026e4660137.png', 'L2', 10, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1018000, '夏日甜心', '', '湖南卫视《夏日甜心》周边', 1019000, 'http://yanxuan.nosdn.127.net/b5e9f174404ef81b8603d6ecc304c62e.png', 'http://yanxuan.nosdn.127.net/2b8497fe583d3c9759128b2d76f89dfd.png', 'L2', 10, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1019000, '志趣', '', '周边精品，共享热爱', 0, 'http://yanxuan.nosdn.127.net/7093cfecb9dde1dd3eaf459623df4071.png', 'http://yanxuan.nosdn.127.net/1706e24a5e605870ba3b37ff5f49aa18.png', 'L1', 10, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020000, '出行用品', '', '出行小物，贴心相伴', 1012000, 'http://yanxuan.nosdn.127.net/b29a11afa76b9f4a57131555f1a54c77.png', 'http://yanxuan.nosdn.127.net/81e18c6970a7809ee0d86f0545428aa4.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020001, '面部护理', '', '温和无刺激的呵护', 1013001, 'http://yanxuan.nosdn.127.net/f73df75f334126cf1f3823696ea0663c.png', 'http://yanxuan.nosdn.127.net/babf6573f8acd53f21205a7577ec03e1.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020002, '用具', '', '小工具成就美好浴室', 1013001, 'http://yanxuan.nosdn.127.net/1a851b2b3c9e16bdfd020a5fc03e9140.png', 'http://yanxuan.nosdn.127.net/4e3aebbd7ffef5bb250d19f13cb85620.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020003, '服饰', '', '萌宝穿搭，柔软舒适触感', 1011000, 'http://yanxuan.nosdn.127.net/4e50f3c4e4d0a64cd0ad14cfc0b6bd17.png', 'http://yanxuan.nosdn.127.net/004f5f96df4aeb0645abbd70c0637239.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020004, '婴童洗护', '', '天然，呵护宝宝肌肤', 1011000, 'http://yanxuan.nosdn.127.net/c55338691ebd46bee9ebf225f80363ce.png', 'http://yanxuan.nosdn.127.net/f2e301b189befff1d99adf917ba8ce20.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020005, '寝居', '', '无荧光剂，婴幼儿A类标准', 1011000, 'http://yanxuan.nosdn.127.net/0f3c5ad63139096fd0760219e12149af.png', 'http://yanxuan.nosdn.127.net/476995896abea91d3f2e9ec20d56bd8d.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020006, '玩具', '', '萌宝童趣必备', 1011000, 'http://yanxuan.nosdn.127.net/7aac7c5819f71345a52a4b9df23d6239.png', 'http://yanxuan.nosdn.127.net/34b3267efcddad09cd652f181d87aab0.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020007, '喂养', '', '宝宝吃得香，妈妈才放心', 1011000, 'http://yanxuan.nosdn.127.net/5db40a5bf84c177515610471d4d08687.png', 'http://yanxuan.nosdn.127.net/6b6f1672fe041594245fe56a5dd80871.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020008, '配饰', '', '与众不同的点睛之笔', 1008000, 'http://yanxuan.nosdn.127.net/d835a76e56a88905194f543b67089b4b.png', 'http://yanxuan.nosdn.127.net/57ce29ca06f592d65aabfa5f0f87ad43.png', 'L2', 8, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020009, '外衣', '', '穿出时尚感', 1010000, 'http://yanxuan.nosdn.127.net/883d89e54a9287569a201eca388a7cda.png', 'http://yanxuan.nosdn.127.net/647f7c39eb7c353958274a59fd821d03.png', 'L2', 10, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1020010, '衬衫', '', '细节讲究，合身剪裁', 1010000, 'http://yanxuan.nosdn.127.net/94aa4a4814e2a7a97639438f1d52dcee.png', 'http://yanxuan.nosdn.127.net/7927f8422c341f7353041a30d01045a2.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1021000, '节日礼盒', '', '限量礼盒限时发售', 1012000, 'http://yanxuan.nosdn.127.net/e7b37b1ed5c18d63dc3e6c3f1aa85d8a.png', 'http://yanxuan.nosdn.127.net/bbb6f0ab4f6321121250c12583b0ff9a.png', 'L2', 8, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1022000, '数码', '', '智能硬件，匠心出品', 1008000, 'http://yanxuan.nosdn.127.net/3ec003761d346bc866de2ec249d7ff19.png', 'http://yanxuan.nosdn.127.net/c33b13875a86da535c935e3d454a6fd2.png', 'L2', 9, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1023000, '厨房小电', '', '厨房里的省心小电器', 1005001, 'http://yanxuan.nosdn.127.net/521bd0c02d283b80ba49e73ca84df250.png', 'http://yanxuan.nosdn.127.net/c09d784ba592e4fadabbaef6b2e95a95.png', 'L2', 8, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1025000, '礼品卡', '', '送礼、福利首选', 1019000, 'http://yanxuan.nosdn.127.net/bb9232716b2fc96d9bdbac4955360dfa.png', 'http://yanxuan.nosdn.127.net/1266f0767a3f67298a40574df0d177fb.png', 'L2', 11, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1027000, '茗茶', '', '一品茶香，品茗即是观心，饮茶涤净尘虑', 1005002, 'http://yanxuan.nosdn.127.net/0c5af0575176c4a3023783bef7a87a0f.png', 'http://yanxuan.nosdn.127.net/cfeb623929f3936cc882ffc6a9a2e927.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1027001, '果干', '', '品尝与收获到的是自然的味道', 1005002, 'http://yanxuan.nosdn.127.net/60f4ae2beef4754347fa36208f84efab.png', 'http://yanxuan.nosdn.127.net/4cdbf6ae196671cca154fe16e152d8d4.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1028001, '唱片', '', '经典音乐，用心典藏，瑞鸣音乐大师匠心打造', 1019000, 'http://yanxuan.nosdn.127.net/71feb3efd3eaee01a74e8aa78430de9d.png', 'http://yanxuan.nosdn.127.net/3b69079ea27f90b4f539e8c3b76680f5.png', 'L2', 8, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1032000, '魔兽世界', '', '艾泽拉斯的冒险，才刚刚开始', 1019000, 'http://yanxuan.nosdn.127.net/336f0186a9920eb0f93a3912f3662ffe.png', 'http://yanxuan.nosdn.127.net/becfba90e8a5c95d403b8a6b9bb77825.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1032001, '炉石传说', '', '快进来坐下吧，看看酒馆的新玩意', 1019000, 'http://yanxuan.nosdn.127.net/97937fcf2defb864d9e53d98a337d78a.png', 'http://yanxuan.nosdn.127.net/b5af3f6bfcbeb459d6c448ba87f8cc35.png', 'L2', 2, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1032002, '守望先锋', '', '物美价廉的补给箱', 1019000, 'http://yanxuan.nosdn.127.net/8cab7bf1225dc9893bd9de06fc51921d.png', 'http://yanxuan.nosdn.127.net/a562f05bf38f5ee478fefb81856aad3d.png', 'L2', 3, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1032003, '暗黑破坏神III', '', '奈非天们，停下脚步整理下行囊', 1019000, 'http://yanxuan.nosdn.127.net/8fe4eb999f748236228a73e09878e277.png', 'http://yanxuan.nosdn.127.net/1e19e948de63a1d0895a8620250c441f.png', 'L2', 4, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1032004, '星际争霸II', '', '记录科普卢星区的战斗时光', 1019000, 'http://yanxuan.nosdn.127.net/433ff879a3686625535ca0304be22ab2.png', 'http://yanxuan.nosdn.127.net/7394ce778791ae8242013d6c974f47e0.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1032005, '风暴英雄', '', '时空枢纽，是个充满惊喜的地方', 1019000, 'http://yanxuan.nosdn.127.net/e091aae0c8cafc5ab48dfabcc52c79b6.png', 'http://yanxuan.nosdn.127.net/ff1e28fb7151008f8dc46bbf8b357f63.png', 'L2', 6, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1033000, '梦幻西游', '', '梦幻西游精品周边', 1019000, 'http://yanxuan.nosdn.127.net/f0698297aaac41b778c1ea65eefb8b34.png', 'http://yanxuan.nosdn.127.net/36711325781ca50fdfe234489fca973e.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1034000, '丝袜', '', '厚木制造商，专利冰丝', 1010000, 'http://yanxuan.nosdn.127.net/d82d0bacfd7243c2ad09dbf2513cfcf9.png', 'http://yanxuan.nosdn.127.net/4f8f86dfd1d4b46a9cf783b4980db47f.png', 'L2', 5, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1034001, '童车童椅', '', '安全舒适，给宝宝一个快乐童年', 1011000, 'http://yanxuan.nosdn.127.net/06bbfb293b6194b27ebdb3350203a1f7.png', 'http://yanxuan.nosdn.127.net/4d16871eb80dac59d1796c7d806a5cea.png', 'L2', 7, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1035000, '卫衣', '', '舒适百搭，时尚选择', 1010000, 'http://yanxuan.nosdn.127.net/97bb55280b8ffa40390f2ee36486314a.png', 'http://yanxuan.nosdn.127.net/0282a81bbcae6c39918808fe7c4e1b93.png', 'L2', 9, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1035001, '毛衣', '', '温暖柔软，品质之选', 1010000, 'http://yanxuan.nosdn.127.net/cc886f16c8b9893305f1b3b6ad4eb0b1.png', 'http://yanxuan.nosdn.127.net/b610b058cfd73a9211dc890b7b0cbc66.png', 'L2', 8, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1035002, '裤装', '', '高质感面料，休闲商务两相宜', 1010000, 'http://yanxuan.nosdn.127.net/a3906045b1367d70f658ce9de03e8193.png', 'http://yanxuan.nosdn.127.net/1728b4eeaa7a3928f5416884f0e75b1c.png', 'L2', 11, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1035003, '肉制品', '', '真嗜肉者，都爱这一味，佳肴美馔真滋味', 1005002, 'http://yanxuan.nosdn.127.net/db3e11b8a6974a253818ae0d6fb2d24e.png', 'http://yanxuan.nosdn.127.net/94480324b376a51af47cf92df70d1ade.png', 'L2', 8, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1036000, '夏凉', '', '夏凉床品，舒适一夏', 1005000, 'http://yanxuan.nosdn.127.net/13ff4decdf38fe1a5bde34f0e0cc635a.png', 'http://yanxuan.nosdn.127.net/bd17c985bacb9b9ab1ab6e9d66ee343c.png', 'L2', 1, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1036001, '眼镜', '', '实用加时尚，造型百搭单品', 1012000, 'http://yanxuan.nosdn.127.net/97f5f75ea1209dfbb85e91932d26c3ed.png', 'http://yanxuan.nosdn.127.net/c25fb420ccb6f692a2d16f1740b60d21.png', 'L2', 9, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1036002, '汽车用品', '', '给你的爱车添装备', 1012000, 'http://yanxuan.nosdn.127.net/382cda1ef9cca77d99bcef05070d7db0.png', 'http://yanxuan.nosdn.127.net/552e943e585a999169fdbc57b59524d6.png', 'L2', 10, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1036003, '调味', '', '烹饪必备，美食调味', 1005002, 'http://yanxuan.nosdn.127.net/2ae44a3944f2bc737416e1cff3d4bcef.png', 'http://yanxuan.nosdn.127.net/13d58949a8c72ec914b5ef63ac726a43.png', 'L2', 9, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);
INSERT INTO `shop_category` VALUES (1036004, '大话西游', '', '大话西游正版周边', 1019000, 'http://yanxuan.nosdn.127.net/b60618db213322bdc2c5b1208655bd7e.png', 'http://yanxuan.nosdn.127.net/470a017f508e9a18f3068be7b315e14b.png', 'L2', 9, 0, '2018-02-01 00:00:00', 'admin', NULL, '2018-02-01 00:00:00', NULL);

-- ----------------------------
-- Table structure for shop_channel
-- ----------------------------
DROP TABLE IF EXISTS `shop_channel`;
CREATE TABLE `shop_channel`  (
  `id` bigint(0) NOT NULL AUTO_INCREMENT,
  `create_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建人',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间/注册时间',
  `update_by` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后更新人',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '最后更新时间',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '编码',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '名称',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '文章栏目' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop_channel
-- ----------------------------
INSERT INTO `shop_channel` VALUES (5, 'admin', '2020-06-26 19:32:35', NULL, NULL, 'news', '新品', '等到');

-- ----------------------------
-- Table structure for sys_dept
-- ----------------------------
DROP TABLE IF EXISTS `sys_dept`;
CREATE TABLE `sys_dept`  (
  `dept_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '部门id',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父部门id',
  `ancestors` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '祖级列表',
  `dept_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '部门名称',
  `sort` int(0) NULL DEFAULT 0 COMMENT '显示顺序',
  `leader` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '负责人',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `dept_status` tinyint(0) NULL DEFAULT 0 COMMENT '部门状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`dept_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 206 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '部门表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dept
-- ----------------------------
INSERT INTO `sys_dept` VALUES (100, 0, '0', '若依科技', 0, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', NULL, 0);
INSERT INTO `sys_dept` VALUES (101, 100, '0,100', '深圳总公司', 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', NULL, 0);
INSERT INTO `sys_dept` VALUES (102, 100, '0,100', '长沙分公司', 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', NULL, 0);
INSERT INTO `sys_dept` VALUES (103, 101, '0,100,101', '研发部门', 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2020-05-31 21:44:31', NULL, 0);
INSERT INTO `sys_dept` VALUES (104, 101, '0,100,101', '市场部门', 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2020-05-31 21:41:05', NULL, 0);
INSERT INTO `sys_dept` VALUES (105, 101, '0,100,101', '测试部门', 3, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', NULL, 0);
INSERT INTO `sys_dept` VALUES (106, 101, '0,100,101', '财务部门', 4, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', NULL, 0);
INSERT INTO `sys_dept` VALUES (107, 101, '0,100,101', '运维部门', 5, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', NULL, 0);
INSERT INTO `sys_dept` VALUES (108, 102, '0,100,102', '市场部门', 1, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2020-04-23 17:10:33', NULL, 0);
INSERT INTO `sys_dept` VALUES (109, 102, '0,100,102', '财务部门', 2, '若依', '15888888888', 'ry@qq.com', 0, 'admin', '2018-03-16 11:33:00', 'wayn', '2020-04-26 12:51:35', NULL, 0);
INSERT INTO `sys_dept` VALUES (203, 102, '0,100,102', 'test', 3, NULL, NULL, NULL, 0, 'admin', '2020-04-24 14:26:17', '', NULL, NULL, 1);
INSERT INTO `sys_dept` VALUES (204, 102, '0,100,102', 'test', 3, NULL, NULL, NULL, 0, 'admin', '2020-05-31 21:41:31', 'admin', '2020-05-31 21:44:46', NULL, 1);
INSERT INTO `sys_dept` VALUES (205, 204, '0,100,102,204', 'test', 3, NULL, NULL, NULL, 0, 'admin', '2020-05-31 21:41:36', 'admin', '2020-05-31 21:44:40', NULL, 1);

-- ----------------------------
-- Table structure for sys_dict
-- ----------------------------
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict`  (
  `dict_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '标签名',
  `value` varchar(100) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '数据值',
  `dict_status` tinyint(0) NULL DEFAULT NULL COMMENT '部门状态（0启用  1禁用）',
  `type` tinyint(0) NULL DEFAULT NULL COMMENT '字典类型（1字典类型  2字典数据）',
  `sort` int(0) NULL DEFAULT NULL COMMENT '排序',
  `parent_type` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT '0' COMMENT '字典类型的父类型',
  `create_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(255) CHARACTER SET utf8 COLLATE utf8_bin NULL DEFAULT NULL COMMENT '备注信息',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标记（0存在 1删除）',
  PRIMARY KEY (`dict_id`) USING BTREE,
  INDEX `sys_dict_value`(`value`) USING BTREE,
  INDEX `sys_dict_label`(`name`) USING BTREE,
  INDEX `sys_dict_del_flag`(`del_flag`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 162 CHARACTER SET = utf8 COLLATE = utf8_bin COMMENT = '字典表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_dict
-- ----------------------------
INSERT INTO `sys_dict` VALUES (123, '状态', 'status', 0, 1, 1, NULL, 'admin', '2019-06-29 16:20:21', 'admin', '2020-05-31 22:14:09', '1 启用  -1 禁用', 0);
INSERT INTO `sys_dict` VALUES (133, '启用', '0', 0, 2, 0, 'status', 'admin', '2019-06-30 15:41:45', 'admin', '2019-09-05 11:51:14', '', 0);
INSERT INTO `sys_dict` VALUES (134, '禁用', '1', 0, 2, 1, 'status', 'admin', '2019-06-30 15:48:17', 'admin', '2019-06-30 15:48:32', '禁用', 0);
INSERT INTO `sys_dict` VALUES (135, '爱好', 'hobby', 0, 1, 2, NULL, 'admin', '2019-06-30 15:49:16', 'admin', '2020-05-31 22:13:31', 'swim 游泳 playball 打球', 0);
INSERT INTO `sys_dict` VALUES (136, '打球', 'playball', 1, 2, 0, 'hobby', 'admin', '2019-06-30 15:49:43', 'admin', '2020-06-04 22:58:30', '', 0);
INSERT INTO `sys_dict` VALUES (138, '菜单类型', 'menuType', 0, 1, 0, NULL, 'admin', '2019-07-07 12:33:25', 'admin', '2020-05-31 21:35:23', '1字典类型  2字典数据', 0);
INSERT INTO `sys_dict` VALUES (139, '目录', '1', 0, 2, 1, 'menuType', 'admin', '2019-07-07 12:33:46', 'admin', '2019-09-05 09:00:44', '', 0);
INSERT INTO `sys_dict` VALUES (140, '菜单', '2', 0, 2, 2, 'menuType', 'admin', '2019-07-07 12:33:54', 'admin', '2019-09-05 09:00:47', '', 0);
INSERT INTO `sys_dict` VALUES (141, '按钮', '3', 0, 2, 3, 'menuType', 'admin', '2019-07-07 12:33:58', 'admin', '2019-09-05 09:00:52', '', 0);
INSERT INTO `sys_dict` VALUES (142, '游泳', 'swim', 0, 2, 1, 'hobby', 'admin', '2019-07-08 07:01:25', 'admin', '2019-07-19 03:35:44', 'swim 游泳 playball 打球。。。', 0);
INSERT INTO `sys_dict` VALUES (145, '跑步', 'run', 0, 2, 2, 'hobby', 'admin', '2019-08-22 07:11:35', NULL, NULL, '', 0);
INSERT INTO `sys_dict` VALUES (146, '洗澡', 'bathing', 0, 2, 3, 'hobby', 'admin', '2019-08-22 07:11:55', 'admin', '2020-06-04 22:58:26', '', 0);
INSERT INTO `sys_dict` VALUES (148, '执行策略', 'misfirePolicy', 0, 1, 3, NULL, 'admin', '2019-09-05 11:30:06', 'admin', '2020-06-03 22:37:39', '定时任务调度 ”失火策略“', 0);
INSERT INTO `sys_dict` VALUES (149, '立即执行', '1', 0, 2, 1, 'misfirePolicy', 'admin', '2019-09-05 11:30:31', NULL, NULL, '', 0);
INSERT INTO `sys_dict` VALUES (152, '执行一次', '2', 0, 2, 2, 'misfirePolicy', 'admin', '2019-09-05 12:12:55', NULL, NULL, '', 0);
INSERT INTO `sys_dict` VALUES (155, '放弃执行', '3', 0, 2, 3, 'misfirePolicy', 'admin', '2019-09-05 12:13:43', NULL, NULL, '', 0);
INSERT INTO `sys_dict` VALUES (161, '跑步', 'run', 1, 2, 2, 'hobby', 'admin', '2019-08-22 07:11:35', 'admin', '2020-06-04 22:53:22', '', 1);

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
  `menu_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
  `menu_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '菜单名称',
  `parent_id` bigint(0) NULL DEFAULT 0 COMMENT '父菜单ID',
  `sort` int(0) NULL DEFAULT 0 COMMENT '显示顺序',
  `path` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '路由地址',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '组件路径',
  `is_frame` tinyint(0) NULL DEFAULT 1 COMMENT '是否为外链（0是 1否）',
  `menu_type` char(1) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '菜单类型（M目录 C菜单 F按钮）',
  `menu_status` tinyint(0) NULL DEFAULT NULL COMMENT '菜单状态（0启用 1禁用）',
  `visible` tinyint(0) NULL DEFAULT NULL COMMENT '显示状态（0显示 1隐藏）',
  `perms` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限标识',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '#' COMMENT '菜单图标',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`menu_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2036 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '菜单权限表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, '系统管理', 0, 1, 'system', NULL, 1, 'M', 0, NULL, '', 'system', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '系统管理目录');
INSERT INTO `sys_menu` VALUES (2, '系统监控', 0, 2, 'monitor', NULL, 1, 'M', 0, NULL, '', 'monitor', 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '系统监控目录');
INSERT INTO `sys_menu` VALUES (3, '系统工具', 0, 3, 'tool', NULL, 1, 'M', 0, NULL, '', 'tool', 'admin', '2018-03-16 11:33:00', 'admin', '2020-05-31 21:43:31', '系统工具目录');
INSERT INTO `sys_menu` VALUES (100, '用户管理', 1, 1, 'user', 'system/user/index', 1, 'C', 0, NULL, 'system:user:list', 'user', 'admin', '2018-03-16 11:33:00', 'admin', '2020-04-26 10:19:24', '用户管理菜单');
INSERT INTO `sys_menu` VALUES (101, '角色管理', 1, 2, 'role', 'system/role/index', 1, 'C', 0, NULL, 'system:role:list', 'peoples', 'admin', '2018-03-16 11:33:00', 'admin', '2020-04-26 10:19:38', '角色管理菜单');
INSERT INTO `sys_menu` VALUES (102, '菜单管理', 1, 3, 'menu', 'system/menu/index', 1, 'C', 0, NULL, 'system:menu:list', 'tree-table', 'admin', '2018-03-16 11:33:00', 'admin', '2020-04-26 10:18:46', '菜单管理菜单');
INSERT INTO `sys_menu` VALUES (115, '系统接口', 3, 3, 'swagger', 'tool/swagger/index', 1, 'C', 0, NULL, '', 'swagger', 'admin', '2018-03-16 11:33:00', 'admin', '2020-05-31 19:54:54', '系统接口菜单');
INSERT INTO `sys_menu` VALUES (1008, '角色查询', 101, 1, '', '', 1, 'F', 0, NULL, 'system:role:query', '#', 'admin', '2018-03-16 11:33:00', 'admin', '2020-04-22 17:21:06', '');
INSERT INTO `sys_menu` VALUES (2006, '部门管理', 1, 4, 'dept', 'system/dept/index', 1, 'C', 0, NULL, 'system:dept:list', 'tree', 'admin', '2020-04-22 19:08:31', 'admin', '2020-04-26 10:19:53', '');
INSERT INTO `sys_menu` VALUES (2010, '数据监控', 2, 1, 'druid', 'monitor/druid/index', 1, 'C', 0, NULL, NULL, 'bug', 'admin', '2020-04-25 15:59:10', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2011, '用户查询', 100, 1, '', NULL, 1, 'F', 0, NULL, 'system:user:query', '#', '张三', '2020-04-26 10:12:47', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2012, '用户新增', 100, 2, '', NULL, 1, 'F', 0, NULL, 'system:user:add', '#', 'admin', '2020-04-26 10:16:04', 'admin', '2020-04-26 10:16:12', '');
INSERT INTO `sys_menu` VALUES (2013, '用户修改', 100, 3, '', NULL, 1, 'F', 0, NULL, 'system:user:update', '#', 'admin', '2020-04-26 10:16:39', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2014, '用户删除', 100, 4, '', NULL, 1, 'F', 0, NULL, 'system:user:delete', '#', 'admin', '2020-04-26 10:17:16', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2015, '用户导出', 100, 5, '', NULL, 1, 'F', 0, NULL, 'system:user:export', '#', 'admin', '2020-04-26 10:17:35', 'admin', '2020-05-31 22:04:39', '');
INSERT INTO `sys_menu` VALUES (2016, '用户导入', 100, 6, '', NULL, 1, 'F', 0, NULL, 'system:user:import', '#', 'admin', '2020-04-26 10:17:48', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2017, '角色新增', 101, 2, '', NULL, 1, 'F', 0, NULL, 'system:role:add', '#', 'admin', '2020-04-26 10:21:31', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2018, '角色修改', 101, 3, '', NULL, 1, 'F', 0, NULL, 'system:role:update', '#', 'admin', '2020-04-26 10:21:47', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2019, '角色删除', 101, 4, '', NULL, 1, 'F', 0, NULL, 'system:role:delete', '#', 'admin', '2020-04-26 10:22:01', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2020, '角色导出', 101, 5, '', NULL, 1, 'F', 0, NULL, 'system:role:export', '#', 'admin', '2020-04-26 10:23:21', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2021, '菜单新增', 102, 2, '', NULL, 1, 'F', 0, NULL, 'system:menu:add', '#', 'admin', '2020-04-26 10:23:50', 'admin', '2020-04-26 10:24:59', '');
INSERT INTO `sys_menu` VALUES (2022, '菜单修改', 102, 3, '', NULL, 1, 'F', 0, NULL, 'system:menu:update', '#', 'admin', '2020-04-26 10:24:05', 'admin', '2020-04-26 10:25:07', '');
INSERT INTO `sys_menu` VALUES (2023, '部门修改', 2006, 3, '', NULL, 1, 'F', 0, NULL, 'system:dept:update', '#', 'admin', '2020-04-26 10:24:18', 'admin', '2020-04-26 10:26:49', '');
INSERT INTO `sys_menu` VALUES (2024, '菜单查询', 102, 1, '', NULL, 1, 'F', 0, NULL, 'system:menu:query', '#', 'admin', '2020-04-26 10:24:47', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2025, '部门查询', 2006, 1, '', NULL, 1, 'F', 0, NULL, 'system:dept:query', '#', 'admin', '2020-04-26 10:26:25', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2026, '部门新增', 2006, 2, '', NULL, 1, 'F', 0, NULL, 'system:dept:add', '#', 'admin', '2020-04-26 10:27:19', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2027, '部门删除', 2006, 4, '', NULL, 1, 'F', 0, NULL, 'system:dept:delete', '#', 'admin', '2020-04-26 10:27:34', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2028, '字典管理', 1, 5, 'dict/type', 'system/dict/index', 1, 'C', 0, NULL, 'system:dcit:list', 'dict', 'admin', '2020-05-31 16:21:28', 'admin', '2020-05-31 16:24:08', '');
INSERT INTO `sys_menu` VALUES (2029, '字典新增', 2028, 2, '', NULL, 1, 'F', 0, NULL, 'system:dict:add', '#', 'admin', '2020-05-31 21:56:20', 'admin', '2020-05-31 21:57:44', '');
INSERT INTO `sys_menu` VALUES (2030, '字典修改', 2028, 3, '', NULL, 1, 'F', 0, NULL, 'system:dict:update', '#', 'admin', '2020-05-31 21:57:13', 'admin', '2020-05-31 21:57:58', '');
INSERT INTO `sys_menu` VALUES (2031, '字典查询', 2028, 1, '', NULL, 1, 'F', 0, NULL, 'system:dict:query', '#', 'admin', '2020-05-31 21:57:36', '', NULL, '');
INSERT INTO `sys_menu` VALUES (2032, '字典删除', 2028, 4, '', NULL, 1, 'F', 0, NULL, 'system:dict:delete', '#', 'admin', '2020-05-31 22:11:36', 'admin', '2020-05-31 22:12:05', '');
INSERT INTO `sys_menu` VALUES (2033, '商城管理', 0, 1, 'mall', NULL, 1, 'M', 0, NULL, NULL, 'shopping', 'admin', '2020-06-14 11:43:25', 'admin', '2020-06-14 11:44:06', '');
INSERT INTO `sys_menu` VALUES (2034, '栏目管理', 2033, 2, 'channel', 'shop/channel/index', 1, 'C', 0, NULL, NULL, 'time-range', 'admin', '2020-06-15 22:40:12', 'admin', '2020-06-26 21:33:56', '');
INSERT INTO `sys_menu` VALUES (2035, 'banner管理', 2033, 3, 'banner', 'shop/banner/index', 1, 'C', 0, NULL, NULL, 'checkbox', 'admin', '2020-06-15 22:44:20', 'admin', '2020-06-26 21:33:30', '');
INSERT INTO `sys_menu` VALUES (2036, '分类管理', 2033, 1, 'category', 'shop/category/index', 1, 'C', 0, NULL, NULL, 'checkbox', 'admin', '2020-06-26 21:32:50', 'admin', '2020-06-26 21:33:51', '');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `role_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色名称',
  `role_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色权限字符串',
  `sort` int(0) NOT NULL COMMENT '显示顺序',
  `role_status` tinyint(0) NOT NULL COMMENT '角色状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '管理员', 'admin', 1, 0, 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '管理员', 0);
INSERT INTO `sys_role` VALUES (2, '普通角色', 'common', 2, 0, 'admin', '2018-03-16 11:33:00', 'admin', '2018-03-16 11:33:00', '普通角色', 0);
INSERT INTO `sys_role` VALUES (3346, '测试人员', 'test', 3, 0, 'admin', '2020-04-20 12:56:26', 'admin', NULL, '<script>alert(123)</script>', 0);
INSERT INTO `sys_role` VALUES (3347, '测试员2', 'test2', 4, 0, 'admin', '2020-04-20 12:57:51', 'admin', NULL, 'test', 1);
INSERT INTO `sys_role` VALUES (3420, 'ttt', 'ttt', 0, 0, 'admin', '2020-06-26 19:56:34', '', NULL, '', 1);

-- ----------------------------
-- Table structure for sys_role_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_menu`;
CREATE TABLE `sys_role_menu`  (
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  `menu_id` bigint(0) NOT NULL COMMENT '菜单ID',
  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色和菜单关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_menu
-- ----------------------------
INSERT INTO `sys_role_menu` VALUES (2, 1);
INSERT INTO `sys_role_menu` VALUES (2, 3);
INSERT INTO `sys_role_menu` VALUES (2, 100);
INSERT INTO `sys_role_menu` VALUES (2, 101);
INSERT INTO `sys_role_menu` VALUES (2, 102);
INSERT INTO `sys_role_menu` VALUES (2, 115);
INSERT INTO `sys_role_menu` VALUES (2, 1008);
INSERT INTO `sys_role_menu` VALUES (2, 2006);
INSERT INTO `sys_role_menu` VALUES (2, 2011);
INSERT INTO `sys_role_menu` VALUES (2, 2012);
INSERT INTO `sys_role_menu` VALUES (2, 2013);
INSERT INTO `sys_role_menu` VALUES (2, 2014);
INSERT INTO `sys_role_menu` VALUES (2, 2015);
INSERT INTO `sys_role_menu` VALUES (2, 2016);
INSERT INTO `sys_role_menu` VALUES (2, 2017);
INSERT INTO `sys_role_menu` VALUES (2, 2018);
INSERT INTO `sys_role_menu` VALUES (2, 2019);
INSERT INTO `sys_role_menu` VALUES (2, 2020);
INSERT INTO `sys_role_menu` VALUES (2, 2021);
INSERT INTO `sys_role_menu` VALUES (2, 2022);
INSERT INTO `sys_role_menu` VALUES (2, 2023);
INSERT INTO `sys_role_menu` VALUES (2, 2024);
INSERT INTO `sys_role_menu` VALUES (2, 2025);
INSERT INTO `sys_role_menu` VALUES (2, 2026);
INSERT INTO `sys_role_menu` VALUES (2, 2027);
INSERT INTO `sys_role_menu` VALUES (2, 2028);
INSERT INTO `sys_role_menu` VALUES (3346, 2);
INSERT INTO `sys_role_menu` VALUES (3346, 2010);
INSERT INTO `sys_role_menu` VALUES (3347, 1);
INSERT INTO `sys_role_menu` VALUES (3347, 101);
INSERT INTO `sys_role_menu` VALUES (3347, 1008);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `user_id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `dept_id` bigint(0) NULL DEFAULT NULL COMMENT '部门ID',
  `user_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户账号',
  `nick_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户昵称',
  `email` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '用户邮箱',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '手机号码',
  `sex` tinyint(0) NULL DEFAULT 0 COMMENT '用户性别（0男 1女 2未知）',
  `avatar` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '头像地址',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '密码',
  `user_status` tinyint(0) NULL DEFAULT 0 COMMENT '帐号状态（0正常 1停用）',
  `create_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '创建者',
  `create_time` datetime(0) NULL DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '' COMMENT '更新者',
  `update_time` datetime(0) NULL DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `del_flag` tinyint(0) NULL DEFAULT 0 COMMENT '删除标志（0代表存在 1代表删除）',
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 108 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 103, 'admin', 'wayn', 'ry@163.com', '15888888888', 1, 'http://cdn.wayn.xin/7900937bea1d0a20076ea57c12b19f67.jpeg', '$2a$10$2d9re.CKbFGiqVtxI4F4B.3A6PlZIX22LGHDqhkC9SFx0VsqYvaUC', 0, 'admin', '2018-03-16 11:33:00', 'ry', '2018-03-16 11:33:00', '管理员', 0);
INSERT INTO `sys_user` VALUES (2, 105, '张三', 'wayn', 'ry@qq.com', '15666666666', 1, 'http://localhost:81/upload/avatar/2020/04/26/6c09c1be4b36ffa99afe2bcb3075bc89.jpeg', '$2a$10$2d9re.CKbFGiqVtxI4F4B.3A6PlZIX22LGHDqhkC9SFx0VsqYvaUC', 0, 'admin', '2018-03-16 11:33:00', 'admin', '2020-06-07 13:31:13', '测试员', 0);
INSERT INTO `sys_user` VALUES (102, 102, '测试人员', 'test', '1234@qq.com', '1566666665', 0, '', '$2a$10$dVEI7H0pU8i7h.SxdzijNOzNpQ/LJi37JjanvFy6ErwjI9o5WcAf6', 0, 'admin', '2020-04-25 10:37:38', 'admin', '2020-04-25 12:00:42', 'test', 1);
INSERT INTO `sys_user` VALUES (105, 101, '1234', '的发射点', '2342', '1234', 0, '', '$2a$10$zu18q9BAPsi9GdZPiEtlP.Ov.2hbRQCVMRyEA04q6uByvZivZ.Epq', 0, 'admin', '2020-04-25 15:12:13', '', NULL, 'remark', 1);
INSERT INTO `sys_user` VALUES (106, 101, '1234', '的发射点', 'wer@qq.com', '1234', 0, '', '$2a$10$SX/3gLWJ2rAPZZouhv6CdOGy/omfQyOpu6E7T7TJQ8XHYt5yjg0qm', 0, 'admin', '2020-04-25 15:20:17', '1234', '2020-04-25 15:37:17', 'remark', 1);
INSERT INTO `sys_user` VALUES (107, 101, 'wayn', 'erwe', '23424@qq.com', '123423343', 0, '', '$2a$10$YJ5yWNUpOy5YIpFmiimz7.jWsamdpt0qEo4g9kJPmSL.tkLhpRSZa', 0, 'admin', '2020-04-26 10:09:54', 'wayn', '2020-04-26 10:34:21', 'test', 1);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `user_id` bigint(0) NOT NULL COMMENT '用户ID',
  `role_id` bigint(0) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户和角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1);
INSERT INTO `sys_user_role` VALUES (2, 2);
INSERT INTO `sys_user_role` VALUES (2, 3346);
INSERT INTO `sys_user_role` VALUES (100, 3346);
INSERT INTO `sys_user_role` VALUES (101, 3346);
INSERT INTO `sys_user_role` VALUES (102, 2);
INSERT INTO `sys_user_role` VALUES (102, 3346);
INSERT INTO `sys_user_role` VALUES (106, 2);
INSERT INTO `sys_user_role` VALUES (106, 3346);
INSERT INTO `sys_user_role` VALUES (107, 2);

SET FOREIGN_KEY_CHECKS = 1;
