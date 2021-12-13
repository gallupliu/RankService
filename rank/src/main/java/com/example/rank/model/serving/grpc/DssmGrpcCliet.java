package com.example.rank.model.serving.grpc;

import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.ImmutableMap;
import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.tensorflow.example.BytesList;
import org.tensorflow.example.Feature;
import org.tensorflow.example.FloatList;
import org.tensorflow.example.Int64List;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;
import com.google.protobuf.ByteString;
import org.tensorflow.example.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;

/**
 * Tensorflow DSSM
 */
public class DssmGrpcCliet {

    private static final String MODEL_NAME = "dssm";
    private static final String INPUT_NAME = "inputs";
    private static final String OUTPUT_NAME = "scores";
    List<String> intFeatures = new ArrayList<>(Arrays.asList());
    List<String> floatFeatures = new ArrayList<>(Arrays.asList("volume","price"));
    List<String> stringFeatures = new ArrayList<>(Arrays.asList("type"));
    List<String> intListFeatures = new ArrayList<>(Arrays.asList("item","keyword"));
    List<String> floatListFeatures = new ArrayList<>(Arrays.asList());
    List<String> stringListFeatures = new ArrayList<>(Arrays.asList());

    private enum FeatureType {
        // int 类型
        INT_TYPE,
        // float 类型
        FLOAT_TYPE,
        // string 类型
        STRING_TYPE,
        // int list类型
        INT_List_TYPE,
        // Sring list类型
        STRING_List_TYPE,
    }

//    private Map<String, Feature> buildFeature(FeatureType featureType, String featureName, Map<String, Feature> inputFeatures, String value) {
//
//        Feature f = null;
//        if (FeatureType.INT_TYPE == featureType) {
//            f = Feature.newBuilder().setInt64List(Int64List.newBuilder().addValue(Long.parseLong(value))).build();
//        } else if (FeatureType.FLOAT_TYPE == featureType) {
//            f = Feature.newBuilder().setFloatList(FloatList.newBuilder().addValue(Float.parseFloat(value))).build();
//        } else if (FeatureType.STRING_TYPE == featureType) {
//            f = Feature.newBuilder().setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8(value))).build();
//        }
//
//        inputFeatures.put(featureName, f);
//
//        return inputFeatures;
//    }

    private Map<String, Feature> buildFeature( Map<String, Feature> inputFeatures, JSONObject jsonObject) {

        for (String key : jsonObject.keySet()) {
            Feature f = null;
            Object obj = jsonObject.get(key);

            if (intFeatures.contains(key)) {
                f = Feature.newBuilder().setInt64List(Int64List.newBuilder().addValue((Integer) jsonObject.get(key))).build();
            } else if (floatFeatures.contains(key)) {
                f = Feature.newBuilder().setFloatList(FloatList.newBuilder().addValue((Float) jsonObject.get(key))).build();
            } else if (stringFeatures.contains(key)) {
                f = Feature.newBuilder().setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8((String) jsonObject.get(key)))).build();
            } else if (intListFeatures.contains(key)) {
                List<Integer> integers = (List<Integer>) jsonObject.get(key);
                List<Long> longs = new ArrayList<Long>();
                for (Integer i : integers) {
                    longs.add(i.longValue());
                }
                f = Feature.newBuilder().setInt64List(Int64List.newBuilder().addAllValue(longs)).build();
            } else if (stringFeatures.contains(key)) {
                List<String> stringList = (List<String>) jsonObject.get(key);
                List<ByteString> byteStrings = new ArrayList<ByteString>();
                for (String s : stringList) {
                    byteStrings.add(ByteString.copyFromUtf8(s));
                }
                f = Feature.newBuilder().setBytesList(BytesList.newBuilder().addAllValue(byteStrings)).build();


            }
            if (f != null) {
                inputFeatures.put(key, f);
            }

        }


        return inputFeatures;
    }
