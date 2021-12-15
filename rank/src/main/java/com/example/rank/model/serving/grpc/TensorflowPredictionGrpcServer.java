package com.example.rank.model.serving.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lombok.Getter;
import lombok.Setter;
import org.tensorflow.framework.DataType;
import org.tensorflow.framework.TensorProto;
import org.tensorflow.framework.TensorShapeProto;
import tensorflow.serving.Model;
import tensorflow.serving.Predict;
import tensorflow.serving.PredictionServiceGrpc;

@Setter
@Getter
public class TensorflowPredictionGrpcServer {
    private final ManagedChannel channel;
    private final PredictionServiceGrpc.PredictionServiceBlockingStub stub;
    public final Predict.PredictRequest.Builder builder;

    // Initialize gRPC client
    public TensorflowPredictionGrpcServer(String host, int port, String modelName)
    {
        // create a channel for gRPC
        channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext().build();
        stub = PredictionServiceGrpc.newBlockingStub(channel);


        // create a modelspec
        Model.ModelSpec.Builder modelSpecBuilder = Model.ModelSpec.newBuilder();
        modelSpecBuilder.setName(modelName);
        modelSpecBuilder.setSignatureName("serving_default");


        builder = Predict.PredictRequest.newBuilder();
        builder.setModelSpec(modelSpecBuilder);

    }

    public Predict.PredictResponse predict(Predict.PredictRequest request){
        // Request gRPC server
        Predict.PredictResponse response;
        try {
            response = stub.predict(request);

            java.util.Map<java.lang.String, org.tensorflow.framework.TensorProto> outputs = response.getOutputsMap();
            //for (java.util.Map.Entry<java.lang.String, org.tensorflow.framework.TensorProto> entry : outputs.entrySet()) {
            //    System.out.println("Response with the key: " + entry.getKey() + ", value: " + entry.getValue());
            //}

            return response;
        } catch (StatusRuntimeException e) {
            //logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            System.out.println(e);
            throw e;
        }

    }
//
//    public static void main(String[] args) {
//        //System.out.println("Start the predict client");
//
//        String host = "127.0.0.1";
//        int port = 8500;
//        String namespace = "serving_default";
//        String modelName = "dssm";
//        Integer version = 1;
//        String inputJson = "";
//
//        // Parse command-line arguments
//        if (args.length == 5) {
//            host = args[0];
//            port = Integer.parseInt(args[1]);
//            modelName = args[2];
//            inputJson = args[3];
//        }
//
//        // Run predict client to send request
//        TensorflowPredictionGrpcClient client = new TensorflowPredictionGrpcClient(host, port, modelName);
//
//        String response = null;
//        try {
//            response = client.predict(namespace, modelName, version, inputJson);
//        } catch (Exception e) {
//            System.out.println(e);
//        } finally {
//            try {
//                client.shutdown();
//            } catch (Exception e) {
//                System.out.println(e);
//            }
//        }
//
//        //System.out.println("Response: " + response);
//        //System.out.println("End of predict client");
//    }
}
