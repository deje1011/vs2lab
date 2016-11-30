package de.hska.lkit.demo.web.data.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 *
 * Created by Marina on 20.11.2016.
 */
public class User implements Serializable {

    private String id;
    private String name;
    private String password;
    private Set<String> follows;
    private Set<String> followed;
    private List<Post> posts;
    private String image;
    private String session;

    public User(String name, String password){
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

    public Set getFollows() {
        return follows;
    }

    public void setFollows(Set follows) {
        this.follows = follows;
    }

    public Set<String> getFollowedBy() {
        return followed;
    }

    public void setFollowedBy(Set<String> followedBy) {
        this.followed = followedBy;
    }

    public List<Post> getPosts() {
        return posts;
    }

    public void setPosts(List<Post> posts) {
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
