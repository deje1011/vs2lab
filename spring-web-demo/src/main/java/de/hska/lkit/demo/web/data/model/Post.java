package de.hska.lkit.demo.web.data.model;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * Created by Marina on 20.11.2016.
 */
public class Post implements Serializable{

    private String id;
    private String message;
    private User user;
    private Date time;

    public Post(User user, String message, Date time){
        this.message = message;
        this.user = user;
        this.time = time;

    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
