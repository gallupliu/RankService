package com.example.rank.model;


import org.lightgbm.predict4j.SparseVector;
import org.lightgbm.predict4j.v2.Boosting;
import org.lightgbm.predict4j.v2.OverallConfig;
import org.lightgbm.predict4j.v2.Predictor;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: LGBPredictor
 * date: 2021/3/21 上午9:58
 * author: gallup
 * version: 1.0
 */
public class LGBPredictor {
    public static void main(String[] args) throws Exception {
        String path = LGBPredictor.class.getClassLoader().getResource(args[0]).getPath();
        // your model path
        path = URLDecoder.decode(path, "utf8");

        Boosting boosting = Boosting.createBoosting(path);
        // predict config, just like predict.conf in LightGBM
        Map<String, String> map = new HashMap<String, String>();
        OverallConfig config = new OverallConfig();
        config.set(map);
        // create predictor
        Predictor predictor =
                new Predictor(boosting, config.io_config.num_iteration_predict, config.io_config.is_predict_raw_score,
                        config.io_config.is_predict_leaf_index, config.io_config.pred_early_stop,
                        config.io_config.pred_early_stop_freq, config.io_config.pred_early_stop_margin);

        // your data to predict
        int[] indices = {0,1,2,3,4,5,6,7,8, 9};
        double[] values = {1.0,0.0,1.0,0.0,0.24,0.2879,0.81,3.0,16.0};

        long start = System.currentTimeMillis();
        for (int i = 0;i<1000;i++){
            SparseVector v = new SparseVector(values, indices);
            List<Double> predicts = predictor.predict(v);
        }

        System.out.println("总耗时为：" + (System.currentTimeMillis() - start) + "毫秒");
        SparseVector v = new SparseVector(values, indices);
        List<Double> predicts = predictor.predict(v);
        System.out.println("predict values " + predicts.toString());
    }

}
