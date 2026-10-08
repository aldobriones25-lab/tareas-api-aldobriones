# CLAUDE.md

Este archivo proporciona orientación a Claude Code (claude.ai/code) al trabajar con código en este repositorio.

## Descripción del Proyecto

Gestor de tareas pendientes en Java 17. Proyecto de práctica para la mentoría técnica "Programar con Claude" (Generation México, CH70 Java).

## Comandos

```bash
# Ejecutar todas las pruebas
mvn test

# Ejecutar una clase de test específica
mvn test -Dtest=TareaRepositorioTest
mvn test -Dtest=TareaServicioTest

# Ejecutar la demo de consola
mvn -q exec:java

# Limpiar y compilar
mvn clean compile
```

## Arquitectura

Arquitectura de tres capas con persistencia en memoria:

```
App (demo de consola)
    ↓
TareaServicio (lógica de negocio / casos de uso)
    ↓
TareaRepositorio (almacenamiento en memoria con LinkedHashMap)
    ↓
Tarea (modelo de dominio)
```

**Clases principales:**
- `Tarea`: Modelo de tarea inmutable (excepto el flag `completada`). El ID lo asigna el repositorio al guardar (0 = aún no guardada).
- `Prioridad`: Enum (BAJA, MEDIA, ALTA). Usar `ordinal()` para comparaciones numéricas.
- `TareaRepositorio`: Almacenamiento en memoria con IDs autoincrementales empezando en 1. Devuelve `null` cuando no encuentra una tarea.
- `TareaServicio`: Fachada para todos los casos de uso. Esta es la API que consumiría un controlador REST o CLI.
- `TareaNoEncontradaException`: RuntimeException lanzada al acceder a IDs de tareas inexistentes.

## Convenciones Importantes

**Patrones de testing:**
- Usar `@BeforeEach` para inicializar el servicio/repositorio en cada test (ver `TareaServicioTest`)
- Usar nombres de métodos de test descriptivos que expliquen qué se está verificando
- Los tests con `@Disabled` documentan bugs conocidos o TODOs (no borrarlos)

**Manejo de fechas:**
- Se usa `LocalDate` para fechas límite (`null` = sin fecha límite)
- `diasRestantes()` devuelve `Long.MAX_VALUE` para tareas sin fecha límite
- Los tests usan fechas fijas (ej: `LocalDate.of(2026, 10, 8)`) para aserciones deterministas

## Problemas Conocidos

1. **Búsqueda case-insensitive no funciona** (`TareaRepositorioTest:35-39`):
   - `buscarPorTitulo()` dice ser case-insensitive pero usa `contains()` que distingue mayúsculas
   - El test está deshabilitado con `@Disabled("TODO: falla, revisar después")`
   - La solución requiere usar `.toLowerCase()` en ambos strings

2. **Cobertura de tests incompleta** (ver comentarios TODO en archivos de test):
   - `TareaServicio.eliminar(int)` no tiene tests
   - `TareaServicio.completar(999)` debería lanzar excepción pero no está probado
   - `TareaRepositorio.todas()` no tiene test dedicado

3. **Falta validación** (`Tarea.java:19`):
   - El constructor acepta string vacío `""` como título (debería validar)

## Nota sobre Plugin de Maven

El `pom.xml` declara explícitamente `maven-compiler-plugin:3.13.0` porque el Maven 3.8 de Ubuntu/Debian trae la versión 3.1, que ignora `maven.compiler.release` e intenta compilar como Java 5.
