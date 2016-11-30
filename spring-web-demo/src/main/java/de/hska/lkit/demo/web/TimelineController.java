package de.hska.lkit.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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


    @RequestMapping(value = "/api/users/{userId}/timeline/posts", method = RequestMethod.GET)
    public @ResponseBody Post[] getTimelinePostsForUser (@PathVariable int userId) {
        Post post = new Post("Hello " + userId);
        Post[] posts = new Post[1];
        posts[0] = post;
        return posts;
    }
}
