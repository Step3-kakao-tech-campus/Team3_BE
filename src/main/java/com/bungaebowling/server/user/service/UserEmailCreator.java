package com.bungaebowling.server.user.service;

import org.springframework.stereotype.Service;

@Service
public class UserEmailCreator {

    public String createEmailVerificationMail(String link) {
        return """
                <div style='font-family: 'Apple SD Gothic Neo', sans-serif !important; width: 540px; height: 600px; margin: 100px auto; padding-bottom: 30px; box-sizing: border-box;'>
                    <div style='margin-right: 0.25rem; border-bottom: 4px solid #fe7e07; margin-bottom: 30px; padding-bottom: 10px;'>
                        <img src='https://bungaebowling-img-s3.s3.ap-northeast-2.amazonaws.com/LogoTitle-Orange.png' height='45' alt='title'>
                    </div>
                    <h1 style='margin: 0; padding: 0 5px; font-size: 28px; font-weight: 400;'>
                        <span style='color: #fe7e07;'>메일인증</span> 안내입니다.
                    </h1>
                    <p style='font-size: 16px; line-height: 26px; margin-top: 50px; padding: 0 5px;'>
                        안녕하세요.<br/>
                        번개볼링에 가입해 주셔서 진심으로 감사드립니다.<br/>
                        아래 <b style='color: #fe7e07;'>'메일 인증'</b> 버튼을 클릭하여 회원가입을 완료해 주세요.<br/>
                        감사합니다.
                    </p>
                    <div style='text-align: center;'>
                        <a style='color: #FFF; text-decoration: none; text-align: center;' href='%s' target='_blank'>
                            <p style='display: inline-block; width: 210px; height: 45px; margin: 30px 5px 40px; background: #fe7e07; line-height: 45px; vertical-align: middle; font-size: 16px; font-weight: bold; border-radius: 5px'>
                                메일 인증
                            </p>
                        </a>
                    </div>
                                
                    <div style='border-top: 1px solid #DDD; padding: 5px;'>
                        <p style='font-size: 13px; line-height: 21px; color: #555;'>
                            만약 버튼이 정상적으로 클릭되지 않는다면, 아래 링크를 복사하여 접속해 주세요.<br/>
                            %s
                        </p>
                    </div>
                </div>
                """.formatted(link, link);
    }
}
