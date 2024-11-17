package edu.hebeu.partnermatching.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {
    private String host;

    private String port;

    @Bean
    public RedissonClient redissonClient(){
        //1创建配置
        Config config = new Config();
        String address = String.format("redis://%s:%s", host, port);
        //使用单个服务器
        config.useSingleServer().setAddress(address).setDatabase(3);
        //创建实例
        return Redisson.create(config);
    }
}
