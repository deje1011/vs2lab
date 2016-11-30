package de.hska.lkit.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Created by jessedesaever on 24.10.16.
 */
@Controller
public class TimelineController {
    @RequestMapping(value = "/timeline")
    public String deliverTimelineTemplate() {
        return "timeline";
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts/count", method = RequestMethod.GET)
    public @ResponseBody int countTimelinePostsForUser (@PathVariable int userId) {
        return 1000;
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody Post[] getTimelinePostsForUser (@PathVariable int userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        Post[] posts = new Post[limit];
        for (int i = 0; i < limit; i++) {
            posts[i] = new Post("Hello User " + userId + "! This is post #" + (i + 1 + offset));
        }
        return posts;
    }

    /*
    * Returns a boolean to indicate success for now as I don't know how to pass errors to the client.
    * Accepts a string containing the content of the new post as the request body.
    * */
    @RequestMapping(value = "/api/users/{userId}/timeline/posts", method = RequestMethod.POST)
    public @ResponseBody boolean createTimelinePostForUser (@PathVariable int userId, @RequestBody String content) {
        System.out.println("POST WAS CALLED: " + content);
        return true;
    }


    // TODO: DELETE post
    // TODO: PUT post (?)
}
