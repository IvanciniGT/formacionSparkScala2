package com.curso

import scala.util.Random
import scala.collection.parallel.CollectionConverters._

object CalcularPi {

  def main(argumentos:Array[String]) :Unit = {

    val NUMERO_TOTAL_DE_DARDOS = 1000 * 1000 * 100
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
    val coleccionDeDardosADisparar = (1 to NUMERO_TOTAL_DE_DARDOS).par // (1 to NUMERO_TOTAL_DE_DARDOS).toList // Creamos una colección de dardos a disparar. No importa qué contengan, solo necesitamos su número
    val NUMERO_DARDOS_DENTRO_CIRCULO_2 =coleccionDeDardosADisparar.map(  _ => (Random.nextDouble(), Random.nextDouble()) )
                                                                  .map{ case (x,y) => x * x + y * y                      }
                                                                  .filter( distancia => distancia <= 1.0                  )
                                                                  .length

    val PI_ESTIMADO_2 = 4.0 * NUMERO_DARDOS_DENTRO_CIRCULO_2 / NUMERO_TOTAL_DE_DARDOS
    println(s"Estimación de π con $NUMERO_TOTAL_DE_DARDOS dardos: $PI_ESTIMADO_2")

  }

}
