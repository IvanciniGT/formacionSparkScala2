
# Maven

Es una herramienta para automatizar trabajos en proyectos JAVA principalmente (y otros... como Scala).

¿Quién compila un código Java o Scala?
    En el caso de java, el compilador de java: javac.
    En el caso de Scala, el compilador de Scala: scalac.

El JVM interpreta byte-code... lo ejecuta. Maven tampoco es quién compila.

Una cosa que ocurre es que escribir la linea de comando que invoca al compilador (sease de java o de Scala) es un muy tedioso, y si además hay que añadirle las librerías que se usan en el proyecto, pues ya demasiao!

 javac -cp ruta_de_nuestras_librerias -d directorio_de_salida_del_bytecode ruta_de_nuestro_codigo.java
            ^^^^^^^^^
            Hay que poner cada una de las librerías que usamos en el proyecto.
            En un proyecto Apache Spark, podemos estar trabajando fácil 200 librerías.
  
Las rutas, que serán del tipo: c:\ruta_de_mi_proyecto\librerias\apache-spark-core_2.11-2.4.0.jar
cambian cada semana! Van saliendo versiones nuevas de las librerías, y hay que actualizar el proyecto.

Eso no había que configurarlo solo para la compilación... también para la ejecución.

 Herramientas como maven o sbt, o gradle nos ayudan con ese trabajo... Yo solo pongo maven compile.
 Y eso hace muchas cosas:
    - Descarga las librerías que necesito.
    - Genera carpetas (target) donde se guardará el bytecode.
    - Compila mi código.
    - Configura el classpath para que pueda ejecutar mi código.

> Cuando le dais a ejecutar (RUN) en IntelliJ, quién es quien compila el proyecto?

IntelliJ es quien lanza javac/scalac... pero NO LO HACE a través de maven... es él quien se lanzar y construir el classpath y el comando de compilación. También lanza luego el comando de ejecución.
Eso va paralelo a maven, que también puede compilar y ejecutar el proyecto.

Para que quiero maven entonces si ya el intellij lo hace a su bola? con independencia de maven?

El código donde lo guardo? En una carpeta de mi máquina? si lo quiero editar.. Pero a la empresa eso le vale mierda. Lo único importante es lo que ponga en un repositorio de git!.
Al repositorio que subo? 
- La carpeta src
- La carpeta target? NO (byte-code que he compilado en mi máquina)

---

# DEVOPS

Cultura, Movimiento, Filosofía, en pro de la AUTOMATIZACION de los procesos de desarrollo y despliegue de software.

Lo que quiero es que cuando desarrollador haga commit, en automático haya un programa que sea capaz de extraer ese código del repositorio, compilarlo, instalarlo en un servidor de pruebas, ejecutar pruebas automatizadas, y si todo va bien, generar un artefacto (.jar) que se guarde en un repositorio de artefactos (como Artifactory) y que luego se pueda desplegar en producción. TODO EN AUTOMÁTICO

En ese entorno donde se compile el código no habrá INTELLIJ, ni ECLIPSE.. ni mucho menos nadie apretando el botón de ejecuta!

Lo único que tendré ahí será un JVM, un compilador y maven (o sbt o gradle). Y será esa herramienta la que se encargue de compilar, ejecutar pruebas, generar artefactos y desplegarlos.

---

En Spark, qué tipos de datos (clases) existen para guardar información?
- RDD[T]       = Como una Lista/Array de COSAS de tipo T, que tiene funciones de tipo MAP/REDUCE
- Dataset[Row] = Como un RDD(Lista/Array) de COSAS de tipo ROW, donde encontramos funciones de tipo SQL (select, where, group by, order by, etc)
- DataFrame    = No hay genéricos... es una TABLA, con sus filas y sus columnas, aquí también encontramos funciones de tipo SQL (select, where, group by, order by, etc)

El hecho es que los ROW que guarda un Dataset son objetos de la clase Row, que es una clase de Spark que guarda una fila de una tabla, y también tiene columnas.

