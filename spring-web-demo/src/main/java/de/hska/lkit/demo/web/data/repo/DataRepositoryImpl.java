package de.hska.lkit.demo.web.data.repo;

import com.sun.org.apache.xml.internal.security.keys.KeyUtils;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import de.hska.lkit.demo.web.data.model.Post;
import de.hska.lkit.demo.web.data.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * Implements the DataRepository interface.
 * Handles access to database.
 * Created by Marina on 20.11.2016.
 */
@Repository
public class DataRepositoryImpl implements DataRepository {

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
    private RedisTemplate<String, User> redisTemplate;

    /**
     * hash operations for stringRedisTemplate
     */
    private HashOperations<String, String, String> stringHashOperations;

    private SetOperations<String, String> setOperations;

    private SetOperations<String, User> setOperationsUser;

    private ZSetOperations<String, User> zSetOperationsUser;

    private HashOperations<String, Object, Object> redisHashOperations;


    @Autowired
    public DataRepositoryImpl(RedisTemplate<String, User> redisTemplate, StringRedisTemplate stringRedisTemplate) {

        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.userId = new RedisAtomicLong("userid", stringRedisTemplate.getConnectionFactory());
        this.postId = new RedisAtomicLong("postid", stringRedisTemplate.getConnectionFactory());
    }

    @PostConstruct
    private void init() {
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

        String key = Constants.USER_KEY_PREFIX + user.getId();
        stringHashOperations.put(key, Constants.KEY_SUFFIX_ID, user.getId());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_NAME, user.getName());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_PASSWORD, user.getPassword());
        stringHashOperations.put(Constants.USER_KEY_PREFIX + user.getName(), Constants.KEY_SUFFIX_ID, user.getId());

        setOperations.add(Constants.KEY_GET_ALL_USERS, user.getId());
    }


    @Override
    public boolean isPasswordValid(String name, String password) {

        if (stringRedisTemplate.hasKey(Constants.USER_KEY_PREFIX + name)) {

            String id = getUserId(name);
            String redisPassword = stringHashOperations.get(Constants.USER_KEY_PREFIX + id, Constants.KEY_SUFFIX_PASSWORD);

            if (redisPassword.equals(password)) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }


    @Override
    public String getUserId(String name) {

        String string = stringHashOperations.get(Constants.USER_KEY_PREFIX + name, Constants.KEY_SUFFIX_ID);
        return string;
    }


    @Override
    public Set<String> getAllUsers() {
        //  Map<Object,Object> users = redisHashOperations.entries(Constants.KEY_GET_ALL_USERS);
        Set<String> users = setOperations.members(Constants.KEY_GET_ALL_USERS);
        return users;
    }


    @Override
    public boolean isUserNameUnique(String name) {
        if (stringRedisTemplate.hasKey(Constants.USER_KEY_PREFIX + name)) {
            return false;
        }else {
            return true;
        }
    }


    @Override
    public User getUserById(String id) {

        if (stringRedisTemplate.hasKey(Constants.USER_KEY_PREFIX + id)) {

            String name = stringHashOperations.get(Constants.USER_KEY_PREFIX + id, Constants.KEY_SUFFIX_NAME);
            String password = stringHashOperations.get(Constants.USER_KEY_PREFIX + id, Constants.KEY_SUFFIX_PASSWORD);
            User user = new User(name, password);
            user.setId(id);
            return user;
        }
        return null;
    }


    @Override
    public Set<String> getAllFollowers(String id) {
        return null;
    }


    @Override
    public Set<String> getAllFollowed(String id) {
        return null;
    }


    @Override
    public void addFollower(String currentUserId, String userToFollowId) {

    }

    @Override
    public void removeFollower(String currentUserId, String userToUnfollow) {

    }

    @Override
    public Set<String> getAllGlobalPosts() {

        Set<String> posts = setOperations.members(Constants.KEY_GET_ALL_GLOBAL_POSTS);
        return posts;
    }

    @Override
    public Set<String> getTimelinePosts(String id) {
        return null;
    }

    @Override
    public void addPost(Post post) {

        String id = String.valueOf(postId.incrementAndGet());

        post.setId(id);

        String key = Constants.POST_KEY_PREFIX + post.getId();
       // stringHashOperations.put(key, Constants.KEY_SUFFIX_ID, post.getId());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_MESSAGE, post.getMessage());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_USER, post.getUser().getId());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_TIME, post.getTime().toString());



        //add post to users post list

        //add post to posts of followers

        //add post to global post list
        setOperations.add(Constants.KEY_GET_ALL_GLOBAL_POSTS, post.getId());
    }

    @Override
    public Post getPostById(String id) {

        if (stringRedisTemplate.hasKey(Constants.POST_KEY_PREFIX + id)) {

            String userId = stringHashOperations.get(Constants.POST_KEY_PREFIX + id, Constants.KEY_SUFFIX_USER);
            User user = getUserById(userId);
            String message = stringHashOperations.get(Constants.POST_KEY_PREFIX + id, Constants.KEY_SUFFIX_MESSAGE);
            String timeString = stringHashOperations.get(Constants.POST_KEY_PREFIX + id, Constants.KEY_SUFFIX_TIME);
            Date time = new Date();
            if(timeString!=null) {
                time = new Date();

            }
            Post post = new Post(user, message,time);
            post.setId(id);
            return post;
        }

        return null;
    }
}
