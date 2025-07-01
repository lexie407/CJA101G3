package com.toiukha.itnSpot.controller;

import com.toiukha.itnSpot.model.ItnSpotId;
import com.toiukha.itnSpot.model.ItnSpotService;
import com.toiukha.itnSpot.model.ItnSpotVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/itnSpot")
public class ItnSpotController {

    @Autowired
    private ItnSpotService itnSpotService;

    //查全部
    @GetMapping("/getAllItnSpots")
    public List<ItnSpotVO> getAllItnSpots() {
        return itnSpotService.findAll();
    }
    
  //查行程的中的所有景點，路徑後面的數字就是要查行程的所有景點，GET方法
    @GetMapping("/{itnId}")
    public List<ItnSpotVO> getSpotsByItn(@PathVariable("itnId") Integer itnId) {
        return itnSpotService.getSpotsByItineraryId(itnId);
    }

    /*行程中建立景點，POST方法
     JSON格式
     	{
    	"itnId": ,
    	"spotId": 
		}
     */
    @PostMapping("/createItnSpot")
    public ItnSpotVO createItnSpot(@RequestBody ItnSpotId itnSpotId) {
    	Integer itnId = itnSpotId.getItnId();
    	Integer spotId = itnSpotId.getSpotId();
        return itnSpotService.addItnSpot(itnId, spotId);
    }
    
    /*只換景點順序，路徑後面的數字是要換景點順序的行程，POST方法
     * JSON格式
     * [
     * 	景點編號的排序
     * ]
     */
    @PostMapping("/updateItnSpotList/{itnId}")
    public List<ItnSpotVO> updateItnSpotList(
    		@PathVariable Integer itnId, 
    		@RequestBody List<Integer> spotList){
    	return itnSpotService.updateItnSpotSequence(itnId, spotList);
    }
    
    /* 只換行程中的其中一個景點，路徑後面的數字是要變更的景點編號，POST方法
     * JSON格式
     * {
     * 	"itnSpotId": 
     * }
     */
    @PostMapping("/updateItnSpot/{newSpot}")
    public ItnSpotVO updateItnSpot(
    		@PathVariable Integer newSpot,
    		@RequestBody ItnSpotId itnSpotId){
    	ItnSpotVO ItnSpotVO = itnSpotService.findById(itnSpotId);
    	itnSpotService.updateSpo(ItnSpotVO, newSpot);
    	itnSpotId.setSpotId(newSpot);
    	return itnSpotService.findById(itnSpotId);
    }

    /* 刪除行程中的景點，DELETE方法
     * JSON格式
     * {
     * 	"itnId": ,
     * 	"spotId":
     * }
     */
    @DeleteMapping
    public List<ItnSpotVO> deleteItnSpot(@RequestBody ItnSpotId itnSpotId) {
        itnSpotService.deleteItnSpot(itnSpotId.getItnId(), itnSpotId.getSpotId());
        return itnSpotService.getSpotsByItineraryId(itnSpotId.getItnId());
    }
}
