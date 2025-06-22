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
public interface ParticipateRepository extends JpaRepository<ParticipantVO, ParticipantId> {

    /**
     * 根據活動ID和會員ID查詢參加記錄
     */
    Optional<ParticipantVO> findByActIdAndMemId(Integer actId, Integer memId);

    /**
     * 根據活動ID查詢所有參加者
     */
    List<ParticipantVO> findByActId(Integer actId);

    /**
     * 根據活動ID查詢所有參加者的會員ID
     */
    @Query("select p.memId from ParticipantVO p where p.actId = :actId")
    List<Integer> findMemIdsByActId(Integer actId);
}
