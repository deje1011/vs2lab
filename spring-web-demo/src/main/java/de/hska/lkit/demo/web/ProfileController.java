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
        model.addAttribute("profileUser", user);
        model.addAttribute("followerNumber", this.dataRepository.getAllFollowers(userID).size());
        model.addAttribute("followedNumber", this.dataRepository.getAllFollowed(userID).size());

        return "profile";
    }
    @RequestMapping(value = "/profile", params = {"username"}, method = RequestMethod.GET)
    public String deliverProfileTemplateByName(@RequestParam(value = "username") String username, @ModelAttribute UserX userX, Model model) {
        String userID = this.dataRepository.getUserId(username);
        if(userID == null)
            return "login";
        UserX user = this.dataRepository.getUserById(userID);
        if(user == null)
            return "login";
        /*if(!this.dataRepository.isUserLoggedIn(user))
            return "login";*/
        model.addAttribute("userX", user);
        return "profile";
    }
}