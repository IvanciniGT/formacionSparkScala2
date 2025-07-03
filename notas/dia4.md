
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

