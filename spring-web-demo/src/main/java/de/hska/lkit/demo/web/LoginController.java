package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
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
    @RequestMapping(value = "/loginU", method = RequestMethod.POST)
    public String loginU(UserX userX) {

        if (!userX.getName().isEmpty() && !userX.getPassword().isEmpty()){

            if (dataRepository.isPasswordValid(userX.getName(), userX.getPassword())){
                this.dataRepository.loginUser(userX);
                return "timeline";
            } else {
                System.out.print("login failed");
            }
        }

        return "login";
    }

    @RequestMapping(value = "/login")
    public String login(UserX userX) {
        return "login";
    }
}
