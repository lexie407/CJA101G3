package com.toiukha.itnSpot.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface ItnSpotRepository extends JpaRepository<ItnSpotVO, ItnSpotId> {

	//查行程所有的景點
	@Query(value = "FROM ItnSpotVO AS iVO WHERE iVO.id.itnId = :itnId ORDER BY iVO.seq")
	public List<ItnSpotVO> getSpotsByitnId(Integer itnId);
	
	//查行程的最大景點數
	@Query(value = "SELECT MAX(iVO.seq) FROM ItnSpotVO AS iVO WHERE iVO.id.itnId = :itnId")
	public Optional<Integer> getMaxSeqByItnId(Integer itnId);
	
	//查行程的排序是否存在
	@Query(value = "FROM ItnSpotVO AS iVO WHERE iVO.id.itnId = :itnId AND iVO.seq = :seq")
	public Optional<ItnSpotVO> getMaxSeqByItnId(Integer itnId, Integer seq);
	
	//修改景點的排序
	@Transactional
	@Modifying
	@Query(value = "UPDATE ItnSpotVO AS iVO SET iVO.seq = :newSeq WHERE iVO.id = :id AND iVO.seq = :oldSeq")
	public void updateSeq(ItnSpotId id, Integer oldSeq, Integer newSeq);
	
	@Transactional
	@Modifying
	@Query(value = "UPDATE ItnSpotVO AS iVO SET iVO.id.spotId = :newSpotId WHERE iVO.id.itnId = :itnId AND iVO.id.spotId = :oldSpotId")
	public void updateSpo(Integer newSpotId, Integer itnId, Integer oldSpotId);
	
}
