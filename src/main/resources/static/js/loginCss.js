function adjustImageSize() {
    // 페이지 크기
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    const container = document.getElementsByClassName("container")[0];
    const section = document.getElementsByClassName("section")[0];
    const dashedLine = document.getElementsByClassName("dashed-line")[0];
    const grayBackground = document.getElementsByClassName("gray-background")[0];
    const main = document.getElementsByClassName("main")[0];

    const loginMinimi = document.getElementsByClassName("login-minimi")[0];

    const userID = document.getElementsByClassName("userID")[0];
    const login = document.getElementsByClassName("login")[0];
    const loginInput = document.getElementsByClassName("login-input")[0];
    const btnSocial = document.getElementsByClassName("btn-social")[0];
    const btnGroup = document.getElementsByClassName("btn-group")[0];

    // 페이지 크기가 1600px 보다 작거나 같은 경우
    if ( windowWidth <= 1600 ) {
        // 페이지 크기가 1000px 보다 작거나 같은 경우
        if ( windowWidth <= 1000 ) {
            section.style.width = 90 + "%";

            dashedLine.style.width = 97.6 - ( 1000 - windowWidth ) * 0.003 + "%";
            grayBackground.style.width = 98.9 - ( 1000 - windowWidth ) * 0.001 + "%";
            main.style.width = 94.2 - ( 1000 - windowWidth ) * 0.007 + "%";

            login.style.marginTop = 4 + ( 1000 - windowWidth ) * 0.01 + "%";
            loginInput.style.width = 77 + ( 1000 - windowWidth ) * 0.016 + "%";
            btnSocial.style.width = 77 + ( 1000 - windowWidth ) * 0.016 + "%";
            btnGroup.style.width = 77 + ( 1000 - windowWidth ) * 0.016 + "%";
        } else {
            section.style.width = "";

            dashedLine.style.width = 97.5 - ( 1600 - windowWidth ) * 0.003 + "%";
            grayBackground.style.width = 98.5 - ( 1600 - windowWidth ) * 0.001 + "%";
            main.style.width = 93.5 - ( 1600 - windowWidth ) * 0.007 + "%";

            login.style.marginTop = 4 + ( 1600 - windowWidth ) * 0.01 + "%";
            loginInput.style.width = 79 + ( 1600 - windowWidth ) * 0.006 + "%";
            btnSocial.style.width = 79 + ( 1600 - windowWidth ) * 0.006 + "%";
            btnGroup.style.width = 79 + ( 1600 - windowWidth ) * 0.006 + "%";
        }
    // 페이지 크기가 1600px 보다 큰 경우
    } else {
        section.style.width = "";

        dashedLine.style.width = "";
        grayBackground.style.width = "";
        main.style.width = "";

        login.style.marginTop = "";
        loginInput.style.width = "";
        btnSocial.style.width = "";
        btnGroup.style.width = "";
    }

    if ( windowHeight < 860 ) {
        container.style.height = ( windowHeight - 70 ) + "px";
        container.style.overflowY = "scroll";
    } else {
        container.style.overflowY = "hidden";
        if ( windowHeight >= 1060 ) {
            if ( windowHeight <= 1720 ) {
                container.style.height = windowHeight + "px";
                section.style.height = "94%";

                dashedLine.style.height = 94 + ( windowHeight - 1060 ) * 0.003 + "%";
                grayBackground.style.height = 98 + ( windowHeight - 1060 ) * 0.001 + "%";
                main.style.height = 95 + ( windowHeight - 1060 ) * 0.00227 + "%";

                loginMinimi.style.marginTop = ( windowHeight - 1060 ) * 0.06 + "%";

                if ( windowWidth <= 1000 ) {
                    login.style.marginTop = 4 + ( 1000 - windowWidth ) * 0.01 + "%";
                }
                userID.style.marginTop = "20%";
            } else {
                container.style.height = "1720px";
                section.style.height = "94%";

                dashedLine.style.height = 94 + ( 1720 - 1060 ) * 0.003 + "%";
                grayBackground.style.height = 98 + ( 1720 - 1060 ) * 0.001 + "%";
                main.style.height = 95 + ( 1720 - 1060 ) * 0.00227 + "%";

                loginMinimi.style.marginTop = ( 1720 - 1060 ) * 0.06 + "%";

                if ( windowWidth <= 1000 ) {
                    login.style.marginTop = 4 + ( 1000 - windowWidth ) * 0.01 + "%";
                }
                userID.style.marginTop = "20%";
            }
        } else {
            container.style.height = "";
            section.style.height = "";

            dashedLine.style.height = "";
            grayBackground.style.height = "";
            main.style.height = "";

            loginMinimi.style.marginTop = "";

            userID.style.marginTop = "";
        }
    }
}

// 초기 페이지 로드 시 이미지 크기 조절
window.addEventListener("load", adjustImageSize);

// 페이지 크기 변경 이벤트 처리
window.addEventListener("resize", adjustImageSize);