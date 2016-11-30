package de.hska.lkit.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by jessedesaever on 24.10.16.
 */
@Controller
public class TimelineController {
    @RequestMapping(value = "/timeline")
    public String deliverTimelineTemplate() {
        return "timeline";
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody Post[] getTimelinePostsForUser (@PathVariable int userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        Post[] posts = new Post[limit];
        for (int i = 0; i < limit; i++) {
            posts[i] = new Post("Hello User " + userId + "! This is post #" + (i + 1 + offset));
        }
        return posts;
    }

    // TODO: POST post
    // TODO: DELETE post
    // TODO: PUT post (?)
}
