package com.mikeldi.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class GamesApiController {

    @Value("${rawg.api.key}")
    private String rawgApiKey;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/games/{slug}")
    public ResponseEntity<String> getGameInfo(@PathVariable String slug) {
        String url = "https://api.rawg.io/api/games/" + slug + "?key=" + rawgApiKey;
        try {
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("{\"error\": \"No se pudo obtener la información del juego\"}");
        }
    }

    // NUEVO: búsqueda por nombre
    @GetMapping("/games/search")
    public ResponseEntity<String> searchGames(@RequestParam String q) {
        String url = "https://api.rawg.io/api/games?key=" + rawgApiKey + "&search=" + q + "&page_size=6";
        try {
            String response = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("{\"error\": \"No se pudo realizar la búsqueda\"}");
        }
    }
}
