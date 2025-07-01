package com.toiukha.itinerary.controller;

import com.toiukha.itinerary.model.ItineraryService;
import com.toiukha.itinerary.model.ItineraryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/itinerary")
public class ItineraryController {

    @Autowired
    private ItineraryService itineraryService;

    //查全部，POST方法
    @PostMapping("/getAllItineraries")
    public List<ItineraryVO> getAllItineraries() {
        return itineraryService.findAll();
    }

    //查一個，路徑後面是要查詢的編號，GET方法
    @GetMapping("/{itnId}")
    public ItineraryVO getItineraryById(@PathVariable Integer itnId) {
        Optional<ItineraryVO> optional = itineraryService.findById(itnId);
        return optional.orElse(null);
    }

    /* 新增一個，POST方法
     * JSON格式
     * {
     * 	"itnName": ,
     * 	"crtId": ,
     * 	"itnDesc":
     * }
     */
    @PostMapping("/createItinerary")
    public ItineraryVO createItinerary(@RequestBody ItineraryVO itineraryVO) {
        return itineraryService.save(itineraryVO);
    }

    /* 修改一個，路徑後面的是要修改的景點編號，POST方法
     * JSON格式
     * {
     * 	需要的資料就是下面方法中的註解
     * }
     */
    @PostMapping("/{itnId}")
    public ItineraryVO updateItinerary(@PathVariable Integer itnId, @RequestBody ItineraryVO itineraryDetails) {
        Optional<ItineraryVO> optional = itineraryService.findById(itnId);
            ItineraryVO itineraryVO = optional.get();
            //以下JSON需要的資料
            itineraryVO.setItnName(itineraryDetails.getItnName());
            itineraryVO.setCrtId(itineraryDetails.getCrtId());
            itineraryVO.setItnDesc(itineraryDetails.getItnDesc());
            itineraryVO.setIsPublic(itineraryDetails.getIsPublic());
            itineraryVO.setItnStatus(itineraryDetails.getItnStatus());
            //以上JSON需要的資料
            itineraryVO.setItnUpdateDat(Timestamp.from(Instant.now()));
            return itineraryService.save(itineraryVO);
    }

    
}
