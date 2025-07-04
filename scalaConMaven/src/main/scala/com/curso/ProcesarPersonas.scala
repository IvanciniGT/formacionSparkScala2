package com.curso

import com.curso.DNIUtils.validarDNI
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.{Row, SparkSession}

import scala.jdk.CollectionConverters._

object ProcesarPersonas {

  def main(args: Array[String]): Unit = {

    val conexion = SparkSession.builder()
                                .appName("ProcesarPersonas")
                                .master("local[*]") 
                                .getOrCreate()

    conexion.stop()
  }

}
