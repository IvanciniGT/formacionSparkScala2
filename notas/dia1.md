
# Spark desde SCALA

## Spark?

Framework escrito en el lenguaje Scala (aunque ofrece apis de comunicación con otros lenguajes como Python, Java, R, etc.)
que nos ofrece un modelo de programación MAP/REDUCE en una infraestructura Bigdata.

## Bigdata

Nos referimos a las estrategias que usamos cuando tenemos tal:
- Volumen de datos
- Complejidad de datos
- Velocidad de generación de datos
que las técnicas que hemos usado tradicionalmente dejan de ser efectivas.

Tiene que ver con una infraestructura (HARDWARE!).

Cuando trabajo con datos, con independencia de lo que quiera hacer (almacenar, analizar, transmitir...) puedo optar por un enfoque clásico o un enfoque Bigdata.

Bigdata va de tener un montón de maquinas de mierda (commodity hardware) que son muy baratas pero que pongo no una.. sino una granja entera a trabajar como si fueran 1.

Una de las grandes gracias del mundo Bigdata es la capacidad de escalar horizontalmente.

Puedo tener un cluster (grupo/granja) de 200 máquinas... y a la hora quedarme con 10... y a la media hora, 500.. y al rato: 3.
Muchas veces, esas máquinas las alquilo a un cloud (público o privado).

Para hacer eso, tengo que poder:
- Hacer uso de la CPU de las máquinas y repartir entre ellas la carga de trabajo (Como si fuera un ordenador gigante)
- Hacer uso de la RAM de las máquinas y repartir entre ellas los datos (Como si fuera un ordenador gigante)
- Hacer uso de su capacidad de almacenamiento y repartir entre ellas los datos (Como si tuviera un HDD gigante)

Qué programa de una computadora se encarga de la gestión/ asignación de tareas en la CPU? Sistema operativo.
Qué programa de una computadora se encarga de la gestión de la RAM? Sistema operativo.
Qué programa de una computadora se encarga de la gestión del almacenamiento? Sistema operativo.

Y en el mundo bigdata, necesitamos algo así como un sistema operativo que se encargue de gestionar la CPU, la RAM y el almacenamiento de todas las máquinas del cluster. Quién es ese? Apache Hadoop.

Hadoop es el equivalente a un sistema operativo en un entorno BigData. Ofrece básicamente 2 cosas:
- Un sistema de ficheros distribuido (HDFS) alternativo a FAT/NTFS
- Soporte para el modelo de programación MAP/REDUCE

El problema es que el modelo de programación Map/Reduce que implementa Hadoop es muy poco eficiente (se apoya en HDD).
Para tener un buen rendimiento hubo que hacer una implementación alternativa del modelo de programación Map/Reduce que se apoyara en RAM y no en HDD. Esa implementación es Spark.

Spark es una reimplementación del modelo de programación Map/Reduce de Apache Hadoop que se apoya en RAM y no en HDD.

Todo el mundo del bigdata lo originó Google que fueron los primeros que se encontraron con el problema de tener que procesar grandes volúmenes de datos: (Producto BigTable)
- Crearon un sistema de ficheros distribuido (GFS)
- Crearon un modelo de programación Map/Reduce (Map/Reduce)
Crearon papers sobre ambos temas... Públicos, con sus conclusiones.

Basado en esos papers, la comunidad (Un par de hombrecillos) crearon Hadoop, que es una implementación de GFS y Map/Reduce.

El modelo de programación MAP/REDUCE se basa en programación FUNCIONAL (El paradigma de programación funcional).

### Lista de la compra

- Excel         30.000 cosas.. si meto más.. al Excel le empieza a doler!
- MySQL       10.000.000 de cosas.. si meto más.. al MySQL le empieza a doler!
- MS SQL SERVER  100.000.000 de cosas.. si meto más.. al MS SQL Server le empieza a doler!
- Oracle DB   1.000.000.000 de cosas.. si meto más.. al Oracle DB le empieza a doler!

---

## SCALA

Lenguaje de programación.

### Clasificación de los lenguajes de programación / Características

#### Lenguajes compilados vs Lenguajes interpretados

Los computadores hablan JAVA? NO... y C? tampoco... Python? Ni flores de python.
Que habla una computadora? El lenguaje que ofrece su Sistema Operativo.... ese lenguaje a su vez, está influenciado por la arquitectura del procesador.

