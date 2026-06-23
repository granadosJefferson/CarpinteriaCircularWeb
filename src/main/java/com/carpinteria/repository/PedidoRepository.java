package com.carpinteria.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByFechaDesc();

    long countByEstado(EstadoPedido estado);

    List<Pedido> findByEstado(EstadoPedido estado);
}