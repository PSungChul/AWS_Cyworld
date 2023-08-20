package com.social.cyworld.dto;

import com.social.cyworld.entity.Gallery;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.text.SimpleDateFormat;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GalleryDTO {
    private Integer idx;

    private int galleryIdx;

    private String galleryTitle;

    private String galleryRegDate;

    private String galleryContent;

    private String galleryFileName;

    private String galleryFileExtension;

    private int galleryLikeNum;

    private MultipartFile galleryFile;
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public GalleryDTO(Gallery gallery) {
        this.idx = gallery.getIdx();
        this.galleryIdx = gallery.getGalleryIdx();
        this.galleryTitle = gallery.getGalleryTitle();
        this.galleryRegDate = gallery.getGalleryRegDate();
        this.galleryContent = gallery.getGalleryContent();
        this.galleryFileName = gallery.getGalleryFileName();
        this.galleryFileExtension = gallery.getGalleryFileExtension();
        this.galleryLikeNum = gallery.getGalleryLikeNum();
        this.galleryFile = null;
    }

    // 게시글 작성
    public Gallery toInsertEntity() {
        // 작성 시간을 기록하기 위해 Date객체 사용
        Date date = new Date();
        // Date객체를 원하는 모양대로 재조합
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return Gallery.builder()
                .idx(null)
                .galleryIdx(galleryIdx)
                .galleryTitle(galleryTitle)
                .galleryRegDate(today.format(date))
                .galleryContent(galleryContent)
                .galleryFileName(galleryFileName)
                .galleryFileExtension(galleryFileExtension)
                .galleryLikeNum(0)
                .build();
    }

    // 게시글 수정
    public Gallery toModifyEntity() {
        // 수정 시간을 기록하기 위해 Date객체 사용
        Date date = new Date();
        // Date객체를 원하는 모양대로 재조합
        SimpleDateFormat today = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return Gallery.builder()
                .idx(idx)
                .galleryIdx(galleryIdx)
                .galleryTitle(galleryTitle)
                .galleryRegDate(today.format(date))
                .galleryContent(galleryContent)
                .galleryFileName(galleryFileName)
                .galleryFileExtension(galleryFileExtension)
                .galleryLikeNum(galleryLikeNum)
                .build();
    }
}
