package com.toiukha.participant.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 參加者 Repository
 */
@Repository
public interface PartRepository extends JpaRepository<PartVO, PartVO.CompositeKey> {

    /**
     * 根據活動ID和會員ID查詢參加記錄
     */
    Optional<PartVO> findByActIdAndMemId(Integer actId, Integer memId);

    /**
     * 根據活動ID查詢所有參加者
     */
    List<PartVO> findByActId(Integer actId);

    /**
     * 根據活動ID查詢所有參加者的會員ID
     */
    @Query("select p.memId from PartVO p where p.actId = :actId")
    List<Integer> findMemIdsByActId(Integer actId);
    
    /**
     * 根據會員ID查詢所有參加的活動
     */
    List<PartVO> findByMemId(Integer memId);
    
    /**
     * 根據會員ID查詢所有參加的活動ID
     */
    @Query("select p.actId from PartVO p where p.memId = :memId")
    List<Integer> findActIdsByMemId(Integer memId);
    
    /**
     * 根據活動ID和會員ID刪除參加記錄
     */
    void deleteByActIdAndMemId(Integer actId, Integer memId);
    
    /**
     * 根據活動ID刪除所有參加記錄
     */
    void deleteByActId(Integer actId);
}
