package com.example.feature

import java.io.{ByteArrayInputStream, FileOutputStream}

import com.microsoft.ml.spark.lightgbm.{LightGBMBooster, LightGBMClassificationModel, LightGBMClassifier}
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.ml.Pipeline
import org.apache.spark.ml.evaluation.BinaryClassificationEvaluator
import org.apache.spark.mllib.evaluation.BinaryClassificationMetrics
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.types.{DoubleType, IntegerType, StructType}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.jpmml.lightgbm.LightGBMUtil
import org.jpmml.model.MetroJAXBUtil
import org.apache.spark.rdd.RDD
import org.jpmml.sparkml.PMMLBuilder

import scala.collection.mutable.ListBuffer

object LigthGBMOnSparkML {
  def main(args: Array[String]): Unit = {
    // Creates a SparkSession.
    val spark = SparkSession
      .builder
      .master("local[*]")
      .appName(s"${this.getClass.getSimpleName}")
      .getOrCreate()
    val sc = spark.sparkContext

    var originalData: DataFrame = spark.read.option("header", "true") //第一行作为Schema
      .option("inferSchema", "true") //推测schema类型
      .csv("/home/gallup/study/search/RankService/feature/src/main/scala/com/example/feature/hour.csv")

    val labelCol = "workingday"
    //离散列
    val cateCols = Array("season", "yr", "mnth", "hr")
    // 连续列
    val conCols: Array[String] = Array("temp", "atemp", "hum", "casual", "cnt")
    //feature列
    val vecCols = conCols ++ cateCols

    import spark.implicits._
    vecCols.foreach(col => {
      originalData = originalData.withColumn(col, $"$col".cast(DoubleType))
    })
    originalData = originalData.withColumn(labelCol, $"$labelCol".cast(IntegerType))

    val assembler = new VectorAssembler()
      .setInputCols(vecCols)
      .setOutputCol("features")

    val classifier: LightGBMClassifier = new LightGBMClassifier()
      .setNumIterations(100)
      .setNumLeaves(31)
      .setBoostFromAverage(false)
      .setFeatureFraction(1.0)
      .setMaxDepth(-1)
      .setMaxBin(255)
      .setLearningRate(0.1)
      .setMinSumHessianInLeaf(0.001)
      .setLambdaL1(0.0)
      .setLambdaL2(0.0)
      .setBaggingFraction(1.0)
      .setBaggingFreq(0)
      .setBaggingSeed(1)
      .setObjective("binary")
      .setLabelCol(labelCol)
      .setCategoricalSlotNames(cateCols)
      .setFeaturesCol("features")
      .setBoostingType("gbdt") //rf、dart、goss

    val pipeline: Pipeline = new Pipeline().setStages(Array(assembler, classifier))

    val Array(tr, te) = originalData.randomSplit(Array(0.7, .03), 666)
    val model = pipeline.fit(tr)
    val modelDF = model.transform(te)
    val evaluator = new BinaryClassificationEvaluator().setLabelCol(labelCol).setRawPredictionCol("prediction")
    println(evaluator.evaluate(modelDF))
    val value: RDD[(Double, Double)] = modelDF
      .select(labelCol, "probability")
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
//    model.saveNativeModel(spark, args(1), overwrite = true)
    model.save("/home/gallup/study/search/RankService/feature/src/main/scala/com/example/feature/model")

    //增加导出pmml
    val classificationModel = model.stages(1).asInstanceOf[LightGBMClassificationModel]
    saveToPmml(classificationModel.getModel, "/home/gallup/study/search/RankService/feature/src/main/scala/com/example/feature/classificationModel.xml")
//    val pmml = new PMMLBuilder(tr.schema, model).build()
//    val hadoopConf = new Configuration()
//    val fs = FileSystem.get(hadoopConf)
//    val path = new Path("/home/gallup/study/search/RankService/feature/src/main/scala/com/example/feature/classificationModel.xml")
//    if (fs.exists(path)) {
//
//      fs.delete(path, true)
//    }
//    val out = fs.create(path)
//
//    MetroJAXBUtil.marshalPMML(pmml, out)
  }



  //保存pmml模型

  def saveToPmml(booster: LightGBMBooster, path: String): Unit = {
    try {
      val gbdt = LightGBMUtil.loadGBDT(new ByteArrayInputStream(booster.model.getBytes))
      import scala.collection.JavaConversions.mapAsJavaMap
      val pmml = gbdt.encodePMML(null, null, Map("compact" -> true))
      MetroJAXBUtil.marshalPMML(pmml, new FileOutputStream(path))
    } catch {
      case e: Exception => e.printStackTrace()
    }
  }
}
