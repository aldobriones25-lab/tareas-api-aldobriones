# Mapa del Proyecto: tareas-api

## 1. ¿Qué hace el proyecto?

Gestor de tareas pendientes en Java 17 con almacenamiento en memoria. Permite crear, completar, eliminar y buscar tareas con prioridades, fechas límite y reportes de estado.

## 2. Tabla de clases

| Clase | Responsabilidad | Depende de |
|-------|-----------------|------------|
| `App` | Demo de consola que crea tareas de ejemplo y muestra un reporte | `TareaServicio`, `TareaRepositorio`, `Prioridad` |
| `Tarea` | Modelo de dominio inmutable que representa una tarea con título, descripción, prioridad, fecha límite y estado | `Prioridad` |
| `Prioridad` | Enum con tres niveles de prioridad (BAJA, MEDIA, ALTA) | Ninguna |
| `TareaRepositorio` | Almacenamiento en memoria con LinkedHashMap, asigna IDs autoincrementales empezando en 1 | `Tarea` |
| `TareaServicio` | Fachada de lógica de negocio que coordina casos de uso y genera reportes | `TareaRepositorio`, `Tarea`, `Prioridad`, `TareaNoEncontradaException` |
| `TareaNoEncontradaException` | Excepción lanzada cuando se busca una tarea por un ID inexistente | Ninguna |
| `TareaRepositorioTest` | Suite de tests unitarios para la capa de persistencia en memoria | `TareaRepositorio`, `Tarea`, `Prioridad` |
| `TareaServicioTest` | Suite de tests unitarios para la lógica de negocio y casos de uso | `TareaServicio`, `TareaRepositorio`, `Tarea`, `Prioridad` |

## 3. Cómo se compila y se prueba

```bash
# Compilar el proyecto
mvn clean compile

# Ejecutar todas las pruebas
mvn test

# Ejecutar una clase de test específica
mvn test -Dtest=TareaRepositorioTest
mvn test -Dtest=TareaServicioTest

# Ejecutar la demo de consola
mvn -q exec:java
```

## 4. Tres cosas sospechosas o incompletas

### 1. Bug en búsqueda case-insensitive
**Archivo:** `src/main/java/mx/generation/tareas/TareaRepositorio.java:44`

El método `buscarPorTitulo()` promete en su documentación (línea 38) ser "sin distinguir mayúsculas de minúsculas", pero usa `contains()` que sí las distingue. El test correspondiente está deshabilitado en `TareaRepositorioTest.java:33` con `@Disabled("TODO: falla, revisar después")`. La búsqueda de "informe" no encuentra "INFORME".

### 2. Cálculo invertido de días restantes
**Archivo:** `src/main/java/mx/generation/tareas/TareaServicio.java:67`

El método `diasRestantes()` tiene los parámetros de `ChronoUnit.DAYS.between()` al revés: usa `between(fechaLimite, hoy)` cuando debería ser `between(hoy, fechaLimite)`. Por eso el test en la línea 64 de `TareaServicioTest.java` espera `-3` en lugar de `3` para una tarea que vence en 3 días (el signo está invertido).

### 3. Bug en filtro de prioridad mínima
**Archivo:** `src/main/java/mx/generation/tareas/TareaServicio.java:51`

El método `listarPorPrioridadMinima()` usa `>` (mayor estricto) en lugar de `>=` (mayor o igual) para comparar prioridades. Esto excluye la prioridad mínima solicitada: si pides `MEDIA`, solo obtienes las `ALTA`, cuando deberías obtener `MEDIA` y `ALTA`. El test de la línea 52 de `TareaServicioTest.java` lo confirma esperando 2 resultados con mínima BAJA (debería devolver las 3).
