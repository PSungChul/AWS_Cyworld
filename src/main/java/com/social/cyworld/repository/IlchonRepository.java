package com.social.cyworld.repository;

import com.social.cyworld.entity.Ilchon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface IlchonRepository extends JpaRepository<Ilchon, Object> {
    // 일촌 전체 목록 조회
    List<Ilchon> findByIlchonSessionIdxAndIlchonUp(int ilchonSessionIdx, int inchonUp);

    // 로그인한 유저와 해당 미니홈피 유저의 일촌관계
    Ilchon findByIlchonIdxAndIlchonSessionIdx(int ilchonIdx, int ilchonSessionIdx);

    // 일촌 이중 조회
    int countByIlchonIdxAndIlchonSessionIdxOrIlchonIdxAndIlchonSessionIdx(int ilchonIdx1, int ilchonSessionIdx1, int ilchonSessionIdx2, int ilchonIdx2);

    // 일촌 갱신
    @Query("UPDATE Ilchon i SET i.ilchonUp = :ilchonUp WHERE i.ilchonIdx = :ilchonSessionIdx AND i.ilchonSessionIdx = :ilchonIdx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void updateSetIlchonUpByIlchonIdxAndIlchonSessionIdx(@Param("ilchonUp") int ilchonUp, @Param("ilchonSessionIdx") int ilchonSessionIdx, @Param("ilchonIdx") int ilchonIdx);

    // 일촌 해제
    @Query("DELETE FROM Ilchon i WHERE i.ilchonIdx = :ilchonIdx AND i.ilchonSessionIdx = :ilchonSessionIdx")
    @Modifying(clearAutomatically = true)
    @Transactional
    void deleteByIlchonIdxAndIlchonSessionIdx(@Param("ilchonIdx") int ilchonIdx, @Param("ilchonSessionIdx") int ilchonSessionIdx);

    // 일촌수 조회
    int countByIlchonIdxAndIlchonUp(int ilchonIdx, int ilchonUp);
}
