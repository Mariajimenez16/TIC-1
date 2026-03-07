package com.tic.optimizacionespacios.controllers;

import com.tic.optimizacionespacios.dto.ActualizarEstadoDTO;
import com.tic.optimizacionespacios.dto.ReporteRequestDTO;
import com.tic.optimizacionespacios.dto.ReporteResponseDTO;
import com.tic.optimizacionespacios.enums.EstadoReporte;
import com.tic.optimizacionespacios.enums.Rol;
import com.tic.optimizacionespacios.enums.Urgencia;
import com.tic.optimizacionespacios.services.ReporteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final ReporteService reporteService;

    // ─────────────────────────────────────────────────────────────────────
    //  POST /api/reportes
    //  Crea un nuevo reporte. Usa multipart/form-data para incluir archivos.
    //
    //  Desde el frontend (React) se manda así:
    //    const formData = new FormData();
    //    formData.append("datos", new Blob([JSON.stringify(datos)], {type:"application/json"}));
    //    formData.append("archivos", archivo1);
    //    fetch("/api/reportes", { method: "POST", body: formData });
    // ─────────────────────────────────────────────────────────────────────
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReporteResponseDTO> crearReporte(
            @Valid @RequestPart("datos") ReporteRequestDTO dto,
            @RequestPart(value = "archivos", required = false) List<MultipartFile> archivos
    ) {
        ReporteResponseDTO respuesta = reporteService.crearReporte(dto, archivos);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }


    // ─────────────────────────────────────────────────────────────────────
    //  GET /api/reportes
    //  Lista todos. Acepta filtros opcionales por query params:
    //    /api/reportes?bloque=6
    //    /api/reportes?estado=PENDIENTE
    //    /api/reportes?urgencia=CRITICAL&bloque=6
    //    /api/reportes?rol=DOCENTE&categoria=Eléctrico
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<ReporteResponseDTO>> listar(
            @RequestParam(required = false) Integer       bloque,
            @RequestParam(required = false) EstadoReporte estado,
            @RequestParam(required = false) Urgencia      urgencia,
            @RequestParam(required = false) Rol           rol,
            @RequestParam(required = false) String        categoria
    ) {
        boolean hayFiltros = bloque != null || estado != null
                || urgencia != null || rol != null || categoria != null;

        List<ReporteResponseDTO> lista = hayFiltros
                ? reporteService.buscarConFiltros(bloque, estado, urgencia, rol, categoria)
                : reporteService.listarTodos();

        return ResponseEntity.ok(lista);
    }


    // ─────────────────────────────────────────────────────────────────────
    //  GET /api/reportes/{id}
    //  Obtiene un reporte específico por su ID
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ReporteResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reporteService.obtenerPorId(id));
    }


    // ─────────────────────────────────────────────────────────────────────
    //  GET /api/reportes/bloque/{numero}
    //  Todos los reportes de un bloque → útil para el mapa del campus
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/bloque/{numero}")
    public ResponseEntity<List<ReporteResponseDTO>> porBloque(
            @PathVariable Integer numero) {
        return ResponseEntity.ok(reporteService.buscarPorBloque(numero));
    }


    // ─────────────────────────────────────────────────────────────────────
    //  GET /api/reportes/bloque/{numero}/salon/{salon}
    //  Reportes de un salón puntual: /api/reportes/bloque/6/salon/301
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/bloque/{numero}/salon/{salon}")
    public ResponseEntity<List<ReporteResponseDTO>> porBloqueYSalon(
            @PathVariable Integer numero,
            @PathVariable String  salon
    ) {
        return ResponseEntity.ok(reporteService.buscarPorBloqueYSalon(numero, salon));
    }


    // ─────────────────────────────────────────────────────────────────────
    //  GET /api/reportes/inhabilitados
    //  Espacios que están fuera de servicio (sin resolver aún)
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/inhabilitados")
    public ResponseEntity<List<ReporteResponseDTO>> inhabilitados() {
        return ResponseEntity.ok(reporteService.obtenerEspaciosInhabilitados());
    }


    // ─────────────────────────────────────────────────────────────────────
    //  PATCH /api/reportes/{id}/estado
    //  El admin cambia el estado. Body JSON:
    //  { "estado": "EN_PROCESO", "notaAdmin": "Técnico asignado" }
    // ─────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/estado")
    public ResponseEntity<ReporteResponseDTO> actualizarEstado(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEstadoDTO dto
    ) {
        return ResponseEntity.ok(reporteService.actualizarEstado(id, dto));
    }


    // ─────────────────────────────────────────────────────────────────────
    //  DELETE /api/reportes/{id}
    //  Elimina un reporte y sus archivos del servidor
    // ─────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> eliminar(@PathVariable Long id) {
        reporteService.eliminar(id);
        return ResponseEntity.ok(
                Map.of("mensaje", "Reporte " + id + " eliminado correctamente"));
    }


    // ─────────────────────────────────────────────────────────────────────
    //  GET /api/reportes/estadisticas
    //  Totales y rankings para el dashboard administrativo
    // ─────────────────────────────────────────────────────────────────────
    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> estadisticas() {
        return ResponseEntity.ok(reporteService.obtenerEstadisticas());
    }
}
