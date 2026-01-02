package com.example.oldcaresystem.util;

/**
 * API响应类（ResponseUtil的别名）
 */
public class ApiResponse<T> extends ResponseUtil<T> {
    public ApiResponse() {
    }

    public ApiResponse(Integer code, String message, T data) {
        super(code, message, data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(0, "操作成功", null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(0, "操作成功", data);
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(0, message, null);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(0, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }

    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null);
    }
}