---

Acabamos de montar la UDF validarDNI. La hemos registrado en el contexto de Spark, y ahora podemos usarla en los SQL. Y hemos visto que funciona.

> Funciona?

Eso parece... pero me temo que hay que tener cosas en cuenta. La primera, que está funcionando en mi entorno local. La pregunta es si funcionará en un entorno de producción (cuando trabajemos con una granja de nodos Spark).

Y ahí me temo que podría no funcionar.

> Dónde se va a ejecutar la función: DNIUtils.validarDNI?

En mi máquina?? NO. En los workers.
Y los workers tienen la clase DNIUtils? 
En su JVM está cargada la clase DNIUtils? NO. En su JVM no está cargada la clase DNIUtils.
Por tanto cuando mande el programa a la granja... petardazo!

    ClassNotFoundException: DNIUtils

El tema es más complejo. No es solo necesario  distribuir a los nodos trabajadores:
- Los datos que se van a procesar (los RDD, Datasets, DataFrames)
- Las funciones que voy a ejecutar sobre esos datos
   - Map/Reduce
   - SQL

A veces también debo mandar el BYTE-CODE de clases que tengo que definen funciones que voy a ejecutar sobre esos datos.

    val NUMERO_DARDOS_DENTRO_CIRCULO_2 = coleccionDeDardosADisparar.map(  _ => (Random.nextDouble(), Random.nextDouble()) )
                                                                   .map{ case (x,y) => x * x + y * y                      }
                                                                   .filter( distancia => distancia <= 1.0                  )
                                                                   .count() // en lugar de .length

A los nodos trabajadores les mando:
- Los datos (coleccionDeDardosADisparar)
- Le mando también las funciones que voy a ejecutar sobre esos datos (map, filter, count)... con sus argumentos:
    -  _ => (Random.nextDouble(), Random.nextDouble())
    -  case (x,y) => x * x + y * y
    -  distancia => distancia <= 1.0
Esas funciones (argumentos de los map, filter, count) son funciones anónimas (lambdas)... y viajan por red.
Lo que le estamos diciendo a los nodos trabajadores es: 
- Coge los dardos a disparar y para cada uno de ellos genera una tupla con 2 números aleatorios.

El nodo trabajador SABE DE ANTEMANO generar números aleatorios, porque tiene la clase Random en su JVM. Es una clase estandar de Scala.

Pero en nuestro caso, con los DNI, le estamos diciendo al nodo trabajador:
- Coge los datos (los Datasets) y para cada uno de ellos, ejecuta
  - DNIUtils.validarDNI

Y el nodo trabajador NO SABE qué es DNIUtils, porque no tiene esa clase en su JVM. Esa clase no viaja por red.
Por red viajan datos, funciones anónimas.... referencias de funciones.

El jugar desde un entorno de pruebas es una cosa... el hacer que un programa de este tipo funcione en un entorno de producción es otra.
Una de las cosas que necesitaremos hacer cuando mandemos nuestro programa a una granja es mandar el byte-code de las clases que sean necesarias en los nodos trabajadores. Normalmente tenemos ese byte code en archivos .jar

Esos jars de nuestro proyecto habrá que mandarlos a los nodos trabajadores.

Spark me da una utilidad para que yo pueda hacer eso de forma sencilla:

                                                        Los jar que yo tengo en mi proyecto,y necesito que estén en los nodos trabajadores
                                                          vvvv
- spark-submit --class DNIUtils --master <master-url> <path-to-jar>     
                          ^^^               ^^^
                          ^^^^              URL del maestro de la granja de nodos Spark
                          Programa que voy a ejecutar

---

# Sacar datos de archivos.

- csv, xlsx, json      ESTO ARCHIVOS SON UN POCO RUINILLA... al trabajar en el mundo BigData, no son los más adecuados.
- parquet, avro

Por qué?

Los archivos csv, json, xlsx(xml) son archivos de texto.
Imaginad este csv:

