package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import de.hska.lkit.demo.web.data.session.SimpleSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sun.plugin2.message.CookieOpMessage;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Created by jessedesaever on 23.10.16.
 */
@Controller
public class LoginController {

    private final DataRepository dataRepository;
    private static final Duration TIMEOUT = Duration.ofMinutes(1);

    @Autowired
    public LoginController(DataRepository repository){
        super();
        dataRepository = repository;
    }
    @RequestMapping(value = "/loginU", method = RequestMethod.POST)
    public String loginU(UserX userX, HttpServletResponse response,Model model ) {

        if (!userX.getName().isEmpty() && !userX.getPassword().isEmpty()){

            if (dataRepository.isPasswordValid(userX.getName(), userX.getPassword())){
                this.dataRepository.loginUser(userX);


              /*  if (dataRepository.auth(userX.getName(), userX.getPassword())) {

                    String auth = dataRepository.addAuth(userX.getName(),TIMEOUT.getSeconds(), TimeUnit.SECONDS);
                    Cookie cookie = new Cookie("auth", auth);
                    response.addCookie(cookie);
                    model.addAttribute("user", userX.getName());
                }*/

                return "timeline" ;
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



   /* @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(){
        if(SimpleSecurity.isSignedIn()){
            String name = SimpleSecurity.getName();
            dataRepository.deleteAuth(name);
        }
        return "login";
    }*/
}
