package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.TestData;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

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

        return "profile";
    }
}