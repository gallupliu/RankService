package com.example.rank.model;
//
//import com.mlspark.lightgbm.predictor.SparseVector;
//import com.mlspark.lightgbm.predictor.V2.Boosting;
//import com.mlspark.lightgbm.predictor.V2.OverallConfig;
//import com.mlspark.lightgbm.predictor.V2.Predictor;

//import au.com.seek.lightgbm4j.LightGBMBooster;
//import org.lightgbm.predict4j.SparseVector;

import io.github.metarank.lightgbm4j.LGBMBooster;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * description: LGBModel
 * date: 2021/3/18 下午5:53
 * author: gallup
 * version: 1.0
 */
public class LGBModel {

    public static void main(String[] args) throws Exception {
//        LGBMBooster booster = LGBMBooster.createFromModelfile("/Users/gallup/study/search/rank/RankService/rank/src/main/resources/model.txt");
        File f = new File("/Users/gallup/study/search/rank/RankService/rank/src/main/resources/model.txt");
        FileInputStream in = new FileInputStream(f);

        byte[] b = new byte[(int)f.length()];
        in.read(b);
        String temp = new String(b);
        LGBMBooster booster = LGBMBooster.loadModelFromString(temp);
        float[] input = new float[] {1.0f, 1.0f, 1.0f, 1.0f};


        float[] values = {1.0f, 0.0f, 1.0f, 0.0f, 0.24f, 0.2879f, 0.81f, 3.0f, 16.0f,2.0f, 1.0f, 0.0f, 0.0f, 0.24f, 0.2879f, 0.81f, 3.0f, 10.0f};


        long start = System.currentTimeMillis();
        for(int i = 0;i < 500; i++)
        {
            double[] pred = booster.predictForMat(values, 2, 9, true);
//            System.out.println(pred[0]);
        }
        System.out.println("总耗时为："+(System.currentTimeMillis()-start)+"毫秒");

        float[] values_1 = {1.0f, 0.0f, 1.0f, 0.0f, 0.24f, 0.2879f, 0.81f, 3.0f, 16.0f};
        float[] values_2 = {2.0f, 1.0f, 0.0f, 0.0f, 0.24f, 0.2879f, 0.81f, 3.0f, 10.0f};
        double[] pred_1 = booster.predictForMat(values_1, 1, 9, true);
        double[] pred_2 = booster.predictForMat(values_2, 1, 9, true);
        System.out.println(pred_1[0]);
        System.out.println(pred_2[0]);
//        String modelPath = "模型地址";
//        Boosting boosting = Boosting.createBoosting(modelPath);
//        Map String> map = new HashMap();
//        OverallConfig config = new OverallConfig();
//        config.set(map);
//        Predictor predictor =
//                new Predictor(boosting, config.io_config.num_iteration_predict, config.io_config.
//                        is_predict_raw_score,
//                        config.io_config.is_predict_leaf_index, config.io_config.pred_early_stop,
//                        config.io_config.pred_early_stop_freq, config.io_config.pred_early_stop_margin);
//        int[] indices = new int[949];
//        for (int i = 0; i < indices.length; i++) {
//            indices[i] = i;
//        }
//        double[] values = {};
//        System.out.println(values.length);
//        SparseVector v = new SparseVector(values, indices);
//        List predicts = predictor.predict(v);
//        System.out.println("predict values " + predicts.toString());
//        values = new double[]{};
//        System.out.println(values.length);
//        v = new SparseVector(values, indices);
//        predicts = predictor.predict(v);
//        System.out.println("predict values " + predicts.toString());
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < 1000; i++) {
//            v = new SparseVector(values, indices);
//            predicts = predictor.predict(v);
//        }
    }
}
