package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.TestData;
import de.hska.lkit.demo.web.data.model.Query;
import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    @RequestMapping(value = "/searchU", method = RequestMethod.POST)
    public String getSearchResult(Model model, Query query) {

        // int queryLength = query.getData().length()-1;
        // String stringstar = Integer.toString(queryLength);
        boolean wildcard = query.getData().endsWith("*");


        Set<String> userId = this.dataRepository.getAllUsers();
        List<UserX> allUser = new ArrayList<>();
        List<UserX> searchResultUser = new ArrayList<>();


        Iterator<String> iterator = userId.iterator();
        while (iterator.hasNext()) {
            allUser.add(this.dataRepository.getUserById(iterator.next()));
        }

        //  for (int i = 0; i < userId.size(); i++) {
        //      allUser.add(this.dataRepository.getUserById(userId.get(i)));
        //  }

        if (wildcard == true) {
            String searchData = query.getData().substring(0, query.getData().length()-1); //vom 1. bis zum vorletzen zeichen slice funktion von 0,length-1
            for (int i = 0; i < allUser.size(); i++) {
                if (allUser.get(i).getName().startsWith(searchData)) {
                    searchResultUser.add(allUser.get(i));
                }
            }
        } else {
            for (int i = 0; i < allUser.size(); i++) {
                if (allUser.get(i).getName().equals(query.getData())) {
                    searchResultUser.add(allUser.get(i));
                }
            }
        }
        model.addAttribute("result", searchResultUser);

        return "search";
    }

    @RequestMapping(value = "/profile")
    public String deliverTimelineTemplate(Query query) {

        return "profile";
    }
}