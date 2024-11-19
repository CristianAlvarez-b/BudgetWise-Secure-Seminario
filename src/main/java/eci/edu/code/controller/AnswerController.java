package eci.edu.code.controller;

import eci.edu.code.model.Answer;
import eci.edu.code.model.User;
import eci.edu.code.service.AnswerService;
import eci.edu.code.service.CsrfTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import eci.edu.code.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.owasp.encoder.Encode;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;
    @Autowired
    private UserService userService;
    @Autowired
    private CsrfTokenService csrfTokenService;

    @GetMapping
    public List<Answer> getAllAnswers() {
        return answerService.getAllAnswers();
    }

    @GetMapping("/{questionId}")
    public List<Answer> getAnswersByQuestionId(@PathVariable Long questionId) {
        return answerService.getAnswersByQuestionId(questionId);
    }

    @GetMapping("/user/{username}")
    public List<Answer> getAnswersByUsername(@PathVariable String username) {
        return answerService.getAnswersByUsername(username);
    }

    @PostMapping
    public ResponseEntity<String> createAnswer( @RequestHeader("Authorization") String token,
                                                @RequestHeader("X-CSRF-Token") String csrfToken,
                                                @RequestBody Answer answer) {
        String username = userService.getUsernameFromToken(token);  // Obtener el username usando el token
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<User> userOptional = userService.getUserByUsername(username);
        User user = null;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        }else {
            // Si no se encuentra el usuario, se devuelve un error
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validar el token CSRF
        if (!csrfTokenService.validateToken(String.valueOf(user.getId()), csrfToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid CSRF token");
        }
        answer.setUsername(username);  // Establecer el username en la respuesta
        answer.setText(sanitize(answer.getText()));
        answerService.createAnswer(answer);  // Crear la respuesta
        return ResponseEntity.status(HttpStatus.CREATED).body("Answer created successfully");
    }

    @DeleteMapping("/{id}")
    public void deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
    }
    public String sanitize(String input) {
        return input == null ? null : Encode.forHtmlContent(input);
    }
}