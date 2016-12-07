package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import de.hska.lkit.demo.web.data.model.Query;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by jannika on 07.12.16.
 */
public class SearchController {
    private final DataRepository dataRepository;

    @Autowired
    public SearchController(DataRepository repository) {
        super();
        dataRepository = repository;
    }

    public String data;

    @RequestMapping(value = "searchResultU", method = RequestMethod.POST)

    public String getSearchResult(Model model, Query query, @RequestParam String userName) {

        String query2 = query.getData();
        int queryLength = query2.length()-1;
        boolean wildcard = query2.indequeryLength] == "*";
        Set<String> userId = this.dataRepository.getAllUsers();
        List<UserX> allUser = new ArrayList<>();
        List<UserX> searchResultUser = new ArrayList<>();

        for (int i = 0; i < userId.size(); i++) {
            allUser.add(this.dataRepository.getUserById(userId.get(i)));
        }

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

        //wildcard suche
        //auf jedem user von allUser
        //User nickname
        //liste mit usern durchgehen bei jedem user.getname start vergleichen

        return "test";
    }
}








//datenbank name durchgehen und nach string data suchen
//wenn string vorhanden getUser() und in liste speichern
//am ende ganze liste ausgeben