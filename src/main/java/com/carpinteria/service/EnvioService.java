package com.carpinteria.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpinteria.model.Envio;
import com.carpinteria.model.EstadoEnvio;
import com.carpinteria.model.EstadoPedido;
import com.carpinteria.model.Pedido;
import com.carpinteria.repository.EnvioRepository;
import com.carpinteria.repository.PedidoRepository;

@Service
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final PedidoRepository pedidoRepository;

    public EnvioService(
            EnvioRepository envioRepository,
            PedidoRepository pedidoRepository) {

        this.envioRepository = envioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public List<Envio> listarTodos() {
        return envioRepository.findAllByOrderByFechaRegistroDesc();
    }

    public List<Envio> listarPorEstado(EstadoEnvio estado) {

        if (estado == null) {
            return listarTodos();
        }

        return envioRepository
                .findByEstadoOrderByFechaRegistroDesc(estado);
    }

    public Envio buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del envío es obligatorio"
            );
        }

        return envioRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el envío"
                        )
                );
    }

    public Envio buscarPorPedido(Long pedidoId) {

        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "El identificador del pedido es obligatorio"
            );
        }

        return envioRepository
                .findByPedidoId(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El pedido no tiene un envío registrado"
                        )
                );
    }

    @Transactional
    public Envio crearEnvio(
            Long pedidoId,
            Envio envio) {

        if (pedidoId == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un pedido"
            );
        }

        if (envio == null) {
            throw new IllegalArgumentException(
                    "Los datos del envío son inválidos"
            );
        }

        Pedido pedido = pedidoRepository
                .findById(pedidoId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el pedido"
                        )
                );

        if (envioRepository.existsByPedidoId(pedidoId)) {
            throw new IllegalArgumentException(
                    "El pedido ya tiene un envío registrado"
            );
        }

        if (pedido.getEstado() == EstadoPedido.CANCELADO) {
            throw new IllegalArgumentException(
                    "No se puede crear un envío para un pedido cancelado"
            );
        }

        validarDatos(envio);
        normalizarDatos(envio);

        envio.setId(null);
        envio.setPedido(pedido);
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envio.setFechaEnvio(null);
        envio.setFechaEntrega(null);

        return envioRepository.save(envio);
    }

    @Transactional
    public Envio actualizarEnvio(
            Long id,
            Envio datos) {

        Envio envio = buscarPorId(id);

        validarDatos(datos);
        normalizarDatos(datos);

        envio.setDireccionEntrega(
                datos.getDireccionEntrega()
        );

        envio.setTransportista(
                datos.getTransportista()
        );

        envio.setNumeroSeguimiento(
                datos.getNumeroSeguimiento()
        );

        envio.setObservaciones(
                datos.getObservaciones()
        );

        return envioRepository.save(envio);
    }

    @Transactional
    public Envio cambiarEstado(
            Long id,
            EstadoEnvio nuevoEstado) {

        Envio envio = buscarPorId(id);

        if (nuevoEstado == null) {
            throw new IllegalArgumentException(
                    "Debe seleccionar un estado válido"
            );
        }

        EstadoEnvio estadoActual = envio.getEstado();

        if (estadoActual == nuevoEstado) {
            return envio;
        }

        if (envio.getPedido().getEstado() == EstadoPedido.CANCELADO
                && nuevoEstado != EstadoEnvio.CANCELADO) {

            throw new IllegalArgumentException(
                    "No se puede procesar el envío porque el pedido está cancelado"
            );
        }

        validarTransicion(
                estadoActual,
                nuevoEstado
        );

        if (nuevoEstado == EstadoEnvio.EN_CAMINO
                && envio.getFechaEnvio() == null) {

            envio.setFechaEnvio(LocalDateTime.now());
        }

        if (nuevoEstado == EstadoEnvio.ENTREGADO) {

            if (envio.getFechaEnvio() == null) {
                envio.setFechaEnvio(LocalDateTime.now());
            }

            envio.setFechaEntrega(LocalDateTime.now());
        }

        if (nuevoEstado == EstadoEnvio.CANCELADO) {
            envio.setFechaEntrega(null);
        }

        envio.setEstado(nuevoEstado);

        return envioRepository.save(envio);
    }

    public long contarTodos() {
        return envioRepository.count();
    }

    public long contarPorEstado(EstadoEnvio estado) {

        if (estado == null) {
            return 0;
        }

        return envioRepository.countByEstado(estado);
    }

    private void validarDatos(Envio envio) {

        if (envio == null) {
            throw new IllegalArgumentException(
                    "Los datos del envío son inválidos"
            );
        }

        if (envio.getDireccionEntrega() == null
                || envio.getDireccionEntrega().isBlank()) {

            throw new IllegalArgumentException(
                    "La dirección de entrega es obligatoria"
            );
        }

        validarLongitud(
                envio.getDireccionEntrega(),
                300,
                "La dirección no puede superar los 300 caracteres"
        );

        validarLongitud(
                envio.getTransportista(),
                120,
                "El transportista no puede superar los 120 caracteres"
        );

        validarLongitud(
                envio.getNumeroSeguimiento(),
                100,
                "El número de seguimiento no puede superar los 100 caracteres"
        );

        validarLongitud(
                envio.getObservaciones(),
                500,
                "Las observaciones no pueden superar los 500 caracteres"
        );
    }

    private void validarTransicion(
            EstadoEnvio estadoActual,
            EstadoEnvio nuevoEstado) {

        if (estadoActual == null) {
            throw new IllegalArgumentException(
                    "El envío no tiene un estado válido"
            );
        }

        boolean transicionPermitida = switch (estadoActual) {

            case PENDIENTE ->
                    nuevoEstado == EstadoEnvio.PREPARANDO
                    || nuevoEstado == EstadoEnvio.CANCELADO;

            case PREPARANDO ->
                    nuevoEstado == EstadoEnvio.EN_CAMINO
                    || nuevoEstado == EstadoEnvio.CANCELADO;

            case EN_CAMINO ->
                    nuevoEstado == EstadoEnvio.ENTREGADO
                    || nuevoEstado == EstadoEnvio.CANCELADO;

            case ENTREGADO, CANCELADO -> false;
        };

        if (!transicionPermitida) {
            throw new IllegalArgumentException(
                    "No se puede cambiar el envío de "
                            + estadoActual
                            + " a "
                            + nuevoEstado
            );
        }
    }

    private void validarLongitud(
            String texto,
            int longitudMaxima,
            String mensaje) {

        if (texto != null
                && texto.trim().length() > longitudMaxima) {

            throw new IllegalArgumentException(mensaje);
        }
    }

    private void normalizarDatos(Envio envio) {

        envio.setDireccionEntrega(
                envio.getDireccionEntrega().trim()
        );

        envio.setTransportista(
                normalizarTexto(envio.getTransportista())
        );

        envio.setNumeroSeguimiento(
                normalizarTexto(envio.getNumeroSeguimiento())
        );

        envio.setObservaciones(
                normalizarTexto(envio.getObservaciones())
        );
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}