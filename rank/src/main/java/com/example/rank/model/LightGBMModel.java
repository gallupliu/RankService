package com.example.rank.model;


import com.microsoft.ml.lightgbm.*;
import com.microsoft.ml.spark.core.env.NativeLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * description: LightGBMModel
 * date: 2021/3/15 下午9:11
 * author: gallup
 * version: 1.0
 */
public class LightGBMModel {
    private SWIGTYPE_p_void boosterPtr;

    private String modelPath;

    public static final int numFeatures = 9;

    public LightGBMModel(String modelPath) {
        String modelString = readToString(modelPath);
        this.modelPath = modelString;
        initModel();
    }

    public void initModel() {
        try {
            init(modelPath);
        } catch (Exception e) {
            throw new RuntimeException("模型加载失败", e);
        }
    }

    public void init(String modelPath) throws Exception {
        initEnv();
        if (StringUtils.isEmpty(modelPath)) {
            throw new Exception("the inpute model string must not null");
        }
        this.boosterPtr = getBoosterPtrFrommodelPath(modelPath);
    }

    private void initEnv() throws IOException {
        String osPrefix = NativeLoader.getOSPrefix();
        new NativeLoader("/com/microsoft/ml/lightgbm").loadLibraryByName(osPrefix + "_lightgbm");
        new NativeLoader("/com/microsoft/ml/lightgbm").loadLibraryByName(osPrefix + "_lightgbm_swig");
    }

    private void validate(int result) throws Exception {
        if (result == -1) {
            throw new Exception("Booster LoadFromString" + "call failed in LightGBM with error: " + lightgbmlib.LGBM_GetLastError());
        }
    }

    private SWIGTYPE_p_void getBoosterPtrFrommodelPath(String lgbmodelPath) throws Exception {
        SWIGTYPE_p_p_void boosterOutPtr = lightgbmlib.voidpp_handle();
        SWIGTYPE_p_int numItersOut = lightgbmlib.new_intp();
        validate(
                lightgbmlib.LGBM_BoosterLoadModelFromString(lgbmodelPath, numItersOut, boosterOutPtr)
        );
        return lightgbmlib.voidpp_value(boosterOutPtr);
    }

    /**
     * 预测
     *
     * @param data        批量向量
     * @param numRows     预测行数
     * @param numFeatures 向量大小
     * @return 批量预测结果
     */
    public double[] predictForMat(double[] data, int numRows, int numFeatures) {
        int data64bitType = lightgbmlibConstants.C_API_DTYPE_FLOAT64;
        int isRowMajor = 1;
        String datasetParams = "";
        SWIGTYPE_p_double scoredDataOutPtr = lightgbmlib.new_doubleArray(numRows * numFeatures);

        SWIGTYPE_p_long_long scoredDataLengthLongPtr = lightgbmlib.new_int64_tp();
        lightgbmlib.int64_tp_assign(scoredDataLengthLongPtr, numRows * numFeatures);

        SWIGTYPE_p_double doubleArray = lightgbmlib.new_doubleArray(data.length);
        for (int i = 0; i < data.length; i++) {
            lightgbmlib.doubleArray_setitem(doubleArray, i, data[i]);
        }
        SWIGTYPE_p_void pdata = lightgbmlib.double_to_voidp_ptr(doubleArray);

        try {
            lightgbmlib.LGBM_BoosterPredictForMat(
                    boosterPtr,
                    pdata,
                    data64bitType,
                    numRows,
                    numFeatures,
                    isRowMajor,
                    0,
                    -1,
                    datasetParams,
                    scoredDataLengthLongPtr,
                    scoredDataOutPtr);
            return predToArray(scoredDataOutPtr, numRows);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(lightgbmlib.LastErrorMsg());
        } finally {
            lightgbmlib.delete_doublep(doubleArray);
            lightgbmlib.delete_doublep(scoredDataOutPtr);
            lightgbmlib.delete_int64_tp(scoredDataLengthLongPtr);
        }
        return new double[numRows];
    }

    private double[] predToArray(SWIGTYPE_p_double scoredDataOutPtr, int numRows) {
        double[] res = new double[numRows];
        for (int i = 0; i < numRows; i++) {
            res[i] = lightgbmlib.doubleArray_getitem(scoredDataOutPtr, i);
        }
        return res;
    }

    private String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args) {

        LightGBMModel model = new LightGBMModel(args[0]);
        long start = System.currentTimeMillis();
        double[] data = {1.0, 0.0, 1.0, 0.0, 0.24, 0.2879, 0.81, 3.0, 16.0};
        for (int i = 0;i<1000;i++){
            double[] scores = model.predictForMat(data, 1, 9);
        }

        System.out.println("总耗时为：" + (System.currentTimeMillis() - start) + "毫秒");
        double[] scores = model.predictForMat(data, 1, 9);
        System.out.println(scores[0]);

    }
}
