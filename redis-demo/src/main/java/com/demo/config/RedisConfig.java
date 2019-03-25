/**
 * @Title: RedisConfig
 * @Package com.shein.common.config
 * @Description: redis配置
 * @author: liuzhe
 * @date: 2018/1/18
 * @version: V1.0
 */
package com.demo.config;

import com.demo.util.JedisUtil;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.ShardedJedisPool;

import java.text.SimpleDateFormat;

/**
 * redis配置
 */
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnClass({Jedis.class, ShardedJedisPool.class})
public class RedisConfig {

    /**
     * redisTemplate配置，通过在其他bean中注入该实例来操作redis
     * @param redisConnectionFactory
     * @return
     */
   @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<String, Object>();
        template.setConnectionFactory(redisConnectionFactory);
        // 设置序列化方式
        RedisSerializer<?> defaultSerializer = new StringRedisSerializer();
        template.setDefaultSerializer(defaultSerializer);

        //RedisSerializer<Object> valueSerializer = createRedisValueSerializer();
        // 定义key的序列化方式
        //template.setKeySerializer(defaultSerializer);
        // 定义value的序列化方式
        template.setValueSerializer(defaultSerializer);
        template.setHashValueSerializer(defaultSerializer);
        //template.setHashKeySerializer(defaultSerializer);
        template.afterPropertiesSet();
        //开户事务
        template.setEnableTransactionSupport(true);
        JedisUtil.init(template);
        return template;
    }
    /**
     * 创建RedisSerializer
     * @return
     */
    private RedisSerializer<Object> createRedisValueSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        // 对象序列化成json
        return new GenericJackson2JsonRedisSerializer(objectMapper);
    }

}
