package cn.noctiflorous;

import cn.noctiflorous.entity.User;
import cn.noctiflorous.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class ReadWriteTest {

    @Autowired
    private UserMapper userMapper;

    /**
     * 写入数据的测试：运行时会向真实主库插入一条用户记录。
     */
    @Test
    void testInsert() {
        User user = new User();
        user.setUname("张三丰");
        assertEquals(1, userMapper.insert(user));
    }

    @Transactional
    @Test
    void testTrans() {
        User user = new User();
        user.setUname("铁锤");
        userMapper.insert(user);

        List<User> users = userMapper.selectList(null);
    }

    /**
     * 负载均衡测试。
     */
    @Test
    void testSelectAll() {
        List<User> users1 = userMapper.selectList(null);
        List<User> users2 = userMapper.selectList(null);
        List<User> users3 = userMapper.selectList(null);
        List<User> users4 = userMapper.selectList(null);
    }

}
