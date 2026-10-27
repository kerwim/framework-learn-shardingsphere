package cn.noctiflorous.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@TableName("t_order") // 逻辑表名
@Data
public class Order {

    // AUTO 让 MyBatis-Plus 在 id 为空时省略该列，并接收 JDBC 回填的主键。
    // 水平分片配置由 ShardingSphere 的 SNOWFLAKE 生成；未配置时依赖数据库自增。
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo; // 自动映射数据库中的 order_no 字段
    private Long userId; // 自动映射数据库中的 user_id 字段
    private BigDecimal amount;
}
