package com.curso

import ResultadoValidacionDNI.ResultadoValidacionDNI
import scala.collection.parallel.CollectionConverters._

//import scala.util.matching.Regex

object DNIUtils {

  val NUMERO_DNI_MAXIMO = 99999999
  val SEPARADORES_ADMITIDOS = " -"
  val DIGITOS_CONTROL_VALIDOS = "TRWAGMYFPDXBNJZSQVHLCKE"
  //val PATRON_DNI:Regex = """^(([0-9]+)|([0-9]{1,3}([.][0-9]{3})*))[ -]?[a-zA-Z]$""".r // . convierte el patron en una expresión regular (regex) que valida el formato del DNI
  // En ese caso, tendríamos que importar Regex de scala
  val PATRON_DNI = """^(([0-9]+)|([0-9]{1,3}([.][0-9]{3})*))[ -]?[a-zA-Z]$""".r // . convierte el patron en una expresión regular (regex) que valida el formato del DNI
  // La triple comilla nos evita el tener que escapar caracteres especiales de dentro del patrón

  // Esta función está escrita en ESPAÑOL!
  // Hay funciones que creamos en Español
  // Otras funciones las creamos usando código SCALA... funciones de scala... cosas de scala (o python, o java...)
  // No queremos mezzclarlas... No queremos una función donde hablemos en Español y en SCALA!
  // FOLLON!!!! = MANTENIEBILIDAD/LEGIBILIDAD MUY POBRE!!! ESO NO!!!!
  def validarDNI(dni:String): ResultadoValidacionDNI = {
    if(!verificarSuFormato(dni))
      return ResultadoValidacionDNI.ERROR_INVALID_FORMAT
    val dniLimpio = limpiarDNIDePuntosYSeparadores(dni)
    val (numero,letra) = extraerNumeroYLetra(dniLimpio)
    if(!estaElNumeroEnRangoValido(numero))
      return ResultadoValidacionDNI.ERROR_INVALID_LENGTH
    if(!coincideLaLetraQueMeHanDado(numero, letra))
      return ResultadoValidacionDNI.ERROR_INVALID_CONTROL_DIGIT
    return ResultadoValidacionDNI.OK
  }

  def verificarSuFormato(dni: String) : Boolean = PATRON_DNI.matches(dni) //PATRON_DNI.findFirstIn(dni) != Option.empty

  // Texto    HOLA AMIGO
  // Patrón   H.LA
  // Que devuelve FindFirst?  HOLA   ->  Option[String]
  // Texto    HULA AMIGO
  // Que devuelve FindFirst?  HULA   ->  Option[String]
  // Texto    ADIOS AMIGO
  // Que devuelve FindFirst?  Aquí no hay ocurrencia. No quiero devolver un String vacío. No devuelvo nada: Option.empty


  def estaElNumeroEnRangoValido(numero:Long) : Boolean = numero <= NUMERO_DNI_MAXIMO && numero > 0

  def limpiarDNIDePuntosYSeparadores(dni: String) : String = dni.replaceAll("[."+SEPARADORES_ADMITIDOS+"]", "")

  def coincideLaLetraQueMeHanDado(numero: Long, letra: String) : Boolean = letra.equalsIgnoreCase(DIGITOS_CONTROL_VALIDOS.charAt(numero.toInt  % 23).toString)

  def extraerNumeroYLetra(dni: String) : (Long, String) = {
    val numero = dni.substring(0, dni.length - 1).toLong
    val letra = dni.charAt(dni.length - 1).toString;    //dni.substring(dni.length - 1, dni.length)
    return (numero, letra)
  }

  def main(args: Array[String]): Unit = {
    val dnis = Array("23000000T","23000000A", "23000000-T", "23000000 T", "23000000t", "23000000-t", "23000000 t",
      "23000000$t", "23.000.000T", "230.00.000T", "23000000000000T").par

    // Los valido usando por ejemplo un filter
    dnis.map(dni => (dni, validarDNI(dni)))
      .filter{  case (_, resultado) => resultado != ResultadoValidacionDNI.OK}
      .foreach(println)

  }
  /*
   23000000T        √
   23000000-T       √
   23000000 T       √
   23000000t        √
   23000000-t       √
   23000000 t       √
   23000000$t       x   DUDO DE LA CALIDAD DEL DATO!
   23.000.000T      √
   230.00.000T      x   DUDO DE LA CALIDAD DEL DATO! Si no quieres poner puntos, está bien.
                        Si los pones, también... siempre y cuando pongas el punto en el lugar correcto.
   23000000000000T  x
   */
}

// Definimos un enumerado con los valores potenciales retornados por nuestra función de validación
object ResultadoValidacionDNI extends Enumeration{
  type ResultadoValidacionDNI = Value
  val OK, ERROR_INVALID_FORMAT, ERROR_INVALID_LENGTH, ERROR_INVALID_CONTROL_DIGIT = Value
}

