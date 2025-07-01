package com.toiukha.spot.dto;

import com.toiukha.spot.model.SpotVO;
import org.springframework.stereotype.Component;

/**
 * 景點資料轉換器
 * 手動實現 DTO 和 VO 之間的轉換
 * 
 * @author CJA101G3 景點模組開發
 * @version 1.0
 */
@Component
public class SpotMapper {
    
    /**
     * 將 SpotDTO 轉換為 SpotVO
     * 
     * @param dto SpotDTO 物件
     * @return SpotVO 物件
     */
    public SpotVO toVO(SpotDTO dto) {
        if (dto == null) {
            return null;
        }
        
        SpotVO vo = new SpotVO();
        vo.setSpotName(dto.getSpotName());
        vo.setSpotLoc(dto.getSpotLoc());
        vo.setSpotLat(dto.getSpotLat());
        vo.setSpotLng(dto.getSpotLng());
        vo.setSpotStatus(dto.getSpotStatus());
        vo.setSpotDesc(dto.getSpotDesc());
        
        return vo;
    }
    
    /**
     * 將 SpotVO 轉換為 SpotDTO
     * 
     * @param vo SpotVO 物件
     * @return SpotDTO 物件
     */
    public SpotDTO toDTO(SpotVO vo) {
        if (vo == null) {
            return null;
        }
        
        SpotDTO dto = new SpotDTO();
        dto.setSpotName(vo.getSpotName());
        dto.setSpotLoc(vo.getSpotLoc());
        dto.setSpotLat(vo.getSpotLat());
        dto.setSpotLng(vo.getSpotLng());
        dto.setSpotStatus(vo.getSpotStatus());
        dto.setSpotDesc(vo.getSpotDesc());
        
        return dto;
    }
}