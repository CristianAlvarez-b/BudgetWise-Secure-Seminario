package eci.edu.code.repository;

import eci.edu.code.model.Pocket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PocketRepository extends JpaRepository<Pocket, Long> {
    List<Pocket> findByUserId(Long userId);
}