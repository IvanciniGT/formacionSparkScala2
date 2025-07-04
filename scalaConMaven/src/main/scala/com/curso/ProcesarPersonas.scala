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

    val datosDePersonas = conexion.read.json("personas.json")
    datosDePersonas.show()
    datosDePersonas.printSchema()

    val datosCPs = conexion.read
                           .option("header", "true")
                           .csv("cps.csv")
    datosCPs.show()
    datosCPs.printSchema()

    datosDePersonas.createOrReplaceTempView("personas") // Creamos una vista temporal para poder hacer consultas SQL
    datosCPs.createOrReplaceTempView("codigos_postales") // Creamos una vista temporal para poder hacer consultas SQL

    // JOIN con SQL
    val datosEnriquecidos = conexion.sql(
      """
        |SELECT p.nombre, p.apellidos, p.edad, p.dni, p.email, p.cp
        |FROM personas p
        |JOIN codigos_postales cp ON p.cp = cp.cp
      """.stripMargin)

    datosEnriquecidos.show()

    //Igual que hemos hecho esto con SQL, tenemos funciones que nos permiten hacerlo directamente con DataFrame API
    val datosEnriquecidos2 = datosDePersonas.join(datosCPs, datosDePersonas("cp") === datosCPs("cp"))

    // quiero enriquecer el fichero de personas, con el municipio y la provincia que viene en el fichero de códigos postales.
    // Si fueran tablas SQL un JOIN!
    // Podemos hacer JOINS en Spark? SI... PERO NO! O SI... pero CUIDADO...que no es como en una BBDD

    // En general con los UDF pocas veces me hace falta transformar un Dataset o dataframe en RDD

    conexion.stop()
  }

}
