package com.example.rank.feature;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.ml.feature.CountVectorizer;
import org.apache.spark.ml.feature.CountVectorizerModel;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.*;


import scala.collection.mutable.Seq;

import org.apache.spark.ml.feature.RegexTokenizer;
import org.apache.spark.ml.feature.Tokenizer;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;

// col("...") is preferable to df.col("...")
import static org.apache.spark.sql.functions.callUDF;
import static org.apache.spark.sql.functions.col;

import java.util.Arrays;
import java.util.List;

/**
 * description: Feature
 * date: 2021/3/16 下午2:14
 * author: gallup
 * version: 1.0
 */
public class Feature {
    public Dataset<Row> getDistance(Dataset<Row> ds){
        return ds;
    }
    public static void main(String[] args){

        SparkSession spark = SparkSession
                .builder()
                .appName("feature")
                .config("spark.some.config.option", "some-value")
                .getOrCreate();

//从sparkContext中得到JavaSparkContext
        JavaSparkContext sc = JavaSparkContext.fromSparkContext(spark.sparkContext());

//从JavaSparkContext中得到SparkContext
        SparkContext sparkContext = JavaSparkContext.toSparkContext(sc);

        List<Row> data = Arrays.asList(
                RowFactory.create(2.2, true, "1", "foo"),
                RowFactory.create(3.3, false, "2", "bar"),
                RowFactory.create(4.4, false, "3", "baz"),
                RowFactory.create(5.5, false, "4", "foo")
        );
        StructType schema = new StructType(new StructField[]{
                new StructField("real", DataTypes.DoubleType, false, Metadata.empty()),
                new StructField("bool", DataTypes.BooleanType, false, Metadata.empty()),
                new StructField("stringNum", DataTypes.StringType, false, Metadata.empty()),
                new StructField("string", DataTypes.StringType, false, Metadata.empty())
        });
        Dataset<Row> dataset = spark.createDataFrame(data, schema);

    }
}
