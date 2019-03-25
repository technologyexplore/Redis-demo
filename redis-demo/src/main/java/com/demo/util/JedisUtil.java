package com.demo.util;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.util.Assert;

import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisCommands;

/**
 * Jedis工具类
 *
 * @author LJJ
 * @date 2018/02/26
 */
public class JedisUtil {
    private static final Logger log = LoggerFactory.getLogger(JedisUtil.class);
    private static final String NOT_HAS_TEXT = "Parameter 'key' must not be empty.";

    /**
     * RedisTemplate初始化标志，仅初始化一次
     */
    private static volatile boolean hasInit = false;

    private static RedisTemplate<String, Object> redisTemplate;
    //默认锁存活时间
    private static final long DEFAULT_WAIT_LOCK_TIME_OUT = 60000;

    /**
     * 系统启动时初始化redisTemplate
     *
     * @param redisTemplate
     */
    public static void init(RedisTemplate<String, Object> redisTemplate) {
        if (!hasInit) {
            synchronized (JedisUtil.class) {
                if (!hasInit) {
                    JedisUtil.redisTemplate = redisTemplate;
                    hasInit = true;
                }
            }
        }
    }

    /**
     * 根据key与hashKey获取其关联的值
     *
     * @param key
     * @param hashKey
     */
    public static Object hget(String key, Object hashKey) {
        if (StringUtil.isEmpty(key) || hashKey == null) {
            return null;
        }

        return getRedisTemplate().opsForHash().get(key, hashKey);
    }

    /**
     * Get values for given hashKeys from hash at key.
     *
     * @param key
     * @param hashKeys
     * @return
     */
    public static List<Object> hmget(String key, Collection<Object> hashKeys) {
        if (StringUtil.isEmpty(key) || hashKeys == null) {
            return Collections.emptyList();
        }

        return getRedisTemplate().opsForHash().multiGet(key, hashKeys);
    }

    /**
     * 为hashKey设置值
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public static void hset(String key, Object hashKey, Object value) {
        Assert.hasText(key, NOT_HAS_TEXT);
        Assert.notNull(hashKey, "Parameter 'hashKey' must not be null.");
        getRedisTemplate().opsForHash().put(key, hashKey, value);
    }

    /**
     * Set the value of a hash hashKey only if hashKey does not exist.
     *
     * @param key
     * @param hashKey
     * @param value
     */
    public static void hsetIfAbsend(String key, Object hashKey, Object value) {
        Assert.hasText(key, NOT_HAS_TEXT);
        Assert.notNull(hashKey, "Parameter 'hashKey' must not be null.");
        getRedisTemplate().opsForHash().putIfAbsent(key, hashKey, value);
    }

    /**
     * 为key设置多个值
     *
     * @param key
     * @param map
     */
    public static void hmset(String key, Map<Object, Object> map) {
        Assert.hasText(key, NOT_HAS_TEXT);
        Assert.notNull(map, "Parameter 'map' must not be null.");
        getRedisTemplate().opsForHash().putAll(key, map);
    }

    /**
     * 为key设置过期时间
     *
     * @param key
     * @param timeout
     * @param unit
     * @return true or false
     */
    public static boolean expire(String key, final long timeout, final TimeUnit unit) {
        if (StringUtil.isEmpty(key) || unit == null) {
            return false;
        }

        return getRedisTemplate().expire(key, timeout, unit);
    }

    /**
     * 为key设置值
     *
     * @param key
     * @param value
     */
    public static void set(String key, Object value) {
        Assert.hasText(key, NOT_HAS_TEXT);
        getRedisTemplate().opsForValue().set(key, value);
    }

    /**
     * 为key设置值
     *
     * @param key
     * @param value
     */
    public static void setMap(String key, Map value) {
        Assert.hasText(key, NOT_HAS_TEXT);
        getRedisTemplate().opsForHash().putAll(key, value);
    }

