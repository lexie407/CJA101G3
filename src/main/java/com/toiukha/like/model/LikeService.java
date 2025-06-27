package com.toiukha.like.model;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {
	
	@Autowired
	private LikeRepository likeRepository;
	
	//新增與修改
	public void eidtOne(LikeVO likeVO) {
		likeRepository.save(likeVO);
	}
	
	//查文章的讚
	public List<LikeVO> getArtLikes(Integer parDocId){
		return likeRepository.searchByArt(parDocId);
	}
	
	//查單一的讚
	public LikeVO getOneLike(Integer parDocId, Integer docId) {
		return likeRepository.searchOne(parDocId, docId);
	}
	
	public LikeVO getOneLike(Integer docId) {
		return likeRepository.searchOne(docId);
	}
	
}
