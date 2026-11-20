-- =============================================================
-- 医院预约挂号系统 - 演示数据
-- 说明：
--   1. 脚本可重复执行，使用固定 ID 段（科室 9001+、医生 9001+、患者 9011+），
--      执行前会先清理上一次生成的演示数据，不会影响手工创建的数据。
--   2. 所有账号初始密码均为 123456（MD5: e10adc3949ba59abbe56e057f20f883e）。
--   3. 号源与预约日期使用 CURDATE() 生成，随时执行都能看到"今天"的数据。
-- =============================================================
SET NAMES utf8mb4;

START TRANSACTION;

-- ---------- 0. 清理孤儿数据与上一次的演示数据 ----------
DELETE FROM prescription WHERE medical_record_id NOT IN (SELECT id FROM medical_record)
                           OR medical_record_id BETWEEN 9001 AND 9099;
DELETE FROM patient_queue WHERE id BETWEEN 9001 AND 9099 OR doctor_id BETWEEN 9001 AND 9006;
DELETE FROM medical_record WHERE id BETWEEN 9001 AND 9099;
DELETE FROM appointment WHERE id BETWEEN 9001 AND 9099;
DELETE FROM source_setting WHERE doctor_id BETWEEN 9001 AND 9006
                              OR doctor_id NOT IN (SELECT id FROM doctor);
DELETE FROM patient_profile WHERE id BETWEEN 9001 AND 9099 OR user_id BETWEEN 9011 AND 9016;
DELETE FROM doctor WHERE id BETWEEN 9001 AND 9006;
DELETE FROM user WHERE id BETWEEN 9001 AND 9099;
DELETE FROM drug WHERE id BETWEEN 9001 AND 9099;
DELETE FROM department WHERE id BETWEEN 9001 AND 9099;
DELETE FROM operation_log WHERE id BETWEEN 9001 AND 9099;

-- ---------- 1. 科室 ----------
INSERT INTO department (id, name, code, description, location, phone, sort_order, status, create_time) VALUES
(9001, '内科', 'NK', '呼吸、消化、心血管等内科常见疾病诊治', '门诊楼 2 层 A 区', '010-88001001', 1, 1, NOW()),
(9002, '外科', 'WK', '普外科、微创外科及外伤处理', '门诊楼 3 层 B 区', '010-88001002', 2, 1, NOW()),
(9003, '儿科', 'EK', '0-14 岁儿童常见病、多发病诊治', '门诊楼 1 层 C 区', '010-88001003', 3, 1, NOW()),
(9004, '妇产科', 'FCK', '妇科炎症、孕期保健及产前检查', '住院楼 2 层', '010-88001004', 4, 1, NOW()),
(9005, '骨科', 'GK', '颈椎腰椎疾病、关节疾病及运动损伤', '门诊楼 3 层 A 区', '010-88001005', 5, 1, NOW()),
(9006, '眼科', 'YK', '近视防控、白内障、眼底疾病诊治', '门诊楼 4 层', '010-88001006', 6, 1, NOW()),
(9007, '耳鼻喉科', 'EBH', '鼻炎、咽喉炎、中耳炎等疾病诊治', '门诊楼 4 层', '010-88001007', 7, 1, NOW()),
(9008, '皮肤科', 'PFK', '湿疹、皮炎、痤疮等皮肤疾病诊治', '门诊楼 2 层 B 区', '010-88001008', 8, 1, NOW());

