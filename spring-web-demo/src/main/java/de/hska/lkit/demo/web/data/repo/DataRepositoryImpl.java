package de.hska.lkit.demo.web.data.repo;

import com.sun.org.apache.xml.internal.security.keys.KeyUtils;
import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.Intercepter;
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

    private RedisTemplate<String, String> redisTemplatePost;

    /**
     * hash operations for stringRedisTemplate
     */
    private HashOperations<String, String, String> stringHashOperations;

    private SetOperations<String, String> setOperations;

    private SetOperations<String, User> setOperationsUser;

    private ZSetOperations<String, User> zSetOperationsUser;

    private ZSetOperations<String, String> zSetOperationsPost;

    private HashOperations<String, Object, Object> redisHashOperations;


    @Autowired
    public DataRepositoryImpl(RedisTemplate<String, User> redisTemplate, RedisTemplate<String, String> redisTemplatePost, StringRedisTemplate stringRedisTemplate) {

        this.redisTemplate = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplatePost = redisTemplatePost;
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
        zSetOperationsPost = redisTemplatePost.opsForZSet();
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
        double scorekey = 0;

        for( int i = 0; i < user.getName().length(); i++){

            if(i == 0){
               scorekey += user.getName().charAt(i)*(256^3);
            }
            if(i == 1){
                scorekey += user.getName().charAt(i)*(256^2);
            }
            if(i == 2){
                scorekey += user.getName().charAt(i)*(256^1);
            }
            if(i == 3){
                scorekey += user.getName().charAt(i);
            }
        }
        zSetOperationsUser.add(Constants.KEY_GET_ALL_USERS_2, user,scorekey);
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
    public Set<User> getAllUsers() {
        //  Map<Object,Object> users = redisHashOperations.entries(Constants.KEY_GET_ALL_USERS);
        Set<String> users = setOperations.members(Constants.KEY_GET_ALL_USERS);

        Set<User> user = zSetOperationsUser.range(Constants.KEY_GET_ALL_USERS_2, (long)0, zSetOperationsUser.size(Constants.KEY_GET_ALL_USERS_2));

        return user;
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

        //Set<String> posts = setOperations.members(Constants.KEY_GET_ALL_GLOBAL_POSTS);

        Set<String> posts = zSetOperationsPost.range(Constants.KEY_GET_ALL_GLOBAL_POSTS_2, (long)0, zSetOperationsPost.size(Constants.KEY_GET_ALL_GLOBAL_POSTS_2));
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
        //setOperations.add(Constants.KEY_GET_ALL_GLOBAL_POSTS, post.getId());


        String score = Integer.toString(post.getTime().getYear()) + Integer.toString(post.getTime().getMonth())
                + Integer.toString(post.getTime().getDay()) + Integer.toString(post.getTime().getHours())
                + Integer.toString(post.getTime().getMinutes())+ Integer.toString(post.getTime().getSeconds());
        double scorekey = (double) Long.parseLong(score);

        zSetOperationsPost.add(Constants.KEY_GET_ALL_GLOBAL_POSTS_2, post.getId(), scorekey);
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
