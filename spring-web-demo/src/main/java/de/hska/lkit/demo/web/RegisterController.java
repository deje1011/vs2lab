package de.hska.lkit.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jessedesaever on 23.10.16.
 */
@Controller
public class RegisterController {
    @RequestMapping(value = "/register")
    public String deliverRegistrationTemplate() {
        return "registration";
    }
}