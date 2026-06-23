package com.carpinteria.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpinteria.model.DetallePedido;

public interface DetallePedidoRepository
        extends JpaRepository<DetallePedido, Long> {
}