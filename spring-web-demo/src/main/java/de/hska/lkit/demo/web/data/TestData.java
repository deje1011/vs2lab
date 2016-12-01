package de.hska.lkit.demo.web.data;

import de.hska.lkit.demo.web.data.model.Post;

import de.hska.lkit.demo.web.data.model.Userx;
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

    private ArrayList<Userx> mUsers = new ArrayList<>();
    private ArrayList<Post> mPosts = new ArrayList<>();
    private DataRepository mRepository;

    public void testDataBaseMethods(DataRepository repository){
        mRepository = repository;
        addUsersToList();
        //registerUsersToDatabase();


        Set<String> users = mRepository.getAllUsers();


        mRepository.addFollower("115", "128");
        Userx user = mRepository.getUserById("115");

        printAllTimelinePosts("115");

        //testIsPasswordValid(mUsers.get(0),mUsers.get(5));
        //testIsUserNameUnique("lukas");

     /*   addPostsToList();

        for(Post post: mPosts){
            mRepository.addPost(post);
        }
        printAllPosts();

        */





    }

    private void printAllTimelinePosts(String userid){

        Set<String> posts = mRepository.getTimelinePosts(userid);

        for(String post: posts){

            Post p = mRepository.getPostById(post);
            System.out.print("\nPost Conent: " + p.getMessage());
        }

    }
    private void printAllFollowers(String userId){

        Set<String> followers = mRepository.getAllFollowers(userId);

        for(String follower: followers){
            System.out.print("\n Der User: " + userId + " folgt: " + follower );
        }
    }

    private void printAllFollowed(String userId){

        Set<String> followers = mRepository.getAllFollowed(userId);

        for(String follower: followers){
            System.out.print("\n Dem User: " + userId + " folgt: " + follower );
        }
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

            Userx user = mRepository.getUserById(u);
            System.out.print("\n user:" + user.getName() + ":id " + u);
            System.out.print("\n user:" + u +":name " + user.getName());
            System.out.print("\n user:" + u + ":password " + user.getPassword() + "\n");
            Set<String> follows = user.getFollows();

            if(follows != null) {
                for (String follow : follows) {
                    Userx p = mRepository.getUserById(follow);
                    System.out.print("\n user:" + u + ":follows:" + p.getId() + ":" + p.getName());
                }
            }

            Set<String> followedBy = user.getFollowed();

            if(followedBy != null) {
                for (String follow : followedBy) {
                    Userx p = mRepository.getUserById(follow);
                    System.out.print("\n user:" + u + ":followedBy:" + p.getId() + ":" + p.getName());
                }
            }






           /* Set<String> posts = user.getPosts();

            if(posts != null) {
                for (String post : posts) {
                    Post p = mRepository.getPostById(post);
                    System.out.print("\n user:" + u + ":posts:" + p.getId() + ":" + p.getMessage());
                }
            }*/
            System.out.print("\n ");
        }
    }

    private void registerUsersToDatabase(){

        for(Userx user: mUsers){
           // if(mRepository.isUserNameUnique(user.getName())) {
                mRepository.registerUser(user);
           // }
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

    private void addPostForUser(String userid){

        mRepository.addPost(new Post(mRepository.getUserById(userid), "This is a message from user: " + userid, Date.from(Instant.now())));

    }


    private void addUsersToList(){

        mUsers.add(new Userx("tim", "shdfjkj"));
        mUsers.add(new Userx("lukas", "serger"));
        mUsers.add(new Userx("tom", "gjkjh"));
        mUsers.add(new Userx("hans", "fgjgkhjkhj"));
        mUsers.add(new Userx("peter", "adfgfd"));
        mUsers.add(new Userx("lisa", "wererztz"));
        mUsers.add(new Userx("hanna", "yyxvcfnd"));
        mUsers.add(new Userx("kim", "djghkhj"));
        mUsers.add(new Userx("andreas", "dfgutghfg"));


    }
}
