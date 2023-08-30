package com.social.cyworld.service;

import com.social.cyworld.entity.Ilchon;
import com.social.cyworld.entity.Ilchonpyeong;
import com.social.cyworld.entity.Views;
import com.social.cyworld.repository.IlchonRepository;
import com.social.cyworld.repository.IlchonpyeongRepository;
import com.social.cyworld.repository.ViewsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Service
public class MainService {
    @Autowired
    ViewsRepository viewsRepository;
    @Autowired
    IlchonpyeongRepository ilchonpyeongRepository;
    @Autowired
    IlchonRepository ilchonRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Views
    // 로그인 유저가 해당 미니홈피로 방문한 기록 조회
    public Views findByViewsIdxAndViewsSessionIdx(HashMap<String, Object> map) {
        Views views = viewsRepository.findByViewsIdxAndViewsSessionIdx((Integer) map.get("1"), (Integer) map.get("2"));
        return views;
    }

    // 방문 기록이 없는 경우 첫 방문 저장
    public void insertIntoViews(Views views) {
        viewsRepository.save(views);
    }

    // 방문 기록이 있는 경우 날짜 비교 후 날짜가 다르면 해당 날짜에 현재 날짜로 갱신
    public void updateSetTodayDateByViewsIdxAndViewsSessionIdx(HashMap<String, Object> map) {
        viewsRepository.updateSetTodayDateByViewsIdxAndViewsSessionIdx((String) map.get("3"), (Integer) map.get("1"), (Integer) map.get("2"));
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Ilchonpyeong
    // 일촌평 전체 목록 조회
    public List<Ilchonpyeong> findByIlchonpyeongIdx(int idx) {
        List<Ilchonpyeong> ilchonpyeongList = ilchonpyeongRepository.findByIlchonpyeongIdx(idx);
        return ilchonpyeongList;
    }

    // 일촌평 작성
    public Ilchonpyeong insertIntoIlchonpyeong(Ilchonpyeong ilchonpyeong) {
        Ilchonpyeong res = ilchonpyeongRepository.save(ilchonpyeong);
        return res;
    }
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////Ilchon
    // 일촌 전체 목록 조회
    public List<Ilchon> findByIlchonSessionIdxAndIlchonUp(Ilchon ilchon) {
        List<Ilchon> ilchonList = ilchonRepository.findByIlchonSessionIdxAndIlchonUp(ilchon.getIlchonSessionIdx(), ilchon.getIlchonUp());
        return ilchonList;
    }

    // 로그인 유저와 해당 미니홈피 유저의 일촌관계
    public Ilchon findByIlchonIdxAndIlchonSessionIdx(Ilchon ilchon) {
        Ilchon ilchonUser = ilchonRepository.findByIlchonIdxAndIlchonSessionIdx(ilchon.getIlchonIdx(), ilchon.getIlchonSessionIdx());
        return ilchonUser;
    }

    // 일촌 이중 조회
    public int countByIlchonIdxAndIlchonSessionIdxOrIlchonIdxAndIlchonSessionIdx(Ilchon ilchon) {
        int count = ilchonRepository.countByIlchonIdxAndIlchonSessionIdxOrIlchonIdxAndIlchonSessionIdx(ilchon.getIlchonIdx(), ilchon.getIlchonSessionIdx(), ilchon.getIlchonSessionIdx(), ilchon.getIlchonIdx());
        return count;
    }

    // 일촌 등록
    public void insertIntoIlchon(Ilchon ilchon) {
        ilchonRepository.save(ilchon);
    }

    // 일촌 갱신
    public void updateSetIlchonUpByIlchonIdxAndIlchonSessionIdx(Ilchon ilchon) {
        ilchonRepository.updateSetIlchonUpByIlchonIdxAndIlchonSessionIdx(ilchon.getIlchonUp(), ilchon.getIlchonSessionIdx(), ilchon.getIlchonIdx());
    }

    // 일촌 해제
    public void deleteByIlchonIdxAndIlchonSessionIdx(Ilchon ilchon) {
        ilchonRepository.deleteByIlchonIdxAndIlchonSessionIdx(ilchon.getIlchonIdx(), ilchon.getIlchonSessionIdx());
    }

    // 일촌수 조회
    public int countByIlchonIdxAndIlchonUp(Ilchon ilchon) {
        int count = ilchonRepository.countByIlchonIdxAndIlchonUp(ilchon.getIlchonIdx(), ilchon.getIlchonUp());
        return count;
    }
}
