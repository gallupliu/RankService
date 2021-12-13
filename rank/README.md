# 搜索算法online服务
## model
相关模型
## service
对外统一调用GRPC/rest/jni服务 服务端

分析模型输入输出
```
saved_model_cli show --dir /Users/gallup/work/python/search-ranking/pb/match/dssm/1 --all

```
通过模型直接启动
```
docker run -p 8500:8500  -p 8501:8501 \
  --mount type=bind,source=/Users/gallup/work/python/search-ranking/pb/match/dssm,target=/models/dssm \
  -e MODEL_NAME=dssm -t tensorflow/serving
```

通过配置文件启动
```
docker run -p 8500:8500 -p 8501:8501 --mount type=bind,source=/Users/gallup/work/python/search-ranking/pb/match,target=/models -t tensorflow/serving \
--model_config_file=/models/model.config
```

```
protoc -I=serving -I=tensorflow --plugin=/usr/local/protoc/bin/protoc-gen-grpc-java --grpc-java_out=java --java_out=java serving/tensorflow_serving/*/*.proto
protoc -I=serving -I=tensorflow --plugin=/usr/local/protoc/bin/protoc-gen-grpc-java --grpc-java_out=java --java_out=java serving/tensorflow_serving/sources/storage_path/*.proto
```




## demo 
客户端示例代码+示例模型文件