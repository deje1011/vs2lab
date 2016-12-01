package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.Post;
import de.hska.lkit.demo.web.data.model.Userx;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

/**
 * Created by jessedesaever on 24.10.16.
 */
@Controller
public class TimelineController {


    private DataRepository dataRepository;
    private Userx fakeUser = new Userx("Jesse", "123");

    @Autowired
    public TimelineController (DataRepository dataRepository) {
        super();
        this.dataRepository = dataRepository;
        this.fakeUser.setId("1");
    }


    /*private class FakeDatabase {

        private ArrayList<Post> posts = new ArrayList<Post>();

        public FakeDatabase () {
            for (int i = 0; i < 1000; i++) {
                this.posts.add(new Post(fakeUser, "This is post #" + (i + 1), new Date()));
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
            this.posts.add(0, new Post(fakeUser, content, new Date()));
        }


        public int indexOfPost (String postId) {
            int index = -1;
            int numberOfPosts = this.countTimelinePosts(""); // TODO: userId für Authorization
            for (int i = 0; i < numberOfPosts && index == -1; i++) {
                Post post = this.posts.get(i);
                if (post.getId() == postId) {
                    index = i;
                }
            }
            return index;
        }

        public void deletePost (String postId) {
            int index = indexOfPost(postId);
            if (index != -1) {
                this.posts.remove(index);
            }
        }

        public void updatePost (String postId, String content) {
            int index = indexOfPost(postId);
            if (index != -1) {
                this.posts.get(index).setMessage(content);
            }
        }



        public int countTimelinePosts (String userId) {
            return this.posts.size();
        };
    }

    private FakeDatabase database = new FakeDatabase();
    */


    @RequestMapping(value = "/timeline")
    public String deliverTimelineTemplate() {
        return "timeline";
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts/count", method = RequestMethod.GET)
    public @ResponseBody int countTimelinePostsForUser (@PathVariable String userId) {
        //return this.database.countTimelinePosts(userId);
        return this.dataRepository.getAllGlobalPosts().size();
    }


    @RequestMapping(value = "/api/users/{userId}/timeline/posts", params = {"offset", "limit"}, method = RequestMethod.GET)
    public @ResponseBody Post[] getTimelinePostsForUser (@PathVariable String userId, @RequestParam(value = "offset") int offset, @RequestParam(value = "limit") int limit) {
        //return this.database.getTimelinePosts(userId, limit, offset);
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
        //this.database.addPost(userId, content);
        this.dataRepository.addPost(new Post(this.fakeUser, content, new Date()));
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
