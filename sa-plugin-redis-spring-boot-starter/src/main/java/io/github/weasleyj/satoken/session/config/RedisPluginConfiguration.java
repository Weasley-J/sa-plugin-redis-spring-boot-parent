package io.github.weasleyj.satoken.session.config;

import cn.dev33.satoken.dao.SaTokenDao;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalTimeSerializer;
import io.github.weasleyj.satoken.session.PluginBanner;
import io.github.weasleyj.satoken.session.annotation.EnableSaIndependentRedisSession;
import io.github.weasleyj.satoken.session.core.DefaultIndependentRedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Redis Plugin Configuration
 *
 * @author weasley
 * @version 1.0.0
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({RedisPluginProperties.class})
@ConditionalOnClass({EnableSaIndependentRedisSession.class})
public class RedisPluginConfiguration {
    public static final String LOCAL_TIME_PATTERN = "HH:mm:ss";
    public static final String LOCAL_DATE_PATTERN = "yyyy-MM-dd";
    public static final String LOCAL_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final ApplicationContext applicationContext;

    public RedisPluginConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * redis存储时序列化方式
     */
    @Bean({"redisPluginRedisSerializer"})
    public RedisSerializer<Object> redisPluginRedisSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        JavaTimeModule timeModule = new JavaTimeModule();
        timeModule.addSerializer(LocalTime.class, new LocalTimeSerializer(DateTimeFormatter.ofPattern(LOCAL_TIME_PATTERN)));
        timeModule.addSerializer(LocalDate.class, new LocalDateSerializer(DateTimeFormatter.ofPattern(LOCAL_DATE_PATTERN)));
        timeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN)));
        timeModule.addDeserializer(LocalTime.class, new LocalTimeDeserializer(DateTimeFormatter.ofPattern(LOCAL_TIME_PATTERN)));
        timeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(DateTimeFormatter.ofPattern(LOCAL_DATE_PATTERN)));
        timeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(LOCAL_DATE_TIME_PATTERN)));
        mapper.registerModule(timeModule).findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    /**
     * redis连接方式
     */
    @Bean({"redisPluginRedisConnectionFactory"})
    public RedisConnectionFactory redisPluginRedisConnectionFactory(RedisPluginProperties redisPluginProperties) {
        boolean independentSession = redisPluginProperties.getIndependentSession();
        // 不启独立session配置就返回默认工厂
        if (!independentSession) {
            return applicationContext.getBean(RedisConnectionFactory.class);
        }

        RedisProperties properties = redisPluginProperties.getIndependentRedis();
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(properties.getHost());
        redisConfig.setPort(properties.getPort());
        redisConfig.setUsername(properties.getUsername());
        redisConfig.setPassword(RedisPassword.of(properties.getPassword()));
        redisConfig.setDatabase(properties.getDatabase());

        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder();

        GenericObjectPoolConfig<?> poolConfig = new GenericObjectPoolConfig<>();
        RedisProperties.Lettuce lettuce = properties.getLettuce();
        if (lettuce.getPool() != null) {
            RedisProperties.Pool pool = properties.getLettuce().getPool();
            if ((pool.getEnabled() != null) && Boolean.TRUE.equals(pool.getEnabled())) {
                poolConfig.setMaxTotal(pool.getMaxActive());
                poolConfig.setMaxIdle(pool.getMaxIdle());
                poolConfig.setMinIdle(pool.getMinIdle());
                poolConfig.setMaxWait(Duration.of(pool.getMaxWait().toMillis(), ChronoUnit.MILLIS));
                builder.poolConfig(poolConfig);
            }
        }

        if (properties.getTimeout() != null) builder.commandTimeout(properties.getTimeout());
        if (lettuce.getShutdownTimeout() != null) builder.shutdownTimeout(lettuce.getShutdownTimeout());

        LettuceClientConfiguration clientConfig = builder.build();
        LettuceConnectionFactory factory = new LettuceConnectionFactory(redisConfig, clientConfig);
        factory.afterPropertiesSet();

        return factory;
    }

    /**
     * Redis Plugin: String Redis Template
     */
    @Bean({"redisPluginStringRedisTemplate"})
    public StringRedisTemplate redisPluginStringRedisTemplate(@Qualifier("redisPluginRedisConnectionFactory") RedisConnectionFactory redisPluginRedisConnectionFactory) {
        StringRedisTemplate stringTemplate = new StringRedisTemplate();
        stringTemplate.setConnectionFactory(redisPluginRedisConnectionFactory);
        stringTemplate.afterPropertiesSet();
        return stringTemplate;
    }

    /**
     * Redis Plugin: Object Redis Template
     */
    @Bean({"redisPluginObjectRedisTemplate"})
    public RedisTemplate<String, Object> redisPluginObjectRedisTemplate(@Qualifier("redisPluginRedisSerializer") RedisSerializer<Object> redisPluginRedisSerializer,
                                                                        @Qualifier("redisPluginRedisConnectionFactory") RedisConnectionFactory redisPluginRedisConnectionFactory) {
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisPluginRedisConnectionFactory);
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);
        template.setValueSerializer(redisPluginRedisSerializer);
        template.setHashValueSerializer(redisPluginRedisSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 优先采用独立Session存储方案，与业务缓存解耦
     */
    @Bean
    @Primary
    public SaTokenDao saTokenDao(RedisPluginProperties redisPluginProperties,
                                 @Qualifier("redisPluginStringRedisTemplate") StringRedisTemplate redisPluginStringRedisTemplate,
                                 @Qualifier("redisPluginObjectRedisTemplate") RedisTemplate<String, Object> redisPluginObjectRedisTemplate) {
        DefaultIndependentRedisRepository repository = new DefaultIndependentRedisRepository(redisPluginStringRedisTemplate, redisPluginObjectRedisTemplate);
        repository.setRedisBasePrefix(redisPluginProperties.getRedisBasePrefix());
        if (log.isInfoEnabled()) {
            log.info("Succeed in initialization for Sa-Token independence redis session storage scheme.");
        }
        if (redisPluginProperties.getShowBanner().equals(true)) {
            PluginBanner.getInstance().showBanner();
        }
        return repository;
    }

}
