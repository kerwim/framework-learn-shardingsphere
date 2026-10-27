package cn.noctiflorous;

import cn.noctiflorous.entity.Order;
import cn.noctiflorous.entity.OrderItem;
import cn.noctiflorous.entity.OrderVo;
import cn.noctiflorous.mapper.OrderItemMapper;
import cn.noctiflorous.mapper.OrderMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:shardingsphere:classpath:shardingsphere-order-item.yaml"
})
class OrderItemShardingTest {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    /**
     * 水平分片：两个用户共插入八笔订单，每笔包含三条同订单号、同用户的明细。
     * 运行时会写入真实数据库，主键由 ShardingSphere 雪花算法生成。
     */
    @Test
    void testInsertOrderAndOrderItem() {
        for (long i = 1; i < 5; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(1L);
            orderMapper.insert(order);

            for (int j = 0; j < 3; j++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo("ATGUIGU" + i);
                orderItem.setUserId(1L);
                orderItem.setPrice(new BigDecimal(10));
                orderItem.setCount(2);
                orderItemMapper.insert(orderItem);
            }
        }

        for (long i = 5; i < 9; i++) {
            Order order = new Order();
            order.setOrderNo("ATGUIGU" + i);
            order.setUserId(2L);
            orderMapper.insert(order);

            for (int j = 0; j < 3; j++) {
                OrderItem orderItem = new OrderItem();
                orderItem.setOrderNo("ATGUIGU" + i);
                orderItem.setUserId(2L);
                orderItem.setPrice(new BigDecimal(3));
                orderItem.setCount(3);
                orderItemMapper.insert(orderItem);
            }
        }
    }



    @Test
    void testGetOrderAmount() {
        List<OrderVo> orderAmountList = orderMapper.getOrderAmount();
        orderAmountList.forEach(System.out::println);
    }

}
