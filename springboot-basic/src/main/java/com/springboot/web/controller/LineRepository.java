package com.springboot.web.controller;

import com.springboot.data.domain.Line;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LineRepository {

    @Autowired
    private MongoTemplate template;

    List<Line> findByIdTest(ObjectId id) {
        Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("_id").is(id)),
                Aggregation.lookup("home", "_id", "line", "homes"));
        AggregationResults<Line> aggregate = template.aggregate(aggregation, "line", Line.class);

        return aggregate.getMappedResults();
    }

    List<Line> findLines() {
        List<Line> all = template.findAll(Line.class);
        return all;
    }
}
