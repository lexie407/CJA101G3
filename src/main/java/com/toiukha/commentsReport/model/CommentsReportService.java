package com.toiukha.commentsReport.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.toiukha.comments.model.CommentsService;

@Service
public class CommentsReportService {

	@Autowired
	private CommentsreportRepository commentsreportRepository;
	
	//新增檢舉
	public void addOne (CommentsReportVO commentsReportVO) {
		commentsreportRepository.save(commentsReportVO);
	}
	
	//檢舉狀態修改
	public void changeSta (CommentsReportVO commentsReportVO) {
		commentsreportRepository.save(commentsReportVO);
	}
	
	//查一個
	public CommentsReportVO getOne(Integer commRepId) {
		Optional<CommentsReportVO> optional = commentsreportRepository.findById(commRepId);
		return optional.orElse(null);			
	}
	
	//查全部
	public List<CommentsReportVO> getAll() {
		return commentsreportRepository.findAll();
	}
	
	//查會員的
	public List<CommentsReportVO> getMemList(Integer memId) {
		return commentsreportRepository.getbyMem(memId);
	}
	
}
