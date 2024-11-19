package eci.edu.code.service;

import java.util.List;
import java.util.Optional;

import eci.edu.code.model.User;
import eci.edu.code.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void createUser(User user) {
        // Verificar si el username ya existe
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("The username is already in use, please try a new one.");
        }
        // Si no existe, crea el usuario
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    public Optional<User> getUserByUsernameAndPassword(String username, String password) {
        return userRepository.findByUsernameAndPassword(username, password);
    }
    public String validateUser(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(password)) {
                // Generar un token único usando UUID
                String token = UUID.randomUUID().toString();
                user.setToken(token);
                userRepository.save(user); // Guarda el token en la base de datos
                return token;
            }
        }
        return null; // Usuario no encontrado o contraseña incorrecta
    }
    public String getUsernameFromToken(String token) {
        // Busca el usuario con el token proporcionado
        Optional<User> user = userRepository.findByToken(token);

        // Retorna el nombre de usuario si el usuario fue encontrado, de lo contrario, retorna null
        return user.map(User::getUsername).orElse(null);
    }
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}