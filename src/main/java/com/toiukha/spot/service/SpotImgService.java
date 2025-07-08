package com.toiukha.spot.service;

import com.toiukha.spot.model.SpotImgVO;
import com.toiukha.spot.repository.SpotImgRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpotImgService {
    @Autowired
    private SpotImgRepository spotImgRepository;

    public List<SpotImgVO> getImagesBySpotId(Integer spotId) {
        return spotImgRepository.findBySpotIdOrderByImgIdAsc(spotId);
    }

    public SpotImgVO saveImage(SpotImgVO img) {
        return spotImgRepository.save(img);
    }

    public void deleteImage(Integer spotId, Integer imgId) {
        spotImgRepository.deleteBySpotIdAndImgId(spotId, imgId);
    }

    public void deleteAllImagesBySpotId(Integer spotId) {
        spotImgRepository.deleteBySpotId(spotId);
    }
} 