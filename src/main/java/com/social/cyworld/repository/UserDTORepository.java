package com.social.cyworld.repository;

import com.social.cyworld.dto.UserDTO;
import com.social.cyworld.entity.Sign;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.persistence.Tuple;
import java.util.ArrayList;
import java.util.List;

@Repository
public interface UserDTORepository extends JpaRepository<Sign, UserDTO> {
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////페이지 유저 정보 조회
    // 메인 페이지 유저 정보 조회
    @Query("SELECT s.idx, s.roles, s.platform, up.name, um.dotory, um.ilchon, um.mainTitle, um.mainPhoto, um.mainText, um.today, um.total FROM Sign s JOIN UserProfile up ON s.pUid = up.idx JOIN UserMain um ON s.mUid = um.idx WHERE s.idx = :idx")
    Tuple findMainByIdx(@Param("idx") int idx);
    default UserDTO findMainByIdxUserDTO(int idx) {
        Tuple tuple = findMainByIdx(idx);
        if ( tuple != null ) {
            return UserDTO.builder()
                    .idx(tuple.get(0, Integer.class))
                    .roles(tuple.get(1, String.class))
                    .platform(tuple.get(2, String.class))
                    .name(tuple.get(3, String.class))
                    .dotory(tuple.get(4, Integer.class))
                    .ilchon(tuple.get(5, Integer.class))
                    .mainTitle(tuple.get(6, String.class))
                    .mainPhoto(tuple.get(7, String.class))
                    .mainText(tuple.get(8, String.class))
                    .today(tuple.get(9, Integer.class))
                    .total(tuple.get(10, Integer.class))
                    .build();
        }
        return null;
    }

    // 프로필 페이지 유저 정보 조회
    @Query("SELECT s.idx, s.platform, up.email, up.gender, up.name, up.phoneNumber, um.minimi, um.mainTitle, um.mainPhoto, um.mainText, um.today, um.total FROM Sign s JOIN UserProfile up ON s.pUid = up.idx JOIN UserMain um ON s.mUid = um.idx WHERE s.idx = :idx")
    Tuple findProfileByIdx(@Param("idx") int idx);
    default UserDTO findProfileByIdxUserDTO(int idx) {
        Tuple tuple = findProfileByIdx(idx);
        if ( tuple != null ) {
            return UserDTO.builder()
                    .idx(tuple.get(0, Integer.class))
                    .platform(tuple.get(1, String.class))
                    .email(tuple.get(2, String.class))
                    .gender(tuple.get(3, String.class))
                    .name(tuple.get(4, String.class))
                    .phoneNumber(tuple.get(5, String.class))
                    .minimi(tuple.get(6, String.class))
                    .mainTitle(tuple.get(7, String.class))
                    .mainPhoto(tuple.get(8, String.class))
                    .mainText(tuple.get(9, String.class))
                    .today(tuple.get(10, Integer.class))
                    .total(tuple.get(11, Integer.class))
                    .build();
        }
        return null;
    }

    // 다이어리 페이지 유저 정보 조회
    @Query("SELECT s.idx, up.name, um.mainTitle, um.today, um.total FROM Sign s JOIN UserProfile up ON s.pUid = up.idx JOIN UserMain um ON s.mUid = um.idx WHERE s.idx = :idx")
    Tuple findDiaryByIdx(@Param("idx") int idx);
    default UserDTO findDiaryByIdxUserDTO(int idx) {
        Tuple tuple = findDiaryByIdx(idx);
        if ( tuple != null ) {
            return UserDTO.builder()
                    .idx(tuple.get(0, Integer.class))
                    .name(tuple.get(1, String.class))
                    .mainTitle(tuple.get(2, String.class))
                    .today(tuple.get(3, Integer.class))
                    .total(tuple.get(4, Integer.class))
                    .build();
        }
        return null;
    }

    // 사진첩 페이지 유저 정보 조회
    @Query("SELECT s.idx, up.name, um.mainTitle, um.today, um.total FROM Sign s JOIN UserProfile up ON s.pUid = up.idx JOIN UserMain um ON s.mUid = um.idx WHERE s.idx = :idx")
    Tuple findGalleryByIdx(@Param("idx") int idx);
    default UserDTO findGalleryByIdxUserDTO(int idx) {
        Tuple tuple = findGalleryByIdx(idx);
        if ( tuple != null ) {
            return UserDTO.builder()
                    .idx(tuple.get(0, Integer.class))
                    .name(tuple.get(1, String.class))
                    .mainTitle(tuple.get(2, String.class))
                    .today(tuple.get(3, Integer.class))
                    .total(tuple.get(4, Integer.class))
                    .build();
        }
        return null;
    }

