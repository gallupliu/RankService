package main.scala.com.example.feature

//import org.apache.flink.api.java.DataSet

import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.ml.feature.{Bucketizer, CountVectorizer, CountVectorizerModel, MaxAbsScaler, MinMaxScaler, OneHotEncoder, QuantileDiscretizer, StandardScaler, StringIndexer, VectorAssembler}
import org.apache.spark.ml.linalg.Vector
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, Row, SparkSession}
import org.apache.spark.sql.functions.{col, udf}
import com.github.vickumar1981.stringdistance.StringDistance._
import com.github.vickumar1981.stringdistance.StringSound._
import com.github.vickumar1981.stringdistance.impl.{ConstantGap, LinearGap}
import main.scala.com.example.feature.Feature.bucketizer
import org.apache.spark.sql.expressions.UserDefinedFunction
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.{DefaultScalaModule, ScalaObjectMapper}

import scala.collection.mutable.HashMap
import scala.io.Source
import scala.util.{Failure, Success, Try}

object Feature {

    def loadEmbedding(filePath:String):  HashMap[String, java.util.List[Double]] ={
      val resMap =  new HashMap[String, java.util.List[Double]]()

      println(s"Reading ${filePath} ...")
      val json =Source.fromFile(filePath)
      val mapper = new ObjectMapper() with ScalaObjectMapper
      mapper.registerModule(DefaultScalaModule)
      val parsedJson = mapper.readValue[Map[String, java.util.List[Double]]](json.reader())
      val keys = parsedJson.keySet
      for (key <- keys){
        resMap.put(key, parsedJson.get(key).toList(0))
      }
      resMap
    }

  val mapper = new ObjectMapper()

  //自动解析json 方法
  def autoParseJson(jsonStr: String, resMap: HashMap[String, Any]): HashMap[String, Any] = {
    Try {
      val rootNode = mapper.readTree(jsonStr)
      val keys = rootNode.fieldNames()
      while (keys.hasNext) {
        val key = keys.next()
        val tmpRes = rootNode.get(key)
        //如果json对象,递归继续解析
        if (tmpRes.isObject) {
          autoParseJson(tmpRes.toString, resMap)
        }
        //如果是数组,直接取出
        else if (tmpRes.isArray) {
          resMap.put(key, tmpRes)
        }
        //其他基本类型,也直接取出
        else {
          if (tmpRes.isTextual) resMap.put(key, tmpRes.asText())
          else resMap.put(key, tmpRes)
        }
      }
    } match {
      case Success(value) => resMap
      case Failure(exception) => println(exception.toString)
        resMap
    }
  }

