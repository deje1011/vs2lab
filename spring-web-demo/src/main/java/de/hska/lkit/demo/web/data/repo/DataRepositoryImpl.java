package de.hska.lkit.demo.web.data.repo;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import de.hska.lkit.demo.web.data.model.Post;
import de.hska.lkit.demo.web.data.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * Implements the DataRepository interface.
 * Handles access to database.
 * Created by Marina on 20.11.2016.
 */
@Repository
public class DataRepositoryImpl implements DataRepository{

    /**
     * to generate unique ids for user
     */
    private RedisAtomicLong userId;

    /**
     * to generate unique ids for post
     */
    private RedisAtomicLong postId;
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

    private SetOperations<String, User> setOperationsUser;

    private ZSetOperations<String, User> zSetOperationsUser;

    private HashOperations<String, Object, Object> redisHashOperations;


    @Autowired
    public DataRepositoryImpl(RedisTemplate<String, User> redisTemplate, StringRedisTemplate stringRedisTemplate){

        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.userId = new RedisAtomicLong("userid", stringRedisTemplate.getConnectionFactory());
    }

    @PostConstruct
    private void init(){
        stringHashOperations = stringRedisTemplate.opsForHash();
        setOperations = stringRedisTemplate.opsForSet();
        redisHashOperations = redisTemplate.opsForHash();
        zSetOperationsUser = redisTemplate.opsForZSet();
        setOperationsUser = redisTemplate.opsForSet();
    }


    @Override
    public void registerUser(User user) {

        String id = String.valueOf(userId.incrementAndGet());

        user.setId(id);

        String key= Constants.USER_KEY_PREFIX + user.getId();
        stringHashOperations.put(key,Constants.KEY_SUFFIX_ID, user.getId());
        stringHashOperations.put(key,Constants.KEY_SUFFIX_NAME, user.getName());
        stringHashOperations.put(key,Constants.KEY_SUFFIX_PASSWORD, user.getPassword());
        stringHashOperations.put(Constants.USER_KEY_PREFIX + user.getName(), Constants.KEY_SUFFIX_ID, user.getId());

        setOperations.add(Constants.KEY_GET_ALL_USERS, user.getId());
    }


    @Override
    public boolean isPasswordValid(String name, String password) {
        return false;
    }



    @Override
    public String getUserId(String name) {

        String string = stringHashOperations.get(Constants.USER_KEY_PREFIX + name, Constants.KEY_SUFFIX_ID);
        return string;
    }



    @Override
    public Set<String> getAllUsers(){
      //  Map<Object,Object> users = redisHashOperations.entries(Constants.KEY_GET_ALL_USERS);
        Set<String> users = setOperations.members(Constants.KEY_GET_ALL_USERS);
        return users;
    }


    @Override
    public boolean isUserNameValid(String name) {
        Map<Object,Object> users = redisHashOperations.entries(Constants.KEY_GET_ALL_USERS);

        return false;
    }


    @Override
    public User getUserById(String id) {

        User user = new User();

        user.setId(id);
        user.setName(stringHashOperations.get(Constants.USER_KEY_PREFIX + id, Constants.KEY_SUFFIX_NAME));
        user.setPassword(stringHashOperations.get(Constants.USER_KEY_PREFIX + id, Constants.KEY_SUFFIX_PASSWORD));

        return user;
    }


    @Override
    public  Map<Object,Object> getAllFollowers(String id) {
        return null;
    }


    @Override
    public  Map<Object,Object> getAllFollowed(String id) {
        return null;
    }

    @Override
    public void addFollower(String currentUserId, String userToFollowId) {

    }

    @Override
    public void removeFollower(String currentUserId, String userToUnfollow) {

    }

    @Override
    public  Map<Object,Object> getAllGlobalPosts() {
        return null;
    }

    @Override
    public  Map<Object,Object> getTimelinePosts(String id) {
        return null;
    }

    @Override
    public void addPost(String id) {

    }
}
