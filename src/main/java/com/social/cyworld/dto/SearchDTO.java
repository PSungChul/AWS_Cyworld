package com.social.cyworld.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class SearchDTO {
    private int loginUserIdx;
    private int idx;
    private String email;
    private String name;
    private String mainPhoto;
    private String searchType;
    private String searchValue;
}
