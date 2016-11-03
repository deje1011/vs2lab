package de.hska.lkit.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by jessedesaever on 24.10.16.
 */
@Controller
public class ProfileController {
    @RequestMapping(value = "/profile")
    public String deliverTimelineTemplate() {
        return "profile";
    }
}