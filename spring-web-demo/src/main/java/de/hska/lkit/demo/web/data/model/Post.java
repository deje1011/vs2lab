package de.hska.lkit.demo.web.data.model;

import java.util.Date;

/**
 *
 * Created by Marina on 20.11.2016.
 */
public class Post {

    private String id;
    private String message;
    private User user;
    private Date time;


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
