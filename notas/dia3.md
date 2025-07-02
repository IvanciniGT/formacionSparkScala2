Option


---

Estamos montando una librería que nos permita buscar el significado de unas palabras en un diccionario.
Entendemos que un significado de una palabra es un String.

```scala
class Diccionario{

    //...

    def getSignificados(palabra:String):Array[String] = {

    }

    def existe(palabra:String):Boolean = {

    }


}
```
- Por último, el desarrollador podría estar devolviendo null !
- Una opción es que esa función, el desarrollador haya decidido que devuelva un Array vacío si no existe la palabra.
- Otra es que genere un error (lanzar una excepción).
  Nunca debería lanzar una Exception para controlar flujo de una app.
  Una Exception es muy cara de generar (computacionalmente hablando). Lo primero es generar un volcado de pila de llamadas, y eso es caro.
  Además conceptualmente NO TIENE SENTIDO. Las excepciones son para tratar casos con los que a priori no es posible lidiar.

A priori no doy información NINGUNA sobre que devuelvo en caso que la palabra no exista.

Como se resuelve esa ambigüedad?
1. POR CONVENIO: Se decide en SCALA que una función NUNCA DEBE DEVOLVER null. Eso está considerado una muy mala práctica.
2. En este caso, quiero poder diferenciar entre "no existe" y "existe".
La función se llama `getSignificados`, y eso solo puedo hacerlo si la palabra existe.
Y quiero dejar claro, a quién use esta función (quizás yo.. o mi yo del futuro.. u otra persona) que si la palabra no existe la función NO PUEDE HACER SU COMETIDO... y por ende NO VOY A DEVOLVER LO MISMO (Un Array[String]) que si la palabra existe. REPITO: En caso que la palabra no exista (ES UN REQUISITO PARA ESTA FUNCION) debe devolver algo diferente.

Ahi sale un concepto (una clase) que se llama `Option`. En Java también existe: Optional.
Nuestra función debería devolver un `Option[Array[String]]` y no un `Array[String]`.

```scala
class Diccionario{
    //...

    def getSignificados(palabra:String):Option[Array[String]] = {
        if(!existe(palabra)){
            None
        } else {
            Some(Array("significado1", "significado2"))
        }
    }

    def existe(palabra:String):Boolean = {
        // Comprobar si la palabra existe en el diccionario
    }
}

// Al usar esa función:
val potencialesSignificados: Option[Array[String]] = diccionario.getSignificados("melón")
if(potencialesSignificados.isEmpty){
    println("La palabra no existe en el diccionario")
} else {
    val significados = potencialesSignificados.get // con el get saco lo que hay dentro del Option (de la caja)
    significados.foreach(println)
}
```

Pensad en el Option como una caja... que puede llegarme rellena o no. Siempre recibo la caja... pero tendrá algo dentro?

```py
def generar_informe(titulo, datos):
    # Codigo
    pass
```


---

Python : pip (módulo de python)
 ~~$ pip install pandas~~
 $ python -m pip install pandas

 PIP es una herramienta (módulo de python) que nos ayuda a descargar e instalar módulos de python. (DEPENDENCIAS)

SBT y MAVEN son mucho más que eso. NO HAY ALGO EQUIVALENTE EN PYTHON.

SBT y MAVEN y GRADLE son herramientas de automatización de tareas de proyectos.
Lo primero que necesitamos hacer con un proyecto SCALA o JAVA es compilarlo.
Eso generará un montón de archivos.class, que me interesará que se guarden en una determinada carpeta.
Para distribuirlo, necesito meter todos esos archivos .class en un ZIP.. al que le ponemos la extensión .jar (Java ARchive).

Todo este tipo de actividades se pueden hacer con SBT, MAVEN o GRADLE: ME AUTOMATIZAN TAREAS HABITUALES EN EL PROYECTO:
- Compilación
- Ejecución de tests
- Generación de documentación
- Empaquetado: Generación de un JAR
- Subir el .jar a un repositorio (artifactory)
- Entre ellas  (entre las cosas que hacen) está también la gestión de dependencias.
Si hay una librería que necesitamos, la registramos en una de estas herramientas... y ellas se encargan de descargarla, de meterla en el classpath, etc.

CLASSPATH es una cosa que usa la JVM...
Es una variable de entorno, como el PATH, que le dice a la JVM donde están las librerías que necesita para ejecutar un programa (que habremos descargado previamente con SBT, MAVEN o GRADLE).
Esas herramientas a su vez se encargan también de configurar el CLASSPATH.

