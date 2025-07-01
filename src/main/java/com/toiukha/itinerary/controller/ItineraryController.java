package com.toiukha.itinerary.controller;

import com.toiukha.itinerary.model.ItineraryService;
import com.toiukha.itinerary.model.ItineraryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/itinerary")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;

    @GetMapping
    public List<ItineraryVO> getAllItineraries() {
        return itineraryService.findAll();
    }

    @GetMapping("/{itnId}")
    public ResponseEntity<ItineraryVO> getItineraryById(@PathVariable Integer itnId) {
        Optional<ItineraryVO> itinerary = itineraryService.findById(itnId);
        return itinerary.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ItineraryVO createItinerary(@RequestBody ItineraryVO itineraryVO) {
        return itineraryService.save(itineraryVO);
    }

    @PutMapping("/{itnId}")
    public ResponseEntity<ItineraryVO> updateItinerary(@PathVariable Integer itnId, @RequestBody ItineraryVO itineraryDetails) {
        Optional<ItineraryVO> optionalItinerary = itineraryService.findById(itnId);
        if (optionalItinerary.isPresent()) {
            ItineraryVO itineraryVO = optionalItinerary.get();
            itineraryVO.setItnName(itineraryDetails.getItnName());
            itineraryVO.setCrtId(itineraryDetails.getCrtId());
            itineraryVO.setItnDesc(itineraryDetails.getItnDesc());
            itineraryVO.setIsPublic(itineraryDetails.getIsPublic());
            itineraryVO.setItnStatus(itineraryDetails.getItnStatus());
            itineraryVO.setItnUpdateDat(itineraryDetails.getItnUpdateDat());
            return ResponseEntity.ok(itineraryService.save(itineraryVO));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{itnId}")
    public ResponseEntity<Void> deleteItinerary(@PathVariable Integer itnId) {
        itineraryService.deleteById(itnId);
        return ResponseEntity.noContent().build();
    }
}
