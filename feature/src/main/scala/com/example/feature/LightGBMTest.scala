package com.example.feature

import com.microsoft.ml.spark.lightgbm.{LightGBMClassificationModel, LightGBMClassifier}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.{col, udf}
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.types.{DoubleType, IntegerType}

import scala.collection.mutable.ListBuffer

object LightGBMTest {
  def main(args: Array[String]): Unit = {
    //scala版本
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[4]") //本地单线程运行
    sparkConf.setAppName("testJob")
    val spark: SparkSession = SparkSession.builder().config(conf = sparkConf).getOrCreate()
    val sc: SparkContext = spark.sparkContext

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

    val assembler = new VectorAssembler()
      .setInputCols(vecCols)
      .setOutputCol("features")
    val output = assembler.transform(originalData).drop(vecCols++otherCols: _*)
    output.show()
    val array: Array[Dataset[Row]] = output.randomSplit(Array(0.7, 0.3))
    val train_data: Dataset[Row] = array(0)
    val test_data: Dataset[Row] = array(1)
    //
    val lgbCl: LightGBMClassifier = new LightGBMClassifier()
      .setLearningRate(0.005d)
      .setMaxDepth(7)
      .setNumIterations(100)
      .setEarlyStoppingRound(20)
      .setParallelism("data_parallel")
      .setTimeout(600)
      .setObjective("binary") //
      .setNumLeaves(160)
      .setMaxBin(511)
    val model: LightGBMClassificationModel = lgbCl.fit(train_data)

    val value: RDD[(Double, Double)] = model
      .transform(test_data)
      .select("label", "probability")
      .rdd
      .mapPartitions(iter => {
        val listBuffer: ListBuffer[(Double, Double)] = new ListBuffer[(Double, Double)]
        iter.foreach(row => {
          listBuffer.+=((row.get(0).toString.toDouble, row.get(1).asInstanceOf[org.apache.spark.ml.linalg.Vector].apply(1)))
        })
        listBuffer.iterator
      })
    val metrics: BinaryClassificationMetrics = new BinaryClassificationMetrics(value)
    println("Area under precision-recall curve = " + metrics.areaUnderPR())
    println("Area under ROC = " + metrics.areaUnderROC())
    model.getModel.saveNativeModel(spark, args(1), overwrite = true)
    spark.stop()
  }
}
