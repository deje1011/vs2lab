package de.hska.lkit.demo.web.data.repo;

import de.hska.lkit.demo.web.data.model.Post;

import de.hska.lkit.demo.web.data.model.UserX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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
    private RedisTemplate<String, String> redisTemplateUser;

    private RedisTemplate<String, String> redisTemplateString;

    /**
     * hash operations for stringRedisTemplate
     */
    private HashOperations<String, String, String> stringHashOperations;

    private SetOperations<String, String> setOperations;


    private ZSetOperations<String, String> zSetOperationsUser;

    private ZSetOperations<String, String> zSetOperationsPost;

    private HashOperations<String, Object, Object> redisHashOperations;

    private DateFormat dateFormat = new SimpleDateFormat("DD.MM.YYYY HH:mm", Locale.ENGLISH);


    @Autowired
    public DataRepositoryImpl(RedisTemplate<String, String> redisTemplate, RedisTemplate<String, String> redisTemplateString, StringRedisTemplate stringRedisTemplate) {

        this.redisTemplateUser = redisTemplate;
        this.stringRedisTemplate = stringRedisTemplate;
        this.redisTemplateString = redisTemplateString;
        this.userId = new RedisAtomicLong("userid", stringRedisTemplate.getConnectionFactory());
        this.postId = new RedisAtomicLong("postid", stringRedisTemplate.getConnectionFactory());
    }

    @PostConstruct
    private void init() {
        stringHashOperations = stringRedisTemplate.opsForHash();
        setOperations = stringRedisTemplate.opsForSet();
        redisHashOperations = redisTemplateUser.opsForHash();
        zSetOperationsUser = redisTemplateUser.opsForZSet();
        zSetOperationsPost = redisTemplateString.opsForZSet();
    }


    @Override
    public void registerUser(UserX userX) {

        String id = String.valueOf(userId.incrementAndGet());

        userX.setId(id);

        String key = Constants.USER_KEY_PREFIX + userX.getId();
        stringHashOperations.put(key, Constants.KEY_SUFFIX_ID, userX.getId());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_NAME, userX.getName());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_PASSWORD, userX.getPassword());
        stringHashOperations.put(Constants.USER_KEY_PREFIX + userX.getName(), Constants.KEY_SUFFIX_ID, userX.getId());

        setOperations.add(Constants.KEY_GET_ALL_USERS, userX.getId());
        double scoreKey = 0;

        for(int i = 0; i < userX.getName().length(); i++){

            if(i == 0){
               scoreKey += userX.getName().charAt(i)*(256^3);
            }
            if(i == 1){
                scoreKey += userX.getName().charAt(i)*(256^2);
            }
            if(i == 2){
                scoreKey += userX.getName().charAt(i)*(256^1);
            }
            if(i == 3){
                scoreKey += userX.getName().charAt(i);
            }
        }
        zSetOperationsUser.add(Constants.KEY_GET_ALL_USERS_2, userX.getId(),scoreKey);
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
        Set<String> users = setOperations.members(Constants.KEY_GET_ALL_USERS);

        Set<String> user = zSetOperationsUser.range(Constants.KEY_GET_ALL_USERS_2, (long)0, zSetOperationsUser.size(Constants.KEY_GET_ALL_USERS_2));

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
    public UserX getUserById(String id) {

        if (id == null) {
            return null;
        }

        if (stringRedisTemplate.hasKey(Constants.USER_KEY_PREFIX + id) == false) {
            return null;
        }

        String name = stringHashOperations.get(Constants.USER_KEY_PREFIX + id, Constants.KEY_SUFFIX_NAME);
        String password = stringHashOperations.get(Constants.USER_KEY_PREFIX + id, Constants.KEY_SUFFIX_PASSWORD);
        Set posts = setOperations.members(Constants.USER_KEY_PREFIX + id + ":" + Constants.KEY_SUFFIX_POSTS);
        Set follows = setOperations.members(Constants.USER_KEY_PREFIX + id + ":" + Constants.KEY_SUFFIX_FOLLOWS);
        Set followedBy = setOperations.members(Constants.USER_KEY_PREFIX + id + ":" + Constants.KEY_SUFFIX_FOLLOWED_BY);
        UserX userX = new UserX(name, password);
        userX.setId(id);
        userX.setPosts(posts);
        userX.setFollowed(followedBy);
        userX.setFollows(follows);
        return userX;
    }


    @Override
    public Set<String> getAllFollowers(String userId) {

        Set follows = setOperations.members(Constants.USER_KEY_PREFIX + userId + ":" + Constants.KEY_SUFFIX_FOLLOWS);

        return follows;
    }


    @Override
    public Set<String> getAllFollowed(String userId) {

        Set followedBy = setOperations.members(Constants.USER_KEY_PREFIX + userId + ":" + Constants.KEY_SUFFIX_FOLLOWED_BY);

        return followedBy;
    }


    @Override
    public void addFollower(String currentUserId, String userToFollowId) {

        //// add current active user to set of followers of a certain user
        String userFollowedByKey = Constants.USER_KEY_PREFIX + userToFollowId + ":" + Constants.KEY_SUFFIX_FOLLOWED_BY;

        Set<String> followedBy = setOperations.members(userFollowedByKey);
        if(!followedBy.contains(currentUserId)) {
            setOperations.add(userFollowedByKey, currentUserId);
        }

        //// add a certain user to set of followed of the current user
        String userFollowsKey = Constants.USER_KEY_PREFIX + currentUserId + ":" + Constants.KEY_SUFFIX_FOLLOWS;

        Set<String> follows = setOperations.members(userFollowsKey);
        if(!follows.contains(userToFollowId)) {
            setOperations.add(userFollowsKey, userToFollowId);
        }

    }

    @Override
    public void removeFollower(String currentUserId, String userToUnfollow) {
        //// remove current active user from set of followers of a certain user
        String userFollowedByKey = Constants.USER_KEY_PREFIX + userToUnfollow + ":" + Constants.KEY_SUFFIX_FOLLOWED_BY;

        Set<String> followedBy = setOperations.members(userFollowedByKey);
        if(followedBy.contains(currentUserId)) {
            setOperations.remove(userFollowedByKey, currentUserId);
        }

        //// removes a certain user from set of followed of the current user
        String userFollowsKey = Constants.USER_KEY_PREFIX + currentUserId + ":" + Constants.KEY_SUFFIX_FOLLOWS;

        Set<String> follows = setOperations.members(userFollowsKey);
        if(follows.contains(userToUnfollow)) {
            setOperations.remove(userFollowsKey, userToUnfollow);
        }



    }

    @Override
    public Set<String> getAllGlobalPosts() {
        return this.getAllGlobalPosts((long)0, zSetOperationsPost.size(Constants.KEY_GET_ALL_GLOBAL_POSTS_2));
    }

    public Set<String> getAllGlobalPosts(long offset, long limit) {
        return zSetOperationsPost.reverseRange(Constants.KEY_GET_ALL_GLOBAL_POSTS_2, offset, limit);
    }

    @Override
    public List<Post> getTimelinePosts(String userId) {

        Set<String> followers = getAllFollowers(userId);
        Set<String> allPosts = this.getAllGlobalPosts();
        ArrayList<Post> timelinePosts = new ArrayList<>();

        for (String postId : allPosts) {
            Post post = this.getPostById(postId);
            String createdBy = post.getUserX().getId();
            if (followers.contains(createdBy) || userId.equals(createdBy)) {
                timelinePosts.add(post);
            }
        }

        return timelinePosts;
    }

    public List<Post> getTimelinePosts(String userId, long offset, long limit) {
        List posts = this.getTimelinePosts(userId);

        int _offset = (int) offset;
        _offset =  Math.min(_offset, posts.size() - (int) limit);
        _offset = Math.max(_offset, 0);

        int _limit = (int) limit;
        _limit = Math.max(_limit, 0);
        _limit = Math.min(_limit, posts.size() - _offset);

        return posts.subList(_offset, _offset + _limit);
    }

    @Override
    public void addPost(Post post) {

        String id = String.valueOf(postId.incrementAndGet());

        post.setId(id);

        String key = Constants.POST_KEY_PREFIX + post.getId();
        stringHashOperations.put(key, Constants.KEY_SUFFIX_MESSAGE, post.getMessage());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_USER, post.getUserX().getId());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_TIME, this.dateFormat.format(post.getTime()));

        ////add post to users post list
        String userPostsKey = Constants.USER_KEY_PREFIX + post.getUserX().getId() + ":" + Constants.KEY_SUFFIX_POSTS;
        setOperations.add(userPostsKey, post.getId());

        Long score = post.getTime().getTime();
        zSetOperationsPost.add(Constants.KEY_GET_ALL_GLOBAL_POSTS_2, post.getId(), score);
    }

    @Override
    public void updatePost (Post post) {
        String key = Constants.POST_KEY_PREFIX + post.getId();
        stringHashOperations.put(key, Constants.KEY_SUFFIX_MESSAGE, post.getMessage());
    }


    @Override
    public void deletePost (Post post) {

        String key = Constants.POST_KEY_PREFIX + post.getId();
        stringHashOperations.delete(key, Constants.KEY_SUFFIX_MESSAGE);
        stringHashOperations.delete(key, Constants.KEY_SUFFIX_USER);
        stringHashOperations.delete(key, Constants.KEY_SUFFIX_TIME);

        String userPostsKey = Constants.USER_KEY_PREFIX + post.getUserX().getId() + ":" + Constants.KEY_SUFFIX_POSTS;
        setOperations.remove(userPostsKey, post.getId());

        zSetOperationsPost.remove(Constants.KEY_GET_ALL_GLOBAL_POSTS_2, post.getId());
    }


    @Override
    public Post getPostById(String id) {

        if (stringRedisTemplate.hasKey(Constants.POST_KEY_PREFIX + id)) {

            String userId = stringHashOperations.get(Constants.POST_KEY_PREFIX + id, Constants.KEY_SUFFIX_USER);
            UserX userX = getUserById(userId);
            String message = stringHashOperations.get(Constants.POST_KEY_PREFIX + id, Constants.KEY_SUFFIX_MESSAGE);
            String timeString = stringHashOperations.get(Constants.POST_KEY_PREFIX + id, Constants.KEY_SUFFIX_TIME);
            Date time = new Date();
            if (timeString != null) {
                try {
                    time = this.dateFormat.parse(timeString);
                } catch (Exception error) {
                    System.out.println("Error parsing date");
                    System.out.println(error);
                };
            }
            Post post = new Post(userX, message, time);
            post.setId(id);
            return post;
        }

        return null;
    }
}
