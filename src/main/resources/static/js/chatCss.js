function adjustImageSize() {
    // 페이지 크기
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 왼쪽 섹션
    const leftSection = document.getElementsByClassName("left-section")[0];
    const leftDashedLine = document.getElementsByClassName("left-dashed-line")[0];
    const leftGrayBackground = document.getElementsByClassName("left-gray-background")[0];

    const searchBox = document.getElementsByClassName("searchBox")[0];
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 오른쪽 섹션
    const rightSection = document.getElementsByClassName("right-section")[0];
    const rightDashedLine = document.getElementsByClassName("right-dashed-line")[0];
    const rightGrayBackground = document.getElementsByClassName("right-gray-background")[0];
    const rightAside = document.getElementsByClassName("right-aside")[0];

    // 페이지 크기가 1600px 보다 작거나 같은 경우
    if ( windowWidth <= 1600 ) {
        // 왼쪽 섹션
        leftSection.style.width = windowWidth - 80 + "px";
        leftDashedLine.style.width = windowWidth - 145 + "px";
        leftGrayBackground.style.width = windowWidth - 175 + "px";

        searchBox.style.left = -718 - ( 1600 - windowWidth ) / 42.86 + "px";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 오른쪽 섹션
        rightSection.style.width = windowWidth - 80 + "px";
        rightDashedLine.style.width = windowWidth - 145 + "px";
        rightGrayBackground.style.width = windowWidth - 175 + "px";
        rightAside.style.width = 1383.2 - ( 1600 - windowWidth ) + "px";
    // 페이지 크기가 1600px 보다 큰 경우
    } else {
        leftSection.style = "";
        leftDashedLine.style = "";
        leftGrayBackground.style = "";
        searchBox.style = "";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        rightSection.style = "";
        rightDashedLine.style = "";
        rightGrayBackground.style = "";
        rightAside.style = "";
    }
}

// 초기 페이지 로드 시 이미지 크기 조절
adjustImageSize();

// 페이지 크기 변경 이벤트 처리
window.addEventListener("resize", adjustImageSize);