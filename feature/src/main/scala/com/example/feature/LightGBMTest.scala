package com.example.feature

import com.microsoft.ml.spark.lightgbm.{LightGBMClassificationModel, LightGBMClassifier}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Dataset, Row, SparkSession}

import scala.collection.mutable.ListBuffer

object LightGBMTest {
  def main(args: Array[String]): Unit = {
    //scala版本
    val sparkConf = new SparkConf()
    sparkConf.setMaster("local[4]")   //本地单线程运行
    sparkConf.setAppName("testJob")
    val session: SparkSession = SparkSession.builder().config(conf = sparkConf).getOrCreate()
    val sc: SparkContext = session.sparkContext
    val data: DataFrame = session
      .read
      .format("libsvm")
      .option("numFeatures", "949") //
      .load(args(0))
      .repartition(500)
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
    model.getModel.saveNativeModel(session, args(1), overwrite = true)
    session.stop()
  }
}
