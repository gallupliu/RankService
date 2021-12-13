package com.example.rank.model;
//
//import biz.k11i.xgboost.Predictor;
//import biz.k11i.xgboost.util.FVec;
//import org.lightgbm.predict4j.SparseVector;
//
//import java.util.List;

/**
 * description: XGBPrediction
 * date: 2021/3/24 下午9:51
 * author: gallup
 * version: 1.0
 */
public class XGBPredictor {

//    public static void main(String[] args) throws java.io.IOException {
//        // If you want to use faster exp() calculation, uncomment the line below
//        // ObjFunction.useFastMathExp(true);
//
//        // Load model and create Predictor
//        Predictor predictor = new Predictor(
//                new java.io.FileInputStream("/Users/gallup/study/search-ranking/models/xgb2.model"));
//
//        // Create feature vector from dense representation by array
//        double[] denseArray = {1.0,0.0,1.0,0.0,0.24,0.2879,0.81,3.0,16.0};
//        FVec fVecDense = FVec.Transformer.fromArray(
//                denseArray,
//                true /* treat zero element as N/A */);
//
//        // Create feature vector from sparse representation by map
//
//        long start = System.currentTimeMillis();
//        for (int i = 0;i<1000;i++){
//            // Predict probability or classification
//            float[] prediction = predictor.predict(fVecDense);
//        }
//        float[] prediction = predictor.predict(fVecDense);
//        System.out.println("总耗时为：" + (System.currentTimeMillis() - start) + "毫秒");
//        System.out.println(prediction[0]);
//
//        // prediction[0] has
//        //    - probability ("binary:logistic")
//        //    - class label ("multi:softmax")
//
//        // Predict leaf index of each tree
//        int[] leafIndexes = predictor.predictLeaf(fVecDense);
//
//        // leafIndexes[i] has a leaf index of i-th tree
//    }

}
