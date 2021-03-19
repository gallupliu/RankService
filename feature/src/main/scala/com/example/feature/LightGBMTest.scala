package com.example.feature

import com.microsoft.ml.spark.lightgbm.{LightGBMClassificationModel, LightGBMClassifier}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}
import org.apache.spark.sql.functions.{col, udf}

import scala.collection.mutable.ListBuffer

object LightGBMTest {
  def main(args: Array[String]): Unit = {
    //scala版本
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[4]")   //本地单线程运行
    sparkConf.setAppName("testJob")
    val spark: SparkSession = SparkSession.builder().config(conf = sparkConf).getOrCreate()
    val sc: SparkContext = spark.sparkContext
//    val data: DataFrame = session
//      .read
//      .format("libsvm")
//      .option("numFeatures", "949") //
//      .load(args(0))
//      .repartition(500)
    val columnNames = Seq("tag", "rate", "price")
    val data = spark.createDataFrame(Seq(
      (0L, "牛奶", "伊利牛奶", 1, 0.1, 10, Seq("A", "B"),0),
      (1L, "牛奶", "奶牛", 4, 0.9, 100, Seq("B"),0),
      (2L, "牛奶", "牛奶", 2, 0.3, 20, Seq.empty,1),
      (3L, "牛奶", "伊利牛奶", 5, 0.7, 40, Seq("D", "E"),0),
      (0L, "牛奶", "蒙牛牛奶", 2, 0.1, 10, Seq("A", "B"),0),
      (1L, "牛奶", "高钙牛奶", 3, 0.5, 100, Seq("B"),1),
      (2L, "牛奶", "脱脂牛奶", 1, 1.0, 52, Seq.empty,0),
      (2L, "牛奶", "纯牛奶", 1, 1.0, 1, Seq.empty,0),
      (3L, "牛奶", "酸奶", 6, 0.9, 34, Seq("D", "E"),1),
      (3L, "martha", "marhta", 6, 0.9, 34, Seq("D", "E"),1),
      (0L, "牛奶", "伊利牛奶", 1, 0.1, 10, Seq("A", "B"),0),
      (1L, "牛奶", "奶牛", 4, 0.9, 100, Seq("B"),0),
      (2L, "牛奶", "牛奶", 2, 0.3, 20, Seq.empty,0),
      (3L, "牛奶", "伊利牛奶", 5, 0.7, 40, Seq("D", "E"),1),
      (0L, "牛奶", "蒙牛牛奶", 2, 0.1, 10, Seq("A", "B"),0),
      (1L, "牛奶", "高钙牛奶", 3, 0.5, 100, Seq("B"),0),
      (2L, "牛奶", "脱脂牛奶", 1, 1.0, 52, Seq.empty,1),
      (2L, "牛奶", "纯牛奶", 1, 1.0, 1, Seq.empty,1),
      (3L, "牛奶", "酸奶", 6, 0.9, 34, Seq("D", "E"),1),
      (3L, "martha", "marhta", 6, 0.9, 34, Seq("D", "E"),1)
    )).toDF("id", "keyword", "title", "tag", "rate", "price", "categories","label").select(columnNames.map(c => col(c)): _*)
    val array: Array[Dataset[Row]] = data.randomSplit(Array(0.7, 0.3))
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
          listBuffer.+=((row.getDouble(0), row.get(1).asInstanceOf[org.apache.spark.ml.linalg.Vector].apply(1)))
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
