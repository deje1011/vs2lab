package de.hska.lkit.demo.web.data.configuration;


import de.hska.lkit.demo.web.data.model.UserX;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;


/**
 *
 * Created by Marina on 20.11.2016.
 */
@Configuration
public class RedisConfiguration {

   @Bean
    public RedisConnectionFactory getConnectionFactory(){
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(new JedisPoolConfig());
        jedisConnectionFactory.setHostName("192.168.43.198");
        jedisConnectionFactory.setPort(6379);
        jedisConnectionFactory.setPassword("");
        return jedisConnectionFactory;
    }


    @Bean(name = "stringRedisTemplate")
    public StringRedisTemplate getStringRedisTemplate(){
        StringRedisTemplate stringRedisTemplate = new StringRedisTemplate(getConnectionFactory());
        stringRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringRedisTemplate.setHashValueSerializer(new StringRedisSerializer());
        stringRedisTemplate.setValueSerializer(new StringRedisSerializer());
        return stringRedisTemplate;
    }


    @Bean(name = "redisTemplate")
    public RedisTemplate<String, UserX> getRedisTemplate() {
        RedisTemplate<String, UserX> redisTemplate = new RedisTemplate<String, UserX>();
        redisTemplate.setConnectionFactory(getConnectionFactory());
        return redisTemplate;
    }

}
