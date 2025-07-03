
hashtags

1. QUEREMOS un listado con los hashtags buenos!!!!
   1. En paralelo (como efecto SECUNDIARIO) quiero saber cuantos he filtrado

---


        Granja de servidores Spark                                        Otra máquina
   --------------------------------------------                          -----------------
   TRABAJADOR 1 <<<<< conjunto1 <<<<
      JVM3
   TRABAJADOR 2 <<<<< conjunto2 <<<<   CONTROLADOR   <<< tweets  <<<<<   PROGRAMA (tweets)
      JVM4                                     
   ...
   TRABAJADOR N <<<<< conjuntoN <<<<
      JVM5

                               JVM2                          Se ejecuta dentro de una JVM1

Qué manda mi programa al controlador?
- Por un lado los tweets
   El controlador, parte la colección de tweets en varios paquetes.. y va mandando cada paquete a un trabajador.
   Para mandarlos, los manda por una RED... por que lo primero que debe hacer mi programa es transformar los datos a bytes (serializarlos). El controlador debe deserializarlos para poder trabajar con ellos.
   Una vez deserializados, el controlador los reparte entre los trabajadores....
   Que por cierto... para ello los vuelve a serializar y los manda por la red a cada uno de los trabajadores.
   Cada trabajador deserializa los datos que le llegan, y los procesa.
- La lógica que debe ejecutarse sobre los tweets (funciones que escribo dentro de los map, filter...)
   Todas esas funciones, mi programa también las manda a la granja!

> Pregunta!
  De todo ese código que tenemos en nuestro programa, TODO EL CODIGO SE EJECUTA EN MI MAQUINA? NO
  Hay una parte del código que se ejecuta en los nodos TRABAJADORES
  ¿Qué parte?
    Las funciones de los map/reduce, por ejemplo el añadir antes de cada # un espacio no se estan ejecutando en mi máquina, se están ejecutando en los nodos trabajadores.

> Pregunta!

Donde se está ejecutando la función `noContienePalabrasProhibidas` y el filter asociado?

```scala

  .filter(noContienePalabrasProhibidas )

  def noContienePalabrasProhibidas(hashtag: String):Boolean = {
    val contieneElHashtagPalabrasProhibidas = palabrasProhibidas.exists(palabraProhibida => hashtag.toLowerCase().contains(palabraProhibida))
    if (contieneElHashtagPalabrasProhibidas) {
      numeroDeHashtagsFiltrados += 1
    }
    return !contieneElHashtagPalabrasProhibidas
  }

```

En los nodos trabajadores.
Qué hace esa función?
- Devolver true o false, indicando si el hashtag contiene o no palabras prohibidas.
- Incrementar el contador de hashtags filtrados.
  - Qué es ese contador? Una variable... que se guarda donde su valor? EN RAM
    En que RAM? en la de los trabajadores.

> Pregunta!

println(s"Número de hashtags filtrados: $numeroDeHashtagsFiltrados")

Donde se está ejecutando ese println? EN MI MAQUINA
Por lo tanto, el valor de qué variable se está imprimiendo? El valor de la variable `numeroDeHashtagsFiltrados` de nuestra máquina.
Y esa variable, en nuestra máquina, ha ido incrementándose? NO

Esa variable se ha ido incrementando en cada uno de los nodos trabajadores.

Es más... cada nodo trabajador tiene su propia variable `numeroDeHashtagsFiltrados`, y cada uno de ellos ha ido incrementando su propia variable.

Es decir, si un nodo ha recibido 100 hashtags, y ha filtrado 10, su variable `numeroDeHashtagsFiltrados` tendrá el valor 10.
Si otro nodo ha recibido 100 hashtags, y ha filtrado 20, su variable `numeroDeHashtagsFiltrados` tendrá el valor 20.

