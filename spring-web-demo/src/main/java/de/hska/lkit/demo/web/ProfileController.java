package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.User;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

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
    public String deliverTimelineTemplate() {

        User user = new User();
        user.setName("marina");
        user.setPassword("xyz");
        dataRepository.addUser(user);
        Map<Object, Object> users = dataRepository.getAllUsers();
        for( Map.Entry e : users.entrySet()){
            User u = (User) e.getValue();
            System.out.print("\n output: " + u.getName());
        }
        String string = dataRepository.getUserId("marina");
        System.out.print("\n output: " + string);

        return "profile";
    }
}