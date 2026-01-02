package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 老人信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("elderly_info")
public class ElderlyInfo {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID */
    @TableField("user_id")
    private Long userId;

    /** 老人姓名 */
    private String name;

    /** 年龄 */
    private Integer age;

    /** 性别：男/女 */
    private String gender;

    /** 身份证号 */
    private String idCard;

    /** 电话号码 */
    private String phone;

    /** 住址 */
    private String address;

    /** 紧急联系人姓名 */
    private String emergencyContact;

    /** 紧急联系人电话 */
    private String emergencyPhone;

    /** 健康状况描述 */
    private String healthStatus;

    /** 病史 */
    private String medicalHistory;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