Esa variable existe en cada una de la JVM que existen.

        Granja de servidores Spark                                        Otra máquina
   --------------------------------------------                          -----------------
   TRABAJADOR 1 <<<<< conjunto1 <<<<
      JVM3
   TRABAJADOR 2 <<<<< conjunto2 <<<<   CONTROLADOR   <<< tweets  <<<<<   PROGRAMA (tweets)
      JVM4                                     
   ...
   TRABAJADOR N <<<<< conjuntoN <<<<
      JVM5

                                           JVM2                         Se ejecuta dentro de una JVM1

 JVM1: numeroDeHashtagsFiltrados = 0
 JVM2: numeroDeHashtagsFiltrados = 0
 JVM3: numeroDeHashtagsFiltrados = 4
 JVM4: numeroDeHashtagsFiltrados = 3
 JVM5: numeroDeHashtagsFiltrados = 5

ESTO ES UN PROBLEMON!
Pero hay algo que no entiendo... por qué narices, cuando hemos ejecutado el programa nos ha salido 1?

Como estamos ejecutando en local, Spark ha creado una granja de prueba, con un solo nodo trabajador, dentro de la misma JVM que mi PROGRAMA. No hay más JVMs. Por ser un entorno de prueba embebido (local[*])

   MI MAQUINA DE DESARROLLO
       JVM
         Mi programa
         Controlador Spark
         1 trabajador Spark

Y en mi máquina JVM solo hay una variable `numeroDeHashtagsFiltrados`, que es la que se ha impreso... y la que se ha ido incrementando.

ESTO ES UN PROBLEMON!
Y a priori no es nada evidente el detectorlo.... porque en principio, cuando estoy haciendo pruebas y mi desarrollo, estoy ejecutando en local, y ahí funciona!

--- 

Pero, me temo que no es el único problema que tenemos. Hay otro! igual de gordo! que aún ni nos imaginamos! consecuencia de nuevo de trabajar en un entorno distribuido.

El nodo controlador, va repartiendo el trabajo entre los nodos trabajadores.

Cuántos paquetes de trabajo manda a cada nodo trabajador el coordinador?
- A priori, dependiendo del número de cores que tenga cada nodo trabajador.
- Eso queremos que sea así? NO
- Ese es el comportamiento por defecto... que no queremos que sea así.... y es algo que hasta ahora no nos habíamos planteado.

---

Tal y como he dejado el programa:
Inicialmente tenía 3 tweets... en cuanto los he particionado? 2 paquetes
- 1 paquete tendrá 2 tweets
- 1 paquete tendrá 1 tweet
Al procesarse esos paquetes dan lugar a colecciones de palabras:
- 1 paquete con 40 palabras
- 1 paquete con 20 palabras
En total 60 palabras.... que parto en 10 paquetes de trabajo... donde cada paquete tendrá 6 palabras.

Tengo 10 nodos? NO
No pasa nada.. el controlador va mandado paquetes 1 a 1 a los nodos trabajadores.
Cuando un nodo trabajador termina de procesar un paquete, le manda el resultado al controlador, y este le manda otro paquete.

Puedo tener 500 paquetes de trabajo, y 10 nodos trabajadores.
El controlador manda 1 paquete a cada nodo trabajador, en una primera atacada manda 10 paquetes, y le quedan 490 paquetes.
Cada vez que un nodo trabajador termina de procesar un paquete, le manda el resultado al controlador, y este le manda otro paquete.... ya quedan 489 paquetes.... y así sucesivamente hasta que se acaben los 500 paquetes.

> Pregunta!

No sería más eficiente repartir el trabajo entre el numero de nodos trabajadores que tengo?
Me interesa que a un nodo trabajador le llegue 1 único paquete más gordo o muchos paquetes más pequeños?

El problema no es tanto la capacidad de procesamiento de cada nodo trabajador, sino el tiempo que tarda en procesar cada paquete... que es un problema:

    Si un nodo es más potente y tarda menos tiempo en procesar su trabajo... me interesa que tome más trabajo, y que no esté esperando a que otros nodos terminen de procesar su trabajo.

