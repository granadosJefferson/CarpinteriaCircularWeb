
package com.carpinteria.controller;

import com.carpinteria.model.Cliente;
import com.carpinteria.model.Usuario;
import com.carpinteria.repository.ClienteRepository;
import com.carpinteria.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final ClienteRepository clienteRepository;

    public AuthController(
            UsuarioService usuarioService,
            ClienteRepository clienteRepository) {

        this.usuarioService = usuarioService;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping("/login")
    public String mostrarLogin(
            @RequestParam(required = false) String redirect,
            @RequestParam(required = false) String correo,
            Model model) {

        model.addAttribute(
                "redirect",
                obtenerDestinoSeguro(redirect)
        );

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
                usuarioService.iniciarSesion(
                        correo,
                        password
                );

        if (usuarioEncontrado == null) {

            model.addAttribute(
                    "error",
                    "Correo o contraseña incorrectos"
            );

            model.addAttribute("correo", correo);
            model.addAttribute("redirect", destino);

            return "login";
        }

        /*
         * Los usuarios CLIENTE deben tener también
         * un registro en la tabla clientes.
         *
         * Esto corrige automáticamente usuarios antiguos
         * que existen en usuarios, pero no en clientes.
         */
        if (esCliente(usuarioEncontrado)) {
            obtenerOCrearCliente(usuarioEncontrado);
        }

        guardarUsuarioEnSesion(
                session,
                usuarioEncontrado
        );

        if (esAdministrador(usuarioEncontrado)) {
            return "redirect:/admin";
        }

        return "redirect:" + destino;
    }

    @GetMapping("/registro")
    public String mostrarRegistro(
            @RequestParam(required = false) String redirect,
            Model model) {

        model.addAttribute(
                "usuario",
                new Usuario()
        );

        model.addAttribute(
                "redirect",
                obtenerDestinoSeguro(redirect)
        );

        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(
            Usuario usuario,
            @RequestParam(required = false) String redirect,
            HttpSession session,
            Model model) {

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

        /*
         * Todo usuario que se registra desde la página pública
         * será CLIENTE.
         */
        usuario.setRol("CLIENTE");

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

        /*
         * Busca nuevamente al usuario guardado para utilizar
         * el registro persistido en la sesión.
         */
        Usuario usuarioRegistrado =
                usuarioService.iniciarSesion(
                        usuario.getCorreo(),
                        usuario.getPassword()
                );

        if (usuarioRegistrado == null) {
            usuarioRegistrado = usuario;
        }

        /*
         * Crea automáticamente el cliente asociado.
         */
        obtenerOCrearCliente(usuarioRegistrado);

        /*
         * El registro también inicia la sesión automáticamente.
         */
        guardarUsuarioEnSesion(
                session,
                usuarioRegistrado
        );

        return "redirect:" + destino;
    }

    @GetMapping("/perfil")
    public String mostrarPerfil(
            HttpSession session,
            Model model) {

        Usuario usuarioLogueado =
                (Usuario) session.getAttribute(
                        "usuarioLogueado"
                );

        if (usuarioLogueado == null) {
            return "redirect:/login?redirect=/perfil";
        }

        model.addAttribute(
                "usuario",
                usuarioLogueado
        );

        return "perfil";
    }

    @GetMapping("/logout")
    public String cerrarSesion(
            HttpSession session) {

        session.invalidate();

        return "redirect:/";
    }

    /**
     * Busca un cliente usando el correo del usuario.
     *
     * Si todavía no existe, lo crea automáticamente.
     * No genera duplicados porque el correo del cliente
     * es único y primero se realiza la búsqueda.
     */
    private Cliente obtenerOCrearCliente(
            Usuario usuario) {

        String correo = usuario.getCorreo().trim();

        return clienteRepository
                .findByCorreoIgnoreCase(correo)
                .orElseGet(() -> {

                    Cliente cliente = new Cliente();

                    cliente.setNombre(
                            usuario.getNombre().trim()
                    );

                    cliente.setCorreo(correo);

                    /*
                     * Usuario todavía no posee teléfono.
                     * Se utiliza un valor temporal para cumplir
                     * con la columna obligatoria de Cliente.
                     */
                    cliente.setTelefono("Pendiente");

                    cliente.setDireccion(null);
                    cliente.setActivo(true);

                    return clienteRepository.save(cliente);
                });
    }

    private boolean esAdministrador(
            Usuario usuario) {

        return usuario != null
                && "ADMIN".equalsIgnoreCase(
                        usuario.getRol()
                );
    }

    private boolean esCliente(
            Usuario usuario) {

        return usuario != null
                && "CLIENTE".equalsIgnoreCase(
                        usuario.getRol()
                );
    }

    private void guardarUsuarioEnSesion(
            HttpSession session,
            Usuario usuario) {

        session.setAttribute(
                "usuarioLogueado",
                usuario
        );

        session.setAttribute(
                "correo",
                usuario.getCorreo()
        );

        session.setAttribute(
                "nombre",
                usuario.getNombre()
        );

        session.setAttribute(
                "rol",
                usuario.getRol()
        );
    }

    private String obtenerDestinoSeguro(
            String redirect) {

        if (redirect == null
                || redirect.isBlank()) {

            return "/catalogo";
        }

        if (redirect.startsWith("/")
                && !redirect.startsWith("//")) {

            return redirect;
        }

        return "/catalogo";
    }
}

