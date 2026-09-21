# Diagramas Mermaid — Lab 01 Hadoop/MapReduce (Qali Warma)

---

## 1. Arquitectura general (nodo único pseudo-distribuido)

```mermaid
flowchart TD
    A[dataQaliWarma.csv<br/>66,000 filas] -->|hadoop fs -put| B[HDFS<br/>/input_dir]
    B --> C{Jobs MapReduce<br/>Consulta1.jar ... Consulta14.jar}
    C --> D[HDFS<br/>/output_PC1_cN]
    D -->|hadoop fs -cat| E[Resultados finales<br/>para el informe]

    subgraph Nodo Unico - VM Hadoop 2.8.0
        B
        C
        D
    end

    F[NameNode] -.gestiona.-> B
    G[ResourceManager] -.orquesta.-> C
    H[DataNode / NodeManager] -.ejecuta.-> C
```

---

## 2. Flujo genérico de un job MapReduce simple (Grupos 1 a 4)

```mermaid
flowchart LR
    A[Input Split<br/>lineas del CSV] --> B[Map]
    B -->|"key, value"| C[Shuffle and Sort<br/>agrupa por key]
    C --> D[Reduce]
    D --> E[Output<br/>part-00000]

    B1["ej: Consulta1Mapper<br/>emite Departamento,Nivel -> NroUsuarios"] -.implementa.-> B
    D1["ej: SumReducer<br/>suma los NroUsuarios por llave"] -.implementa.-> D
```

---

## 3. Grupo 5 — MapReduce encadenado con join (Consultas 9 y 10)

```mermaid
flowchart TD
    subgraph JOB1[Job 1: Totales por zona]
        A1[Dataset original] --> M1[Job1Mapper<br/>emite Distrito -> NroUsuarios]
        M1 --> R1[SumReducer]
        R1 --> O1["/intermedio_c9<br/>Distrito TAB Total"]
    end

    subgraph JOB2[Job 2: Proporcion por IIEE]
        A1 --> M2[Job2Mapper]
        O1 --> M2
        M2 -->|"DATA: codigo,institucion,nroUsuarios"| R2[PropZonaReducer]
        M2 -->|"TOTAL: total"| R2
        R2 --> O2["/output_PC1_c9<br/>proporcion decimal por IIEE"]
    end

    O1 -.2da entrada.-> JOB2
```

---

## 4. Grupos 6 y 7 — Descenso de gradiente iterativo (Consultas 11-14)

```mermaid
flowchart TD
    A["Pesos iniciales<br/>w = 0,0,...,0"] --> B["Epoca N: JobConf<br/>set('pesos', w)"]
    B --> C["Job MapReduce<br/>Mapper calcula prediccion h(x)<br/>y gradiente parcial de cada fila"]
    C --> D["SumDoubleReducer<br/>suma gradientes, loss, n"]
    D --> E["Driver lee resultado de HDFS<br/>(FileSystem.open)"]
    E --> F["Actualiza pesos:<br/>w = w - tasa * gradiente_promedio"]
    F --> G{"Epoca < N_EPOCAS?"}
    G -->|Si| B
    G -->|No| H["Pesos finales +<br/>tabla epoca/loss/accuracy"]
```

---

---

## 5. Diagramas individuales por consulta

Uno por consulta, pensados para ir junto a su subsección en el informe
(uno chico, no el flujo genérico completo — para eso están los diagramas 2-4).

### Consulta 1 — Total NroUsuarios por Departamento+Nivel
```mermaid
flowchart LR
    A["Fila CSV"] --> B["Mapper<br/>key = Departamento,Nivel<br/>value = NroUsuarios"]
    B --> C["SumReducer<br/>suma NroUsuarios"]
    C --> D["Output<br/>Departamento,Nivel -> total"]
```

### Consulta 2 — Conteo IIEE por Provincia+Modalidad
```mermaid
flowchart LR
    A["Fila CSV"] --> B["Mapper<br/>key = Provincia,Modalidad<br/>value = 1"]
    B --> C["SumReducer<br/>cuenta filas"]
    C --> D["Output<br/>Provincia,Modalidad -> conteo IIEE"]
```

### Consulta 3 — Total NroUsuarios por UnidadTerritorial+Item
```mermaid
flowchart LR
    A["Fila CSV"] --> B["Mapper<br/>key = UnidadTerritorial,Item<br/>value = NroUsuarios"]
    B --> C["SumReducer<br/>suma NroUsuarios"]
    C --> D["Output<br/>UnidadTerritorial,Item -> total"]
```

### Consulta 4 — Conteo IIEE por Distrito+Nivel
```mermaid
flowchart LR
    A["Fila CSV"] --> B["Mapper<br/>key = Distrito,Nivel<br/>value = 1"]
    B --> C["SumReducer<br/>cuenta filas"]
    C --> D["Output<br/>Distrito,Nivel -> conteo IIEE"]
```

### Consulta 5 — Total NroUsuarios por ComiteCompra+Modalidad
```mermaid
flowchart LR
    A["Fila CSV"] --> B["Mapper<br/>key = ComiteCompra,Modalidad<br/>value = NroUsuarios"]
    B --> C["SumReducer<br/>suma NroUsuarios"]
    C --> D["Output<br/>ComiteCompra,Modalidad -> total"]
```

### Consulta 6 — Promedio, mediana y desviación por Departamento
```mermaid
flowchart LR
    A["Fila CSV"] --> B["Mapper<br/>key = Departamento<br/>value = NroUsuarios"]
    B --> C["Reducer<br/>junta todos los valores en lista,<br/>calcula promedio, ordena para<br/>mediana, calcula desviacion std"]
    C --> D["Output<br/>Departamento -> promedio;mediana;desv"]
```

