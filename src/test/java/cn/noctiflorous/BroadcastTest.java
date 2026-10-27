package cn.noctiflorous;

import cn.noctiflorous.entity.Dict;
import cn.noctiflorous.mapper.DictMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:shardingsphere:classpath:shardingsphere-broadcast.yaml"
})
class BroadcastTest {

    @Autowired
    private DictMapper dictMapper;

    /**
     * 广播表：插入测试。
     */
    @Test
    void testInsertBroadcast() {
        Dict dict = new Dict();
        dict.setDictType("type1");
        dictMapper.insert(dict);
    }

    @Test
    void testSelectBroadcast() {
        List<Dict> dictList = dictMapper.selectList(null);
        dictList.forEach(System.out::println);
    }
}
