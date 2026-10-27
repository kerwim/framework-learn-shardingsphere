package cn.noctiflorous;

import org.apache.shardingsphere.broadcast.config.BroadcastRuleConfiguration;
import org.apache.shardingsphere.broadcast.rule.BroadcastRule;
import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BroadcastConfigurationTest {
    @Test
    void initializesBroadcastAndShardingRulesWithoutConnectingToMysql() throws Exception {
        try (var input = getClass().getResourceAsStream("/shardingsphere-broadcast.yaml")) {
            assertThat(input).isNotNull();
            var yaml = YamlEngine.unmarshal(input.readAllBytes(), YamlJDBCConfiguration.class);
            var configs = new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(yaml.getRules());
            assertThat(configs).hasSize(2);
            var broadcastConfig = (BroadcastRuleConfiguration) configs.stream()
                    .filter(BroadcastRuleConfiguration.class::isInstance).findFirst().orElseThrow();
            var shardingConfig = (ShardingRuleConfiguration) configs.stream()
                    .filter(ShardingRuleConfiguration.class::isInstance).findFirst().orElseThrow();
            Map<String, DataSource> sources = Map.of("server-user", mock(DataSource.class),
                    "server-order0", mock(DataSource.class), "server-order1", mock(DataSource.class));
            var broadcast = new BroadcastRule(broadcastConfig, sources, List.of());
            assertThat(broadcast.getTables()).containsExactly("t_dict");
            assertThat(broadcast.getDataSourceNames()).containsExactlyInAnyOrderElementsOf(sources.keySet());
            var sharding = new ShardingRule(shardingConfig, sources,
                    mock(ComputeNodeInstanceContext.class), List.of(broadcast));
            assertThat(sharding.getShardingTable("t_order").getActualDataNodes()).hasSize(4);
            assertThat(sharding.getShardingTable("t_order_item").getActualDataNodes()).hasSize(4);
            assertThat(sharding.isAllConfigBindingTables(List.of("t_order", "t_order_item"))).isTrue();
        }
    }
}
