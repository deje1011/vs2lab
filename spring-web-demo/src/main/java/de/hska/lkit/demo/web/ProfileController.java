package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.TestData;
import de.hska.lkit.demo.web.data.model.Query;
import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jessedesaever on 24.10.16.
 */
@Controller
public class ProfileController {


    private final DataRepository dataRepository;

    @Autowired
    public ProfileController(DataRepository dataRepository){
        super();
        this.dataRepository = dataRepository;
    }

    private String getProfile (String userId, Model model) {
        UserX user = this.dataRepository.getUserById(userId);

        if (user == null) {
            return "login";
        }

        model.addAttribute("userX", user);
        model.addAttribute("followerNumber", this.dataRepository.getAllFollowers(userId).size());
        model.addAttribute("followedNumber", this.dataRepository.getAllFollowed(userId).size());

        return "profile";
    }


    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String deliverProfileTemplate(@CookieValue("TWITTER_CLONE_SESSION") String userId, @ModelAttribute UserX userX, Model model) {
        return this.getProfile(userId, model);
    }
    @RequestMapping(value = "/profile", params = {"username"}, method = RequestMethod.GET)
    public String deliverProfileTemplateByName(@CookieValue("TWITTER_CLONE_SESSION") String currentUserId, @RequestParam(value = "username") String username, @ModelAttribute UserX userX, Model model) {
        String userId = this.dataRepository.getUserId(username);
        Set<String> followerIds = this.dataRepository.getAllFollowed(userId);

        model.addAttribute("showFollowButton", !followerIds.contains(currentUserId));
        model.addAttribute("showUnfollowButton", followerIds.contains(currentUserId));
        return this.getProfile(userId, model);
    }

    @RequestMapping(value = "api/current-user/follow/{userToFollowId}", method = RequestMethod.POST)
    public @ResponseBody boolean follow (@CookieValue("TWITTER_CLONE_SESSION") String userId, @PathVariable String userToFollowId) {
        this.dataRepository.addFollower(userId, userToFollowId);
        return true;
    }


    @RequestMapping(value = "api/current-user/unfollow/{userToUnfollowId}", method = RequestMethod.POST)
    public @ResponseBody boolean unfollow (@CookieValue("TWITTER_CLONE_SESSION") String userId, @PathVariable String userToUnfollowId) {
        System.out.println(userId + ':' + userToUnfollowId);
        this.dataRepository.removeFollower(userId, userToUnfollowId);
        return true;
    }
}