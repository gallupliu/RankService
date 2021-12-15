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
import java.lang.reflect.Array;
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



    private Map<String, Feature> buildFeature( Map<String, Feature> inputFeatures, JSONObject jsonObject) {

        for (String key : jsonObject.keySet()) {
            Feature f = null;
            Object obj = jsonObject.get(key);

            if (intFeatures.contains(key)) {
                f = Feature.newBuilder().setInt64List(Int64List.newBuilder().addValue((Integer) obj)).build();
            } else if (floatFeatures.contains(key)) {
                f = Feature.newBuilder().setFloatList(FloatList.newBuilder().addValue(((Double) obj).floatValue())).build();
            } else if (stringFeatures.contains(key)) {
                f = Feature.newBuilder().setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8((String) jsonObject.get(key)))).build();
            } else if (intListFeatures.contains(key)) {
                List<Long> longs = new ArrayList<>();
                if(obj.getClass().isArray()){
                    for (int i =0;i<Array.getLength(obj);i++){
                        longs.add(Long.valueOf(Array.get(obj,i).toString()));
                    }
                }

                f = Feature.newBuilder().setInt64List(Int64List.newBuilder().addAllValue(longs)).build();
            } else if (stringFeatures.contains(key)) {
                List<ByteString> byteStrings = new ArrayList<>();

                if(obj.getClass().isArray()){
                    for (int i =0;i<Array.getLength(obj);i++){
                        byteStrings.add((ByteString) Array.get(obj,i));
                    }
                }
                f = Feature.newBuilder().setBytesList(BytesList.newBuilder().addAllValue(byteStrings)).build();


            }
            if (f != null) {
                inputFeatures.put(key, f);
            }

        }


        return inputFeatures;
    }
    private void  buildFeatureTest( Map<String, TensorProto>  inputFeatures, JSONObject jsonObject){
        for (String key : jsonObject.keySet()) {
            Object obj = jsonObject.get(key);
            TensorProto.Builder tensorProtoBuilder = TensorProto.newBuilder();

            TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
            // 第一维是 batch_size
            int batchSize = Array.getLength(obj);
            tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize( batchSize));

            if (intListFeatures.contains(key)) {
                tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize( Array.getLength(obj)));
            } else {
                tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
            }

            tensorProtoBuilder.setTensorShape(tensorShapeBuilder.build());


            for(int index =0;index<batchSize;index++){

                if (intFeatures.contains(key)) {
                    List<Long> longs = new ArrayList<>();
                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            longs.add(Long.valueOf(Array.get(obj,i).toString()));
                        }
                    }
                    tensorProtoBuilder.addAllInt64Val(longs);
                    tensorProtoBuilder.setDtype(DataType.DT_INT64);
