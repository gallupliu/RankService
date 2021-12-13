package com.example.rank.model.serving.rest;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.example.rank.entity.ItemSortModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class DssmTest {
    @Test
    public void main() {


        int item[][] = {{11, 4, 11, 4, 11, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {11, 4, 11, 4, 11, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {10, 19, 23, 10, 19, 23, 16, 24, 0, 0, 0, 0, 0, 0, 0},
                {15, 21, 24, 15, 21, 24, 15, 21, 24, 0, 0, 0, 0, 0, 0}
        };

        int keyword[][] = {{11, 4, 0, 0, 0},
                {11, 4, 0, 0, 0},
                {10, 19, 23, 0, 0},
                {15, 21, 24, 0, 0}
        };

        double volume[][] = {{0.2},
                {0.2},
                {0.1},
                {0.3}
        };
        String type[][] = {{"0"},
                {"0"},
                {"0"},
                {"1"}
        };

        double price[][] = {{30},
                {30},
                {10},
                {19}
        };
        JSONArray features = new JSONArray();
        for (int i = 0; i < item.length; i++) {
            JSONObject obj = new JSONObject();
            obj.put("item", item[i]);
            obj.put("keyword", keyword[i]);
            obj.put("volume", volume[i]);
            obj.put("type", type[i]);
            obj.put("price", price[i]);
            features.add(obj);
        }
        List<ItemSortModel> result = new ArrayList<>();
        Dssm model = new Dssm();
        model.call(features, result);

    }
}
