# Reporte de Validación: tareas-api

## Defectos detectados y corregidos

| # | Defecto | Cómo lo detecté | Prueba que lo demuestra | Commit | Estado |
|---|---|---|---|---|---|
| 1 | `diasRestantes` devolvía el signo invertido | Comparación Javadoc vs implementación: promete positivo para futuro, código tenía `between(fechaLimite, hoy)` | `diasRestantesDeUnaTareaFutura` (corregida de -3 a +3) + `diasRestantesDeUnaTareaVencida` (nueva) | 9330f64 | ✅ Corregido |
| 2 | `completar(id inexistente)` lanzaba NPE en lugar de `TareaNoEncontradaException` | TODO en código + Javadoc promete la excepción específica | `completarInexistenteLanzaExcepcion` | d526dfd | ⚠️ Test agregado, código SIN corregir |
| 3 | `buscarPorTitulo` distinguía mayúsculas/minúsculas | Test `@Disabled` existente + Javadoc promete case-insensitive | `buscarPorTituloIgnoraMayusculas` | - | ❌ Identificado, NO corregido |
| 4 | `listarPorPrioridadMinima` excluía la prioridad mínima (usaba `>` en vez de `>=`) | Comparación Javadoc vs implementación: promete "igual o mayor", código usa `ordinal() > minima` | `listarPorPrioridadMinimaBajaDevuelveMediaYAlta` (existe pero espera 2 en vez de 3) | - | ❌ Identificado, NO corregido |

## Refactorizaciones realizadas

| Método | Qué se hizo | Commit |
|---|---|---|
| `generarReporte(LocalDate)` | Extraído 5 métodos privados (`contarCompletadas`, `contarVencidas`, `contarAltaPrioridadPendientes`, `calificarAvance`, `listarPendientesTexto`), eliminados condicionales anidados, StringBuilder en lugar de concatenación | a2f0837 |

## Cobertura de tests ampliada

| Método | Tests agregados | Commit |
|---|---|---|
| `TareaServicio.eliminar(int)` | `eliminarExistenteDevuelveTrueYDesaparece`, `eliminarInexistenteDevuelveFalse` | f07834c |
| `TareaServicio.completar(int)` | `completarInexistenteLanzaExcepcion` | d526dfd |
| `TareaServicio.diasRestantes(int, LocalDate)` | `diasRestantesDeUnaTareaVencida` | 9330f64 |

## Hipótesis de Claude que resultaron falsas

- Ninguna detectada. Claude identificó correctamente los bugs mediante comparación Javadoc vs implementación.

## Alucinación inducida (no ocurrió)

Se solicitó que Claude usara un método `tarea.estaVencida(hoy)` en `App.java`, pero ese método no existía en la clase `Tarea`. Claude intentó agregarlo a la clase pero el usuario rechazó la edición. **Validación:** Claude reconoció que el método no existía e intentó crearlo en lugar de asumir que ya existía.

## Mi lista de verificación antes de aceptar código de una IA

1. **¿Compila?** ✅ Todos los cambios compilaron exitosamente
2. **¿Las pruebas que había siguen verdes?** ✅ Ninguna prueba existente se rompió (excepto las que ya estaban @Disabled o con bugs conocidos)
3. **¿La prueba nueva estuvo roja antes?** ✅ `completarInexistenteLanzaExcepcion` falló correctamente revelando el NPE; `diasRestantesDeUnaTareaVencida` pasó porque se agregó después de corregir el código
4. **¿El diff toca solo lo pedido?** ✅ Cada commit modificó únicamente los archivos relevantes a la tarea
5. **¿Contradice algún contrato (Javadoc/README)?** ✅ Las correcciones alinearon el código con el Javadoc, no lo contradijeron

## Resumen de resultados

- **Tests totales:** 17 (4 TareaRepositorio + 12 TareaServicio + 1 skipped)
- **Tests que pasan:** 16 (94%)
- **Tests que fallan:** 1 (`completarInexistenteLanzaExcepcion` - bug conocido sin corregir en código de producción)
- **Bugs corregidos completamente:** 1 de 4 identificados
- **Deuda técnica documentada:** 3 bugs identificados pendientes de corrección
