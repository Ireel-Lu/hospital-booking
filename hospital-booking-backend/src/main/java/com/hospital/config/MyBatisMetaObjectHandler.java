package com.hospital.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 公共字段自动填充
 * 配合实体上的 @TableField(fill = FieldFill.INSERT / INSERT_UPDATE) 使用，
 * 插入时填充 createTime、updateTime，更新时刷新 updateTime
 */
@Component
public class MyBatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //更新时强制刷新，避免实体从库里查出来带上旧值后 strictUpdateFill 跳过填充
        if (metaObject.hasSetter("updateTime")) {
            metaObject.setValue("updateTime", LocalDateTime.now());
        }
    }
}
