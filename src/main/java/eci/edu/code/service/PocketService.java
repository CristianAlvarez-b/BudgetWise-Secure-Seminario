package eci.edu.code.service;

import eci.edu.code.model.Pocket;
import eci.edu.code.repository.PocketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PocketService {
    private final PocketRepository pocketRepository;

    @Autowired
    public PocketService(PocketRepository pocketRepository) {
        this.pocketRepository = pocketRepository;
    }

    public List<Pocket> findAll() {
        return pocketRepository.findAll();
    }
    public List<Pocket> findAllByUserId(Long userId){
        return pocketRepository.findByUserId(userId);
    }
    public Optional<Pocket> findById(Long id) {
        return pocketRepository.findById(id);
    }

    public Pocket save(Pocket pocket) {
        return pocketRepository.save(pocket);
    }

    public void deleteById(Long id) {
        pocketRepository.deleteById(id);
    }
}