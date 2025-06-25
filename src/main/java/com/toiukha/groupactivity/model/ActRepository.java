// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/

package com.toiukha.groupactivity.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
//import com.dept.model.DeptVO; //引入join外部套件

//老師的範例:public interface ActRepository extends JpaRepository<ActVO, Integer>, JpaSpecificationExecutor<ActVO>
public interface ActRepository extends JpaRepository<ActVO, Integer>, JpaSpecificationExecutor<ActVO> { //需要繼承JpaRepository<VO物件,欄位型別>

    // 查詢指定團主的所有活動（按 actId 降序排序）
    List<ActVO> findByHostIdOrderByActIdDesc(Integer hostId);
    
    // 查詢指定團主的所有活動（原方法，保持向後相容）
    List<ActVO> findByHostId(Integer hostId);

    // 刪除指定活動（開發階段需要）
    @Transactional
    @Modifying
    @Query("delete from ActVO a where a.actId = :actId")
    void deleteByActId(@Param("actId") int actId);
    //@Query(value = "delete from ActVO where actId =?1")
    //	void deleteByActId(int actId);


    // 彈性複合查詢：關鍵字（活動名或描述）、起訖日、狀態都可單獨或組合查詢
//    @Query("SELECT a FROM ActVO a " +
//            "WHERE (:keyword IS NULL OR a.actName LIKE %:keyword% OR a.actDesc LIKE %:keyword%) " +
//            "AND (:startDate IS NULL OR a.signupStart >= :startDate) " +
//            "AND (:endDate IS NULL OR a.signupEnd <= :endDate) " +
//            "AND (:recruitStatus IS NULL OR a.recruitStatus = :recruitStatus)")
//    List<ActVO> searchActs(@Param("keyword") String keyword,
//                           @Param("startDate") LocalDate startDate,
//                           @Param("endDate") LocalDate endDate,
//                           @Param("recruitStatus") Integer recruitStatus);

//	//條件查詢(透過關聯去查詢資料)
//	@Query(value = "from ActVO where deptno=?1 order by empno")
//	List<ActVO> findByOthers(DeptVO deptvo); //也已用部門編號搭配OneToMany去列出所有的員工

    //條件查詢(限制傳入三個欄位，才能查到資料)
//	@Query(value = "from ActVO where actId=?1 and actName like?2 and hiredate=?3 order by empno")
//	List<ActVO> findByOthers(int empno , String ename , java.sql.Date hiredate);
}