Pero hay un problema más grave. Qué pasa si un nodo está procesando un paquete de 100000 palabras y en la 99999 se cuelga / falla, tiene un error? Pierdo todo el trabajo que se ha hecho hasta el momento en ese nodo trabajador. Desde el punto de vista de Spark, no hay problema... lo controla. El controlador al detectar que ese trabajador se ha visto comprometido, vuelve a enviarle el mismo paquete de trabajo a otro nodo... pero debe empezar desde el principio, desde la palabra 1 hasta la 100000. 

En general me interesa tener muchos más paquetes de trabajo que nodos. Quiero ir cerrando trabajos.
No te mando 1000... te mando mejor 10 paquetes de 100 palabras cada uno. Es más... si después de lo que sea que ese nodo esté haciendo hay que por ejemplo hacer un filtro... A la que voy recibiendo la respuesta de cada paquetito de 100 puedo ir filtrando, y no esperar a que se terminen los 1000.

Pero aquí hay algo más... más oscuro!
Cada vez que Spark manda a un nodo un paquete de trabajo, le manda también el código que debe ejecutar para procesar ese paquete de trabajo (la lógica, las funciones map, filter, reduce...).

Esas funciones a su vez, pueden necesitar de otros datos... por ejemplo la lista de palabras prohibidas que hemos definido al principio del programa.

En nuestro caso, tenemos una lista de palabras prohibidas, con 4 cutres y miseras palabras prohibidas.
Y si tuviera 1 Mbs de palabras prohibidas?
Imaginad que quiero enriquecer unos datos que me están llegando...
- Facturas... y viene un Código Postal... y quiero ponerle al lado el municipio/provincia.
- Y ese dato lo tengo en una tabla de códigos postales... eso ocupará un huevo.
- Y ahora empiezo a mandar a unos trabajadores paquetes de facturas... y en cada paquete las funciones que deben ejecutar son las de enriquecer los datos con el municipio/provincia.... y también la tabla de códigos postales.... ESO CON CADA PAQUETE! ME CARGO LA RED ! Le meto una sobrecarga enorme! Y además todo va a ir mucho más lento!

Me interesaría más que esa listad/tabla de códigos postales / palabras prohibidas estuviera en cada uno de los nodos trabajadores, y que cada uno de ellos la tuviera en RAM, y no tener que estar enviándola con cada paquete de trabajo. Eso optimizaría mucho las cosas.

ESTOS PROBLEMAS SON FRUTO de estar trabajando en UN ENTORNO DISTRIBUIDO. Son cosas que en un entorno local no se dan (local[*]), y que en un entorno distribuido son problemas que hay que resolver.


---

RESUMEN:

Hay problemas complejos que se dan como consecuencia de trabajar en un entorno distribuido.
Spark me da soluciones!
Pero para usarlas:
1. Debo entender que estoy trabajando en un entorno distribuido y el impacto que tiene eso en mi código.
2. Debo saber identificar los problemas que se dan como consecuencia de ejecutar mi programa en un entorno distribuido.
3. Debo saber usar las soluciones que me da Spark para resolver esos problemas.

Nos toca hablar de BROADCAST y ACCUMULATORS, que son la solución propuesta por Spark a los problemas que hemos visto.


Broadcast y accumulators son conceptos antagónicos, son lo contrario.

Un accumulator es una variable compartida entre todas la JVMs de java que entran en juego, de forma que:
- Los nodos trabajadores pueden tocar, pero no leer.
- Mi programa puede leer, pero no tocar.

Un broadcast es una variable compartida entre todas la JVMs de java que entran en juego, de forma que:
- Los nodos trabajadores pueden leer, pero no tocar.
- Mi programa puede tocar.
- Además, se mandan a los nodos trabajadores una única vez, y no con cada paquete de trabajo.

Por ejemplo... teníamos un contador de hashtags filtrados... que deberíamos usar? Un acumulador.
    Quien modifica esa variable? Los nodos trabajadores.
    Quien lee esa variable? Mi programa.

