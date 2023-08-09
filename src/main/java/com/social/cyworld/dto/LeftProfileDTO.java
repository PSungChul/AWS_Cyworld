package com.social.cyworld.dto;

import com.social.cyworld.entity.Sign;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class LeftProfileDTO {
    private int idx;
    private String mainPhoto;
    private String mainText;
    private MultipartFile mainPhotoFile;

    public Sign toEntity() {
        return Sign.builder()
                .idx(idx)
                .mainPhoto(mainPhoto)
                .mainText(mainText)
                .build();
    }

    public LeftProfileDTO(Sign sign) {
        this.idx = sign.getIdx();
        this.mainPhoto = sign.getMainPhoto();
        this.mainText = sign.getMainText();
    }
}
