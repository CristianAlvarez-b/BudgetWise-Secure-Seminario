package eci.edu.code.controller;

import eci.edu.code.model.User;
import eci.edu.code.model.UserLoginDto;
import eci.edu.code.service.CsrfTokenService;
import eci.edu.code.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private CsrfTokenService csrfTokenService;



    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody UserLoginDto userLoginDto) {
        String token = userService.validateUser(userLoginDto.getUsername(), userLoginDto.getPassword());

        if (token != null) {
            // Obtener el usuario por nombre de usuario de manera segura
            Optional<User> userOptional = userService.getUserByUsername(userLoginDto.getUsername());

            if (userOptional.isPresent()) {
                User user = userOptional.get();  // Obtener el usuario
                String csrfToken = csrfTokenService.generateToken(String.valueOf(user.getId()));

                return ResponseEntity.ok(Map.of(
                        "jwt", token,
                        "csrfToken", csrfToken
                ));
            } else {
                // Si no se encuentra el usuario, se devuelve un error
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } else {
            // Si el token JWT es nulo (credenciales inválidas), retornar error
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        String username = userService.getUsernameFromToken(token);
        csrfTokenService.invalidateToken(username);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody User user) {
        try {
            userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body("User created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}