package com.hospital.booking.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 */
@Documented
//作用：执行 javadoc 生成 API 文档的时候，把这个注解显示进文档里。
@Target({ElementType.METHOD, ElementType.TYPE})
/*- `ElementType.TYPE`：可以写在 **类、接口、枚举**上面（类级别）
        - `ElementType.METHOD`：可以写在 **方法**上面（方法级别）*/
@Retention(RetentionPolicy.RUNTIME) // 必须RUNTIME，AOP运行时读取注解
public @interface Log {
    // 操作类型：例如：新增挂号、修改患者信息
    String value() default "";
    // 操作目标类型
    String targetType() default "";
    // 是否保存请求参数
    boolean saveRequestParam() default true;
    // 是否保存返回结果
    boolean saveResponseResult() default true;
}
