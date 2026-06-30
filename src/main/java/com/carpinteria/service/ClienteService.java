package com.carpinteria.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.carpinteria.model.Cliente;
import com.carpinteria.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    public List<Cliente> listarTodos() {
        return clienteRepository.findAllByOrderByNombreAsc();
    }

    public Cliente buscarPorId(Long id) {

        if (id == null) {
            throw new IllegalArgumentException(
                    "El identificador del cliente es obligatorio"
            );
        }

        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No se encontró el cliente"
                        )
                );
    }

    // Normalizes and validates client data before creating or updating it.
    @Transactional
    public Cliente guardar(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                    "Los datos del cliente son inválidos"
            );
        }

        normalizarDatos(cliente);
        validarDatos(cliente);
        validarCorreoDuplicado(cliente);

        return clienteRepository.save(cliente);
    }

    // Toggles the client's availability without deleting its historical data.
    @Transactional
    public void cambiarEstado(Long id) {

        Cliente cliente = buscarPorId(id);

        cliente.setActivo(
                !Boolean.TRUE.equals(cliente.getActivo())
        );

        clienteRepository.save(cliente);
    }

    private void validarDatos(Cliente cliente) {

        if (cliente.getNombre() == null
                || cliente.getNombre().isBlank()) {

            throw new IllegalArgumentException(
                    "El nombre del cliente es obligatorio"
            );
        }

        if (cliente.getCorreo() == null
                || cliente.getCorreo().isBlank()) {

            throw new IllegalArgumentException(
                    "El correo del cliente es obligatorio"
            );
        }

        if (cliente.getTelefono() == null
                || cliente.getTelefono().isBlank()) {

            throw new IllegalArgumentException(
                    "El teléfono del cliente es obligatorio"
            );
        }
    }

    private void normalizarDatos(Cliente cliente) {

        cliente.setNombre(
                normalizarTexto(cliente.getNombre())
        );

        if (cliente.getCorreo() != null) {
            cliente.setCorreo(
                    cliente.getCorreo()
                            .trim()
                            .toLowerCase()
            );
        }

        cliente.setTelefono(
                normalizarTexto(cliente.getTelefono())
        );

        cliente.setDireccion(
                normalizarTexto(cliente.getDireccion())
        );
    }

    // Prevents multiple clients from sharing the same email address.
    private void validarCorreoDuplicado(Cliente cliente) {

        boolean correoDuplicado;

        if (cliente.getId() == null) {
            correoDuplicado =
                    clienteRepository.existsByCorreoIgnoreCase(
                            cliente.getCorreo()
                    );
        } else {
            correoDuplicado =
                    clienteRepository
                            .existsByCorreoIgnoreCaseAndIdNot(
                                    cliente.getCorreo(),
                                    cliente.getId()
                            );
        }

        if (correoDuplicado) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente registrado con ese correo"
            );
        }
    }

    private String normalizarTexto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        return texto.trim();
    }
}