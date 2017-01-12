package de.hska.lkit.demo.web.data.model;

import java.io.Serializable;

/**
 * Created by jessedesaever on 27.12.16.
 */
public class WebsocketMessage implements Serializable {
    private Post post;
    private String action;

    public WebsocketMessage (String action, Post post) {
        this.action = action;
        this.post = post;
    }

    public Post getPost () {
        return this.post;
    }

    public String getAction () {
        return this.action;
    }
}
