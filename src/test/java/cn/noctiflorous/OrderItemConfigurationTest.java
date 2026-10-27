package cn.noctiflorous;

import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OrderItemConfigurationTest {
    @Test
    void orderAndItemsUseSameDatabaseAndTableSuffixWithoutConnectingToMysql() throws Exception {
        try (var input = getClass().getResourceAsStream("/shardingsphere-order-item.yaml")) {
            assertThat(input).isNotNull();
            var root = YamlEngine.unmarshal(input.readAllBytes(), YamlJDBCConfiguration.class);
            var yaml = (YamlShardingRuleConfiguration) root.getRules().iterator().next();
            var config = new YamlShardingRuleConfigurationSwapper().swapToObject(yaml);
            var rule = new ShardingRule(config, Map.of(
                    "server-user", mock(DataSource.class), "server-order0", mock(DataSource.class),
                    "server-order1", mock(DataSource.class)), mock(ComputeNodeInstanceContext.class), List.of());
            assertThat(rule.findBindingTableRule("t_order")).isPresent();
            assertThat(rule.isAllConfigBindingTables(List.of("t_order", "t_order_item"))).isTrue();

            var order = yaml.getTables().get("t_order");
            var item = yaml.getTables().get("t_order_item");
            assertThat(item.getDatabaseStrategy().getStandard().getShardingColumn()).isEqualTo("user_id");
            assertThat(item.getDatabaseStrategy().getStandard().getShardingAlgorithmName())
                    .isEqualTo(order.getDatabaseStrategy().getStandard().getShardingAlgorithmName());
            assertThat(item.getTableStrategy().getStandard().getShardingColumn()).isEqualTo("order_no");
            assertThat(item.getKeyGenerateStrategy().getColumn()).isEqualTo("id");
            assertThat(yaml.getKeyGenerators().get(item.getKeyGenerateStrategy().getKeyGeneratorName()).getType())
                    .isEqualTo("SNOWFLAKE");

            var orderAlgorithm = new InlineShardingAlgorithm();
            orderAlgorithm.init(yaml.getShardingAlgorithms().get(order.getTableStrategy().getStandard()
                    .getShardingAlgorithmName()).getProps());
            var itemAlgorithm = new InlineShardingAlgorithm();
            itemAlgorithm.init(yaml.getShardingAlgorithms().get(item.getTableStrategy().getStandard()
                    .getShardingAlgorithmName()).getProps());
            assertThat(rule.getShardingTable("t_order").getActualDataNodes()).hasSize(4);
            assertThat(rule.getShardingTable("t_order_item").getActualDataNodes()).hasSize(4);
            for (String number : List.of("ATGUIGU1", "ATGUIGU2", "polygenelubricants")) {
                var value = new PreciseShardingValue<Comparable<?>>("t_order", "order_no", null, number);
                String orderTable = orderAlgorithm.doSharding(List.of("t_order0", "t_order1"), value);
                var itemValue = new PreciseShardingValue<Comparable<?>>("t_order_item", "order_no", null, number);
                String itemTable = itemAlgorithm.doSharding(List.of("t_order_item0", "t_order_item1"), itemValue);
                assertThat(itemTable).isEqualTo(orderTable.replace("t_order", "t_order_item"));
            }
        }
    }
}
