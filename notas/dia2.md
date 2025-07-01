
# Map/Reduce

Modelo de programación que:
- Permite paralizar trabajos
- Un proceso map/reduce tiene 2 partes:
  - A una colección de datos le vamos aplicando maps, para ir transformando los datos
  - Luego aplicamos un reduce, que es la que proporciona el resultado final


# Funciones de tipo reduce

Hay muchas también, como por ejemplo: sum, count, max, min, take, ...
Son funciones que se ejecutan de modo ansioso (eager), es decir, se ejecutan inmediatamente y devuelven un resultado. Esa ejecución es la que fuerza a que se ejecuten las funciones tipo map que se hayan definido previamente.

# Funciones tipo map

Hay muchas, como por ejemplo: filter, map, order, group
Son funciones que se ejecutan en modo perezoso (lazy), es decir, no se ejecutan hasta que su resultado se necesita.

Hay otra cosa importante con esto de los maps... Se consumen...que significa esto?

Colección 1 -> map1 -> Colección 2 -> map2 -> Colección 3 -> map3 -> Colección 4 -> reduce

Si después de aplicar el reduce, intento ver los datos de la Colección 2, no podré ver nada, porque ya se ha consumido. Si necesito esos datos, he de volver a generarlos.

Y por qué pasa esto?

Nosotros este esquema visual le entendemos bien:

> Colección 1 -> map1 -> Colección 2 -> map2 -> Colección 3 -> map3 -> Colección 4 -> reduce

Y le usamos para ayudarnos a definir los procesos... pero... la realidad es muy diferente. La realidad de lo que ocurre cuando se ejecuta un proceso map/reduce.

## Ejemplo REAL de cómo se procesa un MapReduce

```java
[1,2,3,4,5,6]
    .map(doble)
    .filter(mayorOIgualQue5)
    .suma()                             // 6+8+10+12= 36
```

A la hora de interpretar esto, pensamos en:

1. Coge la colección                                                                    [1,2,3,4,5,6]
2. Aplica la función doble a cada uno de los datos de la colección, obteniendo              v
                                                                                        [2,4,6,8,10,12]
3. Aplica la función mayorOIgualQue5 a cada uno de los datos de la colección, obteniendo    v
                                                                                        [6,8,10,12]
                                                                                            v
4. Suma los datos de la colección, obteniendo                                              36

Imaginad que escribimos ese algoritmo de forma imperativa, como si lo hiciéramos en Python:

```python
datos = [1,2,3,4,5,6]
suma = 0
for d in datos:
    temporal = d * 2
    if temporal >= 5:
        suma += temporal
print(suma)  # 36
```

Miremos la variante map/reduce:

```python

def doble(d):
    return d * 2

def mayorOIgualQue5(d):
    return d >= 5

dobles = []
for d in datos:
    dobles.append(doble(d))

filtrados = []
for d in dobles:
    if mayorOIgualQue5(d):
        filtrados.append(d)

suma = 0
for d in filtrados:
    suma += d   

print(suma)  # 36
```

Esto de arriba al final NO ES NI PARECIDO A LO QUE SE EJECUTA.
El motor de procesamiento MAP REDUCE decide, al final del proceso (cuando se ejecuta el reduce), cómo va a ejecutar cada uno de los pasos map... si es que los ejecuta!

El código que habíamos planteado nosotros, al final daría lugar a un algormitmo mucho más optimizado... a saber:

```python
def doble(d):
    return d * 2

def mayorOIgualQue5(d):
    return d >= 5

datos = [1,2,3,4,5,6]
suma = 0
for d in datos:
    temporal = doble(d)
    if mayorOIgualQue5(temporal):
        suma += temporal
print(suma)  # 36
```

---


```java
[1,2,3,4,5,6]
    .map(doble)
    .filter(mayorOIgualQue5)
    .suma()                             // 6+8+10+12= 36
```

Qué hace la función map? Cuál es su responsabilidad? Su trabajo?
- Aplica la función que hemos pasado sobre cada elemento de la colección dejando el resultado de esa función para seguir trabajando en el proceso. Me permite ir transformando los elementos de una colección.
  Que transformación aplica? Las que le pasamos como argumento.
  Es decir, como argumento le estamos pasando PARTE DE LA LOGICA que debe ejecutarse en el proceso.

Y eso es lo que nos permitía la programación funcional, que es la que nos permite definir funciones que se pueden pasar como argumentos a otras funciones, inyectando así parte de la lógica del proceso.

---

# Spark.

Es un motor alternativo de procesamiento Map/Reduce sobre Hadoop.

Qué tipo de procesos queremos nosotros ejecutar ahí? Para qué usamos esto?
- ETLs: Procesos de Extracción, Transformación y Carga de datos.
     FUENTE ---> saco datos -> Transformo datos -> cargo ---> DESTINO
     Cuando de ETLs, hablamos realmente de un tipo de programa... hay muchas variantes:
     - ETL
     - TEL
     - TETL
     - TELT
     - ETLT
Y estos procesos, los queremos ejecutar una vez o muchas? Muchas.
Y ... entonces... lo que vamos a montar es un programita que estará instalado en?
- En mi máquina? NO
- En un cloud? Si ... o no.
Estará en un servidor que funcione 24x7

Imaginad que tenemos una ETL que debe ejecutarse cada hora.
Dónde se ejecutará el programa? En una granja de maquinas de mierda!
Pero en esa granja es donde se ejecuta la carga de trabajo.
Quién manda el programa a esa granja? Porque el programa hay que mandarlo a ejecución CADA HORA!


  granja de máquinas               SERVIDOR
       SPARK                Y ese programa se ejecuta cada hora
-------------------------   ------------------------------------
 WORKER 1 <
 WORKER 2 <   COORDINADOR < Programa que montemos en Scala
 ...                                ^^^^^
 WORKER N <                     Dónde está instalado?

                                    ^^^^^^
                                    
 Dónde desarrollo ese programa? En mi máquina!


Ayer, me preguntó un compañero: VAMOS A USAR JUPYTER? NO... vamos a usar un ENTORNO DE DESARROLLO: INTELLIJ (es el mejor, con diferencia, además es el que se usa en el banco!)

Para qué sirve jupyter?
Sirve para análisis de datos.

Jupyter es una plataforma que trabaja con el concepto de NOTEBOOK.
Donde un NOTEBOOK es un cuaderno en el que puedo ir intercalando CODIGO con DOCUMENTACIÓN. Con el código voy extrayendo datos de relevancia (estadísticos, gráficas...) que voy analizando y describiendo en TEXTO.

Podemos usar Jupyter a nivel conceptual para entender cómo funciona Spark, pero no lo vamos a usar para desarrollar el código que vamos a ejecutar en el servidor.

Spark NO ES UNA HERRAMIENTA DE ANALISIS DE DATOS. Es una herramienta para ETLs (bueno... también admite un modo STREAMING, pero eso lo veremos más adelante... pero que en realidad no es sino ir ejecutando las ETLs cada muy poco tiempo).

---

# Case que usamos en Scala

Se hereda de Java.

Con case nos referimos a la forma de nombrar las clases, variables, funciones, etc.

                PYTHON         JAVA/SCALA
Clase           MiClase        MiClase
Variable        mi_variable    miVariable
Función         mi_funcion     miFuncion

                snake_case     camelCase

Esto es un convenio QUE HAY QUE RESPETAR.


---

## Funciones en Scala para el trabajo con colecciones:

- map:            Nos permite aplicar una función a cada uno de los elementos de una colección,
                  transformando así los datos.... nos quedamos con lo que devuelva la función aplicada.
- foreach:        Nos permite aplicar una función a cada uno de los elementos de una colección,
                  pero no nos quedamos con el resultado de la función aplicada. Ya que la función que pasamos devuelve NADA (Unit)


> Pregunta: Qué tipo de función es la función foreach? Es una función tipo REDUCE !

---

# Patrones de Expresiones regulares (sintaxis PERL)

Un patrón es una secuencia de subpatrones.

Un subpatron es:
- Una secuencia de caracteres
- Un modificador de cantidad

## Secuencias de caracteres:
                                  Qué representa?                                                       Ejemplo
    hola                           Qué literalmente aparezca esa secuencia de caracteres (hola)         hola Federico
                                                                                                        ----
                                                                                                         ^ (1 match)
    [hola]                         Qué aparezca alguno de los caracteres que están entre los corchetes  hola Federico
                                                                                                        ----        -
                                                                                                        ^^^^        ^ (5 matches)
    [a-z]                          Qué aparezca un carácter entre a y z (minúsculas) según ASCII        hola Federico (11 matches)
                                                                                                        
    [a-zA-Zñóç-]                   Si quiero el guión, he de ponerlo al final... ya que es un carácter especial...
                                   que se usa para rangos
    [0-9]
                                   Si quiero un patrón que tome los números entre el 10 y el 99?
                                   [10-99] NO... eso sería el 1.. o cualquier carácter entre el 1 y el 9 ... o el 9
                                   PERL no trabaja con NÚMEROS.
                                   [1-9]*[0-9]
    .                              Cualquier carácter. Para que el PUNTO se tome como tal: \.  [.]

## Modificadores de cantidad:
    NADA                           DEBE aparecer 1 vez
    ?                              0 o 1 vez = OPCIONAL
    *                              0 o más veces = OPCIONAL o APARECER MOGOLLON DE VECES
    +                              1 o más veces = AL MENOS 1 VEZ
    {4}                            4 veces
    {4,}                           Al menos 4 veces
    {4,9}                          Entre 4 y 9 veces

## Otros importantes
    
    ^                             Inicio de texto
    $                             Fin de texto
    ()                            Agrupación de subpatrones
    |                             Opción entre subpatrones (OR)   subpatron1|subpatron2


---
DNIs

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

Qué problemas puede tener un DNI?
- Formato
- Longitud
- Letra que no coincide
 
Nuestra función de validación devolverá un RESULTADO qué podrá ser:
- OK
- ERROR_INVALID_FORMAT
- ERROR_INVALID_LENGTH
- ERROR_INVALID_CONTROL_DIGIT

Cómo se calcula la letra del DNI: Dividimos entre 23... pero nos quedamos con el resto de la división.

 23.000.000 | 23
            +-------------
          0   1.000.000
          ^^
          ESO ES LO QUE VALE (El resto de la división entera... también llamado MÓDULO o REMAINDER)

          Entre qué valores está? 0-22
          El ministerio de interior tiene publicada una tabla con la letra que corresponde a cada uno de los valores entre 0 y 22.
           0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19 20 21 22
           T R W A G M Y F P D  X  B  N  J  Z  S  Q  V  H  L  C  K  E

    val DIGITOS_CONTROL = "TRWAGMYFPDXBNJZSQVHLCKE"
    La posición de cada caracter es? el resto

    val letraQueLeTocaria = DIGITOS_CONTROL.charAt(numero%23)

    Paquete regex
    regex.findFirstIn(dni)

    regex.match(dni)

---
# Cómo lo hacemos?

"FEDERICO"

1. Validamos formato
2. PUNTOS , " " , -... quitarlos
3. Extraer letra / numero (mirar su longitud)
4. Vemos que concuerdan

^(([0-9]+)|([0-9]{1,3}([.][0-9]{3})*))[ -]?[a-zA-Z]$

^  Que cuando empiece el DNI
[0-9]{1,3} Tiene que haber entre 1 y 3 números
([.][0-9{3}])*

2.000.000T
23000000T

if NOT cumplePatron:
    return ERROR_FORMATO
else:
    quitoEspacios, puntos y guiones
    Tomo el ultimo caracter = LETRA
    Tomo todo menos el ultimo caracter = NUMERO
    If(NUMERO es mu largo):
        return ERROR_LONGITUD
    else:
        if LETRA QUE APARECE == LETRA QUE CALCULO:
            return OK
        else:
            return ERROR_LETRA

Hay una página ideal para regex: https://regex101.com/


---

CUIDADO !!!! OJO!!!
IMPORTANTE!!!
LO MAS IMPORTANTE DEL CURSO !!!!!
POR SI NO QUEDA CLARO: IMPORTANTISISISISISISMO!!!!!!


  Escribimos código <> PRUEBAS > OK > REFACTORIZAR <> PRUEBAS > OK     > SOLO AQUI EL PRODUCTO ESTA LISTO
  ----------50% del trabajo -------   -------50% del trabajo -----
            8 horas                             8 horas

                                 ^^^
                                 Si se me ocurre entregar aquí, ESTOY HACIENDOLO MUY MAL!
                                 MUYYYYYYYYY MALLLLLLLLLLLL!!!

Un producto de software POR DEFINICIÓN es un producto sujeto a mantenimiento y evolución.

La refactorización es cambiar / mejorar la estructura del código sin cambiar su comportamiento, con una única finalidad:
      mejorar la calidad del código para facilitar su mantenimiento y evolución.

El que piense que esto es una chorrada, AUN NO SE ENTERADO DE QUE VA LO DE CREAR SOFTWARE !
ESTO ES LA CLAVE DE TODO


Pregunta: Un coche es un producto POR DEFINICIÓN sujeto a mantenimientos?

Es suficiente con que el coche ANDE? NI DE COÑA... eso se da por descontado.