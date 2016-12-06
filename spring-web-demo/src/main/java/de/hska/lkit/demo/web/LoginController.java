package de.hska.lkit.demo.web;

import de.hska.lkit.demo.web.data.TestData;
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
        if(!userX.getName().isEmpty() && !userX.getPassword().isEmpty()){
            System.out.print(dataRepository.isPasswordValid(userX.getName(), userX.getPassword()));
            if(dataRepository.isPasswordValid(userX.getName(), userX.getPassword())){

                return "timeline";
            }else{
                System.out.print("login false");
            }
        }

        return "login";
    }

    @RequestMapping(value = "/login")
    public String login(UserX userX) {


        TestData test = new TestData();

        test.testDataBaseMethods(dataRepository);

        return "login";
    }
}
