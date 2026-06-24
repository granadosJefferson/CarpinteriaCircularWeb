package com.carpinteria.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.Pedido;

public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    List<Pedido> findAllByOrderByFechaDesc();

    long countByEstado(EstadoPedido estado);

    List<Pedido> findByEstado(EstadoPedido estado);

    @Query("""
        SELECT COALESCE(SUM(p.total), 0)
        FROM Pedido p
        WHERE p.estado = :estado
    """)
    BigDecimal sumarTotalPorEstado(
            @Param("estado") EstadoPedido estado
    );
}