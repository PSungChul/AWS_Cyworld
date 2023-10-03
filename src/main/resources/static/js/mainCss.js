function adjustImageSize() {
    // 페이지 크기
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 전체 컨테이너
    const container = document.getElementsByClassName("container")[0];
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 왼쪽 섹션
    const leftSection = document.getElementsByClassName("left-section")[0];
    const leftDashedLine = document.getElementsByClassName("left-dashed-line")[0];
    const leftGrayBackground = document.getElementsByClassName("left-gray-background")[0];

    // 메인 사진
    const leftMainImgDiv = document.getElementsByClassName("left-image")[0];
    const leftMainImg = document.getElementsByClassName("leftImg")[0];

    // 메인 소개글
    const leftTextArea = document.getElementsByClassName("left-textarea")[0];

    // 일촌 수
    const ilchonNum = document.getElementById("ilchonNum");
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 오른쪽 섹션
    const rightSection = document.getElementsByClassName("right-section")[0];
    const rightDashedLine = document.getElementsByClassName("right-dashed-line")[0];
    const rightGrayBackground = document.getElementsByClassName("right-gray-background")[0];

    // 메인 타이틀
    const mainTitle = document.getElementById("mainTitle");

    // bgm
    const musicLogo = document.getElementsByClassName("musicLogo")[0];
    const mp3Title = document.getElementsByClassName("mp3_title")[0];
    const bgm = document.getElementById("bgm");

    // 검색
    const search = document.getElementsByClassName("search")[0];

    // 도토리
    const dotory = document.getElementsByClassName("dotory")[0];

    // 미니룸
    const miniRoomImg = document.getElementById("miniRoomImg");

    // 일촌평
    const ilchonpyeongDiv = document.getElementsByClassName("Ilchonpyeong")[0];
    const ilchonpyeongInput = document.getElementById("ilchonpyeongInput");
    const ilchonpyeongBtn = document.getElementsByClassName("Ic-registration")[0];

    // 미니미 배너
    const banner = document.getElementById("banner");
    const title = document.getElementById("title");
    const img_1 = document.getElementsByClassName("img")[0];
    const img_2 = document.getElementsByClassName("img")[1];
    const img_3 = document.getElementsByClassName("img")[2];
    const blink1 = document.getElementsByClassName("blink1")[0];
    const blink2 = document.getElementsByClassName("blink2")[0];
    const blink3 = document.getElementsByClassName("blink3")[0];
    const img1 = document.getElementById("img1");
    const img2 = document.getElementById("img2");
    const img3 = document.getElementById("img3");
    const bannerCancel = document.getElementsByClassName("bannerCancel")[0];

    // 페이지 크기가 1600px 보다 작거나 같은 경우
    if ( windowWidth <= 1600 ) {
        // 페이지 길이가 1800px 보다 작거나 같은 경우
        if ( windowHeight < 1800 ) {
            // 전체 컨테이너
            container.style.height = ( windowHeight - 75 ) + "px";
            container.style.overflowY = "scroll";
        // 페이지 길이가 1800px 보다 큰 경우
        } else {
            // 전체 컨테이너
            container.style.height = "";
            container.style.overflowY = "";
        }
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 왼쪽 섹션
        leftSection.style.width = windowWidth - 80 + "px";
        leftDashedLine.style.width = windowWidth - 145 + "px";
        leftGrayBackground.style.width = windowWidth - 175 + "px";

        // 메인 사진
        leftMainImgDiv.style.height = 360 - ( 1600 - windowWidth ) / 18 + "px";
        leftMainImg.style.height = 360 - ( 1600 - windowWidth ) / 18 + "px";

        // 메인 소개글
        leftTextArea.style.height = 70 - ( 1600 - windowWidth ) / 18 + "px";
        leftTextArea.style.fontSize = 24 - ( 1600 - windowWidth ) / 100 + "px";

        // 일촌 수
        ilchonNum.style.top = 571 - ( 1600 - windowWidth ) / 7.15 + "px";
        ilchonNum.style.left = 745 - ( 1600 - windowWidth ) / 2 + "px";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 오른쪽 섹션
        rightSection.style.width = windowWidth - 80 + "px";
        rightDashedLine.style.width = windowWidth - 145 + "px";
        rightGrayBackground.style.width = windowWidth - 175 + "px";

        // 메인 타이틀
        mainTitle.style.left = 43 - ( 1600 - windowWidth ) / 33.33 + "px";

        if ( windowWidth < 1000 ) {
            // bgm
            musicLogo.style.top = -35 - ( 1000 - windowWidth ) / 50 + "px";
            musicLogo.style.left = 465 - ( 1000 - windowWidth ) / 1.3 + "px";
            mp3Title.style.top = -39 - ( 1000 - windowWidth ) / 50 + "px";
            mp3Title.style.left = 301 - ( 1000 - windowWidth ) / 1.3 + "px";
            bgm.style.top = -7 - ( 1000 - windowWidth ) / 50 + "px";
            bgm.style.left = 445 - ( 1000 - windowWidth ) / 1.3 + "px";
        } else {
            // bgm
            musicLogo.style.top = 47 - ( 1600 - windowWidth ) / 21.5 + "px";
            musicLogo.style.left = 1024 - ( 1600 - windowWidth ) / 1.073 + "px";
            mp3Title.style.top = 43 - ( 1600 - windowWidth ) / 21.5 + "px";
            mp3Title.style.left = 860 - ( 1600 - windowWidth ) / 1.073 + "px";
            bgm.style.top = 74 - ( 1600 - windowWidth ) / 21.5 + "px";
            bgm.style.left = 1004 - ( 1600 - windowWidth ) / 1.073 + "px";
        }

        // 검색
        search.style.top = 48 - ( 1600 - windowWidth ) / 33.4 + "px";
        search.style.left = 1330 - ( 1600 - windowWidth ) / 1.03 + "px";

        // 도토리
        dotory.style.top = 10 + "px";
        dotory.style.left = 995 - ( 1600 - windowWidth ) / 1.19 + "px";

        // 미니룸
        miniRoomImg.style.width = 1203 - ( 1600 - windowWidth ) / 1.19 + "px";

        // 일촌평
        ilchonpyeongDiv.style.width = 1203 - ( 1600 - windowWidth ) / 1.19 + "px";
        ilchonpyeongInput.style.width = 973 - ( 1600 - windowWidth ) / 1.19 + "px";
        ilchonpyeongBtn.style.left = 1123 - ( 1600 - windowWidth ) / 1.19 + "px";

        // 미니미 배너
        banner.style.left = 1521 - ( 1600 - windowWidth ) / 1.19 + "px";
        banner.style.width = 113 - ( 1600 - windowWidth ) / 10 + "px";

        title.style.fontSize = 23 - ( 1600 - windowWidth ) / 50 + "px";

        img_1.style.marginLeft = 3 - ( 1600 - windowWidth ) / 600 + "px";
        img_1.style.width = 103 - ( 1600 - windowWidth ) / 10.3 + "px";
        img_2.style.marginLeft = 3 - ( 1600 - windowWidth ) / 600 + "px";
        img_2.style.width = 103 - ( 1600 - windowWidth ) / 10.3 + "px";
        img_3.style.marginLeft = 3 - ( 1600 - windowWidth ) / 600 + "px";
        img_3.style.width = 103 - ( 1600 - windowWidth ) / 10.3 + "px";

        blink1.style.top = 89 - ( 1600 - windowWidth ) / 20.7 + "px";
        blink1.style.left = -9 - ( 1600 - windowWidth ) / 600 + "px";
        blink2.style.top = 223 - ( 1600 - windowWidth ) / 20.7 + "px";
        blink2.style.left = -9 - ( 1600 - windowWidth ) / 600 + "px";
        blink3.style.top = 357 - ( 1600 - windowWidth ) / 20.7 + "px";
        blink3.style.left = -9 - ( 1600 - windowWidth ) / 600 + "px";

        img1.style.marginTop = 5 + ( 1600 - windowWidth ) / 24 + "px";
        img1.style.marginLeft = 0 - ( 1600 - windowWidth ) / 50 + "px";
        img1.style.width = 90 - ( 1600 - windowWidth ) / 24 + "px";
        img2.style.marginTop = 5 + ( 1600 - windowWidth ) / 24 + "px";
        img2.style.marginLeft = 0 - ( 1600 - windowWidth ) / 75 + "px";
        img2.style.width = 90 - ( 1600 - windowWidth ) / 24 + "px";
        img3.style.marginTop = 5 + ( 1600 - windowWidth ) / 24 + "px";
        img3.style.marginLeft = 0 - ( 1600 - windowWidth ) / 75 + "px";
        img3.style.width = 90 - ( 1600 - windowWidth ) / 24 + "px";

        bannerCancel.style.marginTop = 15 + ( 1600 - windowWidth ) / 24 + "px";
        bannerCancel.style.marginLeft = -( ( 1600 - windowWidth ) / 24 ) + "px";
    // 페이지 크기가 1600px 보다 큰 경우
    } else {
        container.style.height = "";
        container.style.overflowY = "";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        leftSection.style = "";
        leftDashedLine.style = "";
        leftGrayBackground.style = "";
        leftMainImgDiv.style = "";
        leftMainImg.style = "";
        leftTextArea.style = "";
        ilchonNum.style = "";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        rightSection.style = "";
        rightDashedLine.style = "";
        rightGrayBackground.style = "";
        musicLogo.style = "";
        mp3Title.style = "";
        bgm.style = "";
        search.style = "";
        dotory.style = "";
        miniRoomImg.style = "";
        ilchonpyeongDiv.style = "";
        ilchonpyeongInput.style = "";
        ilchonpyeongBtn.style = "";
        banner.style = "";
        title.style = "";
        img_1.style = "";
        img_2.style = "";
        img_3.style = "";
        blink1.style = "";
        blink2.style = "";
        blink3.style = "";
        img1.style = "";
        img2.style = "";
        img3.style = "";
    }
}

// 초기 페이지 로드 시 이미지 크기 조절
adjustImageSize();

// 페이지 크기 변경 이벤트 처리
window.addEventListener("resize", adjustImageSize);