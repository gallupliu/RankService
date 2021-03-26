package com.example.feature

import org.apache.spark.sql.SparkSession
import org.apache.spark.ml.feature.{OneHotEncoderEstimator, StringIndexer, VectorAssembler}
import org.apache.spark.ml.Pipeline
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.types.{DoubleType, IntegerType}
import com.example.feature.utils.{AUCEvaluation, PREvaluation, getProba}
import ml.dmlc.xgboost4j.scala.spark.XGBoostClassifier

object Xgboost {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName("xgboost_milk")
      .enableHiveSupport()
      .getOrCreate()

    var originalData: DataFrame = spark.read.option("header", "true") //第一行作为Schema
      .option("inferSchema", "true") //推测schema类型
      .csv(args(0))
    originalData.show()
    val labelCol = "workingday"
    //离散列
    val cateCols = Array("season", "yr", "mnth", "hr")
    // 连续列
    val conCols: Array[String] = Array("temp", "atemp", "hum", "casual", "cnt")
    //feature列
    val vecCols = conCols ++ cateCols

    //other columns
    val otherCols = Array("instant", "dteday", "holiday", "weekday", "weathersit", "windspeed", "registered")

    import spark.implicits._
    vecCols.foreach(col => {
      originalData = originalData.withColumn(col, $"$col".cast(DoubleType))
    })
    originalData = originalData.withColumn(labelCol, $"$labelCol".cast(IntegerType))
    originalData = originalData.withColumnRenamed(labelCol,"label")
    val array: Array[Dataset[Row]] = originalData.randomSplit(Array(0.7, 0.3))
    val train_data: Dataset[Row] = array(0)
    val test_data: Dataset[Row] = array(1)
    val assembler = new VectorAssembler()
      .setInputCols(vecCols)
      .setOutputCol("features")
    val output = assembler.transform(originalData).drop(vecCols++otherCols: _*)
    output.show()

    //
    //    val df = spark.read.options(Map(("header", "true"), ("inferSchema", "true"))).csv("/data/churn.csv").na.drop()
    //    val Array(training, test) = df.randomSplit(Array(0.8, 0.2), 123)
    //
    //    // 定义连续变量
    //    val continueCols = Array("shop_duration", "recent", "monetary", "max_amount", "items_count",
    //      "valid_points_sum", "member_day", "frequence", "avg_amount", "item_count_turn",
    //      "avg_piece_amount", "monetary3", "max_amount3", "items_count3",
    //      "frequence3", "shops_count", "promote_percent", "wxapp_diff", "store_diff",
    //      "week_percent")
    //
    //    // 定义字符串变量
    //    val stringCols = Array("shop_channel", "infant_group", "water_product_group", "meat_group", "beauty_group", "health_group", "fruits_group", "vegetables_group",
    //      "pets_group", "snacks_group", "smoke_group", "milk_group", "instant_group",
    //      "grain_group")
    //
    //    val indexers = stringCols.map(
    //      col => new StringIndexer().setInputCol(col).setOutputCol(s"${col}_idx").setHandleInvalid("keep") // 新数据集有新数据，设置一个新的索引值
    //    )
    //
    //    // 定义离散变量
    //    val categoryCols = Array("CHANNEL_NUM_ID") ++ stringCols.map(name => s"${name}_idx")
    //
    //    // 定义onehot转化器
    //    val onehot = new OneHotEncoderEstimator()
    //      .setDropLast(false)
    //      .setInputCols(categoryCols)
    //      .setOutputCols(categoryCols map (name => s"${name}_vec"))
    //
    //    // 定义assembler
    //    val assembler = new VectorAssembler()
    //      .setInputCols(continueCols ++ (categoryCols map (name => s"${name}_vec")))
    //      .setOutputCol("features")


    //      .setMaxDepth(7)
    //      .setNumIterations(100)
    //      .setEarlyStoppingRound(20)
    //      .setParallelism("data_parallel")
    //      .setTimeout(600)
    //      .setObjective("binary") //
    //      .setNumLeaves(160)
    //      .setMaxBin(511)

    // 定义模型
    val xgbParam = Map("eta" -> 0.1f,
      "objective" -> "binary:logistic",
      "missing" -> 0.0,
      "num_round" -> 200,
      "num_workers" -> 3,
      "max_depth" -> 7)
    val xgbclassifier = new XGBoostClassifier(xgbParam)
      .setFeaturesCol("features")
      .setLabelCol("label")

    // 定义流水线
    val pipeline = new Pipeline().setStages(Array(assembler, xgbclassifier))

    // 训练
    val model = pipeline.fit(train_data)

    // 预测
    val predictions = model.transform(test_data)

    // 模型评价
//    PREvaluation(predictions, labelCol="label", predCol="prediction")
//    AUCEvaluation(predictions, labelCol = "label", predCol = "rawPrediction")

    // 模型持久化
    model.write.overwrite().save(args(1))

    spark.close()

  }
}
