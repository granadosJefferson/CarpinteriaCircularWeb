package com.carpinteria.controller;

import java.util.Arrays;

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
import com.carpinteria.service.ClienteService;
import com.carpinteria.service.PedidoService;
import com.carpinteria.service.ProductoService;

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

    @GetMapping
    public String listar(Model model) {

        model.addAttribute(
                "pedidos",
                pedidoService.listarTodos());

        model.addAttribute(
                "estados",
                Arrays.asList(EstadoPedido.values()));

        return "admin/pedidos";
    }

    @GetMapping("/nuevo")
    public String mostrarFormulario(Model model) {

        model.addAttribute(
                "pedidoFormulario",
                new PedidoFormulario());

        cargarDatosFormulario(model);

        return "admin/pedido-formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @ModelAttribute("pedidoFormulario")
            PedidoFormulario pedidoFormulario,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            pedidoService.crearPedido(pedidoFormulario);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Pedido registrado correctamente");

            return "redirect:/admin/pedidos";

        } catch (IllegalArgumentException e) {

            model.addAttribute(
                    "error",
                    e.getMessage());

            cargarDatosFormulario(model);

            return "admin/pedido-formulario";
        }
    }

    @GetMapping("/ver/{id}")
    public String verDetalle(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "pedido",
                pedidoService.buscarPorId(id));

        return "admin/pedido-detalle";
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(
            @PathVariable Long id,
            @RequestParam EstadoPedido estado,
            RedirectAttributes redirectAttributes) {

        try {

            pedidoService.cambiarEstado(id, estado);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Estado del pedido actualizado");

        } catch (IllegalArgumentException e) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage());
        }

        return "redirect:/admin/pedidos";
    }

    private void cargarDatosFormulario(Model model) {

        model.addAttribute(
                "clientes",
                clienteService.listarTodos());

        model.addAttribute(
                "productos",
                productoService.listarActivos());
    }
}