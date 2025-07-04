
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
