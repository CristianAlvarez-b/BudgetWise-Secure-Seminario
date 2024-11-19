package eci.edu.code.repository;

import eci.edu.code.model.Movement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovementRepository extends JpaRepository<Movement, Long> {
    List<Movement> findByUserId(Long userId);
}