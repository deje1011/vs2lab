package de.hska.lkit.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

/**
 * Created by jessedesaever on 24.10.16.
 */
@Controller
public class TimelineController {


    private class FakeDatabase {

        private ArrayList<Post> posts = new ArrayList<Post>();

        public FakeDatabase () {
            for (int i = 0; i < 1000; i++) {
                this.posts.add(new Post("This is post #" + (i + 1)));
            }
        }

        public Post[] getTimelinePosts(String userId, int limit, int offset) {

            int numberOfTimelinePosts = this.countTimelinePosts(userId);
            limit = Math.max(numberOfTimelinePosts - offset, 0);
            offset = Math.min(numberOfTimelinePosts, offset);

            Post[] posts = new Post[limit];
            for (int i = 0; i < limit; i++) {
                posts[i] = this.posts.get(i + offset);
            }
            
            return posts;
        }

        public void addPost (String userId, String content) {
            this.posts.add(0, new Post(content));
        }

        public int countTimelinePosts (String userId) {
            return this.posts.size();
        };
    }

    private FakeDatabase database = new FakeDatabase();


    @RequestMapping(value = "/timeline")
    public String deliverTimelineTemplate() {
        return "timeline";
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts/count", method = RequestMethod.GET)
    public @ResponseBody int countTimelinePostsForUser (@PathVariable String userId) {
        return this.database.countTimelinePosts(userId);
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody Post[] getTimelinePostsForUser (@PathVariable String userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        return this.database.getTimelinePosts(userId, limit, offset);
    }

    /*
    * Returns a boolean to indicate success for now as I don't know how to pass errors to the client.
    * Accepts a string containing the content of the new post as the request body.
    * */
    @RequestMapping(value = "/api/users/{userId}/timeline/posts", method = RequestMethod.POST)
    public @ResponseBody boolean createTimelinePostForUser (@PathVariable String userId, @RequestBody String content) {
        System.out.println("GOT NEW POST:" + content);
        this.database.addPost(userId, content);
        return true;
    }


    // TODO: DELETE post
    // TODO: PUT post (?)
}
