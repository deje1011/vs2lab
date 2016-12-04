package de.hska.lkit.demo.web.data.repo;

import de.hska.lkit.demo.web.data.model.Post;

import de.hska.lkit.demo.web.data.model.UserX;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
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

        if (stringRedisTemplate.hasKey(Constants.USER_KEY_PREFIX + id)) {

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
        return null;
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

        //Set<String> posts = setOperations.members(Constants.KEY_GET_ALL_GLOBAL_POSTS);

        Set<String> posts = zSetOperationsPost.range(Constants.KEY_GET_ALL_GLOBAL_POSTS_2, (long)0, zSetOperationsPost.size(Constants.KEY_GET_ALL_GLOBAL_POSTS_2));
        return posts;
    }

    @Override
    public Set<String> getTimelinePosts(String id) {

        Set<String> timelinePosts;
        ArrayList<Set<String>> followerPostSets = new ArrayList<>();

        Set<String> followers = getAllFollowers(id);

        for(String follower: followers){

            UserX userX = getUserById(follower);
            Set<String> posts = userX.getPosts();


            followerPostSets.add(posts);
        }

        Set<String> global = zSetOperationsPost.range(Constants.KEY_GET_ALL_GLOBAL_POSTS_2, (long)0, zSetOperationsPost.size(Constants.KEY_GET_ALL_GLOBAL_POSTS_2));

        String key = Constants.USER_KEY_PREFIX + id + ":" + Constants.KEY_SUFFIX_TIMELINE_POSTS;
      //  zSetOperationsPost.intersectAndStore(followerPostSets.get(0), key);
        UserX userX = getUserById(id);
        Set<String> posts = userX.getPosts();

        String key2 = Constants.USER_KEY_PREFIX + id + ":" + Constants.KEY_SUFFIX_POSTS;

        setOperations.unionAndStore(key2,followerPostSets.get(0),key);

        Set<String> results = setOperations.members(key);

        return results;
    }

    @Override
    public void addPost(Post post) {

        String id = String.valueOf(postId.incrementAndGet());

        post.setId(id);

        String key = Constants.POST_KEY_PREFIX + post.getId();
       // stringHashOperations.put(key, Constants.KEY_SUFFIX_ID, post.getId());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_MESSAGE, post.getMessage());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_USER, post.getUserX().getId());
        stringHashOperations.put(key, Constants.KEY_SUFFIX_TIME, post.getTime().toString());



        ////add post to users post list
        String userPostsKey = Constants.USER_KEY_PREFIX + post.getUserX().getId() + ":" + Constants.KEY_SUFFIX_POSTS;
        setOperations.add(userPostsKey, post.getId());


        ////add post to global post list

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
            UserX userX = getUserById(userId);
            String message = stringHashOperations.get(Constants.POST_KEY_PREFIX + id, Constants.KEY_SUFFIX_MESSAGE);
            String timeString = stringHashOperations.get(Constants.POST_KEY_PREFIX + id, Constants.KEY_SUFFIX_TIME);
            Date time = new Date();
            if(timeString!=null) {
                time = new Date();

            }
            Post post = new Post(userX, message,time);
            post.setId(id);
            return post;
        }

        return null;
    }
}