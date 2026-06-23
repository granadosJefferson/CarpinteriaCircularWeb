package com.carpinteria.controller;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.carpinteria.model.Cliente;
import com.carpinteria.service.ClienteService;

@Controller
@RequestMapping("/admin/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("clientes", clienteService.listarTodos());
        return "admin/clientes";
    }

    @GetMapping("/nuevo")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("cliente", new Cliente());
        model.addAttribute("titulo", "Registrar cliente");
        return "admin/cliente-formulario";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Model model) {

        model.addAttribute("cliente", clienteService.buscarPorId(id));
        model.addAttribute("titulo", "Editar cliente");

        return "admin/cliente-formulario";
    }

    @PostMapping("/guardar")
    public String guardar(
            @Valid Cliente cliente,
            BindingResult resultado,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (resultado.hasErrors()) {
            model.addAttribute(
                    "titulo",
                    cliente.getId() == null
                            ? "Registrar cliente"
                            : "Editar cliente");

            return "admin/cliente-formulario";
        }

        try {
            clienteService.guardar(cliente);

            redirectAttributes.addFlashAttribute(
                    "mensaje",
                    "Cliente guardado correctamente");

            return "redirect:/admin/clientes";

        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());

            model.addAttribute(
                    "titulo",
                    cliente.getId() == null
                            ? "Registrar cliente"
                            : "Editar cliente");

            return "admin/cliente-formulario";
        }
    }

    @PostMapping("/estado/{id}")
    public String cambiarEstado(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        clienteService.cambiarEstado(id);

        redirectAttributes.addFlashAttribute(
                "mensaje",
                "Estado del cliente actualizado");

        return "redirect:/admin/clientes";
    }
}