package eci.edu.code.controller;

import eci.edu.code.model.Pocket;
import eci.edu.code.model.PocketDTO;
import eci.edu.code.model.User;
import eci.edu.code.service.PocketService;
import org.springframework.beans.factory.annotation.Autowired;
import eci.edu.code.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/pockets")
public class PocketController {
    private final PocketService pocketService;

    @Autowired
    public PocketController(PocketService pocketService) {
        this.pocketService = pocketService;
    }
    @Autowired
    private UserService userService;
    @GetMapping
    public List<Pocket> getAllPockets() {
        return pocketService.findAll();
    }

    @GetMapping("/")
    public ResponseEntity<List<PocketDTO>> getPockets(@RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = getUserIdFromToken(token);
        List<Pocket> pockets = pocketService.findAllByUserId(userId); // Obtén los pockets del usuario
        List<PocketDTO> pocketsDTOs = pockets.stream()
                .map(pocket -> new PocketDTO(
                        pocket.getId(),
                        pocket.getName(),
                        pocket.getValue(),
                        pocket.getColor()
                )).collect(Collectors.toList());

        return ResponseEntity.ok(pocketsDTOs);
    }

    @PostMapping("/")
    public ResponseEntity<PocketDTO> createPocket(@RequestBody Pocket pocket, @RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = getUserIdFromToken(token);
        return userService.getUserById(userId)
                .map(user -> {
                    pocket.setUser(user);
                    Pocket createdPocket = pocketService.save(pocket);
                    PocketDTO pocketDTO = new PocketDTO(
                            createdPocket.getId(),
                            createdPocket.getName(),
                            createdPocket.getValue(),
                            createdPocket.getColor()
                    );
                    return ResponseEntity.ok(pocketDTO);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pocket> updatePocket(@PathVariable Long id, @RequestBody Pocket pocket, @RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        return pocketService.findById(id)
                .map(existingPocket -> {
                    pocket.setId(existingPocket.getId());
                    pocket.setUser(existingPocket.getUser());
                    Pocket updatedPocket = pocketService.save(pocket);
                    return ResponseEntity.ok(updatedPocket);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePocket(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        pocketService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // Método para validar el token
    private boolean validateToken(String token) {
        if (token == null || token.isEmpty()) {
            return false; // Token vacío o nulo
        }

        // Busca un usuario que tenga el token proporcionado
        return userService.getAllUsers().stream()
                .anyMatch(user -> user.getToken() != null && user.getToken().equals(token));
    }

    // Método para obtener el ID del usuario desde el token
    private Long getUserIdFromToken(String token) {
        return userService.getAllUsers().stream()
                .filter(user -> user.getToken() != null && user.getToken().equals(token))
                .map(User::getId)
                .findFirst()
                .orElse(null);
    }
}