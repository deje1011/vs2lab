package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.Post;
import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Set;

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
        this.dataRepository.loginUser(this.dataRepository.getUserById("1"));
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


    @RequestMapping(value = "/api/users/{userId}/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody Post[] getTimelinePostsForUser (@PathVariable String userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        UserX user = this.dataRepository.getUserById(userId);
        if (this.dataRepository.isUserLoggedIn(user) == false) {
            return new Post[0];
        }
        Set<String> postIds = this.dataRepository.getAllGlobalPosts();
        String[] postIdsAsArray = postIds.toArray(new String[postIds.size()]);
        Post[] posts = new Post[postIds.size()];
        for (int i = 0; i < postIds.size(); i++) {
            posts[i] = this.dataRepository.getPostById(postIdsAsArray[i]);
        }
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
            return false;
        }
        this.dataRepository.addPost(new Post(user, content, new Date()));
        return true;
    }



    @RequestMapping(value = "/api/posts/{postId}", method = RequestMethod.DELETE)
    public @ResponseBody boolean deleteTimelinePostForUser (@PathVariable String postId) {
        //this.database.deletePost(postId);

        //Post post = this.dataRepository.getPostById(postId);
        //System.out.println("DELETE POST " + postId + " -- " + post.getId());
        //post.setUser(fakeUser);
        //this.dataRepository.deletePost(post);

        return true;
    }

    @RequestMapping(value = "/api/posts/{postId}", method = RequestMethod.PUT)
    public @ResponseBody boolean updateTimelinePostForUser (@PathVariable String postId, @RequestBody String content) {
        //this.database.updatePost(postId, content);
        return true;
    }

}
