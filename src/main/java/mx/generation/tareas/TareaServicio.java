package mx.generation.tareas;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Casos de uso del gestor de tareas. Es la clase que usarían un controlador REST o una CLI.
 */
public class TareaServicio {

    private final TareaRepositorio repositorio;

    public TareaServicio(TareaRepositorio repositorio) {
        this.repositorio = repositorio;
    }

    public Tarea crear(String titulo, String descripcion, Prioridad prioridad, LocalDate fechaLimite) {
        return repositorio.guardar(new Tarea(titulo, descripcion, prioridad, fechaLimite));
    }

    /** Marca la tarea como completada. Si el id no existe, lanza TareaNoEncontradaException. */
    public Tarea completar(int id) {
        Tarea tarea = repositorio.buscar(id);
        tarea.marcarCompletada();
        return tarea;
    }

    public boolean eliminar(int id) {
        return repositorio.eliminar(id);
    }

    public List<Tarea> listarPendientes() {
        List<Tarea> pendientes = new ArrayList<>();
        for (Tarea t : repositorio.todas()) {
            if (!t.isCompletada()) {
                pendientes.add(t);
            }
        }
        return pendientes;
    }

    /**
     * Tareas pendientes con prioridad igual o mayor a la indicada.
     * Con MEDIA devuelve las MEDIA y las ALTA; con ALTA, solo las ALTA.
     */
    public List<Tarea> listarPorPrioridadMinima(Prioridad minima) {
        List<Tarea> resultado = new ArrayList<>();
        for (Tarea t : listarPendientes()) {
            if (t.getPrioridad().ordinal() > minima.ordinal()) {
                resultado.add(t);
            }
        }
        return resultado;
    }

    /**
     * Días que faltan para la fecha límite: positivo si está en el futuro, 0 si es hoy,
     * negativo si ya se venció.
     */
    public long diasRestantes(int id, LocalDate hoy) {
        Tarea tarea = obtener(id);
        if (tarea.getFechaLimite() == null) {
            return Long.MAX_VALUE;
        }
        return ChronoUnit.DAYS.between(hoy, tarea.getFechaLimite());
    }

    /**
     * Reporte en texto plano para el resumen diario.
     */
    public String generarReporte(LocalDate hoy) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== REPORTE DE TAREAS ===\n");

        List<Tarea> todas = repositorio.todas();
        int total = todas.size();
        int completadas = contarCompletadas(todas);
        int vencidas = contarVencidas(todas, hoy);
        int altasPendientes = contarAltaPrioridadPendientes(todas);

        sb.append("Total: ").append(total).append("\n");
        sb.append("Completadas: ").append(completadas).append("\n");
        sb.append("Pendientes: ").append(total - completadas).append("\n");
        sb.append("Vencidas: ").append(vencidas).append("\n");
        sb.append("Alta prioridad pendientes: ").append(altasPendientes).append("\n");
        sb.append(calificarAvance(total, completadas));
        sb.append("--- Pendientes ---\n");
        sb.append(listarPendientesTexto(todas));

        return sb.toString();
    }

    private int contarCompletadas(List<Tarea> tareas) {
        int count = 0;
        for (Tarea t : tareas) {
            if (t.isCompletada()) {
                count++;
            }
        }
        return count;
    }

    private int contarVencidas(List<Tarea> tareas, LocalDate hoy) {
        int count = 0;
        for (Tarea t : tareas) {
            if (t.isCompletada()) {
                continue;
            }
            if (t.getFechaLimite() != null && t.getFechaLimite().isBefore(hoy)) {
                count++;
            }
        }
        return count;
    }

    private int contarAltaPrioridadPendientes(List<Tarea> tareas) {
        int count = 0;
        for (Tarea t : tareas) {
            if (!t.isCompletada() && t.getPrioridad() == Prioridad.ALTA) {
                count++;
            }
        }
        return count;
    }

    private String calificarAvance(int total, int completadas) {
        if (total == 0) {
            return "Estado: SIN TAREAS\n";
        }
        int porcentaje = completadas * 100 / total;
        if (porcentaje >= 80) {
            return "Estado: EXCELENTE (" + porcentaje + "%)\n";
        }
        if (porcentaje >= 50) {
            return "Estado: BIEN (" + porcentaje + "%)\n";
        }
        return "Estado: ATRASADO (" + porcentaje + "%)\n";
    }

    private String listarPendientesTexto(List<Tarea> tareas) {
        StringBuilder sb = new StringBuilder();
        for (Tarea t : tareas) {
            if (t.isCompletada()) {
                continue;
            }
            sb.append("* ").append(t.getTitulo());
            if (t.getFechaLimite() != null) {
                sb.append(" (vence ").append(t.getFechaLimite()).append(")");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private Tarea obtener(int id) {
        Tarea tarea = repositorio.buscar(id);
        if (tarea == null) {
            throw new TareaNoEncontradaException(id);
        }
        return tarea;
    }
}
