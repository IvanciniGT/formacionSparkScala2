def saluda(nombre):
    print("Hola " + nombre)

saluda("Iván")
saluda("Menchu")

texto = "Hola"
print(texto)

miFuncion = saluda       # Programación funcional
miFuncion("Federico")

def generar_saludo_formal(nombre):
    return "Saludos cordiales, " + nombre

def generar_saludo_informal(nombre):
    return "Qué pasa, " + nombre

def imprimir_saludo(funcion_generadora_de_saludos, nombre):
    saludo = funcion_generadora_de_saludos(nombre)
    print(saludo)

imprimir_saludo(generar_saludo_formal, "Iván")
imprimir_saludo(generar_saludo_informal, "Menchu")

# La programación funcional, una de sus consecuencias es que puedo
# crear funciones que aceptan otras funciones como parámetros.

# Si lo pensáis a nivel conceptual, lo que estamos haciendo es suministrar parte de la lógica
# que una función debe ejecutar como argumento a esa función.

## Expresiones lambda?
# Las expresiones lambda son ante todo EXPRESIONES.

texto = "Hola"       # Statement (Declaración, sentencia=frase, oración)
print(texto)         # Statement
numero =  5 + 7      # Statement
          ##### Expresión: Trozo de código que devuelve un valor.

# Por ende, una expresión lambda es un trozo de código que devuelve un valor.
# Qué valor devuelve? Una función anónima declarada dentro de la propia expresión.
# Las lambdas son una sintaxis alternativa para declarar funciones.


def generar_saludo_formal(nombre):          # Aquí estoy declarando una función
    return "Saludos cordiales, " + nombre


miFuncion = generar_saludo_formal           # Aquí estoy asignando una variable a esa función que he declarado previamente


miFuncion = lambda nombre: "Saludos cordiales, " + nombre
                                            # Aquí estoy asignando una variable a una función que he declarado dentro del mismo statement.

