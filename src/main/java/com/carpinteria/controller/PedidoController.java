package com.carpinteria.controller;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.carpinteria.dto.PedidoFormulario;
import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.Pedido;
import com.carpinteria.service.ClienteService;
import com.carpinteria.service.PedidoService;
import com.carpinteria.service.ProductoService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProductoService productoService;

    public PedidoController(
            PedidoService pedidoService,
            ClienteService clienteService,
            ProductoService productoService) {

        this.pedidoService = pedidoService;
        this.clienteService = clienteService;
        this.productoService = productoService;
    }

    /**
     * Muestra todos los pedidos registrados.
     */
    @GetMapping
    public String listar(
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        model.addAttribute(
                "pedidos",
                pedidoService.listarTodos()
        );

        /*
         * Se conserva por compatibilidad con pedidos.html,
         * en caso de que la vista todavía muestre algún
         * selector general de estados.
         */
        model.addAttribute(
                "estados",
                Arrays.asList(EstadoPedido.values())
        );

        return "admin/pedidos";
    }

    /**
     * Muestra el formulario para registrar un pedido
     * desde el panel administrativo.
     */
    @GetMapping("/nuevo")
    public String mostrarFormulario(
            Model model,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        if (!model.containsAttribute("pedidoFormulario")) {
            model.addAttribute(
                    "pedidoFormulario",
                    new PedidoFormulario()
            );
        }

        cargarDatosFormulario(model);

        return "admin/pedido-formulario";
    }

    /**
     * Registra un pedido nuevo.
     */
    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute("pedidoFormulario")
            PedidoFormulario pedidoFormulario,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {

            Pedido pedidoCreado =
                    pedidoService.crearPedido(
                            pedidoFormulario
                    );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Pedido #"
                            + pedidoCreado.getId()
                            + " registrado correctamente"
            );

            return "redirect:/admin/pedidos";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    obtenerMensajeSeguro(
                            e,
                            "Los datos del pedido no son válidos"
                    )
            );

            cargarDatosFormulario(model);

            return "admin/pedido-formulario";

        } catch (Exception e) {

            model.addAttribute(
                    "error",
                    "No fue posible registrar el pedido. "
                            + "Verifique los datos e inténtelo nuevamente."
            );

            cargarDatosFormulario(model);

            return "admin/pedido-formulario";
        }
    }

    /**
     * Muestra el detalle completo de un pedido.
     */
    @GetMapping("/ver/{id}")
    public String verDetalle(
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {

            validarId(id);

            Pedido pedido =
                    pedidoService.buscarPorId(id);

            model.addAttribute(
                    "pedido",
                    pedido
            );

            /*
             * La vista debe utilizar esta colección
             * en lugar de EstadoPedido.values().
             */
            model.addAttribute(
                    "estadosDisponibles",
                    obtenerEstadosDisponibles(
                            pedido.getEstado()
                    )
            );

            return "admin/pedido-detalle";

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    obtenerMensajeSeguro(
                            e,
                            "No se encontró el pedido solicitado"
                    )
            );

            return "redirect:/admin/pedidos";

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible consultar el detalle del pedido"
            );

            return "redirect:/admin/pedidos";
        }
    }

    /**
     * Cambia el estado de un pedido.
     *
     * El parámetro se recibe como String para evitar
     * que Spring genere un error 400 si llega un valor
     * que no pertenece al enum EstadoPedido.
     */
    @PostMapping("/estado/{id}")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam(
                    name = "estado",
                    required = false
            )
            String estadoTexto,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {

            validarId(id);

            EstadoPedido estado =
                    convertirEstado(estadoTexto);

            pedidoService.cambiarEstado(
                    id,
                    estado
            );

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Estado del pedido actualizado correctamente"
            );

            return redireccionDetalle(id);

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    obtenerMensajeSeguro(
                            e,
                            "No se pudo actualizar el estado del pedido"
                    )
            );

            if (id == null || id <= 0) {
                return "redirect:/admin/pedidos";
            }

            return redireccionDetalle(id);

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible actualizar el estado del pedido"
            );

            if (id == null || id <= 0) {
                return "redirect:/admin/pedidos";
            }

            return redireccionDetalle(id);
        }
    }

    /**
     * Registra como pagado un pedido cuyo método
     * de pago es PAGO_AL_RETIRAR.
     */
    @PostMapping("/{id}/registrar-pago-retiro")
    public String registrarPagoAlRetirar(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        if (!esAdministrador(session)) {
            return "redirect:/login";
        }

        try {

            validarId(id);

            pedidoService
                    .marcarPagoAlRetirarComoPagado(id);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "El pago del pedido fue registrado correctamente"
            );

            return redireccionDetalle(id);

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    obtenerMensajeSeguro(
                            e,
                            "No se pudo registrar el pago del pedido"
                    )
            );

            if (id == null || id <= 0) {
                return "redirect:/admin/pedidos";
            }

            return redireccionDetalle(id);

        } catch (Exception e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "No fue posible registrar el pago del pedido"
            );

            if (id == null || id <= 0) {
                return "redirect:/admin/pedidos";
            }

            return redireccionDetalle(id);
        }
    }

    /**
     * Carga las listas necesarias para mostrar nuevamente
     * el formulario después de una validación fallida.
     */
    private void cargarDatosFormulario(Model model) {

        model.addAttribute(
                "clientes",
                clienteService.listarTodos()
        );

        model.addAttribute(
                "productos",
                productoService.listarActivos()
        );
    }

    /**
     * Devuelve únicamente los estados a los que el pedido
     * puede avanzar desde su estado actual.
     */
    private List<EstadoPedido> obtenerEstadosDisponibles(
            EstadoPedido estadoActual) {

        if (estadoActual == null) {
            return List.of();
        }

        return switch (estadoActual) {

            case PENDIENTE -> List.of(
                    EstadoPedido.EN_PROCESO,
                    EstadoPedido.CANCELADO
            );

            case EN_PROCESO -> List.of(
                    EstadoPedido.COMPLETADO,
                    EstadoPedido.CANCELADO
            );

            case COMPLETADO, CANCELADO ->
                    List.of();
        };
    }

    /**
     * Convierte el texto recibido desde el formulario
     * en un valor válido del enum EstadoPedido.
     */
    private EstadoPedido convertirEstado(
            String estadoTexto) {

        if (estadoTexto == null
                || estadoTexto.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar un estado válido"
            );
        }

        String valorNormalizado =
                estadoTexto.trim()
                        .toUpperCase()
                        .replace(" ", "_");

        try {

            return EstadoPedido.valueOf(
                    valorNormalizado
            );

        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException(
                    "El estado seleccionado no es válido"
            );
        }
    }

    private void validarId(Long id) {

        if (id == null || id <= 0) {
            throw new IllegalArgumentException(
                    "El identificador del pedido es inválido"
            );
        }
    }

    private String redireccionDetalle(Long id) {
        return "redirect:/admin/pedidos/ver/" + id;
    }

    /**
     * Evita mostrar mensajes vacíos o detalles técnicos
     * directamente en la interfaz.
     */
    private String obtenerMensajeSeguro(
            Exception excepcion,
            String mensajePredeterminado) {

        if (excepcion == null
                || excepcion.getMessage() == null
                || excepcion.getMessage().isBlank()) {

            return mensajePredeterminado;
        }

        return excepcion.getMessage();
    }

    /**
     * Comprueba que exista una sesión iniciada y que
     * el rol almacenado sea ADMIN.
     */
    private boolean esAdministrador(
            HttpSession session) {

        if (session == null) {
            return false;
        }

        Object usuarioLogueado =
                session.getAttribute(
                        "usuarioLogueado"
                );

        Object rol =
                session.getAttribute("rol");

        return usuarioLogueado != null
                && rol != null
                && "ADMIN".equalsIgnoreCase(
                        rol.toString().trim()
                );
    }
}