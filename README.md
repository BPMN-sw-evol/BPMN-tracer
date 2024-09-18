# BPMN-Tracer

Este repositorio incluye un lector de archivos XML diseñado para modelos BPMN, encargado de extraer información relevante de las actividades.

## Index
1. [Descripción](#descripcion)
2. [Requisitos](#requisitos)

## Descripcion

Este sistema utiliza la librería de Camunda para analizar un archivo XML (modelo BPMN). 
El sistema recorre el archivo buscando diferentes tipos de tareas y elementos del flujo, como:

- **UserTask:** Tareas asignadas a usuarios.
- **ServiceTask**: Tareas que ejecutan servicios automáticos.
- **EventTask**: Eventos dentro del flujo.
- **SendTask**: Tareas que envían mensajes o notificaciones.
- **FlowSequence**: Conexiones entre los elementos del modelo BPMN.
  
De cada uno de estos elementos, el sistema extrae información clave como:
- **ID**: Identificador único del elemento.
- **Nombre**: Nombre del elemento.
- **Tipo de elemento**: Qué tipo de tarea o evento es.
- **Tipo de implementación**: Especifica cómo se implementa el elemento.
- **Referencia de implementación o variables**:
  - **UserTask**: Variables del formulario generado (Generate Task Form).
  - **Otros elementos**: Variables de entradas y salidas (Inputs y Outputs).
    
Con toda esta información, se genera un archivo **JSON** que se almacena en una carpeta llamada `output`.


## Requisitos

- **Maven**: para la creación de proyectos. [Web oficial](https://maven.apache.org/download.cgi?.)
- **Java 21 o Superior**: Se utiliza para definir y configurar las propiedades de la aplicación que está convirtiendo en un ejecutable nativo de Windows. [Web oficial](https://www.oracle.com/java/technologies/downloads/#java21)
