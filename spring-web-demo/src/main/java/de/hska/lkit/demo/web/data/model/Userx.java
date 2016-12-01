package de.hska.lkit.demo.web.data.model;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by Marina on 29.11.2016.
 */
public class UserX implements Serializable{

    private String id;
    private String name;
    private String password;
    private Set<String> follows;
    private Set<String> followed;
    private Set<String> posts;
    private String image;
    private String session;

    public UserX(){

    }
    public UserX(String name, String password){
        this.name = name;
        this.password = password;
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getFollows() {
        return follows;
    }

    public void setFollows(Set<String> follows) {
        this.follows = follows;
    }

    public Set<String> getFollowed() {
        return followed;
    }

    public void setFollowed(Set<String> followed) {
        this.followed = followed;
    }

    public Set<String> getPosts() {
        return posts;
    }

    public void setPosts(Set<String> posts) {
        this.posts = posts;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }


}
