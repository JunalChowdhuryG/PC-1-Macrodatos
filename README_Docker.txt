CÓMO GENERAR LOS JARS POR SEPARADO CON DOCKER
================================================

Estructura de carpetas (ya armada en este zip):

QaliWarma-docker/
  Dockerfile
  Makefile
  lib/                <- AQUI FALTA: copia tus 4 jars de Hadoop
  src/
    grupo1/
      SumReducer.java
      Consulta1Mapper.java
      Consulta1Driver.java
      ... (los 11 archivos)

PASO 0 - Requisito
-------------------
Tener Docker Desktop instalado y corriendo (Windows/Mac/Linux, da igual el
sistema operativo del host, porque todo corre dentro del contenedor Linux).

PASO 1 - Copiar los jars de Hadoop a lib/
-------------------------------------------
Copia ahí los 4 que ya tienes en NetBeans:
  hadoop-common-3.3.0.jar
  hadoop-mapreduce-client-common-*.jar
  hadoop-mapreduce-client-jobclient-*.jar
  hadoop-mapreduce-client-core-*.jar

(Si consigues las versiones 2.8.0, exactamente las del cluster, mejor todavía
- reduce el riesgo de NoSuchMethodError que hablamos antes)

PASO 2 - Construir la imagen (una sola vez)
---------------------------------------------
Desde la carpeta QaliWarma-docker/, en una terminal:

    docker build -t qaliwarma-build .

Esto arma una imagen con JDK 8 + make ya instalados. Solo hay que
reconstruirla si cambias el Dockerfile o agregas/quitas jars de lib/.

PASO 3 - Compilar y generar los 5 jars
-----------------------------------------
    docker run --rm -v "${PWD}/dist:/app/dist" qaliwarma-build

Qué hace: corre "make all" adentro del contenedor (compila todo una vez,
luego arma Consulta1.jar ... Consulta5.jar), y como montamos la carpeta
dist/ del contenedor a tu dist/ local (-v), los jars terminan apareciendo
directo en tu carpeta QaliWarma-docker/dist/ en tu propia PC.

En Windows PowerShell el mismo comando es:
    docker run --rm -v "${PWD}\dist:/app/dist" qaliwarma-build

En cmd.exe clásico:
    docker run --rm -v "%cd%\dist:/app/dist" qaliwarma-build

PASO 4 - Verificar
--------------------
    dir dist        (o "ls dist" si usas WSL/Git Bash)

Deberías ver: Consulta1.jar, Consulta2.jar, Consulta3.jar, Consulta4.jar,
Consulta5.jar -- cada uno con SOLO 3 clases adentro (su Mapper, su Driver,
y SumReducer).

PASO 5 - Llevar los jars a la VM de Hadoop y ejecutar
--------------------------------------------------------
Copia dist/ConsultaN.jar a tu VM (igual que harías con cualquier archivo) y
corre, por ejemplo:

    hadoop jar Consulta1.jar grupo1.Consulta1Driver /input_dir/qali_warma_2023.csv /output_dir/consulta1

Cuando avancemos con los grupos 2 a 7, actualizamos el Makefile agregando
sus consultas y (si tienen un Reducer propio distinto a SumReducer) sus
propias reglas de empaquetado.