//                    tensorProtoBuilder.addInt64Val((Integer) obj);

                } else if (floatFeatures.contains(key)) {
                    tensorProtoBuilder.setDtype(DataType.DT_FLOAT);
//                    tensorProtoBuilder.addFloatVal((((Double) obj).floatValue()));
                    List<Float> longs = new ArrayList<>();
                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            longs.add(Float.valueOf(Array.get(obj,i).toString()));
                        }
                    }
                    tensorProtoBuilder.addAllFloatVal(longs);
                } else if (stringFeatures.contains(key)) {
                    tensorProtoBuilder.setDtype(DataType.DT_STRING);
//                    ByteString bytes = ByteString.copyFromUtf8((String) obj);
//                    tensorProtoBuilder.addStringVal(bytes);
                    List<ByteString> byteStrings = new ArrayList<>();

                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            byteStrings.add((ByteString) Array.get(obj,i));
                        }
                    }
                    tensorProtoBuilder.addAllStringVal(byteStrings);

                } else if (intListFeatures.contains(key)) {
                    List<Long> longs = new ArrayList<>();
                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            longs.add(Long.valueOf(Array.get(obj,i).toString()));
                        }
                    }
                    tensorProtoBuilder.addAllInt64Val(longs);
                } else if (stringListFeatures.contains(key)) {
                    List<ByteString> byteStrings = new ArrayList<>();

                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            byteStrings.add((ByteString) Array.get(obj,i));
                        }
                    }
                    tensorProtoBuilder.addAllStringVal(byteStrings);

                }else if (floatListFeatures.contains(key)) {
                    List<Float> longs = new ArrayList<>();
                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            longs.add(Float.valueOf(Array.get(obj,i).toString()));
                        }
                    }
                    tensorProtoBuilder.addAllFloatVal(longs);
                }
            }

            inputFeatures.put(key, tensorProtoBuilder.build());
        }


    }
    public  Map<String, TensorProto> parseTensorProto(JSONObject jsonObject) {
        Map<String, TensorProto> tensorProtoMap = new HashMap<>();

        for (String key : jsonObject.keySet()) {
            Object obj = jsonObject.get(key);
            TensorProto.Builder tensorProtoBuilder = TensorProto.newBuilder();

            TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
            // 第一维是 batch_size
            int batchSize = Array.getLength(obj);
            tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize( batchSize));

            if (intListFeatures.contains(key)) {
                int dim =Array.getLength(Array.get(obj,0));

                tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize( dim));
            } else {
                tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
            }

            tensorProtoBuilder.setTensorShape(tensorShapeBuilder.build());


            for(int index =0;index<batchSize;index++){

                if (intFeatures.contains(key)) {
//                    List<Long> longs = new ArrayList<>();
//                    if(obj.getClass().isArray()){
//                        for (int i =0;i<Array.getLength(obj);i++){
//                            longs.add(Long.valueOf(Array.get(obj,i).toString()));
//                        }
//                    }
//                    tensorProtoBuilder.addAllInt64Val(longs);
                    tensorProtoBuilder.addInt64Val((Integer) obj);
                    tensorProtoBuilder.setDtype(DataType.DT_INT64);
//                    tensorProtoBuilder.addInt64Val((Integer) obj);

                } else if (floatFeatures.contains(key)) {
                    tensorProtoBuilder.setDtype(DataType.DT_FLOAT);

                    tensorProtoBuilder.addFloatVal(Float.valueOf(Array.get(obj,index).toString()));
//                    List<Float> longs = new ArrayList<>();
//                    if(obj.getClass().isArray()){
//                        for (int i =0;i<Array.getLength(obj);i++){
//                            longs.add(Float.valueOf(Array.get(obj,i).toString()));
//                        }
//                    }
//                    tensorProtoBuilder.addAllFloatVal(longs);
                } else if (stringFeatures.contains(key)) {
                    tensorProtoBuilder.setDtype(DataType.DT_STRING);
                    ByteString bytes = ByteString.copyFromUtf8((String) Array.get(obj,index));
                    tensorProtoBuilder.addStringVal(bytes);
//                    List<ByteString> byteStrings = new ArrayList<>();
//
//                    if(obj.getClass().isArray()){
//                        for (int i =0;i<Array.getLength(obj);i++){
//                            byteStrings.add((ByteString) Array.get(obj,i));
//                        }
//                    }
//                    tensorProtoBuilder.addAllStringVal(byteStrings);

                } else if (intListFeatures.contains(key)) {
                    List<Long> longs = new ArrayList<>();
                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            longs.add(Long.valueOf(Array.get(Array.get(obj,index),i).toString()));
                        }
                    }
                    tensorProtoBuilder.addAllInt64Val(longs);
                } else if (stringListFeatures.contains(key)) {
                    List<ByteString> byteStrings = new ArrayList<>();

                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            byteStrings.add((ByteString) Array.get(Array.get(obj,index),i));
                        }
                    }
                    tensorProtoBuilder.addAllStringVal(byteStrings);

                }else if (floatListFeatures.contains(key)) {
                    List<Float> longs = new ArrayList<>();
                    if(obj.getClass().isArray()){
                        for (int i =0;i<Array.getLength(obj);i++){
                            longs.add(Float.valueOf(Array.get(Array.get(obj,index),i).toString()));
                        }
                    }
                    tensorProtoBuilder.addAllFloatVal(longs);
                }
            }

            tensorProtoMap.put(key, tensorProtoBuilder.build());
        }


        return tensorProtoMap;

    }

    public  Map<String, TensorProto> parseFeatureTensorProto(JSONObject jsonObject) {
        Map<String, TensorProto> tensorProtoMap = new HashMap<>();
        int dim =1;
        for (String key : jsonObject.keySet()) {
            Object obj = jsonObject.get(key);
            TensorProto.Builder tensorProtoBuilder = TensorProto.newBuilder();

            TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
            // 第一维是 batch_size
            int batchSize = Array.getLength(obj);
            tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize( batchSize));

            if (intListFeatures.contains(key)) {
                dim = Array.getLength(Array.get(obj,0));

                tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize( dim));
            } else {
                tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(1));
            }

            tensorProtoBuilder.setTensorShape(tensorShapeBuilder.build());


            for(int index =0;index<batchSize;index++){

                if (intFeatures.contains(key)) {
                    tensorProtoBuilder.addInt64Val((Integer) obj);
                    tensorProtoBuilder.setDtype(DataType.DT_INT64);

                } else if (floatFeatures.contains(key)) {
                    tensorProtoBuilder.setDtype(DataType.DT_FLOAT);

                    tensorProtoBuilder.addFloatVal(Float.valueOf(Array.get(obj,index).toString()));
                } else if (stringFeatures.contains(key)) {
                    tensorProtoBuilder.setDtype(DataType.DT_STRING);
                    ByteString bytes = ByteString.copyFromUtf8((String) Array.get(obj,index));
                    tensorProtoBuilder.addStringVal(bytes);

                } else if (intListFeatures.contains(key)) {
                    tensorProtoBuilder.setDtype(DataType.DT_INT32);
                    List<Long> longs = new ArrayList<>();
                    if(obj.getClass().isArray()){
                        for (int i =0;i<dim;i++){
                            longs.add(Long.valueOf(Array.get(Array.get(obj,index),i).toString()));
                        }
                    }
                    System.out.println(key+"："+Array.getLength(Array.get(obj,index)));
                    System.out.println("size:"+longs.size());
                    System.out.println(longs);
                    tensorProtoBuilder.addAllInt64Val(longs);
                } else if (stringListFeatures.contains(key)) {
                    List<ByteString> byteStrings = new ArrayList<>();

                    if(obj.getClass().isArray()){
                        for (int i =0;i<dim;i++){
                            byteStrings.add((ByteString) Array.get(Array.get(obj,index),i));
                        }
                    }
                    tensorProtoBuilder.addAllStringVal(byteStrings);

                }else if (floatListFeatures.contains(key)) {
                    List<Float> longs = new ArrayList<>();
                    if(obj.getClass().isArray()){
                        for (int i =0;i<dim;i++){
                            longs.add(Float.valueOf(Array.get(Array.get(obj,index),i).toString()));
                        }
                    }
                    tensorProtoBuilder.addAllFloatVal(longs);
                }
            }

            tensorProtoMap.put(key, tensorProtoBuilder.build());
        }


        return tensorProtoMap;

    }
    public  Map<String, TensorProto> parseExampleProto(JSONObject jsonObject){
        Map<String, Feature> featureMap = new HashMap<String, Feature>();
        Map<String, TensorProto> tensorProtoMap = new HashMap<>();
        for (String key : jsonObject.keySet()) {
            Feature f = null;
            Object obj = jsonObject.get(key);

            if (intFeatures.contains(key)) {
                f = Feature.newBuilder().setInt64List(Int64List.newBuilder().addValue((Integer) obj)).build();
            } else if (floatFeatures.contains(key)) {
                f = Feature.newBuilder().setFloatList(FloatList.newBuilder().addValue(((Double) obj).floatValue())).build();
            } else if (stringFeatures.contains(key)) {
                f = Feature.newBuilder().setBytesList(BytesList.newBuilder().addValue(ByteString.copyFromUtf8((String) jsonObject.get(key)))).build();
            } else if (intListFeatures.contains(key)) {
                List<Long> longs = new ArrayList<>();
                if(obj.getClass().isArray()){
                    for (int i =0;i<Array.getLength(obj);i++){
                        longs.add(Long.valueOf(Array.get(obj,i).toString()));
                    }
                }

                f = Feature.newBuilder().setInt64List(Int64List.newBuilder().addAllValue(longs)).build();
            } else if (stringFeatures.contains(key)) {
                List<ByteString> byteStrings = new ArrayList<>();

                if(obj.getClass().isArray()){
                    for (int i =0;i<Array.getLength(obj);i++){
                        byteStrings.add((ByteString) Array.get(obj,i));
                    }
                }
                f = Feature.newBuilder().setBytesList(BytesList.newBuilder().addAllValue(byteStrings)).build();


            }
            if (f != null) {
                featureMap.put(key, f);
            }

        }

        Features features = Features.newBuilder().putAllFeature(featureMap).build();
        ByteString inputStr = Example.newBuilder().setFeatures(features).build().toByteString();

        // batch predict
        List<ByteString> inputBatch = new ArrayList<ByteString>();
        inputBatch.add(inputStr);
        inputBatch.add(inputStr);

        TensorShapeProto.Builder tensorShapeBuilder = TensorShapeProto.newBuilder();
        tensorShapeBuilder.addDim(TensorShapeProto.Dim.newBuilder().setSize(2).build());

        TensorProto proto = TensorProto.newBuilder()
                .addAllStringVal(inputBatch)
                .setTensorShape(tensorShapeBuilder.build())
                .setDtype(DataType.DT_STRING)
                .build();


        tensorProtoMap.put("examples", proto);
        return tensorProtoMap;

    }


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

        double volume[] = {0.2,
                0.2,
                0.1,
                0.3
        };
        String type[] = {"0",
                "0",
                "0",
                "1"
        };

        double price[] = {30,
                30,
                10,
                19
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

    public JSONObject getData(){

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

//        double volume[][] = {{0.2},
//                {0.2},
//                {0.1},
//                {0.3}
//        };
//        String type[][] = {{"0"},
//                {"0"},
//                {"0"},
//                {"1"}
//        };
//
//        double price[][] = {{30},
//                {30},
//                {10},
//                {19}
//        };
        double volume[] = {0.2,
                0.2,
                0.1,
                0.3
        };
        String type[] = {"0",
                "0",
                "0",
                "1"
        };

        double price[] = {30,
                30,
                10,
                19
        };

        JSONObject obj = new JSONObject();
        obj.put("item", item);
        obj.put("keyword", keyword);
        obj.put("volume", volume);
        obj.put("type", type);
        obj.put("price", price);
        return obj;
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
//        Predict.PredictResponse response = model.predict(request);
        //设置入参,访问默认是最新版本，如果需要特定版本可以使用tensorProtoBuilder.setVersionNumber方法
        JSONObject array = client.getData();
        Map<String, TensorProto> tensorProtoMap = client.parseFeatureTensorProto(array);
//        for(String key :  tensorProtoMap.keySet()){
////            if(key=="keyword"){
////                continue;
////            }
//            model.getBuilder().putInputs(key,tensorProtoMap.get(key));
//        }
        model.getBuilder().putAllInputs(tensorProtoMap);
        Predict.PredictResponse response = model.predict(model.getBuilder().build());
        System.out.println(response);

//        }

    }

}
