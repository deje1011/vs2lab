package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.Post;
import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by jessedesaever on 24.10.16.
 */
@Controller
public class TimelineController {


    private DataRepository dataRepository;

    @Autowired
    public TimelineController (DataRepository dataRepository) {
        super();
        this.dataRepository = dataRepository;

        // For testing
        // this.dataRepository.loginUser(this.dataRepository.getUserById("1"));
    }


    @RequestMapping(value = "/timeline")
    public String deliverTimelineTemplate() {
        return "timeline";
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts/count", method = RequestMethod.GET)
    public @ResponseBody int countTimelinePostsForUser (@PathVariable String userId) {
        UserX user = this.dataRepository.getUserById(userId);
        if (this.dataRepository.isUserLoggedIn(user) == false) {
            return 0;
        }
        return this.dataRepository.getAllGlobalPosts().size();
    }


    @RequestMapping(value = "api/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody Post[] getGlobalTimelinePosts (@RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        Set<String> postIds = this.dataRepository.getAllGlobalPosts((long) offset, (long) limit);
        String[] postIdsAsArray = postIds.toArray(new String[postIds.size()]);
        Post[] posts = new Post[postIds.size()];
        for (int i = 0; i < postIds.size(); i++) {
            posts[i] = this.dataRepository.getPostById(postIdsAsArray[i]);
        }
        return posts;
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody List<Post> getTimelinePostsForUser (@PathVariable String userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        UserX user = this.dataRepository.getUserById(userId);
        if (this.dataRepository.isUserLoggedIn(user) == false) {
            return new ArrayList<>();
        }
        List<Post> posts = this.dataRepository.getTimelinePosts(userId, (long) offset, (long) limit);
        return posts;
    }

    /*
    * Returns a boolean to indicate success for now as I don't know how to pass errors to the client.
    * Accepts a string containing the content of the new post as the request body.
    * */
    @RequestMapping(value = "/api/users/{userId}/timeline/posts", method = RequestMethod.POST)
    public @ResponseBody boolean createTimelinePostForUser (@PathVariable String userId, @RequestBody String content) {
        UserX user = this.dataRepository.getUserById(userId);
        if (this.dataRepository.isUserLoggedIn(user) == false) {
            System.out.println("Post Error: User is not logged in");
            return false;
        }
        this.dataRepository.addPost(new Post(user, content, new Date()));
        return true;
    }



    @RequestMapping(value = "/api/posts/{postId}", method = RequestMethod.DELETE)
    public @ResponseBody boolean deleteTimelinePostForUser (@PathVariable String postId) {

        Post post = this.dataRepository.getPostById(postId);
        this.dataRepository.deletePost(post);

        return true;
    }

    @RequestMapping(value = "/api/posts/{postId}", method = RequestMethod.PUT)
    public @ResponseBody boolean updateTimelinePostForUser (@PathVariable String postId, @RequestBody String content) {
        Post post = this.dataRepository.getPostById(postId);
        post.setMessage(content);
        this.dataRepository.updatePost(post);
        return true;
    }

}
