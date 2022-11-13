package io.github.weasleyj.satoken.session.config;

import io.github.weasleyj.satoken.session.core.DefaultIndependentRedisRepository;
import lombok.Data;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import static io.github.weasleyj.satoken.session.config.RedisPluginProperties.PREFIX;


/**
 * Redis plugin Session Properties
 *
 * @author weasley
 * @version 1.0.0
 */
@Data
@ConfigurationProperties(prefix = PREFIX)
public class RedisPluginProperties {
    public static final String PREFIX = "sa-token.plugins.redis";
    /**
     * 打印banner
     */
    private Boolean showBanner = true;
    /**
     * 是否开启独立redis配置
     */
    private Boolean independentSession = true;
    /**
     * Redis前缀, Redis存取值规则:  redisBasePrefix + key
     *
     * @see DefaultIndependentRedisRepository#redisBasePrefix
     */
    private String redisBasePrefix;
    /**
     * 独立redis配置
     */
    @NestedConfigurationProperty
    private RedisProperties independentRedis;
}
