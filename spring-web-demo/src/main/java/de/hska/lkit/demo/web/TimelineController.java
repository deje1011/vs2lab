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
    }


    @RequestMapping(value = "/timeline")
    public String deliverTimelineTemplate (@CookieValue("TWITTER_CLONE_SESSION") String userId, UserX whyTheFuckDoWeNeedThis) {
        UserX user = this.dataRepository.getUserById(userId);
        if (user == null) {
            return "login";
        }
        return "timeline";
    }


    @RequestMapping(value = "/api/timeline/posts/count", method = RequestMethod.GET)
    public @ResponseBody int countGlobalTimelinePosts (@CookieValue("TWITTER_CLONE_SESSION") String userId) {
        UserX user = this.dataRepository.getUserById(userId);
        if (user == null) {
            return 0;
        }
        return this.dataRepository.getAllGlobalPosts().size();
    }


    @RequestMapping(value = "api/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody Post[] getGlobalTimelinePosts (@CookieValue("TWITTER_CLONE_SESSION") String userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        UserX user = this.dataRepository.getUserById(userId);
        if (user == null) {
            return new Post[0];
        }
        Set<String> postIds = this.dataRepository.getAllGlobalPosts((long) offset, (long) limit);
        String[] postIdsAsArray = postIds.toArray(new String[postIds.size()]);
        Post[] posts = new Post[postIds.size()];
        for (int i = 0; i < postIds.size(); i++) {
            posts[i] = this.dataRepository.getPostById(postIdsAsArray[i]);
        }
        return posts;
    }


    @RequestMapping(value = "/api/current-user/timeline/posts/count", method = RequestMethod.GET)
    public @ResponseBody int countTimelinePostsForUser (@CookieValue("TWITTER_CLONE_SESSION") String userId) {
        UserX user = this.dataRepository.getUserById(userId);
        if (user == null) {
            return 0;
        }
        return this.dataRepository.getTimelinePosts(userId).size();
    }


    @RequestMapping(value = "/api/current-user/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody List<Post> getTimelinePostsForUser (@CookieValue("TWITTER_CLONE_SESSION") String userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        UserX user = this.dataRepository.getUserById(userId);
        if (user == null) {
            return new ArrayList<>();
        }
        return this.dataRepository.getTimelinePosts(userId, (long) offset, (long) limit);
    }

    /*
    * Returns a boolean to indicate success for now as I don't know how to pass errors to the client.
    * Accepts a string containing the content of the new post as the request body.
    * */
    @RequestMapping(value = "/api/current-user/timeline/posts", method = RequestMethod.POST)
    public @ResponseBody boolean createTimelinePostForUser (@CookieValue("TWITTER_CLONE_SESSION") String userId, @RequestBody String content) {
        UserX user = this.dataRepository.getUserById(userId);
        if (user == null) {
            System.out.println("Post Error: User is not logged in");
            return false;
        }
        this.dataRepository.addPost(new Post(user, content, new Date()));
        return true;
    }



    @RequestMapping(value = "/api/posts/{postId}", method = RequestMethod.DELETE)
    public @ResponseBody boolean deleteTimelinePostForUser (@PathVariable String postId) {

        // TODO: Authorization

        Post post = this.dataRepository.getPostById(postId);
        this.dataRepository.deletePost(post);

        return true;
    }

    @RequestMapping(value = "/api/posts/{postId}", method = RequestMethod.PUT)
    public @ResponseBody boolean updateTimelinePostForUser (@PathVariable String postId, @RequestBody String content) {

        // TODO: Authorization

        Post post = this.dataRepository.getPostById(postId);
        post.setMessage(content);
        this.dataRepository.updatePost(post);
        return true;
    }

}
