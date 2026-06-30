package com.carpinteria.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;

import com.carpinteria.model.Usuario;
import com.carpinteria.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String correoAdministrador;
    private final String passwordAdministrador;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.email:admin@carpinteria.com}")
            String correoAdministrador,
            @Value("${app.admin.password:1234}")
            String passwordAdministrador) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.correoAdministrador = correoAdministrador;
        this.passwordAdministrador = passwordAdministrador;
    }

    // Creates the initial administrator only when no account uses its email.
    @PostConstruct
    public void crearAdministradorInicial() {

        String correoNormalizado =
                normalizarCorreo(correoAdministrador);

        if (correoNormalizado == null
                || passwordAdministrador == null
                || passwordAdministrador.isBlank()) {

            throw new IllegalStateException(
                    "Las credenciales del administrador no están configuradas"
            );
        }

        if (!usuarioRepository
                .existsByCorreoIgnoreCase(correoNormalizado)) {

            Usuario administrador = new Usuario(
                    "Administrador",
                    correoNormalizado,
                    passwordEncoder.encode(passwordAdministrador),
                    "ADMIN"
            );

            usuarioRepository.save(administrador);
        }
    }

    // Registers a unique client account with an encrypted password.
    @Transactional
    public boolean registrarUsuario(Usuario usuario) {

        if (usuario == null) {
            return false;
        }

        normalizarDatos(usuario);

        if (!datosRegistroValidos(usuario)) {
            return false;
        }

        if (usuarioRepository.existsByCorreoIgnoreCase(
                usuario.getCorreo())) {

            return false;
        }

        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );

        usuario.setRol("CLIENTE");

        usuarioRepository.save(usuario);

        return true;
    }

    public Usuario buscarPorCorreo(String correo) {

        String correoNormalizado = normalizarCorreo(correo);

        if (correoNormalizado == null) {
            return null;
        }

        return usuarioRepository
                .findByCorreoIgnoreCase(correoNormalizado)
                .orElse(null);
    }

    // Authenticates the account by comparing the submitted and encrypted passwords.
    public Usuario iniciarSesion(
            String correo,
            String password) {

        if (password == null || password.isBlank()) {
            return null;
        }

        Usuario usuario = buscarPorCorreo(correo);

        if (usuario != null
                && usuario.getPassword() != null
                && passwordEncoder.matches(
                        password,
                        usuario.getPassword())) {

            return usuario;
        }

        return null;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    private boolean datosRegistroValidos(Usuario usuario) {

        return usuario.getNombre() != null
                && !usuario.getNombre().isBlank()
                && usuario.getCorreo() != null
                && !usuario.getCorreo().isBlank()
                && usuario.getPassword() != null
                && !usuario.getPassword().isBlank();
    }

    private void normalizarDatos(Usuario usuario) {

        if (usuario.getNombre() != null) {
            usuario.setNombre(
                    usuario.getNombre().trim()
            );
        }

        usuario.setCorreo(
                normalizarCorreo(usuario.getCorreo())
        );
    }

    private String normalizarCorreo(String correo) {

        if (correo == null || correo.isBlank()) {
            return null;
        }

        return correo.trim().toLowerCase();
    }
}