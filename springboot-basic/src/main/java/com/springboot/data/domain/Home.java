package com.springboot.data.domain;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.geo.Point;

public class Home {

    @Id
    private ObjectId id;
    private String name;
    private Point pos;
    private ObjectId lineId;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Point getPos() {
        return pos;
    }

    public void setPos(Point pos) {
        this.pos = pos;
    }

    public ObjectId getLineId() {
        return lineId;
    }

    public void setLineId(ObjectId lineId) {
        this.lineId = lineId;
    }
}
