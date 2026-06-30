package com.carpinteria.service;

import java.io.IOException;
import java.io.InputStream;
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

    private static final long TAMANO_MAXIMO = 10 * 1024 * 1024;

    private static final Set<String> TIPOS_PERMITIDOS = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            ".jpg",
            ".jpeg",
            ".png",
            ".webp"
    );

    private final Path directorioImagenes;

    public ImagenService(
            @Value("${app.upload.dir}") String uploadDir) {

        if (uploadDir == null || uploadDir.isBlank()) {
            throw new IllegalStateException(
                    "La carpeta de imágenes no está configurada"
            );
        }

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

    // Validates and stores an uploaded image using a unique file name.
    public String guardar(MultipartFile archivo) {

        if (archivo == null || archivo.isEmpty()) {
            return null;
        }

        validarTamano(archivo);
        validarTipoContenido(archivo);

        String nombreOriginal = archivo.getOriginalFilename();

        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            throw new IllegalArgumentException(
                    "El archivo no tiene un nombre válido"
            );
        }

        String nombreSeguro = Paths.get(nombreOriginal)
                .getFileName()
                .toString();

        String extension = obtenerExtension(nombreSeguro);
        String nombreNuevo = UUID.randomUUID() + extension;

        Path destino = resolverRutaSegura(nombreNuevo);

        try (InputStream entrada = archivo.getInputStream()) {

            Files.copy(
                    entrada,
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

    // Deletes an image only when it belongs to the configured upload directory.
    public void eliminar(String nombreImagen) {

        if (nombreImagen == null || nombreImagen.isBlank()) {
            return;
        }

        if (!Paths.get(nombreImagen)
                .getFileName()
                .toString()
                .equals(nombreImagen)) {

            throw new IllegalArgumentException(
                    "El nombre de la imagen no es válido"
            );
        }

        Path archivo = resolverRutaSegura(nombreImagen);

        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "No se pudo eliminar la imagen",
                    e
            );
        }
    }

    private void validarTamano(MultipartFile archivo) {

        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException(
                    "La imagen no puede superar los 10 MB"
            );
        }
    }

    private void validarTipoContenido(MultipartFile archivo) {

        String tipoContenido = archivo.getContentType();

        if (tipoContenido == null
                || !TIPOS_PERMITIDOS.contains(tipoContenido)) {

            throw new IllegalArgumentException(
                    "Solo se permiten imágenes JPG, PNG o WEBP"
            );
        }
    }

    private Path resolverRutaSegura(String nombreArchivo) {

        Path ruta = directorioImagenes
                .resolve(nombreArchivo)
                .normalize();

        if (!ruta.startsWith(directorioImagenes)
                || !directorioImagenes.equals(ruta.getParent())) {

            throw new IllegalArgumentException(
                    "La ruta del archivo no es válida"
            );
        }

        return ruta;
    }

    private String obtenerExtension(String nombreArchivo) {

        int posicionPunto = nombreArchivo.lastIndexOf('.');

        if (posicionPunto < 0
                || posicionPunto == nombreArchivo.length() - 1) {

            throw new IllegalArgumentException(
                    "La imagen debe tener una extensión válida"
            );
        }

        String extension = nombreArchivo
                .substring(posicionPunto)
                .toLowerCase();

        if (!EXTENSIONES_PERMITIDAS.contains(extension)) {
            throw new IllegalArgumentException(
                    "La extensión de la imagen no está permitida"
            );
        }

        return extension;
    }
}