package com.hospital.booking.entity;

import lombok.Data;

@Data
public class Result {
    private Integer code;
    private String massage;
    private Object data;

    /*
    * 成功响应（无数据）
    * @return 成功结果
    * */
    public static Result success() {
        Result result = new Result();
        result.setCode(200);
        result.setMassage("操作成功");
        return result;
    }

    /*
     * 成功响应（有数据）
     * @return 成功结果
     * */
    public static Result success(Object data) {
        Result result = success();
        result.setData(data);
        return result;
    }

    /*
     * 错误响应（默认500）
     * @param message 错误信息
     * @return 错误结果
     * */
    public static Result error(String message) {
        Result result = new Result();
        result.setCode(500);
        result.setMassage(message);
        return result;
    }

    /*
     * 错误响应（自定义状态码）
     * @param code 状态码
     * @param message 错误信息
     * @return 错误结果
     * */
    public static Result error(Integer code,String message) {
        Result result = new Result();
        result.setCode(code);
        result.setMassage(message);
        return result;
    }
}
