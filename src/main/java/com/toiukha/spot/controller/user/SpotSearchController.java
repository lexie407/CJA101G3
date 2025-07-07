package com.toiukha.spot.controller.user;

import com.toiukha.spot.model.SpotVO;
import com.toiukha.spot.repository.SpotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/spot/api")
public class SpotSearchController {

    @Autowired
    private SpotRepository spotRepository;

    @GetMapping("/search")
    @ResponseBody
    public List<SpotVO> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) Double rating,
            @RequestParam(required = false, defaultValue = "rating") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortDirection) {

        return spotRepository.findBySearchCriteria(keyword, region, rating, sortBy, sortDirection);
    }
} 