### Consulta 7 — Búsqueda de subtexto "SANTA"
```mermaid
flowchart LR
    A["Fila CSV"] --> B{"Institucion o Direccion<br/>o CentroPoblado<br/>contiene 'SANTA'?"}
    B -->|Si| C["Mapper emite<br/>key=CodigoIIEE<br/>value=fila completa"]
    B -->|No| X["Descartada"]
    C --> D["Reducer<br/>reenvia sin cambios"]
    D --> E["Output<br/>registros filtrados completos"]
```

### Consulta 8 — Máximo y mínimo de NroUsuarios por Departamento
```mermaid
flowchart LR
    A["Fila CSV"] --> B["Mapper<br/>key = Departamento<br/>value = NroUsuarios,IIEE,codigo"]
    B --> C["Reducer<br/>compara cada valor contra<br/>max/min visto hasta ahora<br/>(patron argmax/argmin)"]
    C --> D["Output<br/>Departamento -> max:IIEE; min:IIEE"]
```

### Consulta 9 — Proporción de NroUsuarios sobre el total del Distrito
```mermaid
flowchart TD
    A["Job1: Mapper key=Distrito,<br/>value=NroUsuarios"] --> B["Job1: SumReducer<br/>-> total por Distrito"]
    B --> C["Job2: Mapper lee dataset<br/>+ salida de Job1,<br/>etiqueta DATA: / TOTAL:"]
    C --> D["Job2: PropZonaReducer<br/>junta DATA con su TOTAL,<br/>calcula NroUsuarios/total"]
    D --> E["Output<br/>codigoIIEE -> proporcion decimal"]
```

### Consulta 10 — Proporción de NroUsuarios sobre el total de la UnidadTerritorial
```mermaid
flowchart TD
    A["Job1: Mapper key=UnidadTerritorial,<br/>value=NroUsuarios"] --> B["Job1: SumReducer<br/>-> total por UnidadTerritorial"]
    B --> C["Job2: Mapper lee dataset<br/>+ salida de Job1,<br/>etiqueta DATA: / TOTAL:"]
    C --> D["Job2: PropZonaReducer<br/>junta DATA con su TOTAL,<br/>calcula NroUsuarios/total"]
    D --> E["Output<br/>codigoIIEE -> proporcion decimal"]
```

### Consulta 11 — Clasificación de ModalidadAtencion (Regresión Logística)
```mermaid
flowchart LR
    A["Fila CSV +<br/>pesos actuales (epoca N)"] --> B["Mapper<br/>x=Departamento,Nivel,NroUsuarios<br/>h=sigmoide(w.x)<br/>y=1 si RACIONES"]
    B -->|"grad_i, loss, correctos, n"| C["SumDoubleReducer"]
    C --> D["Driver actualiza pesos<br/>-> siguiente epoca"]
    D -.30 epocas.-> A
```

### Consulta 12 — Clasificación de NivelEducativo (Regresión Logística)
```mermaid
flowchart LR
    A["Fila CSV +<br/>pesos actuales (epoca N)"] --> B["Mapper<br/>x=Departamento,NroUsuarios<br/>h=sigmoide(w.x)<br/>y=1 si INICIAL"]
    B -->|"grad_i, loss, correctos, n"| C["SumDoubleReducer"]
    C --> D["Driver actualiza pesos<br/>-> siguiente epoca"]
    D -.30 epocas.-> A
```

### Consulta 13 — Regresión de NroUsuarios (3 features)
```mermaid
flowchart LR
    A["Fila CSV +<br/>pesos actuales (epoca N)"] --> B["Mapper<br/>x=Departamento,Nivel,Modalidad<br/>h=w.x (sin sigmoide)<br/>y=NroUsuarios/1000"]
    B -->|"grad_i, sse, n"| C["SumDoubleReducer"]
    C --> D["Driver: MSE=sse/n<br/>actualiza pesos -> siguiente epoca"]
    D -.40 epocas.-> A
```

### Consulta 14 — Regresión de NroUsuarios (solo Departamento)
```mermaid
flowchart LR
    A["Fila CSV +<br/>pesos actuales (epoca N)"] --> B["Mapper<br/>x=Departamento (unico feature)<br/>h=w.x<br/>y=NroUsuarios/1000"]
    B -->|"grad_i, sse, n"| C["SumDoubleReducer"]
    C --> D["Driver: MSE=sse/n<br/>actualiza pesos -> siguiente epoca"]
    D -.40 epocas.-> A
```


## 6. Mapa general: 7 grupos, 14 consultas

```mermaid
mindmap
  root((14 Consultas<br/>Qali Warma))
    Grupo 1: 2+ campos
      C1 Departamento+Nivel
      C2 Provincia+Modalidad
      C3 UnidadTerritorial+Item
      C4 Distrito+Nivel
      C5 ComiteCompra+Modalidad
    Grupo 2: Estadistica
      C6 Promedio/Mediana/Desv.Std
    Grupo 3: Busqueda texto
      C7 Subtexto SANTA
    Grupo 4: Max/Min
      C8 Max y min por Departamento
    Grupo 5: Encadenado + decimal
      C9 Proporcion por Distrito
      C10 Proporcion por UnidadTerritorial
    Grupo 6: Clasificacion
      C11 Regresion Logistica - Modalidad
      C12 Regresion Logistica - Nivel
    Grupo 7: Regresion
      C13 Regresion Lineal - 3 features
      C14 Regresion Lineal - 1 feature
```
