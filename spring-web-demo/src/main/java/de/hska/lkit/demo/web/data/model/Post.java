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
    private UserX userX;
    private Date time;

    public Post(UserX userX, String message, Date time){
        this.message = message;
        this.userX = userX;
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

    public UserX getUserX() {
        return userX;
    }

    public void setUserX(UserX userX) {
        this.userX = userX;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
