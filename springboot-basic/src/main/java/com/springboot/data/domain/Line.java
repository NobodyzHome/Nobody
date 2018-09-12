package com.springboot.data.domain;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.List;

public class Line {

    @Id
    private ObjectId id;
    private String lineName;
    private ObjectId homeId;
    private List<Home> homes;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getLineName() {
        return lineName;
    }

    public void setLineName(String lineName) {
        this.lineName = lineName;
    }

    public ObjectId getHomeId() {
        return homeId;
    }

    public void setHomeId(ObjectId homeId) {
        this.homeId = homeId;
    }

    public List<Home> getHomes() {
        return homes;
    }

    public void setHomes(List<Home> homes) {
        this.homes = homes;
    }
}
