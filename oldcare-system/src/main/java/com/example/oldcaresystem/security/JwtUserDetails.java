package com.example.oldcaresystem.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * JWT 用户详情类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtUserDetails {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 用户角色 */
    private String role;
}