Pero nosotros no hablamos ese lenguaje. Cuando escribimos un programa, para ejecutarlo en una computadora (para que lo lea su sistema operativo), tenemos que traducirlo a ese lenguaje. Y tenemos 2 estrategias... igual que con los lenguajes humanos:
- Pre traducirlo (compilación)
- Interpretación (traducción en tiempo de ejecución). Esa interpretación la ejecuta / realiza un interprete.

> Lenguajes compilados: C, C++, JAVA
> Lenguajes interpretados? Python, Javascript, JAVA

Java es muy raro... UNICO... ya que es compilado e interpretado.

   .java -> Compilación -> .class ---> Son interpretados por la JVM (Java Virtual Machine)  ---> Código que es ejecutable por un SO.
                          BYTE-CODE             que es un interprete de byte-code.
                           ^^^^^^^
                Es otro lenguaje de programación.

Java como lenguaje de programación es una castaña! Tiene un montón de cagadas en su gramática!
Pero el interprete de byte-code (JVM) es una maravilla! Es un interprete que se puede portar a cualquier sistema operativo y arquitectura de procesador.

Lo que ha ocurrido a lo largo de los años es que han surgido lenguajes alternativos a JAVA que también se compilan a byte-code y que son interpretados por la JVM. Por ejemplo:
- Kotlin
- Scala

   .scala -> Compilación -> .class ---> Son interpretados por la JVM (Java Virtual Machine) ---> Código que es ejecutable por un SO.
                          BYTE-CODE             que es un interprete de byte-code.
                           ^^^^^^^
                Es otro lenguaje de programación.

Muchas características de scala y de java son iguales... ya que son ofrecidas por la JVM.

### Tipado estático (fuerte) vs Tipado dinámico (debil)

> Tipado dinámico: Python, JS 
> Tipado estático: C, Scala, Java

Pregunta: En python hay tipos de datos? SI claro... en todo lenguaje de programación hay tipos de datos.
Con cualquier programa, siempre, lo que hacemos es manipular datos. Y esos datos tiene distintas naturalezas (números, cadenas de texto, booleanos, listas, diccionarios...).

Tipo de dato? 
- 3 (Java: int , Python: int)
- "Hola" (Java: String, Python: str)

Lo que pasa es que hay lenguajes en los que las variables también tienen tipos de datos. Y hay lenguajes en los que las variables no tienen tipos de datos.

```java
String texto = "hola";
```
```python
texto = "hola"
```
```scala
val texto: String = "hola"
```

Qué es lo primero que hace la computadora con esas lineas de código?
- "hola"         Se crea un objeto (dato) en memoria RAM, de tipo String con valor "hola"
- String texto   Creamos una variable con nombre: "texto"
- =              Operador asignación. Asignamos la variable al objeto "hola" que está en memoria RAM.
                 Lo que hago es pegar el postit al lado del objeto "hola" que está en memoria RAM.

```py java scala
texto = "adios"
```

- "adios"       Se crea un objeto (dato) en memoria RAM, de tipo String con valor "adios"
                Dónde se crea ese dato? En el mismo sitio que el dato "hola" o en otro sitio?
                En un sitio distinto. Y llegados a este punto, en RAM hay 2 Strings: "hola" y "adios"
- texto =       Reasignar la variable texto al nuevo objeto "adios" que está en memoria RAM.
                Muevo / despego el postit de "hola" y lo pego al lado del objeto "adios".
                NOTA: El dato "hola" queda huérfano de variable (no hay variable que apunte a él).
                Y se convierte en BASURA (GARBAGE).. y será eliminado (o no.. npi) por el recolector de basura (Garbage Collector), que es un proceso que corre en segundo plano de la JVM, del interprete de python.

En los lenguajes de tipado estático, las variables también tienen tipos de datos. Cuando defino una variable HE DE DECIR qué tipo de dato al que puede apuntar.
En java, String texto era como tomar un postit del taco de postit AZUL (lo que permiten apuntar Strings) y escribir "texto" en él. También hay postits VERDES (lo que permiten apuntar a enteros), AMARILLOS (lo que permiten apuntar a booleanos), etc.
En python, todos los postits son del mismo color (no hay colores) Las variables NO TIENEN TIPO DE DATO.

Luego hay otro concepto:

```scala
val texto: String = "hola"
val otroTexto     = "adios"
```