Teníamos también una lista de palabras prohibidas... que deberíamos usar? Un broadcast.
    Quien modifica esa variable? Mi programa.
    Quien lee esa variable? Los nodos trabajadores.

----

TABLA DE UNA BBDD
        hashtag:
        ----------
        BestFriends
        SummerLove
        SummerHate
        GoodVibes
        OnFire
        BestFriends
        GoodVibes


        ----

        BestFriends     2
        GoodVibes       2
        SummerLove      1
                                            ---- De estos pasamos.. solo los 3 primeros.
                                            SummerHate      1
                                            OnFire          1


 SELECT hashtag, COUNT(*) AS contador from hashtags
 GROUP BY hashtag ORDER BY contador DESC LIMIT 3

Eso estaría guay... pero... nosotros (por ahora) no tenemos SQL...ni una BBDD ... lo 
que tenemos es operaciones MAP/REDUCE

---

Un RDD es como llama SPARK a una lista paralelizable. El equilavente a una lista de Scala a la que hemos hecho un `.par`

RDD = Resilient Distributed Dataset



    hashtags          

    "BestFriends"                               
    "SummerLove"
    "SummerHate"
    "bestfriends"
    "GoodVibes"
    "OnFire"
    "GoodVibes"


    vvvvv
    .groupBy(hashtag => hashtag.toUpperCase())
    vvvvv
    ("BESTFRIENDS", List("BestFriends", "bestfriends"))
    ("SUMMERLOVE", List("SummerLove"))
    ("SUMMERHATE", List("SummerHate"))
    ("GOODVIBES", List("GoodVibes", "GoodVibes"))
    ("ONFIRE", List("OnFire"))  

     vvvv

                    List("BestFriends", "BestFriends")  > Su tamaño: 2
                    List("SummerLove")                  > Su tamaño: 1
        .mapValues
        
     vvvv

        ("BESTFRIENDS", 2)
        ("SUMMERLOVE", 1)
        ("SUMMERHATE", 1)
        ("GOODVIBES", 2)
        ("ONFIRE", 1)

    vvvvv
    Ordenarlo... por qué concepto?

---

Todo esto del Map/Reduce es muy complejo. El aprender todas las operaciones que existen, y cómo se usan, es algo que lleva tiempo.

Ese problema lo tenemos nosotros... y lo tienen el resto de personas que usan Spark.

Que se hizo en Spark. Tiraron todo esto a la basura! ... o mejor dicho lo escondieron en la trastienda.
Y montaron una librería nueva... que funciona sobre ésta librería (spark-core).

La librería spark-core es la BASE de Spark, y es la que nos permite trabajar con RDDs, y hacer operaciones de Map/Reduce.

Lo que hicieron en Spark fue crear una nueva libreria llamada Spark-SQL, esa librería me permite trabajar con algo así como TABLAS (como si fueran tablas de una BBDD), y hacer operaciones sobre esas tablas, como si fueran consultas SQL.

Internamente, Spark-SQL usa Spark-Core, y por lo tanto, las operaciones que se hacen sobre las tablas son operaciones de Map/Reduce.... pero yo no tengo que preocuparme de eso, porque Spark-SQL me abstrae de todo eso, y me permite trabajar con tablas y consultas SQL.

Eso me dulcifica el problema... en exceso!

La parte negativa: NO TODO lo puedo hacer con Spark-SQL. Solamente aquellos datos que pueda representar como tablas, y aquellas operaciones que pueda representar como consultas SQL.

IMAGINAD los tweets... puedo representar la estructura interna de un tweet como una tabla?
  - Texto
  - Hashtag1
  - Hasthag2
  - Mención1
  - Mención2
El hacer eso ya sería un trabajon... fuera de SQL

"Haciendo exámenes de mierda,con mis amigos de mierda #CacaFriends#SummerHate,que asco!!!!"
         vvvvv

    Texto: Haciendo exámenes de mierda,con mis amigos de mierda... que asco!!!!
    Hashtag1 : CacaFriends
    Hashtag2 : SummerHate
    Hashtag3 : null

    Mención1 : null
    Mención2 : null

