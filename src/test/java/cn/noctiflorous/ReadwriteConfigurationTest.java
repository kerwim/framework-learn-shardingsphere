package cn.noctiflorous;

import org.apache.shardingsphere.driver.yaml.YamlJDBCConfiguration;
import org.apache.shardingsphere.infra.util.yaml.YamlEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.rule.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReadwriteConfigurationTest {
    @Test
    void parsesWithInstalledShardingSphereVersionWithoutConnectingToMysql() throws Exception {
        try (var input = getClass().getResourceAsStream("/shardingsphere-dev.yaml")) {
            assertThat(input).isNotNull();
            var config = YamlEngine.unmarshal(input.readAllBytes(), YamlJDBCConfiguration.class);
            assertThat(config.getDataSources()).containsOnlyKeys("master", "slave1", "slave2");
            assertThat(config.getMode().getType()).isEqualTo("Standalone");
            assertThat(String.valueOf(config.getProps().get("sql-show"))).isEqualTo("true");

            var rule = (YamlReadwriteSplittingRuleConfiguration) config.getRules().stream()
                    .filter(YamlReadwriteSplittingRuleConfiguration.class::isInstance)
                    .findFirst().orElseThrow();
            var group = rule.getDataSourceGroups().get("myds");
            assertThat(group.getWriteDataSourceName()).isEqualTo("master");
            assertThat(group.getReadDataSourceNames()).containsExactly("slave1", "slave2");
            assertThat(group.getTransactionalReadQueryStrategy()).isEqualTo("PRIMARY");
            // 允许只切换算法名称，不需要同步修改测试。
            assertThat(rule.getLoadBalancers()).containsKey(group.getLoadBalancerName());
            assertThat(rule.getLoadBalancers().get("alg_round").getType()).isEqualTo("ROUND_ROBIN");
            assertThat(rule.getLoadBalancers().get("alg_random").getType()).isEqualTo("RANDOM");
            assertThat(rule.getLoadBalancers().get("alg_weight").getType()).isEqualTo("WEIGHT");
            assertThat(rule.getLoadBalancers().get("alg_weight").getProps())
                    .containsEntry("slave1", "1").containsEntry("slave2", "2");
            // 不仅检查 YAML 语法，也转换为当前版本的运行时规则对象。
            assertThat(new YamlRuleConfigurationSwapperEngine()
                    .swapToRuleConfigurations(config.getRules())).hasSize(2);
        }
    }
}
