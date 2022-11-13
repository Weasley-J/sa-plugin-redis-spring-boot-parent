package io.github.weasleyj.satoken.session.core;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.util.SaFoxUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Default Sa-Token Independent Redis Repository
 *
 * @author weasley
 * @version 1.0.0
 */
@Data
@Slf4j
public class DefaultIndependentRedisRepository implements SaTokenDao {
    /**
     * String Redis Template
     */
    private final StringRedisTemplate stringRedisTemplate;
    /**
     * Object Redis Template
     */
    private final RedisTemplate<String, Object> objectRedisTemplate;
    /**
     * Redis前缀
     * <p>
     * Redis存取值规则:  redisBasePrefix + key
     */
    private String redisBasePrefix;

    public DefaultIndependentRedisRepository(StringRedisTemplate stringRedisTemplate, RedisTemplate<String, Object> objectRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.objectRedisTemplate = objectRedisTemplate;
    }

    /**
     * 获取Value，如无返空
     */
    @Override
    public String get(String key) {
        key = this.getRealKey(key);
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 写入Value，并设定存活时间 (单位: 秒)
     */
    @Override
    public void set(String key, String value, long timeout) {
        key = this.getRealKey(key);
        if (timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            stringRedisTemplate.opsForValue().set(key, value);
        } else {
            stringRedisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
        }
    }

    /**
     * 修修改指定key-value键值对 (过期时间不变)
     */
    @Override
    public void update(String key, String value) {
        key = this.getRealKey(key);
        long expire = getTimeout(key);
        // -2 = 无此键
        if (expire == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        this.set(key, value, expire);
    }

    /**
     * 删除Value
     */
    @Override
    public void delete(String key) {
        key = this.getRealKey(key);
        stringRedisTemplate.delete(key);
    }

    /**
     * 获取Value的剩余存活时间 (单位: 秒)
     */
    @Override
    @SuppressWarnings({"all"})
    public long getTimeout(String key) {
        key = this.getRealKey(key);
        return stringRedisTemplate.getExpire(key);
    }

    /**
     * 修改Value的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateTimeout(String key, long timeout) {
        key = this.getRealKey(key);
        // 判断是否想要设置为永久
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getTimeout(key);
            // 如果其已经被设置为永久，则不作任何处理
            // 如果尚未被设置为永久，那么再次set一次
            if (expire != SaTokenDao.NEVER_EXPIRE) {
                this.set(key, this.get(key), timeout);
            }
            return;
        }
        stringRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }


    /**
     * 获取Object，如无返空
     */
    @Override
    public Object getObject(String key) {
        key = this.getRealKey(key);
        return objectRedisTemplate.opsForValue().get(key);
    }

    /**
     * 写入Object，并设定存活时间 (单位: 秒)
     */
    @Override
    public void setObject(String key, Object object, long timeout) {
        key = this.getRealKey(key);
        if (timeout == 0 || timeout <= SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        // 判断是否为永不过期
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            objectRedisTemplate.opsForValue().set(key, object);
        } else {
            objectRedisTemplate.opsForValue().set(key, object, timeout, TimeUnit.SECONDS);
        }
    }

    /**
     * 更新Object (过期时间不变)
     */
    @Override
    public void updateObject(String key, Object object) {
        key = this.getRealKey(key);
        long expire = getObjectTimeout(key);
        // -2 = 无此键
        if (expire == SaTokenDao.NOT_VALUE_EXPIRE) {
            return;
        }
        this.setObject(key, object, expire);
    }

    /**
     * 删除Object
     */
    @Override
    public void deleteObject(String key) {
        key = this.getRealKey(key);
        objectRedisTemplate.delete(key);
    }

    /**
     * 获取Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public long getObjectTimeout(String key) {
        key = this.getRealKey(key);
        return objectRedisTemplate.getExpire(key);
    }

    /**
     * 修改Object的剩余存活时间 (单位: 秒)
     */
    @Override
    public void updateObjectTimeout(String key, long timeout) {
        key = this.getRealKey(key);
        // 判断是否想要设置为永久
        if (timeout == SaTokenDao.NEVER_EXPIRE) {
            long expire = getObjectTimeout(key);
            // 如果其已经被设置为永久，则不作任何处理
            // 如果尚未被设置为永久，那么再次set一次
            if (expire != SaTokenDao.NEVER_EXPIRE) {
                this.setObject(key, this.getObject(key), timeout);
            }
            return;
        }
        objectRedisTemplate.expire(key, timeout, TimeUnit.SECONDS);
    }


    /**
     * 搜索数据
     */
    @Override
    public List<String> searchData(String prefix, String keyword, int start, int size, boolean sortType) {
        prefix = this.getRealKey(prefix);
        Set<String> keys = stringRedisTemplate.keys(prefix + "*" + keyword + "*");
        assert keys != null;
        List<String> list = new ArrayList<>(keys);
        return SaFoxUtil.searchList(list, start, size, sortType);
    }

    /**
     * 获取Redis的key
     *
     * @param key 传入的key
     * @return 获取Redis中真实的key
     */
    protected String getRealKey(String key) {
        if (StringUtils.hasText(redisBasePrefix)) return redisBasePrefix + key;
        else return key;
    }
}
