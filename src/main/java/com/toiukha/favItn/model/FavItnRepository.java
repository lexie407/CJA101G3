package com.toiukha.favItn.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavItnRepository extends JpaRepository<FavItnVO, FavItnId> {

    @Query("SELECT f FROM FavItnVO f WHERE f.id.memId = :memId")
    List<FavItnVO> findByMemId(@Param("memId") Integer memId);
    
    @Query(value = "SELECT COUNT(*) FROM favitn WHERE favitnid = :itnId AND memid = :memId", nativeQuery = true)
    int countByItnIdAndMemId(@Param("itnId") Integer itnId, @Param("memId") Integer memId);
    
    @Modifying
    @Query(value = "INSERT IGNORE INTO favitn (favitnid, memid) VALUES (:itnId, :memId)", nativeQuery = true)
    int insertIgnore(@Param("itnId") Integer itnId, @Param("memId") Integer memId);
    
    @Modifying
    @Query(value = "DELETE FROM favitn WHERE favitnid = :itnId AND memid = :memId", nativeQuery = true)
    int deleteByItnIdAndMemId(@Param("itnId") Integer itnId, @Param("memId") Integer memId);
}
