package com.toiukha.favItn.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FavItnService {

    @Autowired
    private FavItnRepository favItnRepository;

    public List<FavItnVO> findAll() {
        return favItnRepository.findAll();
    }

    public Optional<FavItnVO> findById(FavItnId favItnId) {
        return favItnRepository.findById(favItnId);
    }

    public FavItnVO save(FavItnVO favItnVO) {
        return favItnRepository.save(favItnVO);
    }

    public void deleteById(FavItnId favItnId) {
        favItnRepository.deleteById(favItnId);
    }
}
