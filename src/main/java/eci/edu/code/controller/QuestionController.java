package eci.edu.code.controller;

import eci.edu.code.model.Question;
import eci.edu.code.model.User;
import eci.edu.code.service.CsrfTokenService;
import eci.edu.code.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import eci.edu.code.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

import org.owasp.encoder.Encode;


@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private UserService userService;
    @Autowired
    private CsrfTokenService csrfTokenService;

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/user/{username}")
    public List<Question> getQuestionsByUsername(@PathVariable String username) {
        return questionService.getQuestionsByUsername(username);
    }

    @PostMapping
    public ResponseEntity<String> createQuestion(@RequestBody Question question,
                                                 @RequestHeader("Authorization") String token,
                                                 @RequestHeader("X-CSRF-Token") String csrfToken) {
        // Obtén el nombre de usuario usando el token
        String username = userService.getUsernameFromToken(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
        Optional<User> userOptional = userService.getUserByUsername(username);
        User user = null;
        if (userOptional.isPresent()) {
            user = userOptional.get();
        }else {
            // Si no se encuentra el usuario, se devuelve un error
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (!csrfTokenService.validateToken(String.valueOf(user.getId()), csrfToken)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid CSRF token");
        }
        // Asigna el nombre de usuario a la pregunta
        question.setUsername(username);
        question.setText(sanitize(question.getText()));
        // Guarda la pregunta usando el servicio
        questionService.createQuestion(question);
        return ResponseEntity.ok("Question created successfully");
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }

    public String sanitize(String input) {
        return input == null ? null : Encode.forHtmlContent(input);
    }
}