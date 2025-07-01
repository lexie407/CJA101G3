package com.toiukha.itnReport.model;

import java.util.*;
import java.util.Optional;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("itnRptService")
public class ItnRptService {
	
	@Autowired
	ItnRptRepository repository;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void addItnRpt(ItnRptVO itnRptVO) {
		repository.save(itnRptVO);
	}
	public void updateItnRpt(ItnRptVO itnRptVO) {
		repository.save(itnRptVO);
	}
	public void deleteItnRpt(Integer repId) {
		if(repository.existsById(repId))
			repository.deleteById(repId);
	}
	
	public ItnRptVO getOneItnRpt(Integer repId) {
		Optional<ItnRptVO> optional = repository.findById(repId);
		return optional.orElse(null);
	}
	
	public List<ItnRptVO> getAll(){
		return repository.findAll();
	}
	
	/**
	 * 根據會員ID查詢檢舉記錄
	 * @param memId 會員ID
	 * @return 該會員的檢舉記錄列表
	 */
	public List<ItnRptVO> getReportsByMemberId(Integer memId) {
		return repository.findByMemId(memId);
	}
	
	/**
	 * 根據商品ID查詢檢舉記錄
	 * @param itemId 商品ID
	 * @return 該商品的檢舉記錄列表
	 */
	public List<ItnRptVO> getReportsByItemId(Integer itemId) {
		return repository.findByItemId(itemId);
	}
	
	/**
	 * 根據檢舉狀態查詢檢舉記錄
	 * @param repStatus 檢舉狀態 (0:待處理, 1:通過, 2:駁回)
	 * @return 指定狀態的檢舉記錄列表
	 */
	public List<ItnRptVO> getReportsByStatus(Byte repStatus) {
		return repository.findByRepStatus(repStatus);
	}
	
	/**
	 * 檢查會員是否已經檢舉過某商品
	 * @param memId 會員ID
	 * @param itemId 商品ID
	 * @return true表示已檢舉過，false表示未檢舉過
	 */
	public boolean hasMemberReportedItem(Integer memId, Integer itemId) {
		List<ItnRptVO> reports = repository.findByMemIdAndItemId(memId, itemId);
		return !reports.isEmpty();
	}
	
	/**
	 * 統計某個商品被檢舉的次數（已通過的檢舉）
	 * @param itemId 商品ID
	 * @return 已通過的檢舉次數
	 */
	public Long getApprovedReportCountByItemId(Integer itemId) {
		return repository.countApprovedReportsByItemId(itemId);
	}
	
	/**
	 * 獲取待處理的檢舉記錄
	 * @return 待處理的檢舉記錄列表
	 */
	public List<ItnRptVO> getPendingReports() {
		return repository.findByRepStatus((byte) 0);
	}
	
	/**
	 * 獲取已通過的檢舉記錄
	 * @return 已通過的檢舉記錄列表
	 */
	public List<ItnRptVO> getApprovedReports() {
		return repository.findByRepStatus((byte) 1);
	}
	
	/**
	 * 獲取已駁回的檢舉記錄
	 * @return 已駁回的檢舉記錄列表
	 */
	public List<ItnRptVO> getRejectedReports() {
		return repository.findByRepStatus((byte) 2);
	}
		

}
