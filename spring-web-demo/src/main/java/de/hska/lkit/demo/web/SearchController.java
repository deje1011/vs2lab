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
 * Created by whatseb on 23.11.16.
 */
public class SearchController {
    private final DataRepository dataRepository;

    @Autowired
    public SearchController(DataRepository repository) {
        super();
        dataRepository = repository;
    }

    public String data;



    @RequestMapping(value = "/search")
    public String search(Model model, Query query){ return "search";}
}
