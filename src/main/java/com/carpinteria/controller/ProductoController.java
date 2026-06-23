package com.carpinteria.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.carpinteria.model.Producto;
import com.carpinteria.service.ImagenService;
import com.carpinteria.service.ProductoService;

@Controller
@RequestMapping
public class ProductoController {

    private final ProductoService productoService;
    private final ImagenService imagenService;

    public ProductoController(
            ProductoService productoService,
            ImagenService imagenService) {

        this.productoService = productoService;
        this.imagenService = imagenService;
    }

    @GetMapping("/catalogo")
    public String mostrarCatalogo(
            @RequestParam(required = false) String buscar,
            @RequestParam(required = false) String categoria,
            Model model) {

        List<Producto> productos;

        if (buscar != null && !buscar.isBlank()) {
            productos = productoService.buscarPorNombre(buscar);

        } else if (categoria != null && !categoria.isBlank()) {
            productos = productoService.buscarPorCategoria(categoria);

        } else {
            productos = productoService.listarActivos();
        }

        productos = productos.stream()
                .filter(producto ->
                        Boolean.TRUE.equals(producto.getActivo()))
                .toList();

        model.addAttribute("productos", productos);
        model.addAttribute("buscar", buscar);
        model.addAttribute(
                "categoriaSeleccionada",
                categoria);

        return "catalogo";
    }

    @GetMapping("/admin/productos")
    public String listarProductos(Model model) {

        model.addAttribute(
                "productos",
                productoService.listarTodos());

        return "admin/productos";
    }

    @GetMapping("/admin/productos/nuevo")
    public String mostrarFormularioNuevo(Model model) {

        Producto producto = new Producto();
        producto.setActivo(true);

        model.addAttribute("producto", producto);
        model.addAttribute(
                "titulo",
                "Registrar producto");
        model.addAttribute(
                "accion",
                "/admin/productos/guardar");

        return "admin/producto-formulario";
    }

    @PostMapping("/admin/productos/guardar")
    public String guardarProducto(
            Producto producto,
            @RequestParam(
                    name = "imagenArchivo",
                    required = false)
            MultipartFile imagenArchivo,
            Model model) {

        String imagenGuardada = null;

        try {
            imagenGuardada =
                    imagenService.guardar(imagenArchivo);

            producto.setImagen(imagenGuardada);

            productoService.guardar(producto);

            return "redirect:/admin/productos";

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            /*
             * Si el archivo se guardó, pero luego ocurrió
             * un error al guardar el producto, se elimina
             * para evitar imágenes huérfanas.
             */
            if (imagenGuardada != null) {
                try {
                    imagenService.eliminar(imagenGuardada);
                } catch (RuntimeException ignored) {
                    // Se conserva el error original.
                }
            }

            cargarFormularioNuevo(
                    model,
                    producto,
                    e.getMessage());

            return "admin/producto-formulario";
        }
    }

    @GetMapping("/admin/productos/editar/{id}")
    public String mostrarFormularioEditar(
            @PathVariable Long id,
            Model model) {

        Producto producto = productoService.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe"));

        model.addAttribute("producto", producto);
        model.addAttribute(
                "titulo",
                "Editar producto");
        model.addAttribute(
                "accion",
                "/admin/productos/actualizar/" + id);

        return "admin/producto-formulario";
    }

    @PostMapping("/admin/productos/actualizar/{id}")
    public String actualizarProducto(
            @PathVariable Long id,
            Producto producto,
            @RequestParam(
                    name = "imagenArchivo",
                    required = false)
            MultipartFile imagenArchivo,
            Model model) {

        Producto productoExistente =
                productoService.buscarPorId(id)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El producto no existe"));

        String imagenAnterior =
                productoExistente.getImagen();

        String imagenNueva = null;

        try {
            /*
             * Si el usuario seleccionó una imagen nueva,
             * se guarda. Si no, se conserva la anterior.
             */
            if (imagenArchivo != null
                    && !imagenArchivo.isEmpty()) {

                imagenNueva =
                        imagenService.guardar(imagenArchivo);

                producto.setImagen(imagenNueva);

            } else {
                producto.setImagen(imagenAnterior);
            }

            productoService.actualizar(id, producto);

            /*
             * La imagen anterior se elimina solamente
             * después de que la actualización en MySQL
             * fue realizada correctamente.
             */
            if (imagenNueva != null
                    && imagenAnterior != null
                    && !imagenAnterior.isBlank()) {

                try {
                    imagenService.eliminar(imagenAnterior);
                } catch (RuntimeException ignored) {
                    /*
                     * El producto ya fue actualizado.
                     * No se cancela la operación si no se
                     * pudo borrar el archivo anterior.
                     */
                }
            }

            return "redirect:/admin/productos";

        } catch (IllegalArgumentException
                | IllegalStateException e) {

            /*
             * Si la imagen nueva se guardó, pero la
             * actualización falló, se elimina.
             */
            if (imagenNueva != null) {
                try {
                    imagenService.eliminar(imagenNueva);
                } catch (RuntimeException ignored) {
                    // Se conserva el error original.
                }
            }

            producto.setId(id);
            producto.setImagen(imagenAnterior);

            cargarFormularioEditar(
                    model,
                    producto,
                    id,
                    e.getMessage());

            return "admin/producto-formulario";
        }
    }

    @PostMapping("/admin/productos/estado/{id}")
    public String cambiarEstado(
            @PathVariable Long id) {

        productoService.cambiarEstado(id);

        return "redirect:/admin/productos";
    }

    @PostMapping("/admin/productos/eliminar/{id}")
    public String eliminarProducto(
            @PathVariable Long id) {

        Producto producto = productoService.buscarPorId(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "El producto no existe"));

        String imagen = producto.getImagen();

        /*
         * Primero se elimina el registro de MySQL.
         */
        productoService.eliminar(id);

        /*
         * Después se elimina el archivo físico.
         */
        if (imagen != null && !imagen.isBlank()) {
            imagenService.eliminar(imagen);
        }

        return "redirect:/admin/productos";
    }

    private void cargarFormularioNuevo(
            Model model,
            Producto producto,
            String mensajeError) {

        model.addAttribute("producto", producto);
        model.addAttribute(
                "titulo",
                "Registrar producto");
        model.addAttribute(
                "accion",
                "/admin/productos/guardar");
        model.addAttribute(
                "error",
                mensajeError);
    }

    private void cargarFormularioEditar(
            Model model,
            Producto producto,
            Long id,
            String mensajeError) {

        model.addAttribute("producto", producto);
        model.addAttribute(
                "titulo",
                "Editar producto");
        model.addAttribute(
                "accion",
                "/admin/productos/actualizar/" + id);
        model.addAttribute(
                "error",
                mensajeError);
    }
}