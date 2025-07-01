package com.toiukha.itinerary.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ItineraryService {

    @Autowired
    private ItineraryRepository itineraryRepository;

    public List<ItineraryVO> findAll() {
        return itineraryRepository.findAll();
    }

    public Optional<ItineraryVO> findById(Integer itnId) {
        return itineraryRepository.findById(itnId);
    }

    public ItineraryVO save(ItineraryVO itineraryVO) {
        return itineraryRepository.save(itineraryVO);
    }

    public void deleteById(Integer itnId) {
        itineraryRepository.deleteById(itnId);
    }
}
