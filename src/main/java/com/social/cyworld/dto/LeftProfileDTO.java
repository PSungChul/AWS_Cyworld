package com.social.cyworld.dto;

import com.social.cyworld.entity.UserMain;
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
    private String mUid;
    private String mainPhoto;
    private String mainText;
    private MultipartFile mainPhotoFile;

    public UserMain toEntity() {
        return UserMain.builder()
                .idx(mUid)
                .mainPhoto(mainPhoto)
                .mainText(mainText)
                .build();
    }

    public LeftProfileDTO(UserDTO userDTO) {
        this.idx = userDTO.getIdx();
        this.mUid = userDTO.getMUid();
        this.mainPhoto = userDTO.getMainPhoto();
        this.mainText = userDTO.getMainText();
    }
}
