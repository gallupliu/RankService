package com.example.rank.model;


import ai.bleckwen.xgboost.Predictor;
import ai.bleckwen.xgboost.PredictorBuilder;
import ml.dmlc.xgboost4j.java.Booster;
import ml.dmlc.xgboost4j.java.DMatrix;
import ml.dmlc.xgboost4j.java.XGBoost;
import ml.dmlc.xgboost4j.java.XGBoostError;

import java.io.*;

/**
 * description: XGBModel
 * date: 2021/4/15 上午11:05
 * author: gallup
 * version: 1.0
 */
public class XGBModel {
    private byte[] getContent(String filePath) throws IOException {
        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        // 确保所有数据均被读取
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }



    public static void main(String[] args) throws XGBoostError,java.io.IOException {
        XGBModel xgbModel = new XGBModel();
        byte[] bytes = xgbModel.getContent("/Users/gallup/study/search-ranking/models/xgb2.model");
        Predictor predictor = (new PredictorBuilder()).build(bytes) ;

        double[] denseArray = {1.0,0.0,1.0,0.0,0.24,0.2879,0.81,3.0,16.0};
        long start = System.currentTimeMillis();
        for(int i =0;i<1000;i++){
            double score = predictor.predict(denseArray)[0];
        }

        double score = predictor.predict(denseArray)[0];
        long end = System.currentTimeMillis();
        System.out.println("总耗时为：" + (end - start) + "毫秒");
        System.out.println(score);

        Booster loadedBooster = XGBoost.loadModel(new ByteArrayInputStream(bytes));
        float[] values = {1.0f, 0.0f, 1.0f, 0.0f, 0.24f, 0.2879f, 0.81f, 3.0f,16.0f};
        DMatrix testMat = new DMatrix(values, 1, 9);
        for(int i =0;i <1000;i++){
            float[][] scrores = loadedBooster.predict(testMat, false);
        }
        System.out.println("总耗时为：" + (System.currentTimeMillis() - end) + "毫秒");
        float[][] scrores = loadedBooster.predict(testMat, false);
        System.out.println(scrores[0][0]);



    }
}