  def tverskyScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Tversky.score(query, item, 0.5)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "hammingScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def smithWatermanScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      SmithWaterman.score(query, item, ConstantGap())
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "hammingScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def overlapScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String, n: Int): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Overlap.score(query, item, n)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "hammingScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def ngramDistance(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String, n: Int): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      NGram.distance(query, item, n)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "hammingDistance", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def ngramScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String, n: Int): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      NGram.score(query, item, n)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "hammingScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }


  def needlemanWunschScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      NeedlemanWunsch.score(query, item, ConstantGap())
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "jaccardScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def jaroScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Jaro.score(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "jaroScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def jaroWinklerScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      JaroWinkler.score(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "jkScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def jaccardScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Jaccard.score(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "jaccardScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def lscDistance(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      LongestCommonSeq.distance(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "lscDistance", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def hammingDistance(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Hamming.distance(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "hammingDistance", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def hammingScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Hamming.score(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "hammingScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def damerauDistance(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Damerau.distance(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "damerauDistance", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def damerauScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Damerau.score(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "damerauScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def cosineSimi(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val score = (query: String, item: String) => {
      Cosine.score(query, item)
    }
    val addCol = udf(score)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "cosineScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def levenshteinDistance(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val distance = (query: String, item: String) => {
      Levenshtein.distance(query, item)
    }
    val addCol = udf(distance)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "levenshteinDistance", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def levenshteinScore(spark: SparkSession, data: DataFrame, queryColumn: String, itemColumn: String): DataFrame = {
    // 自定义udf的函数
    val distance = (query: String, item: String) => {
      Levenshtein.score(query, item)
    }
    val addCol = udf(distance)
    val cvmData = data
      .withColumn(queryColumn + itemColumn + "levenshteinScore", addCol(col(queryColumn), col(itemColumn)))
    cvmData
  }

  def oneHotEncoder(spark: SparkSession, data: DataFrame, column: String, savePath: String): DataFrame = {
    val indexer = new StringIndexer()
      .setInputCol(column)
      .setOutputCol(column + "Index")
      .fit(data)
    val indexed = indexer.transform(data)

    val encoder = new OneHotEncoder()
      .setInputCol(column + "Index")
      .setOutputCol(column + "onehot")
    val model = encoder.fit(indexed)

    val cvm = model.transform(indexed)
    encoder.write.overwrite().save(savePath)

    val doDense = udf((v: Vector) ⇒ v.toDense)

    val cvmData = cvm.drop(column)
      .withColumn(column, doDense(col(column + "onehot"))).drop(column + "Index")

    cvmData
  }

  def multiHotEncoder(spark: SparkSession, data: DataFrame, column: String): DataFrame = {
    println(data.getClass)
    //     Get distinct tags array
    val categories = data.rdd.flatMap(r => r.getAs[Seq[String]](column)).distinct.collect.sortWith(_ < _)

    System.out.println(categories.mkString(" "))
    // fit a CountVectorizerModel from the corpus

    val cvmData_1 = new CountVectorizerModel(categories)
      .setInputCol(column)
      .setOutputCol(column + "sparse")
      .transform(data)

    val doDense = udf((v: Vector) ⇒ v.toDense)

    val cvmData_2 = cvmData_1.drop(column)
      .withColumn(column, doDense(col(column + "sparse"))).drop(column + "sparse")

    cvmData_2
  }

  def standardScaler(spark: SparkSession, data: DataFrame, column: String): DataFrame = {
    val assembler = new VectorAssembler()
      .setInputCols(Array(column))
      .setOutputCol(column + "1")
    data.show()
    val df = assembler.transform(data).drop(column).withColumnRenamed(column + "1", column)
    df.show()
    val scaler = new StandardScaler()
      .setInputCol(column)
      .setOutputCol(column + "stand")
      .setWithStd(true)
      .setWithMean(false)

    // Compute summary statistics by fitting the StandardScaler.
    val scalerModel = scaler.fit(df)

    // Normalize each feature to have unit standard deviation.
    val scaledData = scalerModel.transform(df)

    scaledData
  }

  def minMaxScaler(spark: SparkSession, data: DataFrame, column: String): DataFrame = {
    val scaler = new MinMaxScaler()
      .setInputCol(column)
      .setOutputCol(column + "minmax")

    // Compute summary statistics and generate MinMaxScalerModel
    val scalerModel = scaler.fit(data)

    // rescale each feature to range [min, max].
    val scaledData = scalerModel.transform(data)

    scaledData
  }

  def maxAbsScaler(spark: SparkSession, data: DataFrame, column: String): DataFrame = {
    val scaler = new MaxAbsScaler()
      .setInputCol(column)
      .setOutputCol(column + "maxabs")

    // Compute summary statistics and generate MaxAbsScalerModel
    val scalerModel = scaler.fit(data)

    // rescale each feature to range [-1, 1]
    val scaledData = scalerModel.transform(data)

    scaledData
  }

  def bucketizer(spark: SparkSession, data: DataFrame, column: String, splits: Array[Double]): DataFrame = {
    val bucketizer = new Bucketizer()
      .setInputCol(column)
      .setOutputCol(column + "buckt")
      .setSplits(splits)

    // Transform original data into its bucket index.
    val bucketedData = bucketizer.transform(data)
    bucketedData
  }

  def quantileDiscretizer(spark: SparkSession, data: DataFrame, column: String, splits: Array[Double]): DataFrame = {
    val discretizer = new QuantileDiscretizer()
      .setInputCol(column)
      .setOutputCol(column + "quanbuckt")
      .setNumBuckets(3)

    // Transform original data into its bucket index.
    val result = discretizer.fit(data).transform(data)
    result
  }


  def main(args: Array[String]): Unit = {
    Logger.getLogger("org").setLevel(Level.ERROR)

    val textArray = Array("keyword", "title")
    val numericArray = Array("rate", "price")
    val multiArray = Array("categories")
    val onehotArray = Array("tag")

    val conf = new SparkConf()
      .setMaster("local")
      .setAppName("ctrModel")
      .set("spark.submit.deployMode", "client")
      .set("spark.driver.allowMultipleContexts", "true")

    val spark = SparkSession.builder.config(conf).getOrCreate()

    // Prepare training documents from a list of (id, text, label) tuples.
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
    )).toDF("id", "keyword", "title", "tag", "rate", "price", "categories","label")

    //
    //    val df_1 = tverskyScore(spark, data, "keyword", "title")
    //    val df_2 = smithWatermanScore(spark, df_1, "keyword", "title")
    //    val df_3 = overlapScore(spark, df_2, "keyword", "title", 1)
    //    val df_4 = ngramDistance(spark, df_3, "keyword", "title", 1)
    //    val df_5 = ngramScore(spark, df_4, "keyword", "title", 1)
    //    val df_6 = needlemanWunschScore(spark, df_5, "keyword", "title")
    //    val df_7 = jaroScore(spark, df_6, "keyword", "title")
    //    val df_8 = jaroWinklerScore(spark, df_7, "keyword", "title")
    //    val df_9 = jaccardScore(spark, df_8, "keyword", "title")
    //    val df_10 = lscDistance(spark, df_9, "keyword", "title")
    //    val df_11 = hammingDistance(spark, df_10, "keyword", "title")
    //    val df_12 = hammingScore(spark, df_11, "keyword", "title")
    //    val df_13 = damerauDistance(spark, df_12, "keyword", "title")
    //    val df_14 = damerauScore(spark, df_13, "keyword", "title")
    //    val df_15 = cosineSimi(spark, df_14, "keyword", "title")
    //    val df_16 = levenshteinDistance(spark, df_15, "keyword", "title")
    //    val df_17 = levenshteinScore(spark, df_16, "keyword", "title")
    //    df_17.show()

    print("stand")
    //    data.show()
    //    val df = standardScaler(spark, data, "rate")

    //    for (column <- numericArray) {
    //      println(column)
    //      val df = standardScaler(spark, data, column)
    //      df.show()
    //      val min_df = minMaxScaler(spark, data, column)
    //      min_df.show()
    //      val max_df = maxAbsScaler(spark, data, column)
    //      max_df.show()
    //    }

    //    for (column <- multiArray) {
    //      println("multione",column)
    //      val multi_df = multiHotEncoder(spark, data, column)
    //      multi_df.show()
    //    }
    //
    //    for (column <- onehotArray) {
    //      println("onehot",column)
    //      val ohe_df = oneHotEncoder(spark, data, column, "～/syudy/data/")
    //      ohe_df.show()
    //    }
    //
    //    df_1.show()


//    val json20200530 = "{\"@timestamp\":\"2020-05-25T09:18:08.332Z\",\"@metadata\":{\"beat\":\"filebeat\",\"type\":\"doc\",\"version\":\"6.5.0\",\"topic\":\"per_log\"},\"source\":\"/data/log-service-procession/logs/per.log\",\"message\":{\"rts\":4851,\"rho\":[[\"https://www.baidu.com\",2406,2406,2406,[2]],[\"https://www.google.cn\",1597,1965,2373,[2,2,2,2]],[\"https://www.bing.com\",349,349,349,[4,2]]],\"g\":\"cef988e-9103-1560529730626\",\"l\":\"34.24742889404297,108.86383819580078\",\"u\":\"568817000061355760\",\"fpr\":441,\"ppo\":[[\"www.baidu.com\",196,196,196,[4,22],4417]],\"s\":\"1.9.7|4g|2.11.1|75|iPhone|iPhone X (GSM+CDMA)\\u003ciPhone10,3\\u003e|iOS 13.3.1|7.0.12\",\"t\":1590398287912},\"fields\":{\"log_topics\":\"perlog\",\"ip\":\"10.0.0.0\"}}"
//
//    var map = new HashMap[String, java.util.List[Double]]()
//
//
//    map = loadEmbedding("/Users/gallup/study/search-ranking/data/char.json")
//
////    autoParseJson(json20200530, map)
//    val keys = map.keySet
//    for (key <- keys) {
//      println((key, map.get(key).toList))
//    }

  }

}