El trabajar con un proyecto SCALA o JAVA es mucho más complejo que con un proyecto de Python.

En nuestro caso estamos usando SBT. SBT es la herramienta recomendada para trabajar con SCALA.
De hecho si os fijais, en INTELLJ IDEA, cuando creamos un proyecto SCALA, solo me deja elegir SBT como herramienta de automatización de tareas.

---

# Dependencias

Mi programa puede requerir de librerías externas para funcionar: Spark, Librería de colecciones paralelizables de Scala, etc.

SBT, MAVEN y GRADLE son herramientas que nos permiten gestionar esas dependencias. Y parte de la gestión es DESCARGARLAS... Pero de donde las descargan esas herramientas?

Habitualmente de MAVEN CENTRAL, que es un repositorio de librerías Java y Scala. Es el gran repositorio MUNDIAL de librerías Java y Scala. Igual que en el paso de python pip, que descarga de PYPI (Python Package Index).

Lo que ocurre es que muchas empresas (Como la vuestra), crean sus propios REGISTROS DE ARTEFACTOS (sitios desde donde se pueden descargar librerías). En ellos ponen tanto librerías propias, como librerías de terceros que han decidido usar en sus proyectos (Apache Spark)

Esto hace que los desarrolladores NO PUEDAN instalar lo que les de la gana... solamente herramientas (librerías) autorizadas por la empresa.

En vuestro caso usáis ARTIFACTORY como registro de artefactos. Es un registro de artefactos que permite gestionar dependencias, subir librerías, etc.

En ese ARTIFACTORY registran librerías... algunas de ellas públicas... y de vez en cuando van actualizando las versiones disponibles de esas librerías.

---
En vuestro caso, en lugar de usar SBT como herramienta de automatización de tareas para SCALA, usáis MAVEN.

Muchos tenéis configurado MAVEN ya en el ordenador... de otros proyectos.
La configuración de maven se crea en un archivo llamado settings.xml, que se encuentra en la carpeta .m2 de vuestro usuario.
En ese archivo vienen los registros de artefactos que usa MAVEN para descargar las librerías que necesita (entre ellos el vuestro del banco) Ahí también viene información de credenciales(usuario y contraseña) para acceder a esos registros de artefactos. Información de proxy, etc.

--- 
Como véis, INTELLIJ NO NOS DA LA OPCION de crear un proyecto SCALA con MAVEN. Solo con SBT.
Y aunque tengamos configurado maven para trabajar con vuestro artifactory, INTELLIJ no lo va a usar. Va a usar SBT.

OPCIONES:
1. Crear una configuración equivalente a la de maven en sbt:
   - Crear un fichero llamado repositories en la carpeta de mi usuario dentro de .sbt
   - Crear un fichero que podemos llamar realmente como queramos (.credentials) en la carpeta de mi usuario dentro de .sbt
   - En IntelliJ hemos dicho a IntelliJ que cuando llame a SBT, le pase ese fichero de credenciales que hemos configurado (eso lo hacíamos en los settings de sbt)
   Esto tiene pinta que está funcionando... PROBLEMA NUEVO: parece que la propia librería SBT (equivalente al modulo de python pip) no está disponible en vuestro registry (artifactory).
2. Pasar del SBT (que es la herramienta buena), y usar MAVEN (al final es lo que hacéis en el banco)
   El problema aquí es que MAVEN no sabe a priori lidiar con proyectos SCALA... solo con proyectos JAVA... Hay que configurar un huevo de cosas en MAVEN para que sepa que es un proyecto SCALA.

   Existe un arquetipo MAVEN (plantilla de proyecto) oficial para SCALA. Vamos a probarla.


---

c:\Usuarios\ VUESTRO USUARIO \ .m2 \settings.xml (USUARIO/CONTRASEÑA=TOKEN)

---

Tenemos 3 programas implementados con MapReduce en Scala.
En esos programas hemos metido paralelización de las operaciones... eso lo hemos hecho con la función `.par`, que hemos aplicado a las colecciones de Scala (Arrays y listas).

Ha ido bien.. ahora estamos haciendo uso de TODA LA CAPACIDAD DE COMPUTO de nuestro ordenador (CPU).

PREGUNTA: Y si aún así TARDASE MUCHO? Y si tengo 10 servidores con 10 CPUs cada uno que están parados (aburridos.. sin hacer nada), no podría usar también sus 100 cores para trabajar en mi problema?

Eso es lo que me da Spark. La capacidad de ejecutar código MAP REDUCE en un clúster de servidores, usando TODA LA CAPACIDAD DE COMPUTO de esos servidores.... y no solo la de mi ordenador.

