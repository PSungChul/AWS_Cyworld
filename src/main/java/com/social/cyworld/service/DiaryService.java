package com.social.cyworld.service;

import com.social.cyworld.entity.Diary;
import com.social.cyworld.repository.DiaryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiaryService {
    @Autowired
    DiaryRepository diaryRepository;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 다이어리 전체 목록 조회 - 내림차순
    public List<Diary> findByDiaryIdxOrderByIdxDesc(int idx) {
        List<Diary> diaryList = diaryRepository.findByDiaryIdxOrderByIdxDesc(idx);
        return diaryList;
    }

    // 새 다이어리 글 추가
    public void insertIntoDiary(Diary diary) {
        diaryRepository.save(diary);
    }

    // 다이어리 글 삭제
    public int deleteByDiaryIdxAndIdx(Diary diary) {
        int res = diaryRepository.deleteByDiaryIdxAndIdx(diary.getDiaryIdx(), diary.getIdx());
        return res;
    }

    // 수정을 위한 다이어리 글 한 건 조회
    public Diary findByDiaryIdxAndIdx(Diary diary) {
        Diary updateDiary = diaryRepository.findByDiaryIdxAndIdx(diary.getDiaryIdx(), diary.getIdx());
        return updateDiary;
    }

    // 다이어리 글 수정
    public int updateSetDiaryContentAndDiaryRegDateByDiaryIdxAndIdx(Diary diary) {
        int res = diaryRepository.updateSetDiaryContentAndDiaryRegDateByDiaryIdxAndIdx(diary.getDiaryContent(), diary.getDiaryRegDate(), diary.getDiaryIdx(), diary.getIdx());
        return res;
    }
}