Esas lineas de scala son iguales? Mejor dicho...
- La variable texto es de tipo: String
- Y la variable otroTexto?      También es de tipo String.
  Lo que pasa es que en ese caso, no lo indicamos explícitamente. Dejamos que el compilador lo INFIERA del tipo del primer dato al que asignamos la variable.

En python no hay inferencia de tipos en las variables... Es otro concepto. En PYTHON LAS VARIABLES NO TIENEN TIPO.

En Java, Python, Scala, JS, una variable NO es un espacio de memoria que se reserva para almacenar un dato.
En estos lenguajes una variable es una referencia a un dato que está en memoria.

#### En base al/a los paradigmas de programación que soportan

> Paradigma de programación??? EIN!?!?!?!

Es una forma de usar el lenguaje... En Español también las hay:
- Felipe, pon una silla debajo de la ventana                          Imperativo
- Felipe, me gustaría, quiero una silla debajo de la ventana          Desiderativo
- Felipe, debajo de la ventana tiene que haber una silla. 
  Es tu responsabilidad                                               Declarativo

En el mundo de la programación:
- Imperativo:           Damos una secuencia de instrucciones al computador para que las ejecute de forma
                        secuencial. A veces necesitamos romper esas secuencialidad (IF, FOR, WHILE...)
- Procedural:           En ocasiones agrupamos esas secuencia de instrucciones y les ponemos un nombre.
                        Cuando el lenguaje me permite hacer eso, y posteriormente ejecutar mediante ese nombre esa secuencia de instrucciones, hablamos de un lenguaje Procedural.
                        Esos grupos, dependiendo del lenguaje los llamamos funciones, procedimientos, métodos, subrutinas...
                        Para qué hacemos esto? Qué aporta?
                        - Reutilización de código
                        - Mejorar la mantenibilidad del código / estructura del código
- Funcional             Es cuando el lenguaje me permite que una variable apunte a una función.
                        Y posteriormente poder ejecutar esa función desde la variable.
                        Una cosa es lo que es la programación funcional.
                        Otra cosa es lo que puedo hacer si el lenguaje me permite programar de forma funcional:
                        - Crear funciones que reciban otras funciones como parámetros
                        - Crear funciones que devuelvan funciones como resultado
                        Y aquí es cuando todo se complica.
                        Al tener soporte de programación funcional, los motivos para crear funciones cambian:
                        - Reutilización de código
                        - Mejorar la mantenibilidad del código / estructura del código
                        - Embeber lógica para suministrarla a otras funciones

                        En ocasiones... solo en ocasiones, de esos 3 motivos, solo quiero 1, el último.
                        Hay veces que necesito pasar lógica a otra función, pero el hecho de definir esa lógica en una función independiente con sintaxis tradicional, me complica la mantenibilidad del código. Y además, esa lógica no voy a querer reutilizarla en ningún otro sitio. 
                        En estos escenarios, los lenguajes que soportan programación funcional nos ofrecen una sintaxis alternativa para definir funciones. Las expresiones LAMBDA.
- Orientación la objetos    Todo lenguaje de programación me permite manejar datos. Esos datos, tiene su naturaleza (Tipos de datos). Todo lenguaje viene con una serie de tipos de datos predefinidos:

                  Tienen algo que los caracteriza/que los identifica       que puedo hacer con ello
      String     una secuencia de caracteres                               - a Mayúsculas
                                                                           - a Minúsculas
                                                                           - Empiezas por XXX?
                                                                           - Cuá es tu Longitud

      Date       dia, mes, año                                             - caes en Jueves?
                                                                           - Eres un año bisiesto?
                                                                           - Suma 4 dias

      List       una colección de datos                                    - Añade un elemento
                                                                           - Elimina un elemento
                                                                           - Ordena
                                                                           - Filtra          

      Persona    nombre, dni, email...                                     - Eres mayor de edad?
                                                                           - Tu email es válido?
                                                                           - Si un DNI es válido?

Hay lenguajes de programación que me permiten definir mis propios tipos de datos: CLASES.
Y posteriormente crearme datos de esos tipos de datos (OBJETOS).

Los lenguajes que me permiten definir mis propios tipos de datos, y posteriormente crearme datos de esos tipos de datos, son lenguajes orientados a objetos.

### Qué es JAVA?

- Lenguaje de programación con sus características (Orientado a objetos... tipado estático... interpretado... etc.)
- JVM (Java Virtual Machine) que es un interprete de byte-code. <--- Comunmente también nos referimos a ésto como JAVA
  

## Código en scala

