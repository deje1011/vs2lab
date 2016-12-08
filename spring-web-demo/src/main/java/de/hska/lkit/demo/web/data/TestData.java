package de.hska.lkit.demo.web.data;

import de.hska.lkit.demo.web.data.model.Post;

import de.hska.lkit.demo.web.data.model.UserX;
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

    private ArrayList<UserX> mUserXES = new ArrayList<>();
    private ArrayList<Post> mPosts = new ArrayList<>();
    private DataRepository mRepository;

    public void testDataBaseMethods(DataRepository repository){
        mRepository = repository;
        addUsersToList();
        //registerUsersToDatabase();


        Set<String> users = mRepository.getAllUsers();


        mRepository.addFollower("115", "128");
        UserX userX = mRepository.getUserById("115");

        printAllTimelinePosts("115");

        //testIsPasswordValid(mUserXES.get(0),mUserXES.get(5));
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
            System.out.print("\n Der UserX: " + userId + " folgt: " + follower );
        }
    }

    private void printAllFollowed(String userId){

        Set<String> followers = mRepository.getAllFollowed(userId);

        for(String follower: followers){
            System.out.print("\n Dem UserX: " + userId + " folgt: " + follower );
        }
    }

    private void printAllPosts(){

        Set<String> posts = mRepository.getAllGlobalPosts();

        for(String post : posts){

            Post p = mRepository.getPostById(post);
            System.out.print("\n post:" + post + ":user " + p.getUserX().getId());
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

            UserX userX = mRepository.getUserById(u);
            System.out.print("\n userX:" + userX.getName() + ":id " + u);
            System.out.print("\n userX:" + u +":name " + userX.getName());
            System.out.print("\n userX:" + u + ":password " + userX.getPassword() + "\n");
            Set<String> follows = userX.getFollows();

            if(follows != null) {
                for (String follow : follows) {
                    UserX p = mRepository.getUserById(follow);
                    System.out.print("\n userX:" + u + ":follows:" + p.getId() + ":" + p.getName());
                }
            }

            Set<String> followedBy = userX.getFollowed();

            if(followedBy != null) {
                for (String follow : followedBy) {
                    UserX p = mRepository.getUserById(follow);
                    System.out.print("\n userX:" + u + ":followedBy:" + p.getId() + ":" + p.getName());
                }
            }






           /* Set<String> posts = userX.getPosts();

            if(posts != null) {
                for (String post : posts) {
                    Post p = mRepository.getPostById(post);
                    System.out.print("\n userX:" + u + ":posts:" + p.getId() + ":" + p.getMessage());
                }
            }*/
            System.out.print("\n ");
        }
    }

    private void registerUsersToDatabase(){

        for(UserX userX : mUserXES){
           // if(mRepository.isUserNameUnique(userX.getName())) {
                mRepository.registerUser(userX);
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

        mUserXES.add(new UserX("tim", "shdfjkj"));
        mUserXES.add(new UserX("lukas", "serger"));
        mUserXES.add(new UserX("tom", "gjkjh"));
        mUserXES.add(new UserX("hans", "fgjgkhjkhj"));
        mUserXES.add(new UserX("peter", "adfgfd"));
        mUserXES.add(new UserX("lisa", "wererztz"));
        mUserXES.add(new UserX("hanna", "yyxvcfnd"));
        mUserXES.add(new UserX("kim", "djghkhj"));
        mUserXES.add(new UserX("andreas", "dfgutghfg"));


    }
}
