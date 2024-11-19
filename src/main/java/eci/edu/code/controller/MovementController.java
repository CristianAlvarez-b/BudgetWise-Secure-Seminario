package eci.edu.code.controller;

import eci.edu.code.model.Movement;
import eci.edu.code.model.MovementDTO;
import eci.edu.code.model.User;
import eci.edu.code.service.MovementService;
import eci.edu.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/movements")
public class MovementController {
    @Autowired
    private MovementService movementService;

    @Autowired
    private UserService userService;

    @GetMapping("/")
    public ResponseEntity<List<MovementDTO>> getMovements(@RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = getUserIdFromToken(token);
        List<Movement> movements = movementService.getAllMovementsByUserId(userId); // Obtén los movimientos del usuario

        // Convierte cada Movement a MovementDTO
        List<MovementDTO> movementDTOs = movements.stream()
                .map(movement -> new MovementDTO(
                        movement.getId(),
                        movement.getName(),
                        movement.getValue(),
                        movement.getDate(),
                        movement.getType()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(movementDTOs);
    }

    @PostMapping("/")
    public ResponseEntity<MovementDTO> createMovement(@RequestBody Movement movement, @RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        Long userId = getUserIdFromToken(token);
        return userService.getUserById(userId)
                .map(user -> {
                    movement.setUser(user);
                    Movement createdMovement = movementService.createMovement(movement);
                    // Convierte el movimiento creado a MovementDTO
                    MovementDTO movementDTO = new MovementDTO(
                            createdMovement.getId(),
                            createdMovement.getName(),
                            createdMovement.getValue(),
                            createdMovement.getDate(),
                            createdMovement.getType()
                    );
                    return ResponseEntity.ok(movementDTO);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Movement> updateMovement(@PathVariable Long id, @RequestBody Movement movement, @RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        return movementService.getMovementById(id)
                .map(existingMovement -> {
                    movement.setId(existingMovement.getId());
                    movement.setUser(existingMovement.getUser());
                    Movement updatedMovement = movementService.updateMovement(movement);
                    return ResponseEntity.ok(updatedMovement);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMovement(@PathVariable Long id, @RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).build();
        }

        movementService.deleteMovement(id);
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
        // Aquí deberías implementar la lógica para obtener el ID del usuario a partir del token
        // Suponiendo que el token es el ID del usuario (esto es un ejemplo, ajusta según tu implementación)
        return userService.getAllUsers().stream()
                .filter(user -> user.getToken() != null && user.getToken().equals(token))
                .map(User::getId)
                .findFirst()
                .orElse(null);
    }
    @PostMapping("/transfer")
    public ResponseEntity<String> transferMoney(
            @RequestParam String targetUsername,
            @RequestParam Double amount,
            @RequestHeader("Authorization") String token) {
        if (!validateToken(token)) {
            return ResponseEntity.status(401).body("Invalid token");
        }

        Long sourceUserId = getUserIdFromToken(token);

        return userService.getUserById(sourceUserId).flatMap(sourceUser ->
                userService.getUserByUsername(targetUsername).map(targetUser -> {
                    if (sourceUser.getId().equals(targetUser.getId())) {
                        return ResponseEntity.status(400).body("Cannot transfer to the same account");
                    }

                    // Crear movimiento outcome para el usuario fuente
                    Movement outcome = new Movement();
                    outcome.setName("Transfer to user " + targetUser.getUsername());
                    outcome.setValue(amount);
                    outcome.setType("transferOut");
                    outcome.setUser(sourceUser);
                    outcome.setDate(LocalDateTime.now());

                    movementService.createMovement(outcome);

                    // Crear movimiento income para el usuario destino
                    Movement income = new Movement();
                    income.setName("Transfer from user " + sourceUser.getUsername());
                    income.setValue(amount);
                    income.setType("transferIn");
                    income.setUser(targetUser);
                    income.setDate(LocalDateTime.now());

                    movementService.createMovement(income);

                    return ResponseEntity.ok("Transfer successful");
                })
        ).orElse(ResponseEntity.status(404).body("Target user not found"));
    }

}