-- ---------- 2. 用户（6 位医生 + 6 位患者） ----------
INSERT INTO user (id, username, gender, pwd, real_name, phone, email, role, status, avatar, create_time, update_time) VALUES
(9001, 'doctor01', '男', 'e10adc3949ba59abbe56e057f20f883e', '张伟', '13800000001', 'zhangwei@hospital.com', 'doctor', 1, NULL, NOW(), NOW()),
(9002, 'doctor02', '女', 'e10adc3949ba59abbe56e057f20f883e', '李娜', '13800000002', 'lina@hospital.com',    'doctor', 1, NULL, NOW(), NOW()),
(9003, 'doctor03', '男', 'e10adc3949ba59abbe56e057f20f883e', '王强', '13800000003', 'wangqiang@hospital.com','doctor', 1, NULL, NOW(), NOW()),
(9004, 'doctor04', '女', 'e10adc3949ba59abbe56e057f20f883e', '刘敏', '13800000004', 'liumin@hospital.com',   'doctor', 1, NULL, NOW(), NOW()),
(9005, 'doctor05', '男', 'e10adc3949ba59abbe56e057f20f883e', '陈杰', '13800000005', 'chenjie@hospital.com',  'doctor', 1, NULL, NOW(), NOW()),
(9006, 'doctor06', '女', 'e10adc3949ba59abbe56e057f20f883e', '赵丽', '13800000006', 'zhaoli@hospital.com',   'doctor', 1, NULL, NOW(), NOW()),
(9011, 'patient01', '男', 'e10adc3949ba59abbe56e057f20f883e', '王小明', '13900000001', 'wangxiaoming@qq.com', 'patient', 1, NULL, NOW(), NOW()),
(9012, 'patient02', '女', 'e10adc3949ba59abbe56e057f20f883e', '李小红', '13900000002', 'lixiaohong@qq.com',   'patient', 1, NULL, NOW(), NOW()),
(9013, 'patient03', '男', 'e10adc3949ba59abbe56e057f20f883e', '张强',   '13900000003', 'zhangqiang@qq.com',   'patient', 1, NULL, NOW(), NOW()),
(9014, 'patient04', '男', 'e10adc3949ba59abbe56e057f20f883e', '刘洋',   '13900000004', 'liuyang@qq.com',      'patient', 1, NULL, NOW(), NOW()),
(9015, 'patient05', '女', 'e10adc3949ba59abbe56e057f20f883e', '陈静',   '13900000005', 'chenjing@qq.com',     'patient', 1, NULL, NOW(), NOW()),
(9016, 'patient06', '女', 'e10adc3949ba59abbe56e057f20f883e', '赵敏',   '13900000006', 'zhaomin@qq.com',      'patient', 1, NULL, NOW(), NOW());

-- ---------- 3. 医生明细 ----------
INSERT INTO doctor (id, user_id, employee_no, name, title, specialty, introduction, department_id, consultation_fee, avatar, status, create_time, update_time) VALUES
(9001, 9001, 'DOC9001', '张伟', '主任医师',   '高血压、糖尿病、冠心病等慢性病综合管理', '从事内科临床工作 25 年，擅长慢性病长期管理，发表核心期刊论文 30 余篇。', 9001, 50.00, NULL, 1, NOW(), NOW()),
(9002, 9002, 'DOC9002', '李娜', '副主任医师', '慢性咳嗽、哮喘、肺部感染诊治',         '呼吸内科副主任医师，长期从事呼吸系统疾病诊疗工作。',               9001, 30.00, NULL, 1, NOW(), NOW()),
(9003, 9003, 'DOC9003', '王强', '主治医师',   '阑尾炎、疝气、体表肿物等普外科手术',   '普外科主治医师，擅长腹腔镜微创手术。',                           9002, 30.00, NULL, 1, NOW(), NOW()),
(9004, 9004, 'DOC9004', '刘敏', '主任医师',   '妇科炎症、孕期保健、产前检查',         '妇产科主任医师，从事妇产科临床工作 20 年。',                     9004, 50.00, NULL, 1, NOW(), NOW()),
(9005, 9005, 'DOC9005', '陈杰', '主治医师',   '颈椎病、腰椎间盘突出、膝关节损伤',     '骨科主治医师，擅长运动损伤的微创治疗与康复指导。',               9005, 30.00, NULL, 1, NOW(), NOW()),
(9006, 9006, 'DOC9006', '赵丽', '副主任医师', '小儿呼吸道感染、小儿腹泻、营养指导',   '儿科副主任医师，耐心细致，深受家长信赖。',                       9003, 40.00, NULL, 1, NOW(), NOW());

