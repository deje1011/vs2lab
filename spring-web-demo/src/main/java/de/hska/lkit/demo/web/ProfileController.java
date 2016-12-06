package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.TestData;
import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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


    @RequestMapping(value = "/profile")
    public String deliverTimelineTemplate(Model model) {

        UserX user= new UserX("marina", "marina");
        UserX user2 = new UserX("india","indai");
        dataRepository.registerUser(user);
        dataRepository.registerUser(user2);
        String id2 = dataRepository.getUserId(user2.getName());
        String id = dataRepository.getUserId(user.getName());
        dataRepository.addFollower(id,id2);
       // String userId = this.dataRepository.getUserId(userName);
        model.addAttribute("profileUser", this.dataRepository.getUserById(id));
        if(this.dataRepository.getAllFollowers(id).size() == 0){
            model.addAttribute("followerNumber", "0");
        }else{
            model.addAttribute("followerNumber", this.dataRepository.getAllFollowers(id).size());
        }

        if(this.dataRepository.getAllFollowed(id).size() == 0){
            model.addAttribute("followedNumber", "0");
        }else{
            model.addAttribute("followedNumber", this.dataRepository.getAllFollowed(id).size());
        }



        return "profile";
    }

   /* @RequestMapping(value="follow/toFollow")
    public void followStatus(@RequestParam String name) { //String
        this.dataRepository.addFollower(sessionVerwaltung.getActualUserID, name);
        // Wird für den anderen User automatisch isFollowedBy gesetzt?
        //return "followList";
    }

    @RequestMapping(value="follow/toUnFollow")
    public void followStatus(@RequestParam String name) { //String
        this.dataRepository.removeFollower((sessionVerwaltung.getActualUserID, name);
        //return "followList";
    }*/

   /* ublic String deliverTimelineTemplate(@RequestParam String userName, Model model) {
        // Bei einem Profilbesuch wird das gesuchte Profil als RequestParam übergeben
        String userId = this.dataRepository.getUserId(userName);
        model.addAttribute("profileUser", this.dataRepository.getUserById(userId));
        //model.addAttribute(("actualUser", sessionVerwaltung.getActualUser());

        model.addAttribute("followNumber", this.dataRepository.getAllFollowed(userName).size());
        model.addAttribute("followerNumber", this.dataRepository.getAllFollowers(userName).size());
        return "profile";
    }*/
}