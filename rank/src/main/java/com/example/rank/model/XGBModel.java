package com.example.rank.model;


import ai.bleckwen.xgboost.Predictor;
import ai.bleckwen.xgboost.PredictorBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

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

    public static void main(String[] args) throws java.io.IOException {
        XGBModel xgbModel = new XGBModel();
        byte[] bytes = xgbModel.getContent("/Users/gallup/study/search-ranking/models/xgb2.model");
        Predictor predictor = (new PredictorBuilder()).build(bytes) ;

        double[] denseArray = {1.0,0.0,1.0,0.0,0.24,0.2879,0.81,3.0,16.0};
        long start = System.currentTimeMillis();
        for(int i =0;i<1000;i++){
            double score = predictor.predict(denseArray)[0];
        }
        double score = predictor.predict(denseArray)[0];
        System.out.println("总耗时为：" + (System.currentTimeMillis() - start) + "毫秒");
        System.out.println(score);

    }
}
