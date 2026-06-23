package com.carpinteria.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.carpinteria.model.Envio;
import com.carpinteria.model.EstadoEnvio;
import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.Pedido;
import com.carpinteria.repository.EnvioRepository;
import com.carpinteria.repository.PedidoRepository;
import com.carpinteria.service.EnvioService;

import jakarta.servlet.http.HttpSession;

@Controller
public class EnvioController {

    private final EnvioService envioService;
    private final EnvioRepository envioRepository;
    private final PedidoRepository pedidoRepository;

    public EnvioController(
            EnvioService envioService,
            EnvioRepository envioRepository,
            PedidoRepository pedidoRepository) {

        this.envioService = envioService;
        this.envioRepository = envioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/admin/envios")
    public String listarEnvios(
            @RequestParam(required = false) EstadoEnvio estado,
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "envios",
                envioService.listarPorEstado(estado)
        );

        model.addAttribute(
                "estadoSeleccionado",
                estado
        );

        model.addAttribute(
                "estados",
                EstadoEnvio.values()
        );

        cargarMetricas(model);

        return "admin/envios";
    }

    @GetMapping("/admin/envios/nuevo")
    public String mostrarFormularioNuevo(
            HttpSession session,
            Model model) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        model.addAttribute("envio", new Envio());

        model.addAttribute(
                "pedidosDisponibles",
                obtenerPedidosDisponibles()
        );

        return "admin/envio-formulario";
    }

    @PostMapping("/admin/envios/guardar")
    public String guardarEnvio(
            @RequestParam(required = false) Long pedidoId,
            Envio envio,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {
            Envio envioGuardado = envioService.crearEnvio(
                    pedidoId,
                    envio
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Envío registrado correctamente"
            );

            return "redirect:/admin/envios/"
                    + envioGuardado.getId();

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            model.addAttribute(
                    "envio",
                    envio
            );

            model.addAttribute(
                    "pedidoIdSeleccionado",
                    pedidoId
            );

            model.addAttribute(
                    "pedidosDisponibles",
                    obtenerPedidosDisponibles()
            );

            return "admin/envio-formulario";
        }
    }

    @GetMapping("/admin/envios/{id}")
    public String verDetalle(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {
            Envio envio = envioService.buscarPorId(id);

            model.addAttribute(
                    "envio",
                    envio
            );

            model.addAttribute(
                    "estadosDisponibles",
                    obtenerEstadosDisponibles(envio.getEstado())
            );

            return "admin/envio-detalle";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/envios";
        }
    }

    @GetMapping("/admin/envios/{id}/editar")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {
            model.addAttribute(
                    "envio",
                    envioService.buscarPorId(id)
            );

            return "admin/envio-editar";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            return "redirect:/admin/envios";
        }
    }

    @PostMapping("/admin/envios/{id}/editar")
    public String actualizarEnvio(
            @PathVariable Long id,
            Envio envio,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {
            envioService.actualizarEnvio(id, envio);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Datos del envío actualizados correctamente"
            );

            return "redirect:/admin/envios/" + id;

        } catch (IllegalArgumentException e) {

            envio.setId(id);

            model.addAttribute(
                    "envio",
                    envio
            );

            model.addAttribute(
                    "error",
                    e.getMessage()
            );

            return "admin/envio-editar";
        }
    }

    @PostMapping("/admin/envios/{id}/estado")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam(required = false) EstadoEnvio estado,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {
            envioService.cambiarEstado(id, estado);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Estado del envío actualizado correctamente"
            );

            return "redirect:/admin/envios/" + id;

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

            /*
             * Se verifica si el envío existe antes de redirigir
             * nuevamente a su detalle.
             */
            try {
                envioService.buscarPorId(id);
                return "redirect:/admin/envios/" + id;

            } catch (IllegalArgumentException ex) {
                return "redirect:/admin/envios";
            }
        }
    }

    private void cargarMetricas(Model model) {

        model.addAttribute(
                "totalEnvios",
                envioService.contarTodos()
        );

        model.addAttribute(
                "enviosPendientes",
                envioService.contarPorEstado(
                        EstadoEnvio.PENDIENTE
                )
        );

        model.addAttribute(
                "enviosPreparando",
                envioService.contarPorEstado(
                        EstadoEnvio.PREPARANDO
                )
        );

        model.addAttribute(
                "enviosEnCamino",
                envioService.contarPorEstado(
                        EstadoEnvio.EN_CAMINO
                )
        );

        model.addAttribute(
                "enviosEntregados",
                envioService.contarPorEstado(
                        EstadoEnvio.ENTREGADO
                )
        );

        model.addAttribute(
                "enviosCancelados",
                envioService.contarPorEstado(
                        EstadoEnvio.CANCELADO
                )
        );
    }

    private List<EstadoEnvio> obtenerEstadosDisponibles(
            EstadoEnvio estadoActual) {

        if (estadoActual == null) {
            return List.of();
        }

        return switch (estadoActual) {

            case PENDIENTE -> List.of(
                    EstadoEnvio.PREPARANDO,
                    EstadoEnvio.CANCELADO
            );

            case PREPARANDO -> List.of(
                    EstadoEnvio.EN_CAMINO,
                    EstadoEnvio.CANCELADO
            );

            case EN_CAMINO -> List.of(
                    EstadoEnvio.ENTREGADO,
                    EstadoEnvio.CANCELADO
            );

            case ENTREGADO, CANCELADO -> List.of();
        };
    }

    private List<Pedido> obtenerPedidosDisponibles() {

        return pedidoRepository
                .findAllByOrderByFechaDesc()
                .stream()
                .filter(pedido ->
                        pedido.getEstado()
                                != EstadoPedido.CANCELADO
                )
                .filter(pedido ->
                        !envioRepository.existsByPedidoId(
                                pedido.getId()
                        )
                )
                .toList();
    }

    private boolean esAdministrador(
            HttpSession session) {

        String rol = (String) session.getAttribute("rol");

        return "ADMIN".equals(rol);
    }
}