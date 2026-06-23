package com.carpinteria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpinteria.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    boolean existsByCorreoIgnoreCase(String correo);
}