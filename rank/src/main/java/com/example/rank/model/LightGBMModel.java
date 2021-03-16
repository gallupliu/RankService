package com.example.rank.model;


import com.microsoft.ml.lightgbm.*;
import com.microsoft.ml.spark.core.env.NativeLoader;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * description: LightGBMModel
 * date: 2021/3/15 下午9:11
 * author: gallup
 * version: 1.0
 */
public class LightGBMModel {
    private SWIGTYPE_p_void boosterPtr;

    private String modelString;

    public LightGBMModel(String modelString) {
        this.modelString = modelString;
        initModel();
    }

    public void initModel() {
        try {
            init(modelString);
        } catch (Exception e) {
            throw new RuntimeException("模型加载失败", e);
        }
    }

    public void init(String modelString) throws Exception {
        initEnv();
        if (StringUtils.isEmpty(modelString)) {
            throw new Exception("the inpute model string must not null");
        }
        this.boosterPtr = getBoosterPtrFromModelString(modelString);
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

    private SWIGTYPE_p_void getBoosterPtrFromModelString(String lgbModelString) throws Exception {
        SWIGTYPE_p_p_void boosterOutPtr = lightgbmlib.voidpp_handle();
        SWIGTYPE_p_int numItersOut = lightgbmlib.new_intp();
        validate(
                lightgbmlib.LGBM_BoosterLoadModelFromString(lgbModelString, numItersOut, boosterOutPtr)
        );
        return lightgbmlib.voidpp_value(boosterOutPtr);
    }

    /**
     * 预测
     * @param data 批量向量
     * @param numRows 预测行数
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
}
