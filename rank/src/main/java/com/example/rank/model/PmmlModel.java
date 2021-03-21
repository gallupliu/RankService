package com.example.rank.model;

import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.ModelEvaluatorFactory;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.filters.ImportFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

import javax.xml.transform.sax.SAXSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * description: PmmlModel
 * date: 2021/3/21 上午10:31
 * author: gallup
 * version: 1.0
 */
public class PmmlModel {
//    private static final Logger LOGGER = LoggerFactory.getLogger(PmmlModel.class);
//
//    private PMML pmml;
//    private Evaluator evaluator;
//
//    private PmmlModel() {
//
//    }
//
//    public PmmlModel(String pmmlPath) throws Exception {
//        pmml = unmarshal(pmmlPath);
//        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
//        evaluator = modelEvaluatorFactory.newModelManager(pmml);
//        evaluator.verify();
//    }
//
//    public PmmlModel(InputStream inputStream) throws Exception {
//        InputSource source = new InputSource(inputStream);
//        SAXSource transformedSource = ImportFilter.apply(source);
//        pmml = JAXBUtil.unmarshalPMML(transformedSource);
//        ModelEvaluatorFactory modelEvaluatorFactory = ModelEvaluatorFactory.newInstance();
//        evaluator = modelEvaluatorFactory.newModelManager(pmml);
//        evaluator.verify();
//    }
//
//    public Double predict(List<Double> features) {
//        List<FieldName> activeFields = evaluator.getInputFields();
//        if (features.size() < activeFields.size()) {
//            LOGGER.error("features size {} is less than activeFields size {}!",
//                    features.size(), activeFields.size());
//            return null;
//        }
//        Map<FieldName, Double> fieldMap = new HashMap<>();
//        for (int i = 0; i < activeFields.size(); ++i) {
//            fieldMap.put(new FieldName("Column_" + i), features.get(i));
//        }
//        try {
//            Map<FieldName, ?> result = evaluator.evaluate(fieldMap);
//            return (Double) result.get(new FieldName("_target"));
//        } catch (Exception e) {
//            LOGGER.error("error {}", e);
//        }
//        return null;
//    }
//
//    private PMML unmarshal(String modelPath) throws Exception {
////        InputSource source = new InputSource(new FileInputStream(new File(modelPath)));
////        SAXSource transformedSource = ImportFilter.apply(source);
////        return JAXBUtil.unmarshalPMML(transformedSource);
//        InputStream inputStream = new FileInputStream(modelPath);
//        return  org.jpmml.model.PMMLUtil.unmarshal(is);
//    }
//    public static void main(String[] args){
//        InputStream inputStream = ModelDataLoader.class.getClassLoader().getResourceAsStream(MODEL_FILEPATH);
//        PmmlModel lgbmModel = new PmmlModel(inputStream);
//        List<Double> features = ModelFeature.getFeatureList(someVar);
//        double score = lgbmModel.predict(features);
//    }

}
