package de.hska.lkit.demo.web.data.repo;

import de.hska.lkit.demo.web.data.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

/**
 * Implements the DataRepository interface.
 * Handles access to database.
 * Created by Marina on 20.11.2016.
 */
@Repository
public class DataRepositoryImpl implements DataRepository{

    private static final String KEY_ONE ="";
    private static final String KEY_TWO="";

  //  private RedisAtomicLong userid

    @Autowired
    public DataRepositoryImpl(RedisTemplate<String, User> redisTemplate, StringRedisTemplate stringRedisTemplate){
     
    }



    @Override
    public void addUser(User user) {

    }

    @Override
    public boolean isPasswordValid(String name, String password) {
        return false;
    }

    @Override
    public String getUserId(String name) {
        return null;
    }
}
