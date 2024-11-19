package eci.edu.code.service;

import eci.edu.code.model.Movement;
import eci.edu.code.repository.MovementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MovementService {
    @Autowired
    private MovementRepository movementRepository;

    public List<Movement> getAllMovementsByUserId(Long userId) {
        return movementRepository.findByUserId(userId);
    }

    public Optional<Movement> getMovementById(Long id) {
        return movementRepository.findById(id);
    }

    public Movement createMovement(Movement movement) {
        return movementRepository.save(movement);
    }

    public Movement updateMovement(Movement movement) {
        return movementRepository.save(movement);
    }

    public void deleteMovement(Long id) {
        movementRepository.deleteById(id);
    }
}