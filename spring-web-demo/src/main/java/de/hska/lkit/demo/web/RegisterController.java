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
public class RegisterController {


    private final DataRepository dataRepository;

    @Autowired
    public RegisterController(DataRepository repository){
        super();
        dataRepository = repository;
    }


    // Hier erhält der Server das vom Client ausgefüllte Client Objekt dass er bei der register funktion bekommen hat.
    @RequestMapping(value = "/registerU", method = RequestMethod.POST)
    public String registerU(UserX userX) {
     //   model.addAttribute("userX", new UserX());
        if(dataRepository.isUserNameUnique(userX.getName())){
        if(!userX.getName().isEmpty() && !userX.getPassword().isEmpty()){
            UserX userx = new UserX(userX.getName(), userX.getPassword());
            dataRepository.registerUser(userx);
                return "login";
            }
        }else{
            System.out.print("userX name not unique");
        }

        //fehlerfall
        return "registration";
    }

    // Wenn du die Seite /register aufrufst dann gebe die html registration zurück
    // Der Client will ja auf registU daten an den Server schicken, darum brauch der Client auch schon ein UserX objekt
    @RequestMapping(value = "/register")
    public String register(UserX userX) {
        return "registration";
    }
}