package com.example.feature

import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.rdd.RDD

object Swing {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().master("local[10]").appName("Swing").enableHiveSupport().getOrCreate()
    Logger.getRootLogger.setLevel(Level.WARN)

    val trainDataPath = "～/Download/ml-100k/ua.base"
    val testDataPath = "～/Download/ml-100k/ua.test"

    import spark.sqlContext.implicits._
    val train: RDD[(String, String, Double)] = spark.sparkContext.textFile(trainDataPath).map(_.split("\t")).map(l => (l(0), l(1), l(2).toDouble))
    val test: RDD[(String, String, Double)] = spark.sparkContext.textFile(testDataPath).map(_.split("\t")).map(l => (l(0), l(1), l(2).toDouble))

    val items: Array[String] = train.map(_._2).collect()

    val swing = new SwingModel(spark).setAlpha(1).setAllItems(items)
    val itemSims: RDD[(String, String, Double)] = swing.fit(train)

    swing.evalute(test)
    swing.predict("")
    swing.predict(Array("", ""))

    spark.close()
  }
}
