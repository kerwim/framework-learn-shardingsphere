package cn.noctiflorous;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.noctiflorous.entity.Order;
import cn.noctiflorous.entity.User;
import cn.noctiflorous.mapper.OrderMapper;
import cn.noctiflorous.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest
class ShardingTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 垂直分库测试：先插入用户，再插入关联该用户的订单。
     */
    @Test
    void testInsertOrderAndUser() {
        User user = new User();
        user.setUname("强哥");
        userMapper.insert(user);

        Order order = new Order();
        order.setOrderNo("ATGUIGU001");
        order.setUserId(user.getId());
        order.setAmount(new BigDecimal(100));
        orderMapper.insert(order);
    }

    @Test
    void testSelectFromOrderAndUser() {
        User user = userMapper.selectById(1L);
        Order order = orderMapper.selectById(1L);
    }

    /**
     * 水平分片学习：先验证订单写入 0 库 0 表。
     */
    @Test
    void testInsertOrder() {
        Order order = new Order();
        order.setOrderNo("ATGUIGU001");
        order.setUserId(1L);
        order.setAmount(new BigDecimal(100));
        orderMapper.insert(order);
    }

    /**
     * 水平分片：分库插入数据测试。
     */
    @Test
    void testInsertOrderDatabaseStrategy() {
        for (long i = 4; i < 8; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU001");
            order.setUserId(i + 1);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }
    }

    /**
     * 水平分片：两个用户的订单分别进入两个库，再根据订单号哈希选择物理表。
     */
    @Test
    void testInsertOrderTableStrategy() {
        for (long i = 1; i < 5; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(1L);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }

        for (long i = 5; i < 9; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(2L);
            order.setAmount(new BigDecimal(100));
            orderMapper.insert(order);
        }
    }

    /**
     * 水平分片：查询所有记录。
     */
    @Test
    void testShardingSelectAll() {
        List<Order> orders = orderMapper.selectList(null);
        orders.forEach(System.out::println);
    }

    /**
     * 水平分片：根据 user_id 查询记录。
     */
    @Test
    void testShardingSelectByUserId() {
        QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
        orderQueryWrapper.eq("user_id", 1L);
        List<Order> orders = orderMapper.selectList(orderQueryWrapper);
        orders.forEach(System.out::println);
    }

}
