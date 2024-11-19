package eci.edu.code.service;

import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CsrfTokenService {


    private final ConcurrentHashMap<String, String> csrfTokens = new ConcurrentHashMap<>();

    // Generar un token CSRF único
    public String generateToken(String sessionId) {
        String token = UUID.randomUUID().toString();
        csrfTokens.put(sessionId, token);
        return token;
    }

    // Validar el token CSRF
    public boolean validateToken(String sessionId, String token) {
        return token != null && token.equals(csrfTokens.get(sessionId));
    }

    // Eliminar token CSRF después de validación
    public void invalidateToken(String sessionId) {
        csrfTokens.remove(sessionId);
    }
}