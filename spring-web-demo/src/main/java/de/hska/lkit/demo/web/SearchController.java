package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.model.UserX;
import de.hska.lkit.demo.web.data.repo.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import de.hska.lkit.demo.web.data.model.Query;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by whatseb on 23.11.16.
 */
@Controller
public class SearchController {
    private final DataRepository dataRepository;

    @Autowired
    public SearchController(DataRepository repository) {
        super();
        dataRepository = repository;

        this.dataRepository.addFollower("1", "2");
        this.dataRepository.addFollower("2", "1");
    }

    public String data;


    private List<UserX> search (String searchData) {

        String[] parts = searchData.split(":");
        boolean isSearchForFollower = false;
        boolean isSearchForFollowed = false;

        if (parts.length > 1) {
            if (parts[0].equals("followers")) {
                isSearchForFollower = true;
            } else if (parts[0].equals("followed")) {
                isSearchForFollowed = true;
            }
            searchData = parts[1];
        }

        Set<String> userId = this.dataRepository.getAllUsers();
        List<UserX> allUser = new ArrayList<>();
        List<UserX> searchResultUser = new ArrayList<>();


        Iterator<String> iterator = userId.iterator();
        while (iterator.hasNext()) {
            allUser.add(this.dataRepository.getUserById(iterator.next()));
        }

        if (isSearchForFollowed) {
            String userIdToGetFollowedOf = this.dataRepository.getUserId(searchData);
            Set<String> allFollowedIds = this.dataRepository.getAllFollowed(userIdToGetFollowedOf);
            Iterator<String> iteratorFollowed = allFollowedIds.iterator();
            while (iteratorFollowed.hasNext()) {
                searchResultUser.add(this.dataRepository.getUserById(iteratorFollowed.next()));
            }
        } else if (isSearchForFollower) {
            String userIdToGetFollowersOf = this.dataRepository.getUserId(searchData);
            Set<String> allFollowerIds = this.dataRepository.getAllFollowers(userIdToGetFollowersOf);
            Iterator<String> iteratorFollows = allFollowerIds.iterator();
            while (iteratorFollows.hasNext()) {
                searchResultUser.add(this.dataRepository.getUserById(iteratorFollows.next()));
            }
        } else {
            for (int i = 0; i < allUser.size(); i++) {
                String username = allUser.get(i).getName().toLowerCase();
                if (username.startsWith(searchData)) {
                    searchResultUser.add(allUser.get(i));
                }
            }
        }
        return searchResultUser;
    }

    @RequestMapping(value = "/searchU", method = RequestMethod.POST)
    public String getSearchResult(Model model, Query query) {
        String searchData = query.getData();//.toLowerCase();
        model.addAttribute("result", this.search(searchData));
        return "search";
    }

    @RequestMapping(value = "/searchFollowed", params = {"username"}, method = RequestMethod.GET)
    public String getFollowed (Model model, @RequestParam(value = "username") String username, Query query) {
        String searchData = "followed:" + username;
        model.addAttribute("result", this.search(searchData));
        return "search";
    }

    @RequestMapping(value = "/searchFollowers", params = {"username"}, method = RequestMethod.GET)
    public String getFollowers (Model model, @RequestParam(value = "username") String username, Query query) {
        String searchData = "followers:" + username;
        model.addAttribute("result", this.search(searchData));
        return "search";
    }


    @RequestMapping(value = "/search")
    public String search(Model model, Query query, @CookieValue("TWITTER_CLONE_SESSION") String userId, UserX aintNobodyGotTimeForThat){
        UserX user = this.dataRepository.getUserById(userId);
        if (user == null) {
            return "login";
        }
        return "search";
    }
}
