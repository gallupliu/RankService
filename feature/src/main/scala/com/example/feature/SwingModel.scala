package com.example.feature

import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.rdd.RDD

import scala.collection.mutable.ArrayBuffer

class SwingModel(spark: SparkSession) extends Serializable{
  var alpha: Option[Double] = Option(0.0)
  var items: Option[ArrayBuffer[String]] = Option(new ArrayBuffer[String]())
  var userIntersectionMap: Option[Map[String, Map[String, Int]]] = Option(Map[String, Map[String, Int]]())

  /*
   * @Description 给参数 alpha赋值
   * @Param double
   * @return cf.SwingModel
   **/
  def setAlpha(alpha: Double): SwingModel = {
    this.alpha = Option(alpha)
    this
  }

  /*
   * @Description 给所有的item进行赋值
   * @Param [array]
   * @return cf.SwingModel
   **/
  def setAllItems(array: Array[String]): SwingModel = {
    this.items = Option(array.toBuffer.asInstanceOf[ArrayBuffer[String]])
    this
  }

  /*
   * @Description 获取两两用户有行为的item交集个数
   * @Param [spark, data]
   * @return scala.collection.immutable.Map<java.lang.String,scala.collection.immutable.Map<java.lang.String,java.lang.Object>>
   **/
  def calUserRateItemIntersection(data: RDD[(String, String, Double)]): Map[String, Map[String, Int]] = {
    val rdd = data.map(l => (l._1, l._2)).groupByKey().map(l => (l._1, l._2.toSet))
    val map = (rdd cartesian rdd).map(l => (l._1._1, (l._2._1, (l._1._2 & l._2._2).toArray.length)))
      .groupByKey()
      .map(l => (l._1, l._2.toMap))
      .collectAsMap().toMap
    map.take(10).foreach(println)
    map
  }

  def fit(data: RDD[(String, String, Double)]): RDD[(String, String, Double)]= {
    this.userIntersectionMap = Option(this.calUserRateItemIntersection(data))
    println(this.userIntersectionMap.take(10))

    val rdd = data.map(l => (l._2, l._1)).groupByKey().map(l => (l._1, l._2.toSet))
    val result: RDD[(String, String, Double)] = (rdd cartesian rdd).map(l => {
      val item1 = l._1._1
      val item2 = l._2._1
      val intersectionUsers = l._1._2 & l._2._2
      var score = 0.0
      for(u1 <- intersectionUsers){
        for(u2 <- intersectionUsers){
          score += 1.0 / (this.userIntersectionMap.get.get(u1).get(u2).toDouble + this.alpha.get)
        }
      }
      (item1, item2, score) // (item1, item2, swingsocre)
    })
    result
  }

  def evalute(test: RDD[(String, String, Double)]) = { }

  def predict(userid: String) = { }

  def predict(userids: Array[String]) = { }

}
