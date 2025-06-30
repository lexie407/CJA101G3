package com.toiukha.articlereport.model;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class ArticlereportService {

	@Autowired
	private ArticlereportRepository articlereportRepository;
	
	@PersistenceContext
    private EntityManager entityManager;
	
	//新增檢舉
		public void addOne (ArticlereportVO articlereportVO) {
			articlereportRepository.save(articlereportVO);
		}
		
		//檢舉狀態修改
		public void changeSta (ArticlereportVO articlereportVO) {
			articlereportRepository.save(articlereportVO);
		}
		
		//查一個
		public ArticlereportVO getOne(Integer artRepId) {
			System.out.println("ArticlereportService: Attempting to get ArticlereportVO for artRepId: " + artRepId);
			Optional<ArticlereportVO> optional = articlereportRepository.findById(artRepId);
			return optional.orElse(null);
		}
		
		//查全部
		public List<ArticlereportVO> getAll() {
			return articlereportRepository.findAll();
		}
		
		//查會員的
		public List<ArticlereportVO> getMemList(Integer memId) {
			return articlereportRepository.getByMem(memId);
		}
	
}
