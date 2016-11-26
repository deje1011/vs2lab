package de.hska.lkit.demo.web.data.repo;

import de.hska.lkit.demo.web.data.model.Post;
import de.hska.lkit.demo.web.data.model.User;
import java.util.Map;
import java.util.Set;

/**
 * Interface to specify interactions with database.
 *
 * Created by Marina on 20.11.2016.
 */
public interface DataRepository {

    /**
     * Adds user to db.
     * @param user user to add
     */
    void registerUser(User user);

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
     * Returns a set of all user ids.
     * @return users
     */
    Set<String> getAllUsers();

    /**
     * Returns user with the given id.
     * @param id id of user
     * @return certain user
     */
    User getUserById(String id);

    /**
     * Returns a set of user ids followers of a certain user.
     * @param id id of user
     * @return followers of user
     */
    Set<String> getAllFollowers(String id);

    /**
     * Returns a set of user ids followed by certain user.
     * @param id id of user
     * @return followed by user
     */
    Set<String> getAllFollowed(String id);

    /**
     * Adds the current user to the set of followers of a certain user
     * and adds a certain user to the set of followed of the current user.
     * @param currentUserId user who is following another user
     * @param userToFollowId user who is being followed.
     */
    void addFollower(String currentUserId, String userToFollowId);

    /**
     * Removes the current user from the set of followers of a certain user
     * and removes a certain user from the set of followed of the current user.
     * @param currentUserId user who is unfollowing another user
     * @param userToUnfollow user who is being unfollowed
     */
    void removeFollower(String currentUserId, String userToUnfollow);

    /**
     * Returns a set of all global post ids.
     * @return set of all global posts
     */
    Set<String> getAllGlobalPosts();

    /**
     * Return a set of all timeline post ids of a certain user.
     * @param id id of user
     * @return set of posts
     */
    Set<String> getTimelinePosts(String id);

    /**
     * Adds a post to the timeline set of a certain user.
     * Adds a post to the timeline sets of all followers.
     * Adds a post to the global posts set.
     * @param post post to add.
     */
    void addPost(Post post);


    /**
     * Returns post with the given id.
     * @param id id of post
     * @return certain post
     */
    Post getPostById(String id);
 }
