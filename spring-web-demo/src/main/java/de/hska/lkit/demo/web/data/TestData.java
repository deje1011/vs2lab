package de.hska.lkit.demo.web.data;

import de.hska.lkit.demo.web.data.model.Post;
import de.hska.lkit.demo.web.data.model.User;
import de.hska.lkit.demo.web.data.repo.DataRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Set;

/**
 * Class to test db methods and add user data.
 * Created by Marina on 20.11.2016.
 */
public class TestData {

    private ArrayList<User> mUsers = new ArrayList<>();
    private ArrayList<Post> mPosts = new ArrayList<>();
    private DataRepository mRepository;

    public void testDataBaseMethods(DataRepository repository){
        mRepository = repository;
        addUsersToList();
        //registerUsersToDatabase();
        //printAllUsers();
        //testIsPasswordValid(mUsers.get(0),mUsers.get(5));
        //testIsUserNameUnique("lukas");

        addPostsToList();

        for(Post post: mPosts){
            mRepository.addPost(post);
        }
        printAllPosts();



    }

    private void printAllPosts(){

        Set<String> posts = mRepository.getAllGlobalPosts();

        for(String post : posts){

            Post p = mRepository.getPostById(post);
            System.out.print("\n post:" + post + ":user " + p.getUser().getId());
            System.out.print("\n post:" + post + ":message " + p.getMessage());
            System.out.print("\n post:" + post + ":time " + p.getTime() + "\n");

        }

    }

    private void testIsUserNameUnique(String name){
        System.out.print("\n Is user name unique: "+ name + ": " + mRepository.isUserNameUnique(name));
    }

    private void testIsPasswordValid(String name, String password){

       System.out.print("Is password valid: " + mRepository.isPasswordValid(name, password));
    }

    private void printAllUsers(){
        Set<String> users = mRepository.getAllUsers();
        for(String u : users){

            User userNew = mRepository.getUserById(u);
            System.out.print("\n user:" + userNew.getName() + ":id " + u);
            System.out.print("\n user:" + u +":name " + userNew.getName());
            System.out.print("\n user:" + u + ":password " + userNew.getPassword() + "\n");
        }
 /*   Map<Object, Object> users = dataRepository.getAllUsers();
        for( Map.Entry e : users.entrySet()){
            User u = (User) e.getValue();
            System.out.print("\n output: " + u.getName());
        }*/



    }

    private void registerUsersToDatabase(){

        for(User user: mUsers){
            mRepository.registerUser(user);
        }
    }

    private void addPostsToList(){

        mPosts.add(new Post(mRepository.getUserById("10"), "Hallöchen", Date.from(Instant.now())));
        mPosts.add(new Post(mRepository.getUserById("10"), "Wie gehts", Date.from(Instant.now())));
        mPosts.add(new Post(mRepository.getUserById("10"), "Was machst du so?", Date.from(Instant.now())));
        mPosts.add(new Post(mRepository.getUserById("10"), "Heute abend grillen", Date.from(Instant.now())));
        mPosts.add(new Post(mRepository.getUserById("10"), "Weihnachstmarkt", Date.from(Instant.now())));
        mPosts.add(new Post(mRepository.getUserById("10"), "Das Wetter ist mies", Date.from(Instant.now())));
        mPosts.add(new Post(mRepository.getUserById("10"), "Was solls", Date.from(Instant.now())));
        mPosts.add(new Post(mRepository.getUserById("10"), "Kalt ists draußen", Date.from(Instant.now())));
        mPosts.add(new Post(mRepository.getUserById("10"), "Guten Morgen", Date.from(Instant.now())));



    }


    private void addUsersToList(){

        mUsers.add(new User("tim", "shdfjkj"));
        mUsers.add(new User("lukas", "serger"));
        mUsers.add(new User("tom", "gjkjh"));
        mUsers.add(new User("hans", "fgjgkhjkhj"));
        mUsers.add(new User("peter", "adfgfd"));
        mUsers.add(new User("lisa", "wererztz"));
        mUsers.add(new User("hanna", "yyxvcfnd"));
        mUsers.add(new User("kim", "djghkhj"));
        mUsers.add(new User("andreas", "dfgutghfg"));


    }
}
