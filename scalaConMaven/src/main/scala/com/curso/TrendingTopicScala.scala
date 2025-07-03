package com.curso
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.util.LongAccumulator
import org.apache.spark.{SparkConf, SparkContext}

//import scala.collection.parallel.CollectionConverters._


object TrendingTopicScala {

  val CUANTOS_HASHTAGS_EN_TRENDING_TOPIC= 4;

  def main(args: Array[String]): Unit = {
    // Me voy a crear una colección de datos... tweets

    val conf = new SparkConf().setAppName("CalcularTrendingTopics").setMaster("local[4]")
    val conexion = new SparkContext(conf) // A las conexiones de Spark las llamamos CONTEXTOS.

    val numeroDeHashtagsFiltrados: LongAccumulator = conexion.longAccumulator("Numero de hashtags filtrados") // Acumulador para contar los hashtags filtrados
                                                        // Ese nombre sirve de identificador compartido entre las JVMs
    val palabrasProhibidas = conexion.broadcast(Array("caca", "culo", "pedo", "pis")) // Con esto estamos mandando el array de palabras prohibidas a todas las JVMs donde vaya a ejecutarse mi proceso Map/Reduce

    val tweets = conexion.parallelize(Array[String](
      "En la playa con un mis amigos #SummerLove#OnFire#BestFriends#GoodVibes",
      "Haciendo exámenes de mierda,con mis amigos de mierda #CacaFriends#SummerHate,que asco!!!!",
      "De parranda con mis amigos #SummerLove#Parranda100%#BestFriends#GoodVibes"
    ), 2);

    // Aplicamos un proceso Map/Reduce para extraer un listado de todos los hashtags que aparecen en esos tweets
    //tweets.map( (tweet:String) => anadirEspacioAntesDelCaracterAlmohadilla( tweet ) ) // No es necesario poner el tipo de dato en los argumentos ya que se infieren de la colección
    //tweets.map( tweet => anadirEspacioAntesDelCaracterAlmohadilla( tweet ) ) // Tampoco es necesario al definir una lambda especificar el tipo de dato que se devuelve ya que se infiere del código... ç
    // de lo que devuelve.... En nuestro caso, lo que devolvemos es el resultado de la función
    // anadirEspacioAntesDelCaracterAlmohadilla, que es un String
    // Eso que hemos hecho es absurdo. Hemos creado una función que recibe un texto y devuelve el resultado de la función anadirEspacioAntesDelCaracterAlmohadilla
    // Y no existía ya una función que hiciera eso? LA PROPIA FUNCION anadirEspacioAntesDelCaracterAlmohadilla
    //tweets.map( anadirEspacioAntesDelCaracterAlmohadilla )
    // Otra alternativa que tendría es no definir con sintaxis tradicional la funcion anadirEspacioAntesDelCaracterAlmohadilla, sino definirla como una lambda
    tweets.map( texto => texto.replaceAll("#", " #") )         // Anotamos que hay que añadir un espacio en blanco antes de cada almohadilla (LAZY). Sería absurdo documentarlo asi.
      // Al escribir comentariop diríamos:
      // Añadimos un espacio en blanco antes de cada almohadilla
      //.map( separarTokens )     // Esto da lugar a una colección(ficticia) con 3 arrays de Strings (3 listas
      // de palabras)
      // ["En", "la", "playa", "con", "un", "mis", "amigos", "#SummerLove", "#OnFire", "#BestFriends", "#GoodVibes"]
      // ["Haciendo", "exámenes", "de", "mierda", "con", "mis", "amigos", "#CacaFriends", "#SummerHate", "que", "asco"]
      // ["De", "parranda", "con", "mis", "amigos", "#SummerLove", "#Parranda100%", "#BestFriends", "#GoodVibes"]
      // Esto NO ES LO QUE QUIERO. Lo que querría es tener UNA lista de palabras.
      // Me gustaría que esas 3 listas de palabras se juntaran en una sola lista de palabras
      .flatMap(separarTokens)                     // Extraemos las palabras (flatmap = Es un map, seguido de un flatten)
      .repartition(10)
      .filter( comienzaPorAlmohadilla )           // Me quedo con los tokens que comienzan por almohadilla
      .map(   hashtag => hashtag.substring(1) )  // Les quito la almohadilla

      /// Queremos eliminar aquellos hashtags que contengan una palabra prohibida
      .filter( hashtag => noContienePalabrasProhibidas(hashtag, numeroDeHashtagsFiltrados, palabrasProhibidas) ) // Aquí usamos el valor del broadcast de palabras prohibidas
      // CALCULAMOS AHORA LOS TRENDING TOPICS
      .groupBy(hashtag => hashtag.toUpperCase())
      .mapValues(lista => lista.size)
      .sortBy{case (hashtag, count) => -1*count} // Ordenamos de mayor a menor
      .take(CUANTOS_HASHTAGS_EN_TRENDING_TOPIC)

      // Cómo lo veis? Esto es enrevesado, complicado... abstracto... lioso... necesito aprender un huevo de funciones map... ES COMPLEJO! MUY COMPLEJO!
      // Pensar un algortimo en términos de MAP/REDUCE es algo MUY COMPLEJO! Es donde está el problema... no en el uso de Spark... que es muy sencillo.
      // Me quedo con los hashtags que NO contienen palabras prohibidas
      .foreach( println );

    println(s"Número de hashtags filtrados: ${numeroDeHashtagsFiltrados.value}")

    //val nuevaColeccionFicticia = tweets.map( texto => texto.replaceAll("#", " #") ) // En esta colección.. que en la realidad no se genera.. pero conceptualmente nos ayuda.
    // Cuántas cosas habría?   3 tweets

    // Este ejemplo por ahora lo dejamos AQUI! Ya cuando lo llevemos a Spark, calcularemos la parte final: TRENDING TOPICS... Que es agrupar ahora esos y contar cuantos hay de cada uno
    // Aqui hemos ido aplicando un algoritmo MAP/REDUCE... sin necesidad de Spark. Spark lo UNICO que me permitirá es llevar la ejecución de
    // este algoritmo que hemos planteado a una granja de nodos.
    // Si lo quiero ejecutar en local, no necesito spark para NADA.
    // Y el llevarlo a Spark será cuestión de cambiar 2 lineas en este programa.... y ninguna de ellas en la parte del algoritmo MAP/REDUCE
    conexion.stop()
  }

  def noContienePalabrasProhibidas(hashtag: String, numeroDeHashtagsFiltrados: LongAccumulator, palabrasProhibidas: Broadcast[Array[String]]): Boolean = {
    val contieneElHashtagPalabrasProhibidas = palabrasProhibidas.value.exists(palabraProhibida => hashtag.toLowerCase().contains(palabraProhibida))
    if (contieneElHashtagPalabrasProhibidas) {
      numeroDeHashtagsFiltrados.add(1)
    }
    return !contieneElHashtagPalabrasProhibidas
  }

  /*
  def anadirEspacioAntesDelCaracterAlmohadilla(texto:String): String = {
    return texto.replaceAll("#", " #")
  }
  */
  def separarTokens(texto: String): Array[String] = {
    texto.split("[ ,.;:_+*/€$¿?¡!()-=<>\"'\\[\\]]+")     // "mierda, con" 2 palabras separadas por ", "     "mierda", "con"
    // Si no indico eso (el + del final= En "mierda, con" me devolvería 3 tokens... habría 2 particiones: "mierda", "", "con"
  }

  def comienzaPorAlmohadilla(token:String) : Boolean = token.charAt(0) == '#'
  //return token.startsWith("#")

}