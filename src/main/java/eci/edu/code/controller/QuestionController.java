package eci.edu.code.controller;

import eci.edu.code.model.Question;
import eci.edu.code.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import eci.edu.code.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    @Autowired
    private QuestionService questionService;
    @Autowired
    private UserService userService;

    @GetMapping
    public List<Question> getAllQuestions() {
        return questionService.getAllQuestions();
    }

    @GetMapping("/user/{username}")
    public List<Question> getQuestionsByUsername(@PathVariable String username) {
        return questionService.getQuestionsByUsername(username);
    }

    @PostMapping
    public ResponseEntity<String> createQuestion(@RequestBody Question question, @RequestHeader("Authorization") String token) {
        // Obtén el nombre de usuario usando el token
        String username = userService.getUsernameFromToken(token);

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }

        // Asigna el nombre de usuario a la pregunta
        question.setUsername(username);

        // Guarda la pregunta usando el servicio
        questionService.createQuestion(question);
        return ResponseEntity.ok("Question created successfully");
    }

    @DeleteMapping("/{id}")
    public void deleteQuestion(@PathVariable Long id) {
        questionService.deleteQuestion(id);
    }
}