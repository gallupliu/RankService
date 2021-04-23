package com.example.feature

import org.apache.spark.graphx._
import org.apache.log4j.{Level, Logger}
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.mllib.linalg.distributed.{CoordinateMatrix, MatrixEntry}
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.{DataFrame, SparkSession}

class SimRankGraph() {
  /**
   * 创建图的结构
   *
   * @param indexedNode
   * @param nodes
   * @return
   */
  def graphStruct(indexedNode: RDD[(String, Long)], nodes: RDD[(String, String)]): Graph[String, Int] = {

    val indexedNodes = nodes.join(indexedNode).map(r => (r._2._1, r._2._2)).join(indexedNode).map(r => (r._2._1, r._2._2))

    val relationShips: RDD[Edge[Int]] = indexedNodes.map { x =>
      val x1 = x._1
      val x2 = x._2
      Edge(x1, x2, 1)
    }
    val users: RDD[(VertexId, String)] = indexedNode.map { x =>
      (x._2, x._1)
    }

    val graph = Graph(users, relationShips)
    graph
  }
}


object SimRank {


  /**
   * 获取item相似图
   *
   * @param nodes
   * @param damp
   */
  def getSimilarity(nodes: RDD[(String, String)], damp: Double) = {
    val itemSet = nodes.map(x => (x._2, "-")).distinct()
    val index2Node = (nodes.map(_._1) union (nodes.map(_._2))).distinct.zipWithIndex().cache()
    val nodesNum = index2Node.count().toInt
    val graph = new SimRankGraph().graphStruct(index2Node, nodes)
    val outs = graph.outDegrees.map(x => (x._1, (1 / x._2.toDouble)))
    val ins = graph.inDegrees.map(x => (x._1, (1 / x._2.toDouble)))

    val rdd_out = graph.outerJoinVertices(outs)((id, _, degin) => (id.toString, degin.getOrElse(0)))
      .triplets.map { x =>
      (x.dstId, x.srcId, x.srcAttr._2.toString.toDouble * x.attr.toInt)
    }
    val rdd_int = graph.outerJoinVertices(ins)((id, _, degin) => (id.toString, degin.getOrElse(0)))
      .triplets.map { x =>
      (x.srcId, x.dstId, x.dstAttr._2.toString.toDouble * x.attr.toInt)
    }

    val rdd_all = rdd_out.union(rdd_int)
    //概率转移矩阵Q
    val transferMatrix = new CoordinateMatrix(rdd_all.map { x =>
      MatrixEntry(x._1, x._2, x._3)
    }).toBlockMatrix

    // 单位矩阵I
    val unitMatrix = new CoordinateMatrix(nodes.sparkContext.parallelize(0 until nodesNum).map { x =>
      MatrixEntry(x, x, 1.0)
    })

    // C
    val cMatrix = new CoordinateMatrix(unitMatrix.entries.map { x =>
      MatrixEntry(x.i, x.j, x.value * damp)
    }).toBlockMatrix

    // (1-c) * I = S0
    val simMatrix = new CoordinateMatrix(unitMatrix.entries.map { x =>
      MatrixEntry(x.i, x.j, x.value * (1 - damp))
    }).toBlockMatrix

    // 初始化相似度矩阵
    val S_0 = simMatrix
    // K次迭代相似度矩阵
    var S_k = S_0
    // K+1次迭代相似度矩阵
    var S_kp1 = S_k

    for (i <- 0 until 5) {
      //S_kp1 = c * Q(T) * S_k * Q + (1-c)* I
      S_kp1 = transferMatrix.transpose.multiply(S_k).multiply(transferMatrix).multiply(cMatrix).add(simMatrix)
      S_k = S_kp1
    }


    val node2Index = index2Node.map(x => (x._2, x._1))
    val result = S_kp1.toCoordinateMatrix.entries.map {
      case MatrixEntry(x, y, j) => (x, y, "%.6f" format j)
    }.map(x => (x._1, (x._2, x._3)))
      .join(node2Index, 500)
      .map(x => (x._2._1._1, (x._2._1._2, x._2._2))).join(node2Index, 500)
      .map(x => (x._2._1._2, (x._2._2, x._2._1._1)))
      .join(itemSet, 500)
      .map(x => (x._1, x._2._1))
    result.filter(x => !x._1.equals(x._2._1)).map(x => (x._1, (x._2._1, x._2._2.toDouble)))
  }


  def main(args: Array[String]): Unit = {

    //屏蔽日志
    Logger.getLogger("org.apache.spark").setLevel(Level.WARN)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.OFF)


    //阻尼系数
    val damp = 0.6
    val spark = SparkSession
      .builder()
      .master("local")
      .appName("SimRank")
      .enableHiveSupport()
      .getOrCreate()

    val data = spark.sparkContext.textFile("data/graph_bipartite").map(x => (x.split("\t")(0), x.split("\t")(1)))
    // 计算item的图的相似度
    val item_sim = getSimilarity(data, damp)

    item_sim.take(100).foreach(println)
    spark.close()
  }
}
