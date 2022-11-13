package io.github.weasleyj.satoken.session.annotation;

import io.github.weasleyj.satoken.session.config.RedisPluginConfiguration;
import io.github.weasleyj.satoken.session.core.DefaultIndependentRedisRepository;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable Sa Independent Redis Session
 *
 * @author weasley
 * @version 1.0.0
 */
@Inherited
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import({RedisPluginConfiguration.class, DefaultIndependentRedisRepository.class})
public @interface EnableSaIndependentRedisSession {
}
