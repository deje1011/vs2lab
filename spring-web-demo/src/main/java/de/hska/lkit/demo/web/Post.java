package de.hska.lkit.demo.web;

/**
 * Created by jessedesaever on 10.11.16.
 */
public class Post {
    private String content;
    private int id;

    public Post (String content) {
        this.id = 1; // TODO: Generate id
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }
}
