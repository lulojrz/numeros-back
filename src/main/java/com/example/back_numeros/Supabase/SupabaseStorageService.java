package com.example.back_numeros.Supabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.service-key}")
    private String serviceKey;

    @Value("${supabase.bucket}")
    private String bucket;

    private final RestClient restClient = RestClient.create();

    public String subirImagen(MultipartFile file, String numeroTerritorio) throws IOException {
        // 1. Generar un nombre de archivo único
        String originalFilename = file.getOriginalFilename();
        String extension = (originalFilename != null && originalFilename.contains("."))
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        String fileName = "territorio-" + numeroTerritorio + "-" + UUID.randomUUID() + extension;

        // 2. Endpoint REST de Supabase Storage para subir archivos
        String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucket + "/" + fileName;

        // 3. Ejecutar el POST enviando los bytes de la imagen
        restClient.post()
                .uri(uploadUrl)
                .header("Authorization", "Bearer " + serviceKey)
                .header("apikey", serviceKey)
                .header("x-upsert", "true")
                .contentType(MediaType.parseMediaType(
                        file.getContentType() != null ? file.getContentType() : "image/jpeg"
                ))
                .body(file.getBytes())
                .retrieve()
                .toBodilessEntity();

        // 4. Devolver la URL pública lista para guardar en SQL
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
    }
}