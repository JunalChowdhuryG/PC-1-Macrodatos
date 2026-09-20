# Imagen liviana solo para COMPILAR y EMPAQUETAR jars.
# No corre un cluster de Hadoop aca dentro -- eso sigue siendo tu VM.
FROM eclipse-temurin:8-jdk

RUN apt-get update && apt-get install -y make && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Coloca en lib/ tus jars de Hadoop (idealmente version 2.8.0, la misma del
# cluster real, para evitar el problema de NoSuchMethodError que vimos antes)
COPY lib/ ./lib/
COPY src/ ./src/
COPY Makefile .

CMD ["make", "all"]