//
//    public static Map<String, TensorProto> parseExampleProto(JSONObject jsonObject) {
//        Map<String, Feature> inputFeatureMap = new HashMap<String, Feature>();
//
//        for (String key : jsonObject.keySet()) {
//            Feature feature = null;
//
//            if (key.equals("note_open_id")) {
//                BytesList.Builder byteListBuilder = BytesList.newBuilder();
//                ByteString bytes = ByteString.copyFromUtf8((String) jsonObject.get(key));
//                byteListBuilder.addValue(bytes);
//                feature = Feature.newBuilder().setBytesList(byteListBuilder.build()).build();
//            } else if (key.equals("note_id")) {
//                Int64List.Builder int64ListBuilder = Int64List.newBuilder();
//                int64ListBuilder.addValue((Integer) jsonObject.get(key));
//                feature = Feature.newBuilder().setInt64List(int64ListBuilder.build()).build();
//            } else if (key.equals("note_video_duration")) {
//                FloatList.Builder floatListBuilder = FloatList.newBuilder();
//                floatListBuilder.addValue((Integer) jsonObject.get(key));
//                feature = Feature.newBuilder().setFloatList(floatListBuilder.build()).build();
//            } else if (key.equals("last_note_ids")) {
//                List<Integer> integers = (List<Integer>) jsonObject.get(key);
//                List<Long> longs = new ArrayList<Long>();
//                for (Integer i : integers) {
//                    longs.add(i.longValue());
//                }
//                Int64List.Builder int64ListBuilder = Int64List.newBuilder();
//                int64ListBuilder.addAllValue(longs);
//                feature = Feature.newBuilder().setInt64List(int64ListBuilder.build()).build();
//            } else if (key.equals("last_note_creators")) {
//                List<String> stringList = (List<String>) jsonObject.get(key);
//                List<ByteString> byteStrings = new ArrayList<ByteString>();
//                for (String s : stringList) {
//                    byteStrings.add(ByteString.copyFromUtf8(s));
//                }
//                BytesList.Builder byteListBuilder = BytesList.newBuilder();
//                byteListBuilder.addAllValue(byteStrings);
//                feature = Feature.newBuilder().setBytesList(byteListBuilder.build()).build();
//            }
//
//            if (feature != null) {
//                inputFeatureMap.put(key, feature);
//            }
//        }
//
//        Features features = Features.newBuilder().putAllFeature(inputFeatureMap).build();
//        ByteString inputStr = Example.newBuilder().setFeatures(features).build().toByteString();
//
//        // batch predict
//        List<ByteString> inputBatch = new ArrayList<ByteString>();
//        inputBatch.add(inputStr);
//        inputBatch.add(inputStr);
//
//        TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
//        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(2).build());
//
//        TensorProto proto = TensorProto.newBuilder()
//                .addAllStringVal(inputBatch)
//                .setTensorShape(tensorShapeBuilder.build())
//                .setDtype(DataType.DT_STRING)
//                .build();
//
//        Map<String, TensorProto> tensorProtoMap = new HashMap<String, TensorProto>();
//        tensorProtoMap.put("examples", proto);
//        return tensorProtoMap;
//    }
//
//    public Predict.PredictRequest getRequest(List<String> testDataArrayList){
//
//        List<ByteString> inputStrs =
//                testDataArrayList.stream()
//                        .map(
//                                o -> {
//                                    String[] elems = o.toString().split(",", -1);
//                                    Map<String, Feature> inputFeatures = new HashMap(1000);
//                                    buildFeature(FeatureType.FLOAT_TYPE, "age", inputFeatures, elems[0]);
//                                    buildFeature(FeatureType.STRING_TYPE, "workclass", inputFeatures, elems[1]);
//                                    buildFeature(FeatureType.FLOAT_TYPE, "fnlwgt", inputFeatures, elems[2]);
//                                    buildFeature(FeatureType.STRING_TYPE, "education", inputFeatures, elems[3]);
//                                    buildFeature(FeatureType.FLOAT_TYPE, "education_num", inputFeatures, elems[4]);
//                                    buildFeature(FeatureType.STRING_TYPE, "marital_status", inputFeatures, elems[5]);
//                                    buildFeature(FeatureType.STRING_TYPE, "occupation", inputFeatures, elems[6]);
//                                    buildFeature(FeatureType.STRING_TYPE, "relationship", inputFeatures, elems[7]);
//                                    buildFeature(FeatureType.STRING_TYPE, "race", inputFeatures, elems[8]);
//                                    buildFeature(FeatureType.STRING_TYPE, "gender", inputFeatures, elems[9]);
//
//                                    buildFeature(FeatureType.FLOAT_TYPE, "capital_gain", inputFeatures, elems[10]);
//                                    buildFeature(FeatureType.FLOAT_TYPE, "capital_loss", inputFeatures, elems[11]);
//                                    buildFeature(FeatureType.FLOAT_TYPE, "hours_per_week", inputFeatures, elems[12]);
//                                    buildFeature(FeatureType.STRING_TYPE, "native_country", inputFeatures, elems[13]);
//
//                                    Features featuresSerializeToString =
//                                            Features.newBuilder().putAllFeature(inputFeatures).build();
//                                    ByteString inputStr =
//                                            Example.newBuilder()
//                                                    .setFeatures(featuresSerializeToString)
//                                                    .build()
//                                                    .toByteString();
//                                    return inputStr;
//                                })
//                        .collect(Collectors.toList());
//
//        TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
//
//        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(testDataArrayList.size()));
//        TensorShapeProto shape = tensorShapeBuilder.build();
//        TensorProto proto = TensorProto.newBuilder()
//                .setDtype(DataType.DT_STRING)
//                .setTensorShape(shape)
//                .addAllStringVal(inputStrs)
//                .build();
//
//        // create request builder
//        Predict.PredictRequest.Builder predictRequestBuilder = Predict.PredictRequest.newBuilder();
//        // add model params
//        Model.ModelSpec.Builder modelTensorBuilder = Model.ModelSpec.newBuilder().setName(MODEL_NAME);
//        // model signature
//        modelTensorBuilder.setSignatureName("serving_default");
//        // add info to request builder
//        predictRequestBuilder.setModelSpec(modelTensorBuilder.build());
//
//        predictRequestBuilder.putAllInputs(ImmutableMap.of(INPUT_NAME, proto));
//        return predictRequestBuilder.build();
//    }


    public Predict.PredictRequest getRequest() {


        int item[][] = {{11, 4, 11, 4, 11, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {11, 4, 11, 4, 11, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {10, 19, 23, 10, 19, 23, 16, 24, 0, 0, 0, 0, 0, 0, 0},
                {15, 21, 24, 15, 21, 24, 15, 21, 24, 0, 0, 0, 0, 0, 0}
        };

        int keyword[][] = {{11, 4, 0, 0, 0},
                {11, 4, 0, 0, 0},
                {10, 19, 23, 0, 0},
                {15, 21, 24, 0, 0}
        };

        double volume[][] = {{0.2},
                {0.2},
                {0.1},
                {0.3}
        };
        String type[][] = {{"0"},
                {"0"},
                {"0"},
                {"1"}
        };

        double price[][] = {{30},
                {30},
                {10},
                {19}
        };

        JSONArray featuresArray = new JSONArray();
        for (int i = 0; i < item.length; i++) {
            JSONObject obj = new JSONObject();
            obj.put("item", item[i]);
            obj.put("keyword", keyword[i]);
            obj.put("volume", volume[i]);
            obj.put("type", type[i]);
            obj.put("price", price[i]);
            featuresArray.add(obj);
        }

        List<ByteString> inputStrs = new ArrayList<>();
        for (int i = 0; i < featuresArray.size(); i++) {
            JSONObject obj = featuresArray.getJSONObject(i);
            Map<String, Feature> inputFeatures = new HashMap(1000);
            buildFeature( inputFeatures, obj);
//            buildFeature( inputFeatures, keyword[i]);
//            buildFeature( inputFeatures, volume[i]);
//            buildFeature( inputFeatures, type[i]);
//            buildFeature(inputFeatures, price[i]);

            Features featuresSerializeToString =
                    Features.newBuilder().putAllFeature(inputFeatures).build();
            ByteString inputStr =
                    Example.newBuilder()
                            .setFeatures(featuresSerializeToString)
                            .build()
                            .toByteString();
            inputStrs.add(inputStr);

        }

        TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();

        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(4));
        TensorShapeProto shape = tensorShapeBuilder.build();
        TensorProto proto = TensorProto.newBuilder()
                .setDtype(DataType.DT_STRING)
                .setTensorShape(shape)
                .addAllStringVal(inputStrs)
                .build();

        // create request builder
        Predict.PredictRequest.Builder predictRequestBuilder = Predict.PredictRequest.newBuilder();
        // add model params
        Model.ModelSpec.Builder modelTensorBuilder = Model.ModelSpec.newBuilder().setName(MODEL_NAME);
        // model signature
        modelTensorBuilder.setSignatureName("serving_default");
        // add info to request builder
        predictRequestBuilder.setModelSpec(modelTensorBuilder.build());

        predictRequestBuilder.putAllInputs(ImmutableMap.of(INPUT_NAME, proto));
        return predictRequestBuilder.build();
    }

    public void printResult(Predict.PredictResponse response) {
        TensorProto outputs = response.getOutputsOrDefault(OUTPUT_NAME, null);
        if (outputs == null) {
            System.out.println("printResult TensorProto outputs is null");
            return;
        }
        List<Float> predict = outputs.getFloatValList();
        int step = 2;
        for (int i = 0; i < predict.size(); i = i + step) {
            System.out.println(predict.get(i) + "," + predict.get(i + 1));
        }
    }

    public List<String> getData(String dataFile) throws IOException {
        File f = new File(dataFile);
        BufferedReader br = new BufferedReader(new FileReader(f));
        String line = br.readLine();
        List<String> dataList = new ArrayList();
        while (line != null) {
            dataList.add(line);
            line = br.readLine();
        }
        return dataList;
    }


    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        DssmGrpcCliet client = new DssmGrpcCliet();
        Predict.PredictRequest request = client.getRequest();
//        String host = "127.0.0.1";
//        int port = 8500;
//        String namespace = "serving_default";
//        String modelName = "dssm";
        TensorflowPredictionGrpcServer model = new TensorflowPredictionGrpcServer("127.0.0.1", 8500, "dssm");
        // get response
        Predict.PredictResponse response = model.predict(request);
        System.out.println(response);
    }

}
