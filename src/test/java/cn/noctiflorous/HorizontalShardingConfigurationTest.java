package cn.noctiflorous;

import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.swapper.YamlShardingRuleConfigurationSwapper;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.algorithm.sharding.inline.InlineShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class HorizontalShardingConfigurationTest {
    @Test
    void initializesCompleteRuleAndRoutesBothKeysWithoutDatabaseConnections() throws Exception {
        try (var input = getClass().getResourceAsStream("/shardingsphere-horizontal.yaml")) {
            assertThat(input).isNotNull();
            var root = YamlEngine.unmarshal(input.readAllBytes(), YamlJDBCConfiguration.class);
            var yaml = (YamlShardingRuleConfiguration) root.getRules().iterator().next();
            var config = new YamlShardingRuleConfigurationSwapper().swapToObject(yaml);
            var rule = new ShardingRule(config, Map.of(
                    "server-user", mock(DataSource.class),
                    "server-order0", mock(DataSource.class),
                    "server-order1", mock(DataSource.class)), mock(ComputeNodeInstanceContext.class), List.of());
            assertThat(rule.getShardingTable("t_order").getActualDataNodes()).hasSize(4);

            var order = yaml.getTables().get("t_order");
            assertThat(order.getKeyGenerateStrategy().getColumn()).isEqualTo("id");
            assertThat(yaml.getKeyGenerators().get(order.getKeyGenerateStrategy().getKeyGeneratorName())
                    .getType()).isEqualTo("SNOWFLAKE");
            var database = new InlineShardingAlgorithm();
            database.init(yaml.getShardingAlgorithms().get(order.getDatabaseStrategy().getStandard()
                    .getShardingAlgorithmName()).getProps());
            var table = new InlineShardingAlgorithm();
            table.init(yaml.getShardingAlgorithms().get(order.getTableStrategy().getStandard()
                    .getShardingAlgorithmName()).getProps());
            var targets = new HashSet<String>();
            for (long userId : List.of(1L, 2L)) {
                String db = database.doSharding(List.of("server-order0", "server-order1"),
                        new PreciseShardingValue<Comparable<?>>("t_order", "user_id", null, userId));
                assertThat(db).isEqualTo("server-order" + userId % 2);
                for (String orderNo : List.of("ATGUIGU001", "ATGUIGU002", "polygenelubricants")) {
                    String physicalTable = table.doSharding(List.of("t_order0", "t_order1"),
                            new PreciseShardingValue<Comparable<?>>("t_order", "order_no", null, orderNo));
                    assertThat(physicalTable).isEqualTo("t_order" + Math.floorMod(orderNo.hashCode(), 2));
                    targets.add(db + "." + physicalTable);
                }
            }
            assertThat(targets).hasSize(4);
        }
    }
}
