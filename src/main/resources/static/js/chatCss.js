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
    const leftAside = document.getElementsByClassName("left-aside")[0];

    const searchBox = document.getElementsByClassName("searchBox")[0];
    const memberBox = document.getElementsByClassName("memberBox")[0];
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 오른쪽 섹션
    const rightSection = document.getElementsByClassName("right-section")[0];
    const rightDashedLine = document.getElementsByClassName("right-dashed-line")[0];
    const rightGrayBackground = document.getElementsByClassName("right-gray-background")[0];
    const rightAside = document.getElementsByClassName("right-aside")[0];

    const tabLabel = document.getElementsByClassName("tabLabel");

    // 페이지 크기가 1600px 보다 작거나 같은 경우
    if ( windowWidth <= 1600 ) {
        container.style.height = "";

        leftDashedLine.style.height = "";
        leftGrayBackground.style.height = "";
        leftAside.style.height = "";
        memberBox.style.height = "";

        rightDashedLine.style.height = "";
        rightGrayBackground.style.height = "";
        rightAside.style.height = "";
        tabLabel[0].style.top = "";
        tabLabel[1].style.top = "";
        tabLabel[2].style.top = "";
        tabLabel[3].style.top = "";
        tabLabel[4].style.top = "";
        tabLabel[5].style.top = "";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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
        container.style.height = windowHeight - 40 + "px";

        leftDashedLine.style.height = windowHeight - 110 + "px";
        leftGrayBackground.style.height = windowHeight - 130 + "px";
        leftAside.style.height = windowHeight - 180 + "px";
        memberBox.style.height = windowHeight - 240 + "px";

        rightDashedLine.style.height = windowHeight - 110 + "px";
        rightGrayBackground.style.height = windowHeight - 130 + "px";
        rightAside.style.height = windowHeight - 180 + "px";
        tabLabel[0].style.top = -( windowHeight / 2 ) - 305 + "px";
        tabLabel[1].style.top = -( windowHeight / 2 ) - 305 + "px";
        tabLabel[2].style.top = -( windowHeight / 2 ) - 305 + "px";
        tabLabel[3].style.top = -( windowHeight / 2 ) - 305 + "px";
        tabLabel[4].style.top = -( windowHeight / 2 ) - 305 + "px";
        tabLabel[5].style.top = -( windowHeight / 2 ) - 305 + "px";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        leftSection.style = "";
        leftDashedLine.style.width = "";
        leftGrayBackground.style.width = "";
        searchBox.style = "";
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        rightSection.style = "";
        rightDashedLine.style.width = "";
        rightGrayBackground.style.width = "";
        rightAside.style.width = "";
    }
}

// 초기 페이지 로드 시 이미지 크기 조절
adjustImageSize();

// 페이지 크기 변경 이벤트 처리
window.addEventListener("resize", adjustImageSize);