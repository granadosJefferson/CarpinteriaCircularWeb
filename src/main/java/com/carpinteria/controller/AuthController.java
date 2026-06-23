package com.carpinteria.controller;

import com.carpinteria.model.Usuario;
import com.carpinteria.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;

    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(required = false) String redirect,
            @RequestParam(required = false) String correo,
            Model model) {

        model.addAttribute("redirect", obtenerDestinoSeguro(redirect));
        model.addAttribute("correo", correo);

        return "login";
    }

    @PostMapping("/login")
    public String procesarLogin(
            @RequestParam String correo,
            @RequestParam String password,
            @RequestParam(required = false) String redirect,
            HttpSession session,
            Model model) {

        String destino = obtenerDestinoSeguro(redirect);

        Usuario usuarioEncontrado =
                usuarioService.iniciarSesion(correo, password);

        if (usuarioEncontrado == null) {
            model.addAttribute(
                    "error",
                    "Correo o contraseña incorrectos"
            );
            model.addAttribute("correo", correo);
            model.addAttribute("redirect", destino);

            return "login";
        }

        guardarUsuarioEnSesion(session, usuarioEncontrado);

        if ("ADMIN".equalsIgnoreCase(usuarioEncontrado.getRol())) {
            return "redirect:/admin";
        }

        return "redirect:" + destino;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(
            @RequestParam(required = false) String redirect,
            Model model) {

        model.addAttribute("usuario", new Usuario());
        model.addAttribute("redirect", obtenerDestinoSeguro(redirect));

        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            Usuario usuario,
            @RequestParam(required = false) String redirect,
            Model model,
            RedirectAttributes redirectAttributes) {

        String destino = obtenerDestinoSeguro(redirect);

        if (usuario.getNombre() == null
                || usuario.getNombre().isBlank()) {

            model.addAttribute(
                    "error",
                    "El nombre es obligatorio"
            );
            model.addAttribute("redirect", destino);

            return "registro";
        }

        if (usuario.getCorreo() == null
                || usuario.getCorreo().isBlank()) {

            model.addAttribute(
                    "error",
                    "El correo es obligatorio"
            );
            model.addAttribute("redirect", destino);

            return "registro";
        }

        if (usuario.getPassword() == null
                || usuario.getPassword().length() < 6) {

            model.addAttribute(
                    "error",
                    "La contraseña debe tener al menos 6 caracteres"
            );
            model.addAttribute("redirect", destino);

            return "registro";
        }

        if (usuario.getConfirmarPassword() == null
                || !usuario.getPassword()
                        .equals(usuario.getConfirmarPassword())) {

            model.addAttribute(
                    "error",
                    "Las contraseñas no coinciden"
            );
            model.addAttribute("redirect", destino);

            return "registro";
        }

        boolean registrado =
                usuarioService.registrarUsuario(usuario);

        if (!registrado) {
            model.addAttribute(
                    "error",
                    "El correo electrónico ya está registrado"
            );
            model.addAttribute("redirect", destino);

            return "registro";
        }

        redirectAttributes.addFlashAttribute(
                "exito",
                "Cuenta creada correctamente. Ahora puede iniciar sesión."
        );
        redirectAttributes.addAttribute("redirect", destino);
        redirectAttributes.addAttribute(
                "correo",
                usuario.getCorreo()
        );

        return "redirect:/login";
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(
            HttpSession session,
            Model model) {

        Usuario usuarioLogueado =
                (Usuario) session.getAttribute("usuarioLogueado");

        if (usuarioLogueado == null) {
            return "redirect:/login?redirect=/perfil";
        }

        model.addAttribute("usuario", usuarioLogueado);

        return "perfil";
    }

    @GetMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    private void guardarUsuarioEnSesion(
            HttpSession session,
            Usuario usuario) {

        session.setAttribute("usuarioLogueado", usuario);
        session.setAttribute("correo", usuario.getCorreo());
        session.setAttribute("nombre", usuario.getNombre());
        session.setAttribute("rol", usuario.getRol());
    }

    private String obtenerDestinoSeguro(String redirect) {

        if (redirect == null || redirect.isBlank()) {
            return "/catalogo";
        }

        if (redirect.startsWith("/")
                && !redirect.startsWith("//")) {
            return redirect;
        }

        return "/catalogo";
    }
}