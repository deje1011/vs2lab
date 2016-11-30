package de.hska.lkit.demo.web;

/**
 * Created by jessedesaever on 10.11.16.
 */
public class Post {

    private static int idCounter = 0;

    private String content;
    private int id;

    public Post (String content) {
        this.id = ++Post.idCounter;
        this.content = content;
    }

    public int getId() {
        return id;
    }

    public void setContent (String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }
}
