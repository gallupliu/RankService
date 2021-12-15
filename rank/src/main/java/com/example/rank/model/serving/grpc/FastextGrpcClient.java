package com.example.rank.model.serving.grpc;

import com.alibaba.fastjson.JSONObject;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Predict;

import java.util.Arrays;
import java.util.Map;

public class FastextGrpcClient {
    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        DssmGrpcCliet client = new DssmGrpcCliet();
        Predict.PredictRequest.Builder requestBuilder = Predict.PredictRequest.newBuilder();
        TensorProto.Builder tensorProtoBuilder = TensorProto.newBuilder();
        tensorProtoBuilder.setDtype(DataType.DT_INT64);
        TensorShapeProto.Dim[] dim = {TensorShapeProto.Dim.newBuilder().setSize(1).build(), TensorShapeProto.Dim.newBuilder().setSize(50).build()};
        tensorProtoBuilder
                .setTensorShape(TensorShapeProto.newBuilder().addAllDim(Arrays.asList(dim)));
        // tensorProtoBuilder.setTensorShape(TensorShapeProto.newBuilder()
        // .addDim(Dim.newBuilder().setSize(1)).addDim(Dim.newBuilder().setSize(31)));
        Long[] inputs = {1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L,
                1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L,
                1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L, 1L};
//        int[] inputs = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
//                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
//                1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
        tensorProtoBuilder.addAllInt64Val(Arrays.asList(inputs));
//        requestBuilder.putInputs("input_x", tensorProtoBuilder.build());


        TensorflowPredictionGrpcServer model = new TensorflowPredictionGrpcServer("127.0.0.1", 8500, "fastext");
        // get response
        Predict.PredictRequest request = model.getBuilder().putInputs("input_x", tensorProtoBuilder.build()).build();
//        Predict.PredictResponse response = model.predict(request);
        //设置入参,访问默认是最新版本，如果需要特定版本可以使用tensorProtoBuilder.setVersionNumber方法;
        Predict.PredictResponse response = model.predict(request);
        System.out.println(response);

//        }

    }

}