```scala
# Definir variables
var nombreVariable: TIPO = valor              El tipo lo podemos omitir y el compilador lo infiere.
# Definir constantes
val nombreConstante: TIPO = valor


object HolaMundo {
  def main(args: Array[String]): Unit = {
    println("Hola Mundo")
  }
}
```

# Que es eso de object?

En cualquier lenguaje de programación Orientado a Objetos, a los tipos de datos que nosotros definimos les llamamos: CLASES
A los datos que creamos de esos tipos de datos les llamamos: OBJETOS

> Qué es la palabra Object en Scala? Una sintaxis reducida de un patrón singleton.

Scala es un lenguaje complejo... con una curva de aprendizaje alta. Es complicado aprender Scala.
Esos si, una vez que lo conocemos es genial... Escribiendo muy poco código podemos hacer muchas cosas.


> Qué es un patrón de programación SINGLETON?

Una clase de la que solo se puede crear un único objeto.


```java
public class MiSingleton {
    private static volatile MiSingleton instancia;
    // Evita el uso de la cache de los cores de la CPU, para que siempre se acceda a la variable instancia desde la memoria principal.
    private MiSingleton() {
        // Constructor privado para evitar instanciación externa
    }

    public static MiSingleton getInstance() {
        if (instancia == null) {                     // Evita que se ejecute el synchronized que es caro computacionalmente.
            synchronized (MiSingleton.class) {       // Evitar un race condicion: Condición de carrera.
            // Esto es un patrón SEMAFORO (Mutex) que me asegura que solo un hilo puede ejecutar el código de dentro de este bloque a la vez.
            // Podría tener 2 hilos en paralelo ejecutando el código de dentro de este bloque.
                if (instancia == null) {             // Me asegura que si la variable ya tiene un valor, no la vuelvo a instanciar.
                    instancia = new MiSingleton();   // Es el que me asegura que solo se va a a crear una única instancia de la clase.
                }
            }
        }
        return instancia;
    }
}

// Desde otro sitio del código, cuando quisiera acceder a la instancia de MiSingleton, lo haría así:
MiSingleton.getInstance();
```

Todo ese código de arriba, en Scala se reduce a:

```scala
object MiSingleton { }

//Y cuando lo quiera usar: 
MiSingleton
```

El problema de scala es que obviamos mucho código, que se genera de forma automática, y que no vemos.
PERO QUE ESTA AHI!
- Cuando escribo JAVA, el código lo veo: TODO... y entonces las cosas son claras.
- Cuando escribo SCALA, el código no lo veo TODO... y entonces las cosas son menos claras... pero si yo las tengo claras... joder... es que escribo 3 palabras.

Es bastante complejo aprender SCALA sin unos buenos conocimientos de JAVA.

Y Java y python... son muy diferentes. Python es un lenguaje de JUGUETE (me refiero a que su gramática es MUY POBRE comparada con JAVA)! 
Que está guay!


La primera cagada que tenemos en python ( o no.. FEATURE!) es que es un lenguaje de tipado DINAMICO!
Y Eso lo INVALIDA como lenguaje para proyectos grandes!

```python
def generar_informe(titulo, datos):
   pass # Lo que sea!!!! 500 lineas de código
``` 

```java
public class Informe { 
   // con sus mierdas
}
public class Datos { 
   // con sus mierdas
}

public class GeneradorInforme {
   public Informe generarInforme(String titulo, List<Datos> datos) {
      // Lo que sea!!!! 500 lineas de código
   }
}
```


```java
public class Informe {

   private final String titulo;
   private List<Datos> datos;

   public Informe(String titulo, List<Datos> datos) {
      this.titulo = titulo.toUpperCase(); // Por ejemplo, lo pongo en mayúsculas
      this.datos = datos;
   }

   public String getTitulo() {
      return titulo;
   }
   public List<Datos> getDatos() {
      return datos;
   }
   public void setDatos(List<Datos> datos) {
      this.datos = datos;
   }
   // Más funciones
   public void imprimir() {
      // Imprimir el informe // Con su código
   }
}
```
```python
class Informe:
   __titulo: str # Pero esto en python es documentación
   @property
   def titulo(self):
      return self.__titulo
   
   def __init__(self, titulo, datos):
      self.titulo = titulo.upper()
      self.datos = datos

   def imprimir(self):
      # Imprimir el informe // Con su código
      pass
```

