
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


{
    "nombre": "María",
    "apellidos": "García",
    "edad": 43, 
    "dni": "23000046T", 
    "cp": "28006", 
    "email": "maria@garcia.com"
}


Al crear programas en Spark, podemos crear programas:
- Que se ejecuten en modo batch (los jueves a las 3am)
- Que se ejecuten en modo streaming (procesando datos según van llegando.. for ever and ever)
   - Spark hace algo cutre con este... realmente lo que hace son muchos programas batch que se ejecutan cada X segundos.
   - Antiguamente había una librería en spark llamada Spark Streaming que hacía eso, pero ya no se usa.
   - Esta totalmente obsoleto! Desde hace años.
   - Hoy en día esa funcionalidad la absorbe Spark Structured Streaming, que es una API de Spark SQL.

En algunos casos, leemos archivos de texto... los jueves a las 3am, y los procesamos.
En otros casos, leemos datos de una cola de mensajes (como Kafka) y los procesamos según van llegando.

---

JOINS en BBDD.

Las BBDD son unos seres extraordinarios. Son capaces de hacer operaciones muy complejas sobre datos de forma muy eficiente.

> Pregunta:

Cómo resuelve una BBDD un JOIN entre dos tablas? Cómo lo hace por dentro?
- Las BBDD tienen distintas formas de hacer un JOIN entre dos tablas:
   - Nested Loop Lookup
   - Hash Join
   - Sort Merge Join


|   TABLA1                    |
| id | nombre         | cp    |
| 1  | Juan           | 19200 | 
| 2  | Ana            | 28900 | 
| 3  | Pedro          | 31890 | 


|  TABLA2              |
| cp    | provincia    |
| 19200 | Madrid       | 
| 28900 | Madrid       | 
| 31890 | Barcelona    | 
| 36890 | Cuenca       |

| TABLA 1 x TABLA 2                             |
| id | nombre | cp(t1)    | cp(t2) | provincia  |
| 1  | Juan   | 19200     | 19200  | Madrid     |
| 2  | Ana    | 28900     | 28900  | Madrid     |
| 3  | Pedro  | 31890     | 31890  | Barcelona  |
| -  |  -     |  -        | 36890  | Cuenca     |


EN GENERAL, CUANDO TENGO UNA TABLA 2 MUY PEQUEÑA MUY PEQUEÑA ESA TABLA ENTRA SIN PROBLEMAS EN MEMORIA RAM... Y UN NESTED LOOP LOOKUP ES MUY EFICIENTE.

EN CUANTO TABLA2 es más grandecita.. la tabla no entra en RAM... y no hay huevos a hacer un NESTED LOOP LOOKUP.... en ese caso, la BBDD opta por hacer un HASH JOIN o un SORT MERGE JOIN.

El problema GORDO DE NARICES que que para que ese JOIN se pueda realizar, lo primero que hay que hacer es que las dos tablas estén ordenadas por el campo de unión (cp en este caso).

> Pregunta: Qué tal se le da a un ORDENADOR ordenar datos? FATAL ! De lo peor que le puedo pedir.

De hecho, las BBDD, expertas en JOINS, usan una estrategia muy particular:
Tener una copia de los datos preordenados en el HDD... ocupando el doble de espacio en el HDD = INDICE

Si una columna de una tabla me ocupa 10Mbs, el índice cuán puede llevar a ocupar? Se me puede ir a 20, 30, 40Mbs

Gracias a esas copias de los datos ordenados, las BBDD pueden hacer JOINS de forma muy eficiente.

En Spark tenemos INDICES? NO

Spark NO ES UNA BBDD... así me hayan hecho el paripé para que pueda usar sintaxis SQL para transformar los datos. La librerías SparkSQL, lo único que hace al final es transformar las consultas SQL operaciones MAP/REDUCE.

Aquí entra ese concepto que os comenté el otro día: Variantes ETLs

 - E, T, L
 - E, T, L, T
 - T, E, T, L
 - T, E, L
 - T, E, L, T

Una transformación de los datos puede ser enriquecerlos. Me interesa hacerlo en Spark? Depende... puede que si... puede que no.

En Spark hay que tener mucho cuidado con los JOINS, porque no tenemos una BBDD, con sus índices... y toda su parafernalia, que me aseguran que esas operaciones se van a hacer de forma eficiente.

Si tengo una tabla 2 (con la que hago el join) muy grande, el JOIN va a ser muy lento (extremadamente lento). SERIA UNA CAGADA desproporcionada en hacer esto con Spark.

OJO A las funciones que veis disponibles.

Una tabla 2 pequeña... básicamente hablamos de que entre en RAM con holgura.

Necesitamos tener en cuenta otra cosa.
La gracia de usar una herramienta como SPARK es poder paralelizar la carga de trabajo REPARTIENDOLA entre distintas máquinas físicas. BIEN!

Sease una tabla 1 con 1M de datos... que quiero enriquecer con datos de una tabla 2 con 100.000 datos.

Se pueden paralelizar los datos de la tabla 1? Claro... sin problema:
     50 paquetes de 20.000 datos cada uno... que iré mandando a los nodos trabajadores.
Se pueden paralelizar los datos de la tabla 2? NI DE BROMA !
     Tengo a priori idea de qué datos le han llegado a cada nodo trabajador de la tabla 1.
     ES DECIR... Tengo la tabla de CP(tabla 2).. y la de clientes (tabla 1).

    Tengo 1M de clientes... que reparto en 50 paquetes de 20.000 clientes cada uno.
    A cada nodo trabajador voy mandando paquetes de 20.000 clientes.
    Conozco a priori los CP que se usan en esos 20.000 clientes? Ni idea.
    Por ende, que datos de la tabla de CP voy a necesitar en ese nodo? TODOS !

