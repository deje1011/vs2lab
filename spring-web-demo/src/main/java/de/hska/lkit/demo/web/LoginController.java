package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.Userx;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by jessedesaever on 23.10.16.
 */
@Controller
public class LoginController {

    private final DataRepository dataRepository;

    @Autowired
    public LoginController(DataRepository repository){
        super();
        dataRepository = repository;

    }
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(Userx user) {
        if(!user.getName().isEmpty() && !user.getPassword().isEmpty()){

            if(dataRepository.isPasswordValid(user.getName(), user.getPassword())){

                return "timeline";
            }else{
                System.out.print("login false");
            }

        }

        return "login";
    }
}
