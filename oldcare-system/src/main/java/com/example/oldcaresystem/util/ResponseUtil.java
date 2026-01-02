package com.example.oldcaresystem.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应工具类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUtil<T> {

    /** 响应码：0-成功，其他-失败 */
    private Integer code;

    /** 响应消息 */
    private String message;

    /** 响应数据 */
    private T data;

    /**
     * 成功响应（无数据）
     */
    public static <T> ResponseUtil<T> success() {
        return success("操作成功", null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> ResponseUtil<T> success(T data) {
        return success("操作成功", data);
    }

    /**
     * 成功响应（带消息和数据）
     */
    public static <T> ResponseUtil<T> success(String message, T data) {
        return new ResponseUtil<>(0, message, data);
    }

    /**
     * 失败响应
     */
    public static <T> ResponseUtil<T> error(Integer code, String message) {
        return new ResponseUtil<>(code, message, null);
    }

    /**
     * 失败响应（默认码500）
     */
    public static <T> ResponseUtil<T> error(String message) {
        return error(500, message);
    }

    /**
     * 参数错误响应
     */
    public static <T> ResponseUtil<T> paramError(String message) {
        return error(400, message);
    }

    /**
     * 未认证响应
     */
    public static <T> ResponseUtil<T> unauthorized(String message) {
        return error(401, message);
    }

    /**
     * 无权限响应
     */
    public static <T> ResponseUtil<T> forbidden(String message) {
        return error(403, message);
    }
}