Los códigos postales los necesito TODOS en TODOS los nodos trabajadores.
 (NOTA: Por cierto... que me interesaría hacer con la tabla de CP?  BROADCAST!  ) 


Hay que entender bien el COMO funcionan los JOINS en Spark, porque si no, podemos hacer una cagada monumental.


Si tengo que mandar 100.000.000 millones de datos a un nodo trabajador... solo en transporte de datos... cuánto me tarda la operación? LA VIDA!

Otra cosa es que los datos YA estén prerepartidos en los nodos trabajadores. ESTO ES MUY HABITUAL

---

## Hadoop. 

Una de las cosas que nos ofrece:
- HDFS (Hadoop Distributed File System)
- Implementación MAP / REDUCE (que ésta es la que nosotros cambiamos por Spark)

Esto está pensado para correr sobre entornos POSIX. HDFS utiliza los comandos típicos de POSIX para interactuar con el sistema de ficheros.


---

# Qué era UNIX?

Era un SO... que hacía la gente de AT&T (en sus lab. Bell) en los años 70.
Lo dejaron de hacer a principios de los 2000.
El tema es que por entonces los SO se licenciaba de forma distinta. Hoy en día tenemos EULAs(End User License Agreements) que nos dicen lo que podemos y no podemos hacer con el SO. Donde el fabricante ofrece una licencia de uso a un usuario. 
Antiguamente AT&T licenciaba UNIX a empresas, y esas empresas (en su mayoría fabricantes de hardware) lo adaptaban a su hardware, y lo vendían como un SO para sus máquinas: Olivetti UNIX, Commodore UNIX, Atari UNIX, etc.

Llego a haber más de 400 versiones de UNIX distintas, cada una adaptada a un hardware distinto.... y muchas empezaron a ser incompatibles entre sí.
Para poner orden salieron 2 estándares (en paralelo): SUS(Single UNIX Specification) y POSIX (Portable Operating System Interface).

# Hoy en día Un SO Unix 
Es aquel que cumple con esos estándares.
Hay muchas empresas que fabrican sus propios SO para su propio HW:
IBM: AIX (UNIX®)
HP: HP-UX (UNIX®)
Apple: macOS (UNIX®)

Hubo iniciativas para montar SO basados en esos estándares, pero sin pagar por el UNIX® (sellito):

BSD (Berkeley Software Distribution). La cagaron... lo consiguieron: 386-BSD... Pero la cagadon... Anunciaron a BOMBO y PLATILLO que era un UNIX®... 
Y AT&T deuncia! Ltigos durante más de una década... Cuando se resolvió, la arquitectura de microprocesadores 386 ya estaba muerta y enterrada, ese SO... inutil.

La gente de GNU (Richard Stallman) intentaron hacer lo mismo:
   GNU: GNU's Not UNIX
   No valieron... lo intentaron... pero no valieron. Montaron TODO lo que hace falta para un SO... menos una piececita: el kernel.

Frustrado, nuestro amigo Linus Torvalds, en 1991, decidió hacer un kernel de SO que fuera compatible con POSIX y SUS.
Y como dedo culo! Se junto Linux - GNU -> GNU/Linux (que en su momento seguía los estándares de UNIX .. o eso creemos.. nunca se certificó como UNIX®). Hoy en día... ya ni se lo plantean.. tienen su linea de desarrollo independiente.

# Linux?

No es un Sistema operativo... es un Kernel de SO.
Un SO está formado por un montón de programas... entre ellos, un subconjunto de programas es lo que llamamos el kernel. Todos los SO tienen un kernel. El kernel es el subconjunto de programas que se encargan de gestionar el hardware de la máquina, y de gestionar algunas funciones core del SO:
- Gestión de procesos,
- Planificación de sistemas de ficheros,
- Gestión de RAM
- Seguridad

Habitualmente usamos la palabra LINUX para referirnos a un SO llamado GNU/Linux.
Ese SO GNU/Linux se ofrece mediante distribuciones, que son conjuntos de programas que incluyen:
- El kernel de Linux
- Un montón de programas GNU (GNU es un proyecto que empezó Richard Stallman )
- Una interfaz gráfica (que puede ser Gnome, KDE, etc)
- Unas terminales: bash, zsh, fish, etc.
- Un gestor de paquetes (apt, yum, dnf, etc)
- Un montón de programas que hacen que el SO sea usable.


# Windows?

No es un SO.... es una familia de SOs.
    Windows 3
    Windows 95, 
    Windows 98,
    Windows NT,
    Windows 2000,
    Windows XP,
    Windows Vista,
    Windows 7,
    Windows 8,
    Windows 10,
    Windows 11.
    Windows server 2019

Microsoft a lo largo de los años ha creado 2 kernels de so:
- DOS
  -  MS-DOS, Windows 3, Windows 95, Windows 98, Windows ME
- NT (New Technology)
  - Windows NT, Windows 2000, Windows XP, Windows Vista, Windows 7, Windows 8, Windows 10, Windows 11, Windows Server 2019


# POSIX

En POSIX de define por ejemplo:
 - Estructura de directorios
    /
     bin/
     tmp/
     opt/
     var/
     home/
     ...
 - Permisos de ficheros
    - rwx rwx rwx
 - Comandos de terminal
    - ls, cp, mv, rm, mkdir, rmdir, cat, head,... y así como 50