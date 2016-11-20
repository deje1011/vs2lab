package de.hska.lkit.demo.web.data.model;

import java.util.List;
import java.util.Set;

/**
 * 
 * Created by Marina on 20.11.2016.
 */
public class User {

    private String id;
    private String name;
    private String password;
    private Set follows;
    private Set<String> followedBy;
    private List<Post> posts;
    private String image;
    private String session;

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
        return followedBy;
    }

    public void setFollowedBy(Set<String> followedBy) {
        this.followedBy = followedBy;
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
