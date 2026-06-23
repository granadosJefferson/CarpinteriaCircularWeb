package com.carpinteria.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagenService {

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private final Path directorioImagenes;

    public ImagenService(@Value("${app.upload.dir}") String uploadDir) {
        this.directorioImagenes = Paths.get(uploadDir)
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(directorioImagenes);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo crear la carpeta de imágenes",
                    e
            );
        }
    }

    public String guardar(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        if (!TIPOS_PERMITIDOS.contains(archivo.getContentType())) {
            throw new IllegalArgumentException(
                    "Solo se permiten imágenes JPG, PNG o WEBP"
            );
        }

        String nombreOriginal = archivo.getOriginalFilename();

        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo no tiene un nombre válido"
            );
        }

        String extension = obtenerExtension(nombreOriginal);
        String nombreNuevo = UUID.randomUUID() + extension;

        Path destino = directorioImagenes
                .resolve(nombreNuevo)
                .normalize();

        if (!destino.getParent().equals(directorioImagenes)) {
            throw new IllegalArgumentException(
                    "La ruta del archivo no es válida"
            );
        }

        try {
            Files.copy(
                    archivo.getInputStream(),
                    destino,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return nombreNuevo;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo guardar la imagen",
                    e
            );
        }
    }

    public void eliminar(String nombreImagen) {

        if (nombreImagen == null || nombreImagen.isBlank()) {
            return;
        }

        Path archivo = directorioImagenes
                .resolve(nombreImagen)
                .normalize();

        if (!archivo.getParent().equals(directorioImagenes)) {
            throw new IllegalArgumentException(
                    "La ruta de la imagen no es válida"
            );
        }

        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo eliminar la imagen",
                    e
            );
        }
    }

    private String obtenerExtension(String nombreArchivo) {

        int posicionPunto = nombreArchivo.lastIndexOf(".");

        if (posicionPunto < 0) {
            throw new IllegalArgumentException(
                    "La imagen debe tener una extensión válida"
            );
        }

        String extension = nombreArchivo
                .substring(posicionPunto)
                .toLowerCase();

        if (!Set.of(".jpg", ".jpeg", ".png", ".webp")
                .contains(extension)) {
            throw new IllegalArgumentException(
                    "La extensión de la imagen no está permitida"
            );
        }

        return extension;
    }
}