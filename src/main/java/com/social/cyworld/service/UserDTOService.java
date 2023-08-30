package com.social.cyworld.service;

import com.social.cyworld.dto.UserDTO;
import com.social.cyworld.repository.UserDTORepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserDTOService {
    @Autowired
    UserDTORepository userDTORepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////페이지 유저 정보 조회
    // 메인 페이지 유저 정보 조회
    public UserDTO findMainByIdx(int idx) {
        UserDTO userDTO = userDTORepository.findMainByIdxUserDTO(idx);
        return userDTO;
    }

    // 프로필 페이지 유저 정보 조회
    public UserDTO findProfileByIdx(int idx) {
        UserDTO userDTO = userDTORepository.findProfileByIdxUserDTO(idx);
        return userDTO;
    }

    // 다이어리 페이지 유저 정보 조회
    public UserDTO findDiaryByIdx(int idx) {
        UserDTO userDTO = userDTORepository.findDiaryByIdxUserDTO(idx);
        return userDTO;
    }

    // 사진첩 페이지 유저 정보 조회
    public UserDTO findGalleryByIdx(int idx) {
        UserDTO userDTO = userDTORepository.findGalleryByIdxUserDTO(idx);
        return userDTO;
    }

    // 방명록 페이지 유저 정보 조회
    public UserDTO findGuestbookByIdx(int idx) {
        UserDTO userDTO = userDTORepository.findGuestbookByIdxUserDTO(idx);
        return userDTO;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////검색
    // 이름으로 친구 검색
    public List<UserDTO> findSignJoinUserProfileJoinUserMainByNameLike(String searchValue) {
        List<UserDTO> signList = userDTORepository.findSignJoinUserProfileJoinUserMainByNameLikeUserDTO(searchValue);
        return signList;
    }

    // email로 친구 검색
    public List<UserDTO> findSignJoinUserProfileJoinUserMainByEmailLike(String searchValue) {
        List<UserDTO> signList = userDTORepository.findSignJoinUserProfileJoinUserMainByEmailLikeUserDTO(searchValue);
        return signList;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////방명록 작성자
    // 유저 프로필 정보 중 이메일 및 이름 + 유저 메인 정보 중 미니미 조회
    public UserDTO findSignJoinUserProfileJoinUserMainByIdx(int idx) {
        UserDTO userDTO = userDTORepository.findSignJoinUserProfileJoinUserMainByIdxUserDTO(idx);
        return userDTO;
    }
}
