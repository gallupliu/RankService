package com.example.rank.model.serving.rest;

import com.example.rank.entity.ItemSortModel;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.List;

import  com.example.rank.utils.HttpClient;
import scala.xml.PrettyPrinter;

public class Dssm {
    private String url ="";


    public void call(JSONArray features, List<ItemSortModel> result){
        if (null == features ){
            return;
        }

        JSONObject instancesRoot = new JSONObject();
        instancesRoot.put("instances",  features);

        //need to confirm the tf serving end point
        String predictionScores = HttpClient.asyncSinglePostRequest("http://localhost:8501/v1/models/dssm:predict", instancesRoot.toString());
        JSONObject objs = JSONObject.parseObject(predictionScores);
//        JSONObject predictionsObject = new JSONObject(predictionScores);
        JSONArray scores = objs.getJSONArray("predictions");
        for (int i = 0 ; i < features.size(); i++){
            JSONObject obj= (JSONObject) features.get(i);
            ItemSortModel item = new ItemSortModel();
            item.setId(obj.getString("id"));
            item.setScore(scores.getFloat(i));
            result.add(item);
        }

    }

}