    // 방명록 페이지 유저 정보 조회
    @Query("SELECT s.idx, up.name, um.mainTitle, um.mainPhoto, um.mainText, um.today, um.total FROM Sign s JOIN UserProfile up ON s.pUid = up.idx JOIN UserMain um ON s.mUid = um.idx WHERE s.idx = :idx")
    Tuple findGuestbookByIdx(@Param("idx") int idx);
    default UserDTO findGuestbookByIdxUserDTO(int idx) {
        Tuple tuple = findGuestbookByIdx(idx);
        if ( tuple != null ) {
            return UserDTO.builder()
                    .idx(tuple.get(0, Integer.class))
                    .name(tuple.get(1, String.class))
                    .mainTitle(tuple.get(2, String.class))
                    .mainPhoto(tuple.get(3, String.class))
                    .mainText(tuple.get(4, String.class))
                    .today(tuple.get(5, Integer.class))
                    .total(tuple.get(6, Integer.class))
                    .build();
        }
        return null;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////검색
    // 이름으로 친구 검색
    @Query("SELECT s.idx, s.platform, up.email, up.name, um.minimi FROM Sign s JOIN UserProfile up ON s.pUid = up.idx JOIN UserMain um ON s.mUid = um.idx WHERE up.name Like %:searchValue%")
    List<Tuple> findSignJoinUserProfileJoinUserMainByNameLike(@Param("searchValue") String searchValue);
    default List<UserDTO> findSignJoinUserProfileJoinUserMainByNameLikeUserDTO(String searchValue) {
        List<Tuple> tupleList = findSignJoinUserProfileJoinUserMainByNameLike(searchValue);
        List<UserDTO> userDTOList = new ArrayList<>();

        for (Tuple tuple : tupleList) {
            UserDTO userDTO = UserDTO.builder()
                    .idx(tuple.get(0, Integer.class))
                    .platform(tuple.get(1, String.class))
                    .email(tuple.get(2, String.class))
                    .name(tuple.get(3, String.class))
                    .minimi(tuple.get(4, String.class))
                    .build();
            userDTOList.add(userDTO);
        }

        return userDTOList;
    }

    // email로 친구 검색
    @Query("SELECT s.idx, s.platform, up.email, up.name, um.minimi FROM Sign s JOIN UserProfile up ON s.pUid = up.idx JOIN UserMain um ON s.mUid = um.idx WHERE up.email Like %:searchValue%")
    List<Tuple> findSignJoinUserProfileJoinUserMainByEmailLike(@Param("searchValue") String searchValue);
    default List<UserDTO> findSignJoinUserProfileJoinUserMainByEmailLikeUserDTO(String searchValue) {
        List<Tuple> tupleList = findSignJoinUserProfileJoinUserMainByEmailLike(searchValue);
        List<UserDTO> userDTOList = new ArrayList<>();

        for (Tuple tuple : tupleList) {
            UserDTO userDTO = UserDTO.builder()
                    .idx(tuple.get(0, Integer.class))
                    .platform(tuple.get(1, String.class))
                    .email(tuple.get(2, String.class))
                    .name(tuple.get(3, String.class))
                    .minimi(tuple.get(4, String.class))
                    .build();
            userDTOList.add(userDTO);
        }

        return userDTOList;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////방명록 작성자
    // 유저 프로필 정보 중 이메일 및 이름 + 유저 메인 정보 중 미니미 조회
    @Query("SELECT up.email, up.name, um.minimi FROM Sign s JOIN UserProfile up ON s.pUid = up.idx JOIN UserMain um ON s.mUid = um.idx WHERE s.idx = :idx")
    Tuple findSignJoinUserProfileJoinUserMainByIdx(@Param("idx") int idx);
    default UserDTO findSignJoinUserProfileJoinUserMainByIdxUserDTO(int idx) {
        Tuple tuple = findSignJoinUserProfileJoinUserMainByIdx(idx);
        if ( tuple != null ) {
            return UserDTO.builder()
                    .email(tuple.get(0, String.class))
                    .name(tuple.get(1, String.class))
                    .minimi(tuple.get(2, String.class))
                    .build();
        }
        return null;
    }
}