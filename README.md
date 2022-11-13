# sa-plugin-redis-spring-boot-starter

一个用于将`sa-token` 的`session`信息存储于独立`Redis`中的`spring-boot-starter`, **其核心目的是为了使`sa-token`的`session`数据与业务缓存数据解耦**，使用单独的`Redis`存放`sa-token`的数据。



- 支持自定义`Redis`基础常量的`key`前缀
- `yaml`配置元数据
- 通过注解`@EnableSaIndependentRedisSession`启用插件功能



## 1 给项目的pom.xml添加maven依赖

```xml
<properties>
  <sa-plugin-redis.verison>1.0.0</sa-plugin-redis.verison>
  <sa-token.version>1.32.0</sa-token.version>
</properties>

<dependency>
  <groupId>io.github.weasley-j</groupId>
  <artifactId>sa-plugin-redis-spring-boot-starter</artifactId>
  <version>${sa-plugin-redis.verison}</version>
</dependency>
<!-- sa-token  -->
<dependency>
  <groupId>cn.dev33</groupId>
  <artifactId>sa-token-spring-boot-starter</artifactId>
  <version>${sa-token.version}</version>
</dependency>
<!-- Sa-Token 整合 Redis （使用 jackson 序列化方式）: 原则上开启io.github.weasley-j:sa-plugin-redis-spring-boot-starter时此插件将会失效 -->
<dependency>
  <groupId>cn.dev33</groupId>
  <artifactId>sa-token-dao-redis-jackson</artifactId>
  <version>${sa-token.version}</version>
</dependency>
<!-- redis启动器 -->
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

```

## 2 配置插件yaml元数据

```yaml
# Sa Token
sa-token:
  enable: true
  # token名称 (同时也是cookie名称)
  token-name: token
  # token有效期，单位s 默认30天, -1代表永不过期
  timeout: 2592000
  # token临时有效期 (指定时间内无操作就视为token过期) 单位: 秒
  activity-timeout: -1
  # 是否允许同一账号并发登录 (为true时允许一起登录, 为false时新登录挤掉旧登录)
  is-concurrent: true
  # 在多人登录同一账号时，是否共用一个token (为true时所有登录共用一个token, 为false时每次登录新建一个token)
  is-share: true
  # token风格
  token-style: simple-uuid
  # 是否输出操作日志
  is-log: false
  plugins:
    redis:
      show-banner: on
      redis-base-prefix: 'login:uc:'
      independent-session: true
      independent-redis:
        host: 127.0.0.1
        port: 6379
        password: 123456
        database: 1
```

配置的`Java`类:



## 3 SpringBoot启动内添加启注解支持

```java
/**
 * Some Springboot Application
 *
 * @author weasley
 */
@SpringBootApplication
@EnableSaIndependentRedisSession
public class SomeSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SomeSpringbootApplication.class, args);
    }

}
```



## 4 效果演示

最终将`Sa-Token`的`token`信息存储到独立的`Redis`中

![image-20221113170656818](https://alphahub-test-bucket.oss-cn-shanghai.aliyuncs.com/image/image-20221113170656818.png)

![image-20221113170723876](https://alphahub-test-bucket.oss-cn-shanghai.aliyuncs.com/image/image-20221113170723876.png)



![image-20221113170744095](https://alphahub-test-bucket.oss-cn-shanghai.aliyuncs.com/image/image-20221113170744095.png)