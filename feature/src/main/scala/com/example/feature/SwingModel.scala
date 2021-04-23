package com.example.feature


//import Rating
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema
import org.apache.spark.sql.functions._
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}

object SwingModel {

//  import spark.implicits._
//
//  var defaultParallelism: Int = spark.sparkContext.defaultParallelism
//  var similarities: Option[DataFrame] = None
//  var alpha: Option[Double] = Option(0.0)
//  var top_n_items: Option[Int] = Option(100)
//
//  /**
//   * @param parallelism 并行度，不设置，则为spark默认的并行度
//   * @return
//   */
//  def setParallelism(parallelism: Int): SwingModel = {
//    this.defaultParallelism = parallelism
//    this
//  }
//
//  /**
//   * @param alpha swing召回模型中的alpha值
//   * @return
//   */
//  def setAlpha(alpha: Double): SwingModel = {
//    this.alpha = Option(alpha)
//    this
//  }
//
//  /**
//   * @param top_n_items 计算相似度时，通过count倒排，取前top_n_items个item进行计算
//   * @return
//   */
//  def setTop_N_Items(top_n_items: Int): SwingModel = {
//    this.top_n_items = Option(top_n_items)
//    this
//  }
//
//  /**
//   * @param ratings 打分dataset
//   * @return
//   */
//  def fit(ratings: Dataset[Rating]): SwingModel = {
//
//    case class UserWithItemSet(user_id: String, item_set: Seq[String])
//
//    def interWithAlpha = udf(
//      (array_1: Seq[GenericRowWithSchema], array_2: Seq[GenericRowWithSchema]) => {
//        var score = 0.0
//        val set_1 = array_1.toSet
//        val set_2 = array_2.toSet
//        val user_set = set_1.intersect(set_2).toArray
//        for (i <- user_set.indices; j <- i + 1 until user_set.length) {
//          val user_1 = user_set(i)
//          val user_2 = user_set(j)
//          val item_set_1 = user_1.getAs[Seq[String]]("_2").toSet
//          val item_set_2 = user_2.getAs[Seq[String]]("_2").toSet
//          score = score + 1 / (item_set_1.intersect(item_set_2).size.toDouble + this.alpha.get)
//        }
//        score
//      }
//    )
//
//    val df = ratings.repartition(defaultParallelism).cache()
//    val groupUsers = df.groupBy("user_id")
//      .agg(collect_set("item_id"))
//      .toDF("user_id", "item_set")
//      .repartition(defaultParallelism)
//    val groupItems = df.join(groupUsers, "user_id")
//      .rdd.map { x =>
//      val item_id = x.getAs[String]("item_id")
//      val user_id = x.getAs[String]("user_id")
//      val item_set = x.getAs[Seq[String]]("item_set")
//      (item_id, (user_id, item_set))
//    }.toDF("item_id", "user")
//      .groupBy("item_id")
//      .agg(collect_set("user"), count("item_id"))
//      .toDF("item_id", "user_set", "count")
//      .sort($"count".desc)
//      .limit(this.top_n_items.get)
//      .drop("count")
//      .repartition(defaultParallelism)
//      .cache()
//    val itemJoined = groupItems.join(broadcast(groupItems))
//      .toDF("item_id_1", "user_set_1", "item_id_2", "user_set_2")
//      .filter("item_id_1 <> item_id_2")
//      .withColumn("score", interWithAlpha(col("user_set_1"), col("user_set_2")))
//      .select("item_id_1", "item_id_2", "score")
//      .filter("score > 0")
//      .repartition(defaultParallelism)
//      .cache()
//    similarities = Option(itemJoined)
//    this
//  }
//
//  /**
//   * 从fit结果，对item_id进行聚合并排序，每个item后截取n个item，并返回。
//   *
//   * @param num 取n个item
//   * @return
//   */
//  def item2item(num: Int): DataFrame = {
//    case class itemWithScore(item_id: String, score: Double)
//    val sim = similarities.get.select("item_id_1", "item_id_2", "score")
//    val topN = sim
//      .map { x =>
//        val item_id_1 = x.getAs[String]("item_id_1")
//        val item_id_2 = x.getAs[String]("item_id_2")
//        val score = x.getAs[Double]("score")
//        (item_id_1, (item_id_2, score))
//      }.toDF("item_id", "itemWithScore")
//      .groupBy("item_id").agg(collect_set("itemWithScore"))
//      .toDF("item_id", "item_set")
//      .rdd.map { x =>
//      val item_id_1 = x.getAs[String]("item_id")
//      val item_set = x.getAs[Seq[GenericRowWithSchema]]("item_set")
//        .map { x =>
//          val item_id_2 = x.getAs[String]("_1")
//          val score = x.getAs[Double]("_2")
//          (item_id_2, score)
//        }.sortBy(-_._2).take(num)
//      (item_id_1, item_set)
//    }.toDF("item_id", "sorted_items")
//      .filter("size(sorted_items) > 0")
//    topN
//  }

//  def main(args: Array[String]): Unit = {
//    val spark = SparkSession.builder().appName("SwingModel").getOrCreate()
//    spark.sparkContext.setLogLevel("ERROR")
//    val Array(log_url, top_n_items, alpha, num, dest_url) = args
//    val model = new SwingModel(spark)
//      .setAlpha(alpha.toDouble)
//      .setTop_N_Items(top_n_items.toInt)
//    val ratings = LogDataProcess.getRatingLog(spark, log_url)
//    val df = model.fit(ratings).item2item(num.toInt)
//    df.write.mode("overwrite").parquet(dest_url)
//  }
}