La versión SCALA, tiene 2 cositas:
- Scala me ofrece la misma potencia / funcionalidad que JAVA
- Y tiene una sintaxis más reducida que la de python.

```scala
             /********** PARAMETROS DEL CONRTRUCTOR ****/
class Informe(titulo: String, var datos: List[Datos]) {


   def imprimir(): Unit = {                                          // Unit: Indica que la función No retorna nada
                                                                     // Realmente si retorna... Retorna Unit
                                                                     // Es como un void de JAVA
      // Imprimir el informe // Con su código
   }

   val tituloMayusculas: String = titulo.toUpperCase() // Por ejemplo, lo pongo en mayúsculas // Código del constructor
}
```

El código del constructor en scala, se reparte a lo largo de la calse.. todo lo que haya en el cuerpo de la clase es código del constructor.

Cuando en los parametros del constructor (lo que ponemos entre paréntesis al lado del nombre de la calse) ponemos delante de cada uno:
- NADA: Significa que es un parámetro del constructor que puedo usar dentro del constructor de la clase. Y ya.
- val: Significa que es un parámetro del constructor que se convierte en un atributo de la clase y puedo usarlo en cualquier método de la clase.
- var: Significa que es un parámetro del constructor que se convierte en un atributo de la clase y puedo usarlo en cualquier método de la clase. Y además, puedo modificar su valor desde cualquier método de la clase.


Cómo hablo con esa función? Qué le tengo que pasar?

## Map/Reduce

Map reduce es un MODELO DE PROGRAMACION, una forma de crear un programa, de plantear un algoritmo.
Está fuertemente basado en programación funcional. No podemos usar map/ reduce si no tenemos un lenguaje que soporte programación funcional.

Es una forma de plantear problema ideal si:
- Tenemos una colección de datos (además de tamaño considerable)
- Nos ofrece la posibilidad de paralelizar trabajos, y además, el número de particiones del trabajo que voy a hacer puede ser dinámico.

## En qué consiste un algoritmo planteado mediante Map/Reduce?

- Básicamente partimos de una colección de datos
- Sobre la que vamos haciendo Transformaciones (Map)
- Para al final obtener un resultado (Reduce)

En un flujo de trabajo (un programa) Map/Reduce, podemos aplicar:
- Tantas transformaciones como queramos (Map)
- Pero al final, solo una reducción (Reduce)
 
Pero qué es un MAP? Y qué es un REDUCE?

# Funciones tipo MAP

Una función tipo MAP es una función que se aplica sobre una colección de datos y que devuelve otra colección de datos que soporta más funciones MAP.

Cuales son: Hay decenas que funciones tipo map. Algunas de las más comunes son:
- `map`: Me permite aplicar una función de transformación a cada uno de los elementos de una colección para obtener una nueva colección con los resultados de esa transformación. EN RESUMEN, NOS PERMITE TRANSFORMAR DATOS!
- `filter`: Me permite aplicar una función de filtrado a cada uno de los elementos de una colección para obtener una nueva colección con los elementos para los que la función de filtrado devuelve true. EN RESUMEN, NOS PERMITE FILTRAR DATOS de una colección!
- Y muchas más!

Las funciones tipo map, son funciones que se ejecutan en modo LAZY (Perezoso). Eso significa que no se ejecutan hasta que no es necesario.

# Funciones tipo REDUCE

Una función tipo REDUCE es una función que se aplica sobre una colección de datos y que devuelve lo que sea que no sea una colección de datos que soporte más funciones MAP.

Cuales son? Hay decenas de funciones tipo reduce. Algunas de las más comunes son:
- Suma: Me permite sumar todos los elementos de una colección de datos numéricos y devolver un único número.
- Count: Me permite contar el número de elementos de una colección de datos y devolver un único número.
- Y muchas más!

Las funciones tipo reduce, son funciones que se ejecutan en modo EAGER (Ansioso). Eso significa que se ejecutan inmediatamente y devuelven un resultado.

 COLECCION QUE SOPORTE MAP
 -> map -> COLECCION QUE SOPORTE MAP
   -> map -> COLECCION QUE SOPORTE MAP
     -> map -> COLECCION QUE SOPORTE MAP
       -> reduce -> DATO QUE NO SOPORTE MAS MAP 
                    Y AQUI ACABA EL CUENTO!

---

