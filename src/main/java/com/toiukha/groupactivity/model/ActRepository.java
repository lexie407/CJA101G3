// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

package com.toiukha.groupactivity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ActRepository extends JpaRepository<ActVO, Integer>, JpaSpecificationExecutor<ActVO> {

    // 查詢指定團主的所有活動（按 actId 降序排序）
    List<ActVO> findByHostIdOrderByActIdDesc(Integer hostId);
    
    // 查詢指定團主的所有活動（原方法，保持向後相容）
    List<ActVO> findByHostId(Integer hostId);

    // 刪除指定活動（開發階段需要）
    @Transactional
    @Modifying
    @Query("delete from ActVO a where a.actId = :actId")
    void deleteByActId(@Param("actId") int actId);

    //專門查詢活動圖片
    @Query("SELECT a.actImg FROM ActVO a WHERE a.actId = :actId")
    byte[] findActImgByActId(@Param("actId") Integer actId);

}


