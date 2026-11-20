ALTER TABLE medical_record
    ADD COLUMN image_urls TEXT NULL COMMENT '病历图片 OSS 地址（JSON 数组）' AFTER advice;
