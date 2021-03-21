package com.example.rank.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.*;

/**
 * description: PMMLPrediction
 * date: 2021/3/21 上午10:49
 * author: gallup
 * version: 1.0
 */
public class PMMLPrediction {
    public static void main(String[] args) throws Exception {
        String  pathxml="/Users/shuubiasahi/Documents/python/credit-tfgan/xml/lightgbm.pmml";
        Map<String, Double>  map=new HashMap<String, Double>();
        map.put("sepal_length", 5.1);
        map.put("sepal_width", 3.5);
        map.put("petal_length", 1.4);
        map.put("petal_width", 0.2);
        predictLrHeart(map, pathxml);
    }

    public static void predictLrHeart(Map<String, Double> irismap,String  pathxml)throws Exception {

        PMML pmml;
        // 模型导入
        File file = new File(pathxml);
        InputStream inputStream = new FileInputStream(file);
        try (InputStream is = inputStream) {
            pmml = org.jpmml.model.PMMLUtil.unmarshal(is);

            ModelEvaluatorBuilder modelEvaluatorBuilder = new ModelEvaluatorBuilder(pmml);

            Evaluator evaluator = modelEvaluatorBuilder.build();
//
//            pmml = org.jpmml.model.PMMLUtil.unmarshal(is);
//
//            ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory
//                    .newInstance();
//            ModelEvaluator<?> modelEvaluator = modelEvaluatorFactory
//                    .newModelEvaluator(pmml);
//
//            Evaluator evaluator = (Evaluator) modelEvaluator;

            List<InputField> inputFields = evaluator.getInputFields();
            // 过模型的原始特征，从画像中获取数据，作为模型输入
            Map<FieldName, FieldValue> arguments = new LinkedHashMap<>();
            for (InputField inputField : inputFields) {
                FieldName inputFieldName = inputField.getName();
                Object rawValue = irismap
                        .get(inputFieldName.getValue());
                FieldValue inputFieldValue = inputField.prepare(rawValue);
                arguments.put(inputFieldName, inputFieldValue);
            }

            Map<FieldName, ?> results = evaluator.evaluate(arguments);
            List<TargetField> targetFields = evaluator.getTargetFields();
            //对于分类问题等有多个输出。
            for (TargetField targetField : targetFields) {
                FieldName targetFieldName = targetField.getName();
                Object targetFieldValue = results.get(targetFieldName);
                System.err.println("target: " + targetFieldName.getValue()
                        + " value: " + targetFieldValue);
            }
        }
    }
}
