package de.hska.lkit.demo.web.data.repo;

import de.hska.lkit.demo.web.data.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Implements the DataRepository interface.
 * Handles access to database.
 * Created by Marina on 20.11.2016.
 */
@Repository
public class DataRepositoryImpl implements DataRepository{

    private static final String KEY_ONE ="";
    private static final String KEY_TWO="";

    /**
     * to generate unique ids for user
     */
    private RedisAtomicLong userid;

    /**
     * to save data in String format
     */
    private StringRedisTemplate stringRedisTemplate;

    /**
     * to save user data as object
     */
    private RedisTemplate<String,User> redisTemplate;

    /**
     * hash operations for stringRedisTemplate
     */
    private HashOperations<String,String,String> stringHashOperations;

    private SetOperations<String, String> setOperations;

    private HashOperations<String, Object, Object> redisHashOperations;


    @Autowired
    public DataRepositoryImpl(RedisTemplate<String, User> redisTemplate, StringRedisTemplate stringRedisTemplate){

        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.userid = new RedisAtomicLong("userid", stringRedisTemplate.getConnectionFactory());
    }

    @PostConstruct
    private void init(){
        stringHashOperations = stringRedisTemplate.opsForHash();
        setOperations = stringRedisTemplate.opsForSet();
        redisHashOperations = redisTemplate.opsForHash();
    }


    @Override
    public void addUser(User user) {

        String id = String.valueOf(userid.incrementAndGet());

        user.setId(id);

        String key="user:" + user.getId();
        stringHashOperations.put(key,"id", user.getId());
        stringHashOperations.put(key,"name", user.getName());
        stringHashOperations.put(key,"password", user.getPassword());
        stringHashOperations.put("user:" + user.getName(), "id", user.getId());

        redisHashOperations.put("all-users", key, user);
    }

    @Override
    public boolean isPasswordValid(String name, String password) {
        return false;
    }

    @Override
    public String getUserId(String name) {

        String string = stringHashOperations.get("user:" + name, "id");
        return string;
    }

    @Override
    public Map<Object, Object> getAllUsers(){
        Map<Object, Object> users = redisHashOperations.entries("all-users");
        return users;
    }
}
