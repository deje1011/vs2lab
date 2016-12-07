package de.hska.lkit.demo.web.data.model;

/**
 * Created by jannika on 07.12.16.
 */
public class Query {
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    private String data;
    public Query(String data){
        this.data = data;
    }


}
