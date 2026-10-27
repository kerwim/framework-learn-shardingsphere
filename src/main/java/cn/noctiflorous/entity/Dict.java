package cn.noctiflorous.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("t_dict")
@Data
public class Dict {

    // 使用 MyBatis-Plus 的雪花算法生成主键。
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String dictType;
}
