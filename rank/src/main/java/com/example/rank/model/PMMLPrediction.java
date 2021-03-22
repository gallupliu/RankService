package com.example.rank.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.util.*;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;
import org.jpmml.evaluator.ModelEvaluatorBuilder;

/**
 * description: PMMLPrediction
 * date: 2021/3/21 上午10:49
 * author: gallup
 * version: 1.0
 */
public class PMMLPrediction {
    private Evaluator evaluator;

    public static void main(String[] args) throws Exception {
        PMMLPrediction model = new PMMLPrediction();
        model.evaluator = model.loadModel("");

        long start = System.currentTimeMillis();
        double[] data = {1.0, 0.0, 1.0, 0.0, 0.24, 0.2879, 0.81, 3.0, 16.0};
        for (int i = 0;i<1000;i++){
//            model.doPredict();
        }

        System.out.println("总耗时为：" + (System.currentTimeMillis() - start) + "毫秒");
//        double[] scores = model.predictForMat(data, 1, 9);
//        System.out.println(scores[0]);
    }

    private Evaluator loadModel(String modelPath)throws Exception{
        File file = new File(modelPath);
        PMML pmml;
        Evaluator evaluator = null;
        InputStream inputStream = new FileInputStream(file);
        try(InputStream is = inputStream){
            pmml= org.jpmml.model.PMMLUtil.unmarshal(is);
            ModelEvaluatorBuilder modelEvaluatorBuilder = new ModelEvaluatorBuilder(pmml);
            evaluator = modelEvaluatorBuilder.build();
        }catch (IOException e){
            System.out.println("pmml model is error");
        }
        return evaluator;
    }

    public List<Double> doPredict(Map<String,Double> features){
        List<Double> scores = new ArrayList<>();
        List<InputField> inputFields = evaluator.getInputFields();
        Map<FieldName,FieldValue> arguments = new LinkedHashMap<>();
        for(InputField inputField:inputFields){
            FieldName fieldName = inputField.getName();
            Object rawValue = features.get(fieldName.getValue());
            FieldValue fieldValue = inputField.prepare(rawValue);
            arguments.put(fieldName,fieldValue);
        }

        Map<FieldName,?> results = evaluator.evaluate(arguments);
        List<TargetField> targetFields = evaluator.getTargetFields();
        for(TargetField targetField:targetFields){
            FieldName targetFielName = targetField.getName();
            Object targetFieldValue = results.get(targetFielName);
            double score = ((ProbabilityDistribution)targetFieldValue).getValue("0");
            scores.add(score);
        }
        return scores;
    }
}
