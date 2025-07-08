package com.toiukha.spot.repository;

import com.toiukha.spot.model.SpotImgVO;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface SpotImgRepository extends JpaRepository<SpotImgVO, Integer> {
    List<SpotImgVO> findBySpotIdOrderByImgIdAsc(Integer spotId);
    void deleteBySpotIdAndImgId(Integer spotId, Integer imgId);
    void deleteBySpotId(Integer spotId);
} 