```csv
id, nombre, CP
1, Juan, 19200
2, Ana, 28900
3, Pedro, 31890
```
Cuánto ocupa "31890" en el HDD? Ese dato se guarda como texto... 5 caracteres: 3, 1, 8, 9, 0
Cuánto ocupa un carácter en un HDD? Depende del juego de caracteres que estemos usando:
- ASCII     1 byte por carácter (pero solo puedo guardar hasta 255 caracteres distintos)
- ISO-8859  1 byte por carácter (pero solo puedo guardar hasta 255 caracteres distintos)
- UTF-8     Entre 1 y 4 bytes por carácter (puedo guardar todos los caracteres que usa la humanidad) 
            Eso lo define el entandar UNICODE.. que ahora mismo tiene cerca de 150.000 caracteres distintos.
            Los 255 primeros caracteres son los mismos que el ASCII, y ocupan 1 byte. (a, j, 9.)
            Los 32.000 siguientes caracteres ocupan 2 bytes (por ejemplo, los caracteres latinos, griegos, cirílicos, etc)
            Los 96.000 siguientes caracteres ocupan 4 bytes (por ejemplo, los caracteres asiáticos)
- UTF-16    2-4 bytes por carácter (puedo guardar todos los caracteres que usa la humanidad)
- UTF-32    4 bytes por carácter (puedo guardar todos los caracteres que usa la humanidad)

UTF-8 suele ser hoy en día el estandar... y en UTF-8, ese CP "31890" ocupa 5 bytes.

En un byte, puedo representar hasta 255 datos distintos.
En 2 bytes, puedo representar hasta 65.535 datos distintos. <<< Ya me da para todos los códigos postales de España

Si guardo el dato como BYTES (BINARIO), en lugar de como texto, el dato "31890" ocupa 2 bytes.
He metido una mejora del 60% en el espacio que ocupa el dato en disco.
El impacto de eso es enorme, sobre todo en el mundo big data, donde los datos que se manejan son enormes:
- Voy a ahorrar una pasta en almacenamiento.
- Mis programas tardarán un 60% menos en leer los datos de disco.
- Mis programas tardarán un 60% menos en escribir los datos a disco.
- Mis programas tardarán un 60% menos en enviar los datos por red.


Parquet y AVRO son archivos binarios.... y eso optimiza MUCHISIMO el espacio que ocupan los datos en disco.

    La diferencia entre parquet y avro es el cómo se guardan los datos dentro del archivo:
    - Avro guarda los datos por registros
                id, nombre, CP
                1, Juan, 19200
                2, Ana, 28900
                3, Pedro, 31890
    - Parquet guarda los datos por columnas
                id, 1, 2, 3
                nombre, Juan, Ana, Pedro
                CP, 19200, 28900, 31890

    Depende el uso que vaya a hacer de los datos, me conviene más un formato u otro.
     BusinessIntelligence -> Parquet
     Spark               entrada de datos --> AVRO
                         salida de datos  --> Parquet

No cambia mucho desde el punto de vista de Spark, el usar formatos binarios o no binarios, en cuanto a la forma de leer los datos.
Otra cosa será el rendimiento que tenga mi programa al leer los datos, y el espacio que ocupen esos datos en disco.

---

El almacenamiento hoy en día es barato o caro? Es con diferencia lo más caro en un entorno de producción.
Tenemos la sensación que el almacenamiento es barato porque tenemos discos duros de 1TB en casa por 50 euros.
En una empresa la cosa cambia...mucho!

1º Los HDD no son del mismo precio... porque no son de la misma calidad. x6...x20 del precio del disco que compro para casa
2º Cuantas copias hago al menos de un dato en un entorno de producción? 3 copias.
    1 Tbs en la empresa, necesito 3 HDD de 1 Tbs de almacenamiento. Donde cada disco es x6... x20 más caro que el de casa.
3º Copias de seguridad... que también se hacen redundantes

Al final ese Tbs, que en casa me sale por 40€, en un entorno de producción se me puede ir a 1.000€ o más.