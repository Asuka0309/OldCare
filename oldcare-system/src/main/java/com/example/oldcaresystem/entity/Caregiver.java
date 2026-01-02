package com.example.oldcaresystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 护工信息实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("caregivers")
public class Caregiver {
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID */
    @TableField("user_id")
    private Long userId;

    /** 护工姓名 */
    private String name;

    /** 年龄 */
    private Integer age;

    /** 性别：男/女 */
    private String gender;

    /** 身份证号 */
    private String idCard;

    /** 电话号码 */
    private String phone;

    /** 所属机构或公司名称 */
    @TableField("company_name")
    private String companyName;

    /** 资质认证信息 */
    private String certification;

    /** 工作经验年数 */
    private Integer experienceYears;

    /** 状态：active-在职、inactive-离职 */
    private String status;

    /** 评分（5分制） */
    private Double rating;

    /** 创建时间 */
    private LocalDateTime createdTime;

    /** 更新时间 */
    private LocalDateTime updatedTime;
}
