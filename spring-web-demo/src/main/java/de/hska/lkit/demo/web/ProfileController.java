package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.User;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
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


    @RequestMapping(value = "/profile")
    public String deliverTimelineTemplate() {

        User user = new User();
        user.setName("marina");
        user.setPassword("xyz");
        dataRepository.registerUser(user);
     /*   Map<Object, Object> users = dataRepository.getAllUsers();
        for( Map.Entry e : users.entrySet()){
            User u = (User) e.getValue();
            System.out.print("\n output: " + u.getName());
        }*/

        Set<String> users = dataRepository.getAllUsers();
        for(String u : users){
            System.out.print("\n user:id " + u);
            User userNew = dataRepository.getUserById(u);
            System.out.print("\n user:name " + userNew.getName());
            System.out.print("\n user:password " + userNew.getPassword() + "\n");
        }

        String string = dataRepository.getUserId("marina");

        return "profile";
    }
}