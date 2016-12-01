package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.Userx;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.apache.tomcat.jni.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by jessedesaever on 23.10.16.
 */
@Controller
public class RegisterController {


    private final DataRepository dataRepository;

    @Autowired
    public RegisterController(DataRepository repository){
        super();
        dataRepository = repository;

    }
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String register(Userx user) {
     //   model.addAttribute("user", new Userx());
        if(dataRepository.isUserNameUnique(user.getName())){
        if(!user.getName().isEmpty() && !user.getPassword().isEmpty()){
            Userx userx = new Userx(user.getName(), user.getPassword());
            dataRepository.registerUser(userx);
                return "login";
            }
        }else{
            System.out.print("user name not unique");
        }

        return "login";
    }
    @RequestMapping(value = "/register")
    public String deliverRegistrationTemplate() {
        return "registration";
    }
}