package de.hska.lkit.demo.web.data.repo;

import de.hska.lkit.demo.web.data.model.Post;
import de.hska.lkit.demo.web.data.model.UserX;

import java.util.Set;

/**
 * Interface to specify interactions with database.
 *
 * Created by Marina on 20.11.2016.
 */
public interface DataRepository {

    /**
     * Adds userX to db.
     * @param userX userX to add
     */
    void registerUser(UserX userX);

    /**
     * Checks if username has already been taken.
     * @param name name to validate
     * @return true if name is unique
     */
    boolean isUserNameUnique(String name);

    /**
     * Compares passwords.
     * @param name name of user to check password for
     * @param password password entered
     * @return true if password is valid
     */
    boolean isPasswordValid(String name, String password);

    /**
     * Get the id of a certain user
     * @param name name of user
     * @return id of user as string
     */
    String getUserId(String name);

    /**
     * Returns a sorted set of all user ids. Ids are sorted by the first 4 letters of a user name.
     * @return users
     */
    Set<String> getAllUsers();

    /**
     * Returns user with the given id.
     * @param id id of user
     * @return certain user
     */
    UserX getUserById(String id);

    /**
     * Returns a set of user ids followers of a certain user.
     * @param userId id of user
     * @return followers of user
     */
    Set<String> getAllFollowers(String userId);

    /**
     * Returns a set of user ids followed by certain user.
     * @param userId id of user
     * @return followed by user
     */
    Set<String> getAllFollowed(String userId);

    /**
     * Adds a follower.
     * @param currentUserId user who is following another user
     * @param userToFollowId user who is being followed.
     */
    void addFollower(String currentUserId, String userToFollowId);

    /**
     * Removes a follower.
     * @param currentUserId user who is unfollowing another user
     * @param userToUnfollow user who is being unfollowed
     */
    void removeFollower(String currentUserId, String userToUnfollow);

    /**
     * Returns a sorted set of all global post ids.
     *
     * limit
     * offset
     * @return set of all global posts
     */
    Set<String> getAllGlobalPosts();

    /**
     * Return a set of all timeline post ids of a certain user.
     *
     * limit: anzahl posts
     * offset: wo anfangen.
     * @param id id of user
     * @return set of posts
     */
    Set<String> getTimelinePosts(String id);

    /**
     * Adds a post.
     * @param post post to add.
     */
    void addPost(Post post);

    /**
     * Deletes a post.
     * @param post post to delete.
     */
    void deletePost(Post post);


    /**
     * Returns post with the given id.
     * @param id id of post
     * @return certain post
     */
    Post getPostById(String id);
 }
