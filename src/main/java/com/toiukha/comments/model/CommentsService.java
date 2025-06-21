package com.toiukha.comments.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CommentsService {

	@Autowired
	private CommentsRepository commentsRepository;
	
	//新增、修改、刪除
	public void editOne(CommentsVO commentsVO) {
		commentsRepository.save(commentsVO);
	}
	
	//查單一留言
	public CommentsVO getOne(Integer commId) {
		Optional<CommentsVO> optional = commentsRepository.findById(commId);
		return optional.orElse(null);
	}
	
	//查文章所屬留言
	public List<CommentsVO> getArtComm(Integer commArt){
		return commentsRepository.getbyArt(commArt);
	}
	
}