Colección inicial:           Colección intermedia 1      Col. intermedia 2      Resultado final
   1      -> MAP            ->  2 -> Filter         2 ->    6      -> Sumar ->   6+8+10+12 = 36
   2              x2            4      (quita los           8        ESTA ES LA FUNCION
   3         Transformación     6      menores de 5)       10        DE REDUCCIÓN
   4                            8                          12 
   5                           10
   6                           12

```pseudocódigo
listado = [1, 2, 3, 4, 5, 6]
doblar = (n) => n * 2
mayorOIgualQue5 = (n) => n >= 5 (TRUE | FALSE)

dobles = listado.map(doblar) 
mayoresQue5 = dobles.filter(mayorOIgualQue5)
```


Esto es una forma de plantear el problema... pero no la única: 
- Código imperativo:
  
  ```python
     numeros = [1, 2, 3, 4, 5, 6]
     suma = 0
     for n in numeros:
         transformado = n * 2
         if transformado > 5:
               suma += transformado
      print(suma)  # Resultado: 36
  ```

  Podría hacerlo con código imperativo... también lo puedo plantear de esa otra forma que comentaba antes: MAP/REDUCE

El problema del código imperativo es que no puedo paralelizarlo, al menos fácilmente.
Me toca reescribirlo entero de otra forma para poder paralelizarlo... de una forma NADA EVIDENTE!
Necesito empezar a trabajar con HILOS (THREADS), sincronizarlos... follón!!!!


Los maps de nuestro ejemplo son paralelizables ( no todo map lo es!)... el reduce ? En realidad también op parte de él.
Si lo pensáis, haciendo uso de la propiedad ASOCIATIVA de la suma:

6+8+10+12 = (6+8) + (10+12) = 14 + 22 = 36
             ^^^^    ^^^^^
             Al menos eso lo puedo paralelizar!




---

# Modos de ejecución LAZY e EAGER

```pseudocódigo
listado = [1, 2, 3, 4, 5, 6]                             # Declaro una variable que referencia a una lista de números.
doblar = (n) => n * 2                                    # Definir una función que dado un número, devuelve el doble de ese número.
mayorOIgualQue5 = (n) => n >= 5                          # Definir una función que dado un número, me indica si es mayor que 4.

dobles = listado.map(doblar)                             # Transformo cada elemento de la lista usando la función doblar.
mayoresQue5 = dobles.filter(mayorOIgualQue5)             # Filtro de la lista de los dobles, los que son mayores o iguales que 5.
suma = mayoresQue5.suma()                                # Sumo todos los elementos anteriores.
```

La realidad es que eso no es lo que hacemos. Y habitualmente es la forma en la que lo expresamos.. y está bien expresarnos de esa forma... Pero lo que no está bien es NO ENTENDER lo que realmente está sucediendo.

Una forma muycho más adecuada de haber expresado lo que realmente está sucediendo es:

```pseudocódigo
listado = [1, 2, 3, 4, 5, 6]                             # Declaro una variable que referencia a una lista de números.
doblar = (n) => n * 2                                    # Definir una función que dado un número, devuelve el doble de ese número.
mayorOIgualQue5 = (n) => n >= 5                          # Definir una función que dado un número, me indica si es mayor que 4.

dobles = listado.map(doblar)                             # Anoto que hay que Transformar cada elemento de la lista con la función doblar.
mayoresQue5 = dobles.filter(mayorOIgualQue5)             # Anoto que hay que Filtrar de la lista, los que son mayores o iguales que 5.
suma = mayoresQue5.suma()                                # Sumo todos los elementos anteriores.
```


La variable `mayoresQue5`no es una lista que contiene los números 6, 8, 10 y 12
La variable `dobles` no es una lista que contiene los números 2, 4, 6, 8, 10 y 12

La variable `dobles`es la lista original (1,2,3,4,5,6) con un positit pegao que dice: "Por favor, multiplicar por 2 antes de hacer nada con ello"

como podéis imaginar, la variable `mayoresQue5` es la lista original (1,2,3,4,5,6) con un positit pegao que dice: "Por favor, multiplicar por 2 antes de hacer nada con ello, y luego otro positit que dice: "Por favor, filtrar los números que son mayores o iguales que 5 antes de hacer nada con ello"


Imaginar que tengo una lista de 1000 millones de números... y que sobre cada uno voy a hacer una transformación que tarda 1 segundo... por ejemplo doblarlo (x2).
Según esos datos que acabo de dar, cuanto tardaría en ejecutarse el código siguiente?

```pseudocódigo
dobles = listado.map(doblar)                             # Anoto que hay que Transformar cada elemento de la lista con la función doblar.
```

- ~~1000 millones de segundos.~~Parecería que si... pero resulta que no.
- Lo que se tarde en pegar un postit en la portada del cuaderno que contiene la lista de números, diciendo: "Por favor, multiplicar por 2 antes de hacer nada con ello".
Da igual el número de datos que haya en la lista al aplicar un map. El map solo anota en la colección que en el futuro se va a aplicar una transformación concreta a cada uno de los elementos de la colección.

Pero hoy no.... MAÑANA !

Lo mismo aplica a la función FILTER.... Son funciones (TODAS LAS MAP) que se ejecutan en modo perezoso (LAZY).

Cuando se ejecutan realmente? Cuando es necesario... Y básicamente el disparador es la función REDUCE... es la que aprieta el gatillo, la que pone la pelota en juego, la que empuja la primera ficha de dominó!

Lo más similar es realmente lo de la ficha de dominó!

Apunto que hay que multiplicar por 2 cada uno de los elementos de la lista.
Apunto que hay que filtrar los números que son mayores o iguales que 5.
Quiero SUMAR !!!!
  Pero claro... para sumar necesito los números concretos que son mayores o iguales que 5.
     Pues a calcularlos (a aplicar el filtro)...
       Pero claro... para aplicar el filtro necesito los números concretos que son el doble de cada uno de los números de la lista.
          Pues a calcularlos (a aplicar el map)...

          Una vez calculados los dobles, podemos aplicar el filtro y al final sumar.

Y ESTO ES TODA LA GRACIA DE MAP/REDUCE... Sin esta característica, MAP REDUCE NO PODRIA EXISTIR!
La idea, con Spark, cuando vayamos a Spark, es poder decirle:
- Ahi tienes una colección de datos... chorreón de datos!!!!
- Te paso una FUNCION (doble), que quiero que uses para transformar cada uno de los datos de la colección.
- Te paso otra función (mayorOIgualQue5), que quiero que uses para filtrar los datos de la colección.
- Pero no quiero que vayas haciendo eso todavía... SOLO ANOTA !
- Y  ahora, ponte en marcha para calcular la suma de los datos... con esas consideraciones


            Cluster de Apache Spark
    GRANJA DE COMPUTADORES DE MIERDA (COMMODITY HARDWARE)                Mi ordenador de mierda!!

       MM1    <<<<<<
        S1 = Suma de los datos (1...300)
       MM2    <<<<<<    Coordinador del cluster                <<<<<<<    Programa, que le pide al coordinador del cluster
        S2 = Suma de los datos (300...600)                   
                          (que es otra maquina de mierda!)                   Quiero que anotes que hay que multiplicar por 2 
       MM3    <<<<<<               S1+S2+S3                                  Quiero que anotes que hay que filtrar
                                                                             Dame el resultado de la suma
         S3 = Suma de los datos (600...1000)

---

El ejemplo anterior era una chorrada sin sentido... x2 -> >=5 -> Sumar (que es esta mierda!!!)

Un ejemplo con cierto sentido:
- La tabla de TRENDING TOPICS (de hashtags) de Twitter(X)

Son datos? Los tweets? SI
Muchos? SI
Los quiero transformar en otra cosa? SI... en el trending topic.
Pues un modelo de programación Map/Reduce es ideal para eso.


COLECCION DE PARTIDA:
- En la playa con un mis amigos #SummerLove#OnFire#BestFriends#GoodVibes
- Haciendo examenes de mierda,con mis amigos de mierda #CacaFriends#SummerHate,que asco!!!!
- De parranda con mis amigos #SummerLove#Parranda100%#BestFriends#GoodVibes

   vvvvvvvvv

#SummerHate

0. Reemplazar cada "#" por " #": MAP(TRANSFORMACION)
   
   > Haciendo examenes de mierda,con mis amigos de mierda  #CacaFriends #SummerHate,que asco!!!!  

1. Separar cada tweet en palabras... en base a carácter/caracteres separo? ESPACIOS,, .;,-()[]!¿?: MAP(TRANSFORMACION)

   > Haciendo     examenes     de      mierda     con     mis     amigos     de     mierda     #CacaFriends    #SummerHate   que    asco

2. Filtrar las palabras que empiezan por "#": quedarme con los hashtags: FILTER

   > #SummerLove #OnFire #BestFriends #GoodVibes
   > #CacaFriends #SummerHate
   > #SummerLove #Parranda100% #BestFriends #GoodVibes

