package com.springboot.web.controller;

import com.springboot.data.domain.Home;
import com.springboot.data.domain.Line;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface HomeRepository extends MongoRepository<Home, Integer> {

    @Query("{\"pos\":{$near:{$geometry:{type:\"Point\",coordinates:[?0,?1]},$minDistance:?2,$maxDistance:?3}}}")
    List<Home> findByPosNear(double x, double y, double min, double max);

    Home findById(ObjectId id);

    Line findByLineId(ObjectId lineId);
}