    /**
     * 为key设置值，并设置失效时间
     *
     * @param key
     * @param value
     */
    public static void set(String key, Object value, int time) {
        Assert.hasText(key, NOT_HAS_TEXT);
        getRedisTemplate().opsForValue().set(key, value, time, TimeUnit.SECONDS);
    }

    /**
     * 为key设置值，并设置失效时间
     *
     * @param key
     * @param time
     */
    public static void expire(String key, int time) {
        Assert.hasText(key, NOT_HAS_TEXT);
        Boolean expire = getRedisTemplate().expire(key, time, TimeUnit.SECONDS);
    }


    /**
     * 查看过期时间
     *
     * @param key
     */
    public static Long getExpire(String key) {
        Long expire = getRedisTemplate().getExpire(key);
        return expire;
    }

    /**
     * 根据key查询value
     *
     * @param key
     * @return
     */
    public static Object get(String key) {
        if (StringUtil.isEmpty(key)) {
            return null;
        }

        return getRedisTemplate().opsForValue().get(key);
    }

    /**
     * Set value of key and return its old value.
     *
     * @param key
     * @param value
     * @return
     */
    public static Object getAndSet(String key, Object value) {
        Assert.hasText(key, NOT_HAS_TEXT);
        return getRedisTemplate().opsForValue().getAndSet(key, value);
    }

    private static RedisTemplate<String, Object> getRedisTemplate() {
        Assert.state(hasInit, "未初始化redisTemplate.");
        return redisTemplate;
    }

    /**
     * 删除某个key
     *
     * @param key
     */
    public static void deleteKey(String key) {
        getRedisTemplate().delete(key);
    }

    /**
     * 判断某个key是否存在
     *
     * @param key
     * @return
     */
    public static boolean existKey(String key) {
        return getRedisTemplate().hasKey(key);
    }

    /**
     * 根据Hash key获取key-value的Map集合
     *
     * @param key
     * @return
     */
    public static Map<Object, Object> getMap(String key) {
        return getRedisTemplate().opsForHash().entries(key);
    }