Para usar solo la capacidad de computo de mi ordenador, he usado las colecciones paralelizables de Scala.
Para usar la capacidad de computo de un clúster de servidores, voy a usar Spark.

Dicho de otra forma... Spark es un equivalente a las colecciones paralelizables de Scala, pero para un clúster de servidores.

---

Para pasar de un programa MAP/REDUCE de puro SCALA a un programa MAP/REDUCE de Spark, hay que hacer muy pocos cambios en el código.
1º Abrir una conexión contra una granja de servidores (un clúster de Spark)
2º Mandarle a Spark los datos que quiero procesar para que los paralelice.
3º Configurar el algoritmo Map/REDUCE (Eso es lo mismo que estaba hecho... no hay cambios.. o son mínimos)
4º Cerra la conexión contra el clúster de Spark.

NOTA: "o son mínimos"

Spark tiene su propio tipo de colección de datos: RDD
El API (las funciones que tenemos disponibles) en un RDD son prácticamente iguales a las que tenemos en una colección paralelizable de Scala.
A veces hay alguna función que cambia de nombre.. es poco habitual... pero pasa

NO SON CAMBIOS EN EL ALGOTIRMO.. son cambios en la NOMENCLATURA de las funciones MAP/REDUCE que usamos. SON POCAS.


JAVA 9 implicó el mayor cambio en JAVA desde que se creo JAVA. 
En esa versión de JAVA aparece lo que se llama el proyecto jigsaw. Básicamente una modularización dela máquina virtual de java.

La JVM tiene dentro un montón de componentes. MUCHISIMOS.
El abrir una JVM implica cargar un montón de clases/modulos que en muchos casos no se usan... pero que ocupaban un huevo en memoria... y hacía que todo fuese más lento.

Desde la versión 1.9 esos componentes se paquetizan (se meten en paquetes) que se llaman módulos.
Y por defecto no todos los módulos se cargan al abrir una JVM.

Spark usa módulos de los que por defecto NO SE CARGAN en las últimas versiones de la JVM.
Necesitamos EXPLICITAMENTE pedir a la JVM que cargue esos módulos.

--add-opens=java.base/java.lang=ALL-UNNAMED --add-opens=java.base/java.lang.invoke=ALL-UNNAMED --add-opens=java.base/java.lang.reflect=ALL-UNNAMED --add-opens=java.base/java.io=ALL-UNNAMED --add-opens=java.base/java.net=ALL-UNNAMED --add-opens=java.base/java.nio=ALL-UNNAMED --add-opens=java.base/java.util=ALL-UNNAMED --add-opens=java.base/java.util.concurrent=ALL-UNNAMED --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED --add-opens=java.base/sun.nio.ch=ALL-UNNAMED --add-opens=java.base/sun.nio.cs=ALL-UNNAMED --add-opens=java.base/sun.security.action=ALL-UNNAMED --add-opens=java.base/sun.util.calendar=ALL-UNNAMED --add-opens=java.security.jgss/sun.security.krb5=ALL-UNNAMED 

---

Cuando arrancamos un cluster local, Ese cluster dispone en paralelo de una App WEB que nos permite hacer DEBUGGING de los trabajaos que estamos solicitando.
Esa app se activa al arrancar el cluster... pero se desactiva al parar el cluster... en nuestro caso, al atrabajar con un cluster local, al acabar de ejecutarse nuestro programa (al cerrar la conexión con el cluster) esa app se para. A veces nos interesa dejarla corriendo (ejecutándose) para ver que trabajos se han ejecutado, y cómo se han realizado.

Poder hacer un truco rastrero... pero sencillo. Antes de cerrar el cluster (la conexión) metemos un sleep al programa... de 1 hora ... o de 2


---


Mi programa le manda los datos al cluster... por red.
Por una red lo que circulan son BYTES.
Pero mi programa maneja datos de tipo String, Int, etc.

Esos Ints o esos Strings, se tienen que serializar (convertir a bytes) para poder enviarlos por red.
En Spark los datos se deserializan (se convierten de bytes a su tipo original) al llegar al cluster.
Spark hace luego sus cálculos, cuando ya tiene los datos en memoria (Se han deserializado). Y genera un resultado... que debe ser enviado al cliente (a mi programa)... por RED de nuevo...
Y para ello, los datos que haya calculado SPARK, Spark debe serializarlos (convertirlos a bytes) para enviarlos por red.
Y cuando lleguen a mi programa, mi programa los deserializa (los convierte de bytes a su tipo original).