SQL y las tablas de una BBDD están pensadas para trabajar con datos estructurados, y los tweets no son datos estructurados, son datos semiestructurados.

Cuando tenga datos estructurados: Listado de clientes, listado de productos, listado de facturas... podré usar Spark-SQL.
Cuando no tenga datos estructurados: Listado de tweets, listado de logs, listado de eventos... no podré usar Spark-SQL.

Hay veces que parte del trabajo lo podré hacer con Spark-SQL, y parte del trabajo lo tendré que hacer con Spark-Core.

El sacar la lista de hashtags que aparecían en los tweets, lo puedo hacer fácil con SQL?


Haciendo exámenes de mierda,con mis amigos de mierda #CacaFriends#SummerHate,que asco!!!!
    vvvv
    ~~CacaFriends~~
    SummerHate

    Filtrando además por hashtags que no contengan palabras prohibidas... 
Ahora...
Teniendo ya ese listado:
    SummerHate
    CacaFriends
    GoodVibes
    GoodVibes
    SummerLove
    BestFriends
     vvvv
     Saca los trending topics, lo puedo hacer fácil con SQL?

      SELECT hashtag, COUNT(*) AS contador from hashtags GROUP BY hashtag ORDER BY contador DESC LIMIT 3

Preferimos escribir esa query o:

      .groupBy(hashtag => hashtag.toUpperCase())
      .mapValues(lista => lista.size)
      .sortBy{case (hashtag, count) => -1*count} // Ordenamos de mayor a menor
      .take(CUANTOS_HASHTAGS_EN_TRENDING_TOPIC)

LA QUERY de todas! Entre otras cosas porque estamos muy acostumbrados a SQL, y porque es más fácil de leer y entender. SQL está además pensado para este tipo de cosas, y es más fácil de usar.


        .sortBy{case (hashtag, count) => -1*count} 
        ORDER BY contador DESC

        .mapValues(lista => lista.size)
        COUNT(*) AS contador


Al trabajar con SparkSQL TODO CAMBIA... incluso la forma de abrir una conexión con el cluster de spark.

La conexión que abrimos con Spark-SQL es diferente a la que abrimos con Spark-Core.
En Spark Core tenemos por ejemplo la función parallelize, que nos permite crear un RDD a partir de una colección de Scala.
Esa función no existe en la conexión que abre Spark-SQL.

Lo que si podemos es pasar de la conexión de Spark-SQL a la de Spark-Core, y viceversa.

Para pasar de una conexión de Spark-SQL a una de Spark-Core, usamos la función `sparkContext` de la conexión de Spark-SQL.

---

Un case class de scala es un objeto de transporte de datos, que nos permite definir una clase que solo alberga datos INMUTABLES... sin funciones asociadas, sin lógica de negocio, sin nada más que datos.

Este concepto existe también en JAVA: Record

---

Nosotros estamos generando un dataframe.
Ese dataframe lo estamos creando (mediante distintas técnicas) desde objetos de un tipo CUSTOM (una clase propia que hemos definido: Persona)

En cualquiera de las opciones que hemos dado, al crear el dataframe, pasamos un segundo argumento: precisamente la clase que define el tipo de los objetos que estamos metiendo en el dataframe.

Si leyeramos de un EXCEL, ese SCHEMA lo genera en automático, y no tenemos que definirlo nosotros.
En nuestro caso, como estamos creando el dataframe desde una colección de objetos, debemos definirlo nosotros.

Aunque lo puede generar el también en automático desde la estructura de la clase que le pasamos como segundo argumento.

Cuando hacemos esto, spark trata de inferir en automático los datos que tenemos en la clase, y los tipos de datos que tiene cada uno de los campos de la clase.
Y cómo hace eso?
Mediante un concepto que en JAVA llamamos REFLECTION.
Lo que hace SPARK es mirar todas las funciones de la clase cuyo nombre empieza por "get", y las convierte en campos del dataframe.