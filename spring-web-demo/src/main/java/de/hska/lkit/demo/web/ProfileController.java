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


    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public String deliverProfileTemplate(@ModelAttribute UserX userX, Model model) {
        String userID = this.dataRepository.getUserId("test");
        if(userID == null)
            return "login";
        UserX user = this.dataRepository.getUserById(userID);
        if(user == null)
            return "login";
        if(!this.dataRepository.isUserLoggedIn(user))
            return "login";

        model.addAttribute("userX", user);
        model.addAttribute("showFollowButton", false);
        model.addAttribute("showUnfollowButton", false);
        model.addAttribute("followerNumber", this.dataRepository.getAllFollowers(userID).size());
        model.addAttribute("followedNumber", this.dataRepository.getAllFollowed(userID).size());

        return "profile";
    }
    @RequestMapping(value = "/profile", params = {"username"}, method = RequestMethod.GET)
    public String deliverProfileTemplateByName(@RequestParam(value = "username") String username, @ModelAttribute UserX userX, Model model) {
        String userID = this.dataRepository.getUserId(username);
        String currentUserId = "1";
        UserX currentUser = this.dataRepository.getUserById(currentUserId);

        if(userID == null) {
            return "login";
        }

        UserX user = this.dataRepository.getUserById(userID);

        if (user == null) {
            return "login";
        }

        if (!this.dataRepository.isUserLoggedIn(currentUser)) {
            return "login";
        }

        model.addAttribute("userX", user);
        model.addAttribute("followerNumber", this.dataRepository.getAllFollowers(userID).size());
        model.addAttribute("followedNumber", this.dataRepository.getAllFollowed(userID).size());


        Set<String> followerIds = this.dataRepository.getAllFollowed(userID);

        model.addAttribute("showFollowButton", !followerIds.contains(currentUserId));
        model.addAttribute("showUnfollowButton", followerIds.contains(currentUserId));

        return "profile";
    }

    @RequestMapping(value = "api/users/{userId}/follow/{userToFollowId}", method = RequestMethod.POST)
    public @ResponseBody boolean follow (@PathVariable String userId, @PathVariable String userToFollowId) {
        this.dataRepository.addFollower(userId, userToFollowId);
        return true;
    }


    @RequestMapping(value = "api/users/{userId}/unfollow/{userToUnfollowId}", method = RequestMethod.POST)
    public @ResponseBody boolean unfollow (@PathVariable String userId, @PathVariable String userToUnfollowId) {
        System.out.println(userId + ':' + userToUnfollowId);
        this.dataRepository.removeFollower(userId, userToUnfollowId);
        return true;
    }
}