package com.carpinteria.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.carpinteria.model.Usuario;
import com.carpinteria.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder) {

        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;

        crearAdministradorInicial();
    }

    private void crearAdministradorInicial() {

        String correoAdmin = "admin@carpinteria.com";

        if (!usuarioRepository.existsByCorreoIgnoreCase(correoAdmin)) {

            Usuario administrador = new Usuario(
                    "Administrador",
                    correoAdmin,
                    passwordEncoder.encode("1234"),
                    "ADMIN"
            );

            usuarioRepository.save(administrador);
        }
    }

    public boolean registrarUsuario(Usuario usuario) {

        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            return false;
        }

        String correoNormalizado =
                usuario.getCorreo().trim().toLowerCase();

        if (usuarioRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            return false;
        }

        usuario.setCorreo(correoNormalizado);
        usuario.setPassword(
                passwordEncoder.encode(usuario.getPassword())
        );
        usuario.setRol("CLIENTE");

        usuarioRepository.save(usuario);

        return true;
    }

    public Usuario buscarPorCorreo(String correo) {

        if (correo == null || correo.isBlank()) {
            return null;
        }

        return usuarioRepository
                .findByCorreoIgnoreCase(correo.trim())
                .orElse(null);
    }

    public Usuario iniciarSesion(String correo, String password) {

        Usuario usuario = buscarPorCorreo(correo);

        if (usuario != null
                && passwordEncoder.matches(
                        password,
                        usuario.getPassword()
                )) {

            return usuario;
        }

        return null;
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }
}