-- ---------- 4. 患者明细 ----------
INSERT INTO patient_profile (id, user_id, gender, birth_date, id_card, address, emergency_contact, emergency_contact_phone, create_time, update_time) VALUES
(9001, 9011, '男', '1990-03-15', '110101199003150011', '北京市朝阳区建国路 88 号 3 单元 502', '王建国', '13911110001', NOW(), NOW()),
(9002, 9012, '女', '1995-07-22', '110101199507220022', '北京市海淀区中关村大街 12 号',       '李文',   '13911110002', NOW(), NOW()),
(9003, 9013, '男', '1988-11-08', '110101198811080033', '北京市西城区西直门南大街 5 号',       '张丽',   '13911110003', NOW(), NOW()),
(9004, 9014, '男', '2000-01-30', '110101200001300044', '北京市东城区安定门外大街 21 号',      '刘芳',   '13911110004', NOW(), NOW()),
(9005, 9015, '女', '1993-05-19', '110101199305190055', '北京市丰台区方庄芳城园 8 号',         '陈刚',   '13911110005', NOW(), NOW()),
(9006, 9016, '女', '1998-09-03', '110101199809030066', '北京市石景山区鲁谷路 33 号',          '赵军',   '13911110006', NOW(), NOW());

-- 补全注册较早的患者资料（用户 4、5、11）
UPDATE user SET gender = '男' WHERE id IN (4, 5, 11);
UPDATE patient_profile SET gender = '男',
    birth_date = IFNULL(birth_date, '1995-03-12'),
    id_card = IFNULL(id_card, '110101199503120077'),
    address = IFNULL(address, '北京市昌平区回龙观西大街 9 号'),
    emergency_contact = IFNULL(emergency_contact, '章鱼哥家属'),
    emergency_contact_phone = IFNULL(emergency_contact_phone, '13911110007'),
    update_time = NOW()
WHERE user_id = 4;
UPDATE patient_profile SET gender = '男',
    birth_date = IFNULL(birth_date, '1992-08-20'),
    id_card = IFNULL(id_card, '110101199208200088'),
    address = IFNULL(address, '北京市通州区新华大街 100 号'),
    emergency_contact = IFNULL(emergency_contact, '海绵家属'),
    emergency_contact_phone = IFNULL(emergency_contact_phone, '13911110008'),
    update_time = NOW()
WHERE user_id = 5;
UPDATE patient_profile SET gender = '男',
    birth_date = IFNULL(birth_date, '1986-12-01'),
    id_card = IFNULL(id_card, '110101198612010099'),
    address = IFNULL(address, '北京市大兴区黄村西大街 6 号'),
    emergency_contact = IFNULL(emergency_contact, '海蟹家属'),
    emergency_contact_phone = IFNULL(emergency_contact_phone, '13911110009'),
    update_time = NOW()
WHERE user_id = 11;

-- ---------- 5. 药品 ----------
INSERT INTO drug (id, name, specification, manufacturer, price, stock, unit, category, description, status, create_time, update_time) VALUES
(9001, '阿莫西林胶囊',     '0.25g*24 粒/盒', '华北制药股份有限公司', 12.50, 500, '盒', '抗生素',   '用于敏感菌所致的呼吸道、泌尿道感染', 1, NOW(), NOW()),
(9002, '布洛芬缓释胶囊',   '0.3g*20 粒/盒',  '中美天津史克制药',     18.00, 320, '盒', '解热镇痛', '用于缓解轻至中度疼痛及发热',       1, NOW(), NOW()),
(9003, '复方氨酚烷胺片',   '12 片/盒',       '哈药集团制药六厂',     9.90,  400, '盒', '感冒用药', '用于缓解普通感冒及流行性感冒症状', 1, NOW(), NOW()),
(9004, '头孢克肟分散片',   '0.1g*12 片/盒',  '广州白云山制药',       26.80, 260, '盒', '抗生素',   '用于敏感菌引起的支气管炎、肺炎',   1, NOW(), NOW()),
(9005, '蒙脱石散',         '3g*10 袋/盒',    '博福-益普生制药',      21.00, 180, '盒', '消化系统', '用于成人及儿童急、慢性腹泻',       1, NOW(), NOW()),
(9006, '奥美拉唑肠溶胶囊', '20mg*14 粒/盒',  '阿斯利康制药',         32.00, 210, '盒', '消化系统', '用于胃溃疡、十二指肠溃疡及反流性食管炎', 1, NOW(), NOW()),
(9007, '硝苯地平缓释片',   '20mg*30 片/盒',  '拜耳医药保健',         28.50, 150, '盒', '心血管',   '用于高血压、心绞痛长期治疗',       1, NOW(), NOW()),
(9008, '二甲双胍片',       '0.5g*48 片/盒',  '中美上海施贵宝制药',   15.60, 240, '盒', '降糖药',   '用于 2 型糖尿病患者的血糖控制',   1, NOW(), NOW()),
(9009, '维生素C片',        '100mg*100 片',   '华中药业股份有限公司', 6.50,  600, '瓶', '维生素',   '用于预防坏血病及维生素 C 缺乏症', 1, NOW(), NOW()),
(9010, '氯雷他定片',       '10mg*10 片/盒',  '上海先灵葆雅制药',     19.80, 300, '盒', '抗过敏',   '用于缓解过敏性鼻炎、荨麻疹症状',   1, NOW(), NOW());

-- ---------- 6. 号源（6 位医生 × 前后 7 天 × 7 个时段） ----------
INSERT INTO source_setting (doctor_id, `date`, time_slot, total_quantity, remaining_quantity, status, create_time, update_time)
SELECT d.id,
       DATE_ADD(CURDATE(), INTERVAL (n.n - 7) DAY),
       s.slot,
       5, 5, 1, NOW(), NOW()
FROM (SELECT 9001 AS id UNION ALL SELECT 9002 UNION ALL SELECT 9003
      UNION ALL SELECT 9004 UNION ALL SELECT 9005 UNION ALL SELECT 9006) d
CROSS JOIN (SELECT 0 AS n UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3
            UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
            UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
            UNION ALL SELECT 10 UNION ALL SELECT 11 UNION ALL SELECT 12
            UNION ALL SELECT 13 UNION ALL SELECT 14) n
CROSS JOIN (SELECT '08:00-09:00' AS slot UNION ALL SELECT '09:00-10:00'
            UNION ALL SELECT '10:00-11:00' UNION ALL SELECT '11:00-12:00'
            UNION ALL SELECT '14:00-15:00' UNION ALL SELECT '15:00-16:00'
            UNION ALL SELECT '16:00-17:00') s;

-- ---------- 7. 预约 ----------
INSERT INTO appointment (id, appointment_no, patient_id, doctor_id, source_setting_id, appointment_date, time_slot, symptoms, status, queue_number, amount, pay_time, cancel_time, create_time) VALUES
(9001, 'A2026001', 9011, 9001, (SELECT id FROM source_setting WHERE doctor_id = 9001 AND `date` = CURDATE() AND time_slot = '08:00-09:00' LIMIT 1), CURDATE(), '08:00-09:00', '反复头痛三天，伴轻微发热', 'calling',   1, 50.00, NOW(), NULL, NOW()),
(9002, 'A2026002', 9012, 9001, (SELECT id FROM source_setting WHERE doctor_id = 9001 AND `date` = CURDATE() AND time_slot = '09:00-10:00' LIMIT 1), CURDATE(), '09:00-10:00', '血压控制不佳，晨起头晕',   'paid',      2, 50.00, NOW(), NULL, NOW()),
(9003, 'A2026003', 9013, 9001, (SELECT id FROM source_setting WHERE doctor_id = 9001 AND `date` = CURDATE() AND time_slot = '10:00-11:00' LIMIT 1), CURDATE(), '10:00-11:00', '口干多饮一周，复查血糖',   'paid',      3, 50.00, NOW(), NULL, NOW()),
(9004, 'A2026004', 9014, 9002, (SELECT id FROM source_setting WHERE doctor_id = 9002 AND `date` = CURDATE() AND time_slot = '08:00-09:00' LIMIT 1), CURDATE(), '08:00-09:00', '咳嗽两周，夜间加重',       'paid',      1, 30.00, NOW(), NULL, NOW()),
(9005, 'A2026005', 9015, 9003, (SELECT id FROM source_setting WHERE doctor_id = 9003 AND `date` = CURDATE() AND time_slot = '14:00-15:00' LIMIT 1), CURDATE(), '14:00-15:00', '右下腹间断疼痛两天',       'paid',      1, 30.00, NOW(), NULL, NOW()),
(9006, 'A2026006', 9016, 9004, (SELECT id FROM source_setting WHERE doctor_id = 9004 AND `date` = CURDATE() AND time_slot = '15:00-16:00' LIMIT 1), CURDATE(), '15:00-16:00', '孕期常规产检',             'pending',   NULL, 50.00, NULL, NULL, NOW()),
(9007, 'A2026007', 9011, 9005, (SELECT id FROM source_setting WHERE doctor_id = 9005 AND `date` = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND time_slot = '09:00-10:00' LIMIT 1), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '09:00-10:00', '腰部酸痛一周，久坐后加重', 'paid', NULL, 30.00, NOW(), NULL, NOW()),
(9008, 'A2026008', 9012, 9006, (SELECT id FROM source_setting WHERE doctor_id = 9006 AND `date` = DATE_SUB(CURDATE(), INTERVAL 3 DAY) AND time_slot = '10:00-11:00' LIMIT 1), DATE_SUB(CURDATE(), INTERVAL 3 DAY), '10:00-11:00', '咳嗽流涕两天，食欲差', 'completed', 1, 40.00, NOW(), NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9009, 'A2026009', 9013, 9001, (SELECT id FROM source_setting WHERE doctor_id = 9001 AND `date` = DATE_SUB(CURDATE(), INTERVAL 5 DAY) AND time_slot = '08:00-09:00' LIMIT 1), DATE_SUB(CURDATE(), INTERVAL 5 DAY), '08:00-09:00', '头晕伴颈肩僵硬', 'completed', 1, 50.00, NOW(), NULL, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(9010, 'A2026010', 9014, 9002, (SELECT id FROM source_setting WHERE doctor_id = 9002 AND `date` = DATE_SUB(CURDATE(), INTERVAL 2 DAY) AND time_slot = '14:00-15:00' LIMIT 1), DATE_SUB(CURDATE(), INTERVAL 2 DAY), '14:00-15:00', '体检咨询', 'cancelled', NULL, 30.00, NOW(), DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9011, 'A2026011', 9015, 9003, (SELECT id FROM source_setting WHERE doctor_id = 9003 AND `date` = DATE_ADD(CURDATE(), INTERVAL 2 DAY) AND time_slot = '16:00-17:00' LIMIT 1), DATE_ADD(CURDATE(), INTERVAL 2 DAY), '16:00-17:00', '体表肿物复查', 'pending', NULL, 30.00, NULL, NULL, NOW()),
(9012, 'A2026012', 9016, 9004, (SELECT id FROM source_setting WHERE doctor_id = 9004 AND `date` = DATE_ADD(CURDATE(), INTERVAL 1 DAY) AND time_slot = '08:00-09:00' LIMIT 1), DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00-09:00', '妇科炎症复查', 'paid', NULL, 50.00, NOW(), NULL, NOW()),
(9013, 'A2026013', 9011, 9001, (SELECT id FROM source_setting WHERE doctor_id = 9001 AND `date` = CURDATE() AND time_slot = '11:00-12:00' LIMIT 1), CURDATE(), '11:00-12:00', '糖尿病复诊取药', 'paid', 4, 50.00, NOW(), NULL, NOW()),
(9014, 'A2026014', 9016, 9002, (SELECT id FROM source_setting WHERE doctor_id = 9002 AND `date` = CURDATE() AND time_slot = '09:00-10:00' LIMIT 1), CURDATE(), '09:00-10:00', '哮喘随访，近期偶有胸闷', 'paid', 2, 30.00, NOW(), NULL, NOW());

-- 按实际预约量回写剩余号源
UPDATE source_setting s
SET s.remaining_quantity = GREATEST(
        s.total_quantity - (SELECT COUNT(*) FROM appointment a
                            WHERE a.source_setting_id = s.id AND a.status <> 'cancelled'), 0)
WHERE s.doctor_id BETWEEN 9001 AND 9006;

-- ---------- 8. 候诊队列（仅今日已支付/叫号中的预约） ----------
INSERT INTO patient_queue (id, appointment_id, doctor_id, queue_number, status, call_time, visit_time, create_time) VALUES
(9001, 9001, 9001, 1, 'calling', NOW(), NULL, NOW()),
(9002, 9002, 9001, 2, 'waiting', NULL, NULL, NOW()),
(9003, 9003, 9001, 3, 'waiting', NULL, NULL, NOW()),
(9004, 9013, 9001, 4, 'waiting', NULL, NULL, NOW()),
(9005, 9004, 9002, 1, 'waiting', NULL, NULL, NOW()),
(9006, 9014, 9002, 2, 'waiting', NULL, NULL, NOW()),
(9007, 9005, 9003, 1, 'waiting', NULL, NULL, NOW());

-- ---------- 9. 病历与处方 ----------
INSERT INTO medical_record (id, appointment_id, patient_id, doctor_id, diagnosis, symptoms, advice, visit_time, status, create_time) VALUES
(9001, 9008, 9012, 9006, '急性上呼吸道感染', '咳嗽、流涕两天，咽部轻度充血，体温 37.6℃',
 '多饮温水，清淡饮食；按时服药，三天后复诊；如持续高热请及时就医。', DATE_SUB(NOW(), INTERVAL 3 DAY), 1, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9002, 9009, 9013, 9001, '原发性高血压 2 级', '头晕伴颈肩僵硬，血压 158/96mmHg',
 '低盐低脂饮食，每日监测血压；规律服用降压药；两周后门诊复查。', DATE_SUB(NOW(), INTERVAL 5 DAY), 1, DATE_SUB(NOW(), INTERVAL 5 DAY));

INSERT INTO prescription (id, medical_record_id, medicine_name, specification, dosage, quantity, remark, create_time) VALUES
(9001, 9001, '阿莫西林胶囊',     '0.25g*24 粒/盒', '每日三次，每次 2 粒，饭后服用', 1, '连服 5 天', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9002, 9001, '复方氨酚烷胺片',   '12 片/盒',       '每日两次，每次 1 片',           1, '连服 3 天', DATE_SUB(NOW(), INTERVAL 3 DAY)),
(9003, 9002, '硝苯地平缓释片',   '20mg*30 片/盒',  '每日一次，每次 1 片，晨起服用', 1, '长期服用',  DATE_SUB(NOW(), INTERVAL 5 DAY)),
(9004, 9002, '维生素C片',        '100mg*100 片',   '每日三次，每次 1 片',           1, '',          DATE_SUB(NOW(), INTERVAL 5 DAY));

-- ---------- 10. 操作日志 ----------
INSERT INTO operation_log (id, operator_id, operator_name, operation_type, target_type, target_id, content, ip_address, create_time) VALUES
(9001, 1,    'admin01',  'ADD_DOCTOR',        'Doctor',      9001, '新增医生：张伟（内科 主任医师）',       '127.0.0.1', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9002, 1,    'admin01',  'ADD_DEPARTMENT',    'Department',  9001, '新增科室：内科',                        '127.0.0.1', DATE_SUB(NOW(), INTERVAL 2 DAY)),
(9003, 9011, '王小明',    'CREATE_APPOINTMENT','Appointment', 9001, '创建预约：A2026001',                    '127.0.0.1', NOW()),
(9004, 9011, '王小明',    'PAY_APPOINTMENT',   'Appointment', 9001, '支付预约：A2026001',                    '127.0.0.1', NOW()),
(9005, 9013, '张强',      'CREATE_APPOINTMENT','Appointment', 9003, '创建预约：A2026003',                    '127.0.0.1', NOW());

COMMIT;