    /**
     * 获取自增值
     *
     * @param key
     * @return
     */
    public static Long incr(String key) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        Long increment = entityIdCounter.getAndIncrement();
        return increment;
    }

    /**
     * 获取自增值 (自定义过期时间)
     *
     * @param key
     * @return
     */
    public static Long incr(String key, long timeout, TimeUnit unit) {
        // 递增后返回结果
        Long inrc = redisTemplate.opsForValue().increment(key, 1);
        if (inrc == 1) {
            //如果等于1则加过期时间
            redisTemplate.expire(key, timeout, unit);
        }
        return inrc;
    }

    /**
     * 获取自增值 (先增，在获取)
     *
     * @param key
     * @return
     */
    public static Long incrAndGet(String key) {
        RedisAtomicLong entityIdCounter = new RedisAtomicLong(key, redisTemplate.getConnectionFactory());
        Long increment = entityIdCounter.incrementAndGet();
        return increment;
    }

    /**
     * 同步锁
     *
     * @param key
     * @return
     */
    public static boolean lock(String key) {
        return lock(key, DEFAULT_WAIT_LOCK_TIME_OUT);
    }

    /**
     * 同步锁,设定失效时长
     *
     * @param key
     * @return
     */
    public static boolean lock(String key, long timeOut) {
        long currentTimeMillis = System.currentTimeMillis();
        long newExpireTime = currentTimeMillis + timeOut;
        RedisConnection connection = getRedisTemplate().getConnectionFactory().getConnection();
        try {
            Boolean setNX = connection.setNX(key.getBytes(), String.valueOf(newExpireTime).getBytes());
            if (setNX) {
                expire(key, timeOut, TimeUnit.MILLISECONDS);
                return true;
            }
            //解决如果在第一步setnx执行成功后，在expire()命令执行成功前，发生了宕机的现象，那么就依然会出现死锁的问题
            String currentValue = (String) get(key);
            if (currentValue != null && Long.parseLong(currentValue) < currentTimeMillis) {
                String oldExpireTime = (String) getAndSet(key, String.valueOf(newExpireTime));
                if (oldExpireTime != null && oldExpireTime.equals(currentValue)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("redis setNX error", e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }

        return false;
    }

    /**
     * 解锁
     *
     * @param key
     */
    public static void unLock(String key) {
        deleteKey(key);
    }

    /**
     * @param key
     * @return
     * @author heweiming
     * @Description 操作List leftPop
     */
    public static Object leftPop(String key) {
        return getRedisTemplate().opsForList().leftPop(key);
    }

    /**
     * @param key
     * @return
     * @author heweiming
     * @Description 操作List leftPop
     */
    public static Long rightPush(String key, String value) {
        return getRedisTemplate().opsForList().rightPush(key, value);
    }

    /**
     * @param key
     * @param value
     * @return
     * @author heweiming
     * @Description redis命令setnx
     */
    public static boolean setnx(String key, String value) {
        return getRedisTemplate().opsForValue().setIfAbsent(key, value);
    }

    /**
     * 同步锁
     *
     * @param key
     * @return
     */
    public static boolean syncLock(String key, Long expireTime) {
        if (expireTime == null) {
            expireTime = DEFAULT_WAIT_LOCK_TIME_OUT;
        }
        long currentTimeMillis = System.currentTimeMillis();
        long newExpireTime = currentTimeMillis + expireTime;
        Boolean setNX = getRedisTemplate().opsForValue().setIfAbsent(key, String.valueOf(newExpireTime));
        //
        if (setNX) {
            expire(key, DEFAULT_WAIT_LOCK_TIME_OUT, TimeUnit.MILLISECONDS);
            return true;
        }
        //解决如果在第一步setnx执行成功后，在expire()命令执行成功前，发生了宕机的现象，那么就依然会出现死锁的问题
        String currentValue = (String) get(key);
        if (currentValue != null && Long.parseLong(currentValue) < currentTimeMillis) {
            String oldExpireTime = (String) getAndSet(key, String.valueOf(newExpireTime));
            if (oldExpireTime != null && oldExpireTime.equals(currentValue)) {
                return true;
            }
        }
        return false;
    }


    /**
     * @param key
     * @param start
     * @param end
     * @return
     * @author heweiming
     * @Description redis lrange 当查找不到时返回空的列表
     */
    public static List<Object> lrange(String key, Integer start, Integer end) {
        List<Object> list = getRedisTemplate().opsForList().range(key, start, end);
        return list;
    }

    /**
     * 递减操作
     *
     * @param key
     * @return
     */
    public static Long decr(String key) {
        Long increment = redisTemplate.opsForValue().increment(key, -1);
        return increment;
    }

    public static boolean retryLock(final String key, final long expire) {
        try {
            String result = redisTemplate.execute(new RedisCallback<String>() {
                @Override
                public String doInRedis(RedisConnection connection) throws DataAccessException {
                    JedisCommands commands = (JedisCommands) connection.getNativeConnection();
                    //单个命令
                    return commands.set(key, UUID.randomUUID().toString(), "NX", "PX", expire);
                }
            });
            return !StringUtils.isEmpty(result);
        } catch (Exception e) {
            log.error("Lock: set redis occured an exception", e);
            return false;
        }
    }

    public static void zAdd(String key, Object object, Double num) {
        redisTemplate.opsForZSet().incrementScore(key, object, num);
    }

    public static Set<ZSetOperations.TypedTuple<Object>> hSet(String key) {
        Set<ZSetOperations.TypedTuple<Object>> typedTuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        return typedTuples;
    }

    public static void incrementScore(String key, Object object, Double num) {
        Double aDouble = redisTemplate.opsForZSet().incrementScore(key, object, num);
    }
}