3. Quitar los #                                                          FILTER

   > SummerLove OnFire BestFriends GoodVibes
   > CacaFriends SummerHate
   > SummerLove Parranda100% BestFriends GoodVibes

4. Filtro los que tengan palabras prohibidas (Caca, culo, pedo, pis)     FILTER
   
   > SummerLove OnFire BestFriends GoodVibes
   > SummerHate
   > SummerLove Parranda100% BestFriends GoodVibes

5. Aplano todos esos datos en una única lista (colección de datos)       FLATTEN

   > SummerLove
   > OnFire
   > BestFriends
   > GoodVibes
   > SummerHate
   > SummerLove
   > Parranda100%
   > BestFriends
   > GoodVibes
 
6. Le pongo al lado de cada uno, un UNO: (1)                              MAP

   > SummerLove: 1
   > OnFire: 1
   > BestFriends: 1
   > GoodVibes: 1
   > SummerHate: 1
   > SummerLove: 1
   > Parranda100%: 1
   > BestFriends: 1
   > GoodVibes: 1

7. Agrupo por el nombre del hashtag y sumo los unos:                       REDUCE BY KEY
   > SummerLove: 2
   > OnFire: 1
   > BestFriends: 2  
   > GoodVibes: 2
   > SummerHate: 1
   > Parranda100%: 1

8. Ordeno por el número de veces que aparece el hashtag (de mayor a menor)   SORT

   > SummerLove: 2
   > BestFriends: 2
   > GoodVibes: 2
   > OnFire: 1
   > SummerHate: 1
   > Parranda100%: 1

9. Quiero los 5 primeros (los 5 más populares) <<<<<<< ESTA ES LA FUNCIÓN DE REDUCCIÓN !      TAKE(5)

   > SummerLove:     2
   > BestFriends:    2
   > GoodVibes:      2
   > OnFire:         1
   > SummerHate:     1


   vvvvvvvvv

 DATOS FINALES: Tabla de 5 TRENDING TOPICS
 - SummerLove:       2
 - BestFriends:      2
 - GoodVibes:        2
 - OnFire:           1
 - ~~CacaFriends: 1~~ Tiene una palabra prohibida (Caca, culo, pedo, pis)
 - SummerHate:       1

El procedimiento para llegar de un sitio a otro es lo que entendemos por nuestro ALGORITMO MAP/REDUCE.
Plantear un programa de esta forma es complejo. Sobre todo cuando no estamos habituados a hacerlo.
Lo primero que necesitaremos es CONOCER el abanico de operaciones MAP/REDUCE que puedo usar... y hay decenas!

Y luego ya... cambiar un poco nuestra forma de pensar para plantear los problemas de esta forma.

La gracia es que un programa planteado de esta forma, se puede paralelizar fácilmente. ESO ES LO QUE ME ASEGURA el modelo de programación Map/Reduce.

OPCION 1: Eso lo puedo aplicar dentro de mi ordenador... si tengo una CPU con 8 cores... puedo querer hacer uso de los 8 cores... y repartir el trabajo entre ellos... en paralelo!

OPCION 2: O lo puedo aplicar contra una granja de computadoras (cluster) y si tengo 8 máquinas con 8 cores cada una, puedo repartir el trabajo entre las 8 máquinas y hacer uso de los 64 cores que tengo en total.

Para la opción 1, me vale con cualquier lenguaje de programación de los que usamos hoy en día: Python, Java, Scala...
Para la opción 2, necesitamos SPARK!... es quien se encarga de gestionar/repartir trabajo entre las computadoras de la granja (cluster) y de ejecutar el código que le pasamos.

Mañana vamos a montar este ejemplo... y otro más (al menos) en SCALA!... sin SPARK! <--- AQUI APRENDEREMOS EL MODELO DE PROGRAMACIÓN MAP/REDUCE.
Una vez  lo tengamos lo pasaremos a Spark                                           <--- AQUI APRENDEREMOS A USAR SPARK PARA REPARTIR CARGAS DE TRABAJO.


Pasar de la opción 1 a la opción 2 solo va a necesitar cambiar 2 líneas de código de nuestro programa.... siempre las mismas 2 líneas de código... da igual el programa que hagamos.

El meollo no va a estar en la parte de SPARK... sino en SCALA y en MAP/REDUCE.
