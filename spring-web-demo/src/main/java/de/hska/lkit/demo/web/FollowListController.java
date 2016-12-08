package de.hska.lkit.demo.web;
import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jessedesaever on 23.10.16.
 */
@Controller
public class FollowListController {

    private final DataRepository dataRepository;

    @Autowired
    public FollowListController(DataRepository repository) {
        super();
        dataRepository = repository;
    }

    @RequestMapping(value = "followList")
    public String followList(Model model, @RequestParam String followType, @RequestParam String userName) {
        String userId = this.dataRepository.getUserId(userName);
        if (followType.equals("follower")) {
            Set<String> followerListId = this.dataRepository.getAllFollowers(userId);

            List<UserX> followerList = new ArrayList<>();

            Iterator<String> iterator = followerListId.iterator();
            while (iterator.hasNext()) {
                followerList.add(this.dataRepository.getUserById(iterator.next()));
            }

            model.addAttribute("followerList", followerList);
            model.addAttribute("active", "follower");
        } else if (followType.equals("followed")) {
            Set<String> followedListId = this.dataRepository.getAllFollowed(userId);
            List<UserX> followedList = new ArrayList<>();

            Iterator<String> iterator = followedListId.iterator();
            while (iterator.hasNext()) {
                followedList.add(this.dataRepository.getUserById(iterator.next()));
            }
            model.addAttribute("followedList", followedList);
            model.addAttribute("active", "followed");
        }
        return "followList";
    }

 /*   @RequestMapping(value = "follow/toFollow")
    public void followStatus(@RequestParam String name) { //String
        this.dataRepository.addFollower(sessionVerwaltung.getActualUserID, name);
        // Wird für den anderen User automatisch isFollowedBy gesetzt?
        //return "followList";
    }

    @RequestMapping(value = "follow/toUnFollow")
    public void followStatus(@RequestParam String name) { //String
        this.dataRepository.removeFollower((sessionVerwaltung.getActualUserID, name);
        //return "followList";
    }*/
}