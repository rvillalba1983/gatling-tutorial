# Gatling + Java: tests de performance

[![CI](https://github.com/rvillalba1983/gatling-tutorial/actions/workflows/ci.yml/badge.svg)](https://github.com/rvillalba1983/gatling-tutorial/actions/workflows/ci.yml)

Proyecto de ejemplo del tutorial publicado en [Adictos al trabajo](https://www.adictosaltrabajo.com/2022/10/17/gatling-java-tests-de-performance/) (octubre de 2022).

> **Actualizado en 2026** a Java 25, Spring Boot 4.1 y Gatling 3.16. Respecto al artículo original se han eliminado las clases auxiliares de Gatling que ya no hacen falta, se han añadido *assertions* a la simulación, la carga es parametrizable y la prueba de carga se ejecuta en GitHub Actions. Algunas capturas son de la versión original y pueden no coincidir exactamente con el código actual.

### Índice
- [Introducción](#introducción)
- [Requisitos](#requisitos)
- [Descripción de la prueba realizada](#descripción-de-la-prueba-realizada)
- [Cómo vamos a ejecutar los tests de Gatling](#cómo-vamos-a-ejecutar-los-tests-de-gatling)
- [Cómo lanzar los tests de este proyecto](#cómo-lanzar-los-tests-de-este-proyecto)
- [Explicación de ficheros del proyecto](#explicación-de-ficheros-del-proyecto)
- [Nuestro test](#nuestro-test)
- [Conceptos de Gatling utilizados en el test](#conceptos-de-gatling-utilizados-en-el-test)
- [Configuración de la carga](#configuración-de-la-carga)
- [Prueba de carga en integración continua](#prueba-de-carga-en-integración-continua)
- [Conclusión](#conclusión)
- [Documentación utilizada](#documentación-utilizada)

## Introducción

Gatling permite programar tests de rendimiento/performance escribiendo código, sin herramientas visuales como podría ser JMeter.
En las primeras versiones de Gatling los tests se escribían en Scala, pero desde la versión 3.7 hay DSL para Java y Kotlin. Este proyecto usa el DSL de Java.

Es una tecnología muy configurable, lo que aporta un gran valor en este tipo de pruebas, y puede utilizarse para la mayoría de los escenarios que se necesiten. Además genera un informe bastante completo con todos los resultados desgranados.

## Requisitos

- Java 25 (recomendamos gestionar las versiones con [SDKMAN!](https://www.adictosaltrabajo.com/2017/04/10/gestiona-tus-herramientas-de-desarrollo-con-sdkman/))
- Maven 3.9 o superior
- Tu IDE favorito. Nosotros utilizamos IntelliJ.

## Descripción de la prueba realizada

Vamos a realizar las pruebas sobre un proyecto Java + Spring Boot con un único endpoint REST:

```
GET /characters/randoms/{quantity}
```

Devuelve `quantity` personajes de videojuego generados aleatoriamente con [Datafaker](https://www.datafaker.net/). Sobre él lanzaremos tests de rendimiento escritos con Gatling en Java.

## Cómo vamos a ejecutar los tests de Gatling

Usamos el [plugin de Maven de Gatling](https://docs.gatling.io/reference/integrations/build-tools/maven-plugin/) (también existe para Gradle y sbt). Hay otras formas de ejecutar Gatling, pero no forman parte de este tutorial.

## Cómo lanzar los tests de este proyecto

1. Clona el proyecto:

   ```bash
   git clone https://github.com/rvillalba1983/gatling-tutorial.git
   ```

   Es un proyecto "dummy" con un endpoint que crea personajes de videojuegos de forma aleatoria. Incluye además un test de integración con Spring Boot para comprobar en local que funciona (y para que juguéis).

   Para que el informe de Gatling quede lo más completo posible, la API **lanza errores de forma aleatoria** (un 5 % de las peticiones por defecto) y simula 5 ms de latencia por cada personaje generado, como si consultara una base de datos. Ambos valores se configuran en `application.properties`:

   ```properties
   app.simulation.error-rate=0.05
   app.simulation.latency-millis=5
   ```

2. Compila y lanza los tests:

   ```bash
   mvn clean verify
   ```

3. Arranca la aplicación. Desde el IDE basta con ejecutar la clase `GatlingTutorialApplication`, o desde terminal:

   ```bash
   mvn spring-boot:run
   ```

   La aplicación escucha en el puerto 8023 (configurado en `application.properties`).

   ![start](docs/images/start.png)

4. Desde otra terminal, en la raíz del proyecto, lanza los tests de Gatling:

   ```bash
   mvn gatling:test
   ```

5. Cuando termina la ejecución, Gatling muestra un resumen en modo texto y un enlace al informe HTML:

   ![resultado](docs/images/resultado.png)

   El informe se guarda en `target/gatling`. Si no se hace `clean`, se conservan todas las ejecuciones, cada una en una carpeta con la fecha y hora en que se lanzó.

   ![targetInforme](docs/images/targetInforme.png)

6. Abre el `index.html` con tu navegador y verás algo como esto:

   ![informe](docs/images/informe.png)

## Explicación de ficheros del proyecto

Los ficheros relacionados con Gatling son:

- `src/test/java/com/rvillalba/gatling/tutorial/simulation/NonPlayableCharacterSimulation.java`: el test propiamente dicho (en Gatling, una *Simulation*).
- `src/test/resources/logback-test.xml`: configuración de logs. Descomentando el logger `io.gatling.http.engine.response` se pueden ver las peticiones fallidas (`DEBUG`) o todas (`TRACE`).

En la versión original del artículo había además varios ficheros copiados de la documentación de Gatling:

- `Engine.java`, `IDEPathHelper.java` y `Recorder.java`, para lanzar Gatling y su grabador desde el IDE.
- `gatling.conf` y `recorder.conf`, con la configuración por defecto.

Con las versiones actuales del plugin ya no hacen falta. `mvn gatling:test` lanza las simulaciones, `mvn gatling:recorder` abre el grabador, y si no hay `gatling.conf` se usa la configuración por defecto. Así el proyecto solo contiene código de test.

## Nuestro test

Realiza varias llamadas al endpoint variando el parámetro `quantity`. Es sencillo, pero suficiente para este tutorial:

```java
private final ChainBuilder progressiveRandoms =
        exec(http("1 result").get("/randoms/1").check(status().is(200)))
                .exec(http("50 results").get("/randoms/50").check(status().is(200)))
                .exec(http("500 results").get("/randoms/500").check(status().is(200)));

private final ChainBuilder massiveRandoms =
        exec(http("1000 results").get("/randoms/1000").check(status().is(200)))
                .exec(http("800 results").get("/randoms/800").check(status().is(200)))
                .exec(http("1300 results").get("/randoms/1300").check(status().is(200)));

{
    setUp(
            progressiveCharactersScenario.injectOpen(rampUsers(USERS).during(RAMP_SECONDS)),
            massiveCharactersScenario.injectOpen(rampUsers(USERS).during(RAMP_SECONDS)))
            .protocols(httpProtocol)
            .assertions(
                    global().failedRequests().percent().lt(10.0),
                    global().responseTime().percentile(95.0).lt(30_000));
}
```

## Conceptos de Gatling utilizados en el test

Gatling maneja varios conceptos que, aunque simples, pueden resultar un poco liosos al principio (hay una pequeña curva de aprendizaje):

- **Chain**: grupo de acciones que se ejecutan en el orden en que se definen. En nuestro caso hay dos, `progressiveRandoms` y `massiveRandoms`, con tres llamadas HTTP cada una.
- **Action**: las acciones se representan con métodos. Estos son los que usamos, más alguno interesante:
  - `exec`: ejecuta una acción, normalmente una llamada HTTP.
  - `pause`: hace una parada del tiempo indicado entre llamada y llamada.
  - `repeat`: repite *n* veces otra acción, por ejemplo un `exec`.
  - `http`: construye una petición, en nuestro caso un GET. Aquí se pueden añadir body, cabeceras, etc. Gatling ofrece también *feeders* para leer ficheros CSV o JSON y lanzar pruebas parametrizadas, y muchas utilidades para validar las respuestas y guardar valores (`saveAs`) que se reutilizan en peticiones posteriores.
- **Check**: validaciones sobre la respuesta. Con `check(status().is(200))` una respuesta con otro código se marca como KO en el informe.
- **HttpProtocolBuilder**: configuración común a todas las peticiones a un mismo servicio, desde la `baseUrl` hasta las cabeceras generales, para no repetirla en cada acción.
- **Scenario**: puede contener varios *chains*. Cada escenario se lanza por separado, con su propia configuración de usuarios y duración, lo que permite probar flujos distintos en paralelo.
- **Assertions**: criterios de aceptación de la prueba completa. Si no se cumplen, `mvn gatling:test` termina con error, lo que permite usar Gatling como "puerta de calidad" en CI. Aquí exigimos menos de un 10 % de peticiones fallidas y un percentil 95 por debajo de 30 segundos.

En el informe HTML aparece todo separado por escenario y petición, por eso conviene poner nombres descriptivos a cada elemento.

## Configuración de la carga

Por defecto cada escenario recibe **1000 usuarios** que van entrando de forma progresiva durante **10 segundos** (`rampUsers`). Cada usuario hace las 3 peticiones de su escenario, así que en total se lanzan 6000 peticiones. Se ve bien en las gráficas del informe.

La carga y la URL se pueden cambiar sin tocar el código, con propiedades de sistema:

```bash
mvn gatling:test -Dusers=200 -DrampSeconds=5 -DbaseUrl=http://mi-entorno:8023
```

## Prueba de carga en integración continua

El workflow [`.github/workflows/ci.yml`](.github/workflows/ci.yml) tiene dos jobs:

1. **Compilar y tests**: `mvn verify`, que ejecuta también el test de integración.
2. **Prueba de carga con Gatling**: empaqueta la aplicación, la arranca, lanza la simulación con una carga reducida (100 usuarios por escenario) y publica el informe HTML como artefacto de la ejecución (`informe-gatling`). Desde la pestaña *Actions* se puede lanzar a mano con otro número de usuarios.

Si las *assertions* no se cumplen, el job falla.

## Conclusión

Gatling funciona bastante bien y permite realizar muchas llamadas en paralelo.
Aun así está sujeto, igual que otras herramientas, a los recursos de la máquina desde la que se lanza, así que los resultados pueden variar según el equipo. Lo ideal es usarlo en un entorno controlado donde esté desplegada la aplicación.
Para complementar los resultados de Gatling sería muy interesante utilizar un APM (como [Glowroot](https://glowroot.org/)).

Además, resulta muy interesante para "acercar al código" las pruebas de rendimiento.
Históricamente se han hecho con herramientas tipo JMeter, que obligan a aprender una aplicación externa al código. Si se prefiere, Gatling también tiene el *Gatling Recorder*, una aplicación de escritorio para grabar y crear las pruebas.

Al poder ejecutarse con Maven puede automatizarse fácilmente en CI/CD, como hacemos en este repositorio, y combinarse con las *assertions* para que una degradación del rendimiento rompa la build.

Como el informe es una web estática, también podría plantearse tener un histórico de resultados o usar los plugins de CI/CD que muestran el informe embebido.

![plugins](docs/images/plugins.png)

## Documentación utilizada
- https://gatling.io/
- https://docs.gatling.io/tutorials/
- https://docs.gatling.io/reference/integrations/build-tools/maven-plugin/
- https://github.com/gatling/gatling-maven-plugin-demo-java
