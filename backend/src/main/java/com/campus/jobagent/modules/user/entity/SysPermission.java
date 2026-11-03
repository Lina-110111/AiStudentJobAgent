package com.campus.jobagent.modules.user.entity;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("sys_permission")
public class SysPermission {
    @TableId
    private Long id;
    private String permCode;
    private String permName;
    private String module;
    private String type;
    private String url;
    private Integer status;
}