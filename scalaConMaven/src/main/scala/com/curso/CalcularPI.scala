package com.curso

import org.apache.spark.{SparkConf, SparkContext}

import scala.util.Random
//import scala.collection.parallel.CollectionConverters._

object CalcularPi {

  def main(argumentos:Array[String]) :Unit = {

    val conf = new SparkConf().setAppName("CalcularPi").setMaster("local[4]")
    val conexion = new SparkContext(conf) // A las conexiones de Spark las llamamos CONTEXTOS.

    // Lo normal en ese setMaster sería poner una URL del tipo:               spark://master:7077
    // NORA: Realmente, lo normal es no poner NADA. Ese dato lo vamos a pasar en tiempo de ejecución.
    // Es decir, en mi máquina quiero que sea local[*].. pero si estoy ejecutando esto en el entorno de producción, será algo del tipo : spark://master:7077
    // No quiero tener ese datos EN EL CODIGO... quiero que sea parametrizable.
    // Pero de eso YA NOS OCUPAREMOS


    val NUMERO_TOTAL_DE_DARDOS = 1000
    var NUMERO_DARDOS_DENTRO_CIRCULO = 0
    /*
        // OPCIÓN 1: Resuelto mediante programación IMPERATIVA
        for (i <- 1 to NUMERO_TOTAL_DE_DARDOS) {     // for (i <- 1 to NUMERO_TOTAL_DE_DARDOS) {
          val x = Random.nextDouble()
          val y = Random.nextDouble()
          if (x * x + y * y <= 1.0) { // Punto dentro del círculo
            NUMERO_DARDOS_DENTRO_CIRCULO += 1
          }
        }

        val PI_ESTIMADO = 4.0 * NUMERO_DARDOS_DENTRO_CIRCULO / NUMERO_TOTAL_DE_DARDOS
        println(s"Estimación de π con $NUMERO_TOTAL_DE_DARDOS dardos: $PI_ESTIMADO")
    */

    // Si veo el proceso de java al ejecutar este programa, veo que usa el 100% de un núcleo de CPU. Mi máquina tiene 8 cores!
    // Quién lleva el código de mi programa a un core? UN THREAD... Un hilo de ejecución.
    // Cuántos hilos tengo en ejecución? 1... Hemos abierto hilos de ejecución paralelos? NO


    // OPCION 2. MAP/REDUCE
    //    val coleccionDeDardosADisparar = (1 to NUMERO_TOTAL_DE_DARDOS).toList // (1 to NUMERO_TOTAL_DE_DARDOS).toList // Creamos una colección de dardos a disparar. No importa qué contengan, solo necesitamos su número
    //val coleccionDeDardosADisparar = (1 to NUMERO_TOTAL_DE_DARDOS).par // (1 to NUMERO_TOTAL_DE_DARDOS).toList // Creamos una colección de dardos a disparar. No importa qué contengan, solo necesitamos su número
    val coleccionDeDardosADisparar = conexion.parallelize(1 to NUMERO_TOTAL_DE_DARDOS) // (1 to NUMERO_TOTAL_DE_DARDOS).toList // Creamos una colección de dardos a disparar. No importa qué contengan, solo necesitamos su número
    // Al poner el .par, ha generado una lista paralelizable. Las operaciones map / reduce que vaya haciendo se van a ir repartiendo los datos en distintos threads (hilos) automáticamente
    // Para hacer uso de toda la capacidad de cómputo de mi computadora
    val NUMERO_DARDOS_DENTRO_CIRCULO_2 = coleccionDeDardosADisparar.map(  _ => (Random.nextDouble(), Random.nextDouble()) )
                                                                   .map{ case (x,y) => x * x + y * y                      }
                                                                   .filter( distancia => distancia <= 1.0                  )
                                                                   .count() // en lugar de .length

    val PI_ESTIMADO_2 = 4.0 * NUMERO_DARDOS_DENTRO_CIRCULO_2 / NUMERO_TOTAL_DE_DARDOS
    println(s"Estimación de π con $NUMERO_TOTAL_DE_DARDOS dardos: $PI_ESTIMADO_2")

    // Vamos a hacer sleep para ser capacer de acceder a la consola web y ver el proceso de ejecución
     Thread.sleep(60*60*1000) // 1 hora de espera
    conexion.stop() // Cerramos la conexión al contexto de Spark. Es una buena práctica hacerlo siempre que terminemos de usarlo.

  }

}
