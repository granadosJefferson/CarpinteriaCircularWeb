package com.carpinteria.service;

import java.util.List;

import org.springframework.stereotype.Service;

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
        return clienteRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("No se encontró el cliente"));
    }

    public Cliente guardar(Cliente cliente) {
        normalizarDatos(cliente);
        validarCorreoDuplicado(cliente);

        return clienteRepository.save(cliente);
    }

    public void cambiarEstado(Long id) {
        Cliente cliente = buscarPorId(id);
        cliente.setActivo(!Boolean.TRUE.equals(cliente.getActivo()));
        clienteRepository.save(cliente);
    }

    private void normalizarDatos(Cliente cliente) {
        if (cliente.getNombre() != null) {
            cliente.setNombre(cliente.getNombre().trim());
        }

        if (cliente.getCorreo() != null) {
            cliente.setCorreo(cliente.getCorreo().trim().toLowerCase());
        }

        if (cliente.getTelefono() != null) {
            cliente.setTelefono(cliente.getTelefono().trim());
        }

        if (cliente.getDireccion() != null) {
            cliente.setDireccion(cliente.getDireccion().trim());
        }
    }

    private void validarCorreoDuplicado(Cliente cliente) {
        boolean correoDuplicado;

        if (cliente.getId() == null) {
            correoDuplicado =
                    clienteRepository.existsByCorreoIgnoreCase(
                            cliente.getCorreo());
        } else {
            correoDuplicado =
                    clienteRepository.existsByCorreoIgnoreCaseAndIdNot(
                            cliente.getCorreo(),
                            cliente.getId());
        }

        if (correoDuplicado) {
            throw new IllegalArgumentException(
                    "Ya existe un cliente registrado con ese correo");
        }
    }
}