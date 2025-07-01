package com.toiukha.like.model;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.toiukha.commentsReport.model.CommentsReportVO;

public interface LikeRepository extends JpaRepository<LikeVO, Integer> {

	//查文章裡留言的讚
	@Query(value = "FROM LikeVO WHERE parDocId = :parDocId")
	public List<LikeVO> searchByArt(Integer parDocId);
	
	//查單一的讚
	@Query(value = "FROM LikeVO WHERE parDocId = :parDocId AND docId = :docId AND memId = :memId")
	public LikeVO searchOne(Integer parDocId, Integer docId, Integer memId);
	
	@Query(value = "FROM LikeVO WHERE parDocId IS NULL AND docId = :docId AND memId = :memId")
	public LikeVO searchOne(Integer docId, Integer memId);
	
	//查文件的按讚數
	@Query(value = "SELECT count(*) FROM LikeVO WHERE docId = :docId AND parDocId IS NULL AND likeSta = 1")
	public Integer searchLikeNum(Integer docId);
	
	@Query(value = "SELECT count(*) FROM LikeVO WHERE docId = :docId AND parDocId = :parDocId AND likeSta = 1")
	public Integer searchLikeNum(Integer parDocId, Integer docId);
	
}
