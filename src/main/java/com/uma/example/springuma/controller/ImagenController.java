package com.uma.example.springuma.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.uma.example.springuma.model.Imagen;
import com.uma.example.springuma.model.ImagenService;
import com.uma.example.springuma.model.Paciente;

@RestController
public class ImagenController {

    @Autowired
    private ImagenService imagenService;
    private static final Logger logger = LoggerFactory.getLogger(ImagenController.class);

    @GetMapping("/imagen/{id}")
    public ResponseEntity<?> downloadImage(@PathVariable long id) {
        try {
            byte[] imageData = imagenService.downloadImage(id);
            return ResponseEntity.ok()
                    .contentType(MediaType.valueOf(
                            "image/png"
                    ))
                    .body(imageData);
        } catch (IOException e) {
            logger.error("Error downloading image id {}", id, e);
            return ResponseEntity.internalServerError().body("Error al descargar la imagen: " + e.getMessage());
        }
    }

    @GetMapping("/imagen/info/{id}")
    public Imagen getImagen(@PathVariable("id") Long id) {
        return imagenService.getImagen(id);
    }

    @GetMapping("/imagen/predict/{id}")
    public ResponseEntity<?> getImagenPrediction(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(imagenService.getNewPrediccion(id));
        } catch (Exception e) {
            logger.error("Error generating prediction for image id {}", id, e);
            return ResponseEntity.internalServerError().body("Error al realizar la prediccion: " + e.getMessage());
        }

    }

    @PostMapping(value = "/imagen", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<?> uploadImage(@RequestPart("image") MultipartFile file,
            @RequestPart("paciente") Paciente paciente) throws IOException {
        String uploadImage = imagenService.uploadImage(file, paciente);
        return ResponseEntity.ok(uploadImage);
    }

    @GetMapping("/imagen/paciente/{id}")
    public List<Imagen> getImagenes(@PathVariable("id") Long id) {
        return imagenService.getImagenesPaciente(id);
    }

    @DeleteMapping("/imagen/{id}")
    public ResponseEntity<?> deleteCuenta(@PathVariable("id") Long id) {
        try {
            imagenService.removeImagenByID(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            logger.error("Error deleting image id {}", id, e);
            return ResponseEntity.internalServerError().body("Error al eliminar la imagen");
        }
    }
}
