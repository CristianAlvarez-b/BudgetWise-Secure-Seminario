package eci.edu.code.controller;

import eci.edu.code.model.Answer;
import eci.edu.code.service.AnswerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import eci.edu.code.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
public class AnswerController {

    @Autowired
    private AnswerService answerService;
    @Autowired
    private UserService userService;

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
    public ResponseEntity<String> createAnswer(@RequestHeader("Authorization") String token, @RequestBody Answer answer) {
        String username = userService.getUsernameFromToken(token);  // Obtener el username usando el token
        if (username != null) {
            answer.setUsername(username);  // Establecer el username en la respuesta
            answerService.createAnswer(answer);  // Crear la respuesta
            return ResponseEntity.status(HttpStatus.CREATED).body("Answer created successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/{id}")
    public void deleteAnswer(@PathVariable Long id) {
        answerService.deleteAnswer(id);
    }
}