package com.curso

import org.apache.spark.sql.{Row, SparkSession}

import scala.jdk.CollectionConverters._

object SparkSQL {

  def main(args: Array[String]): Unit = {

    // 1º Abrir conexión a Spark
    val conexion = SparkSession.builder()
                                .appName("SparkSQL")
                                .master("local[*]") // Usar todos los núcleos locales
                                .getOrCreate()

    // Lo que viene ahora cambia por completo... ya no hay map/reduce... en su lugar hay funciones muy parecidas a las de SQL... o incluso directamente SQL.

    // Cuando trabajamos con spark-core, los datos los tenemos guardados en RDD
    // Cuando trabajamos con spark-sql, los datos los tenemos guardados en DataFrame o Dataset.
    // En el dataframe no vamops a tener operaciones map/reduce... lo que tenemos son operaciones similares a las de SQL.
    // Habéis trabajado con PANDAS? en PYTHON? Esto es más o menos lo mismo!

    // Un dataframe no es una lista de datos como lo era un RDD, sino que es una tabla con filas y columnas.
    // Puedo hacer una query a una BBDD Relacional y su resultado obtenerlo en forma de DataFrame.
    // También puedo leer un archivo CSV, EXCEL, JSON, Parquet, etc. y obtener un DataFrame.
    // Incluso puedo crear un dataframe yo mismo.. a manita. Que por ahora es lo que vamos a hacer!

    val datosPersonas = Array[Persona](
      new Persona("Menchu",   30, "Madrid", "12345678A"),
      new Persona("Federico", 25, "Barcelona", "23456789B"),
      new Persona("Marcial",  35, "Sevilla", "34567890C"),
      new Persona("Fernanda", 28, "Sevilla", "45678901D"),
    ) //.asJava
    // Pregunta... eso (datosPersonas) es un objeto de SparkSQL? NO .. es un ARRAY de SCALA
    //val df = conexion.createDataFrame(datosPersonas, classOf[Persona])
    val rddPersonas = conexion.sparkContext.parallelize(datosPersonas) // Esto crea un RDD de tipo Persona
    val dataframePersonas = conexion.createDataFrame(rddPersonas, classOf[Persona])
    /*
    val rddFilas = conexion.sparkContext.parallelize(datosPersonas.map( persona => Row(
      persona.nombre,
      persona.edad,
      persona.ciudad
    )))
    val dataframePersonas = conexion.createDataFrame(rddFilas,
      conexion.createDataFrame(
        Seq(
          ("nombre", "string"),
          ("edad", "integer"),
          ("ciudad", "string")
        )
      ).schema
    )
    */

    //val dataframePersonas = conexion.createDataset(datosPersonas) // Creamos un Dataset a partir de la colección de datos que hemos creado
    //  .toDF("nombre", "edad", "ciudad") // Convertimos el Dataset a DataFrame y le damos nombre a las columnas

    // En general no usaremos ninguna de esas opciones... Ya que en general, NO VAMOS A CREARNOS CONJUNTOS DE DATOS DENTRO DEL PROGRAMA
    // Los leeremos de archivos, BBDD, KAFKA
    // En nuestro ejemplo, y por ahora vamos a crear aqui dentro el conjunto de datos.
    // Al final debo cerrar conexión

    dataframePersonas.show()
    dataframePersonas.printSchema() // Qué columnas tenemos en la tabla (dataframe) , sus tipos de datos, y si son obligatorias o no (si admiten null)

    println("El tamaño del dataframe es: " + dataframePersonas.count()) // Cuántas filas tiene el dataframe
    // Una vez que tenemos un dataframe, podemos indicarle a spark SQL que queremos operarlo a través de SQL, que utilice ese dataframe como si de una Tabla de una BBDD se tratase.
    dataframePersonas.createOrReplaceTempView("personas") // Asociar el nombre "personas" a mi dataframe.
    // Desde este momento, podré usar queries SQL para trabajar contra una tabla llamada 'personas' sin problema
    val nuevoDataframeConLasPersonasMayoresDe30Anos = conexion.sql("SELECT * FROM personas WHERE edad > 30")
    nuevoDataframeConLasPersonasMayoresDe30Anos.show()

    import conexion.implicits._ // Importamos las implicits de Spark SQL para poder usar funciones como $"nombre" o $"edad" en lugar de col("nombre") o col("edad")

    // No es neceario ejecutar instruccciones SQL para trabajar con un dataframe... Spark también nos da algunas funciones propias.
    dataframePersonas.select( $"nombre", $"edad" ).show() // Seleccionamos las columnas nombre y edad del dataframe personas
    dataframePersonas.groupBy( $"ciudad" ).count().show() // Agrupamos por ciudad y contamos cuántas personas hay en cada ciudad

    // En general, ya que tenemos la opción de SQL, solemos usar poco la sintaxis de funciones de Spark SQL.
    // En algunos casos puede ser más cómodo.. pocos.
    //dataframePersonas.groupBy( $"ciudad" ).count().show() // Agrupamos por ciudad y contamos cuántas personas hay en cada ciudad
    conexion.sql("SELECT ciudad, COUNT(*) as total FROM personas GROUP BY ciudad").show() // Agrupamos por ciudad y contamos cuántas personas hay en cada ciudad usando SQL

    conexion.sql("SELECT nombre, dni, validarDNI(dni) as dniValido from personas ").show() // Seleccionamos nombre y dni de las personas que tienen dni no nulo
    // SQL viene coin una serie de funciones estandar: sum, avg, count, min, max, etc.
    // Viene en SQL la función validarDNI? NO
    // La vamos crear nosotros: UDF = User Defined Function
    // Esto será una opción.
    // La otra: transformar los datos de un dataframe a un RDD, y aplicar una función map sobre ese RDD.
    conexion.stop()
  }

}

class Persona (val nombre: String, val edad: Int, val ciudad: String, val dni:String) extends Serializable {
  def getNombre(): String = nombre
  def getEdad(): Int = edad
  def getCiudad(): String = ciudad
  def getDNI(): String = dni
}
/*
case class Persona ( nombre: String,  edad: Int,  ciudad: String){
  def getNombre(): String = nombre
  def getEdad(): Int = edad
  def getCiudad(): String = ciudad
}

 */
// Cuando trabajamos con case class... las case clases son serializables por defecto