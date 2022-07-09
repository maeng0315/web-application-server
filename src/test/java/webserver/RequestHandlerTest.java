package webserver;

import model.User;
import org.junit.Test;
import util.ParserUtils;
import util.SplitUtils;

import java.net.URLDecoder;
import java.net.URLEncoder;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class RequestHandlerTest {

    @Test
    public void HTTP_HEADER_URL_추출() {
        // Given (준비) | 테스트 준비 과정, 테스트에 사용 되는 변수, 입력 값 등을 정의, Mock 객체 정의
        String httpHeader = "GET /index.html HTTP/1.1";

        // When (실행) | 실제로 액션을 하는 테스트를 실행하는 과정이다.
        String url = SplitUtils.getUrl(httpHeader);

        // Then (검증) | 마지막은 테스트를 검증하는 과정이다. 예상한 값, 실제 실행을 통해서 나온 값을 검증한다.
        assertEquals("/index.html", url);
    }

    @Test
    public void GET_URL_리퀘스트패스_쿼리파라미터_분리() {
        // given
        String url = "/user/create?userId=maeng0315&password=40492923aA%21&name=parkmj&email=dakma%40naver.com";

        // when
        String requestPath = ParserUtils.getRequestPath(url);
        String queryString = ParserUtils.getQueryString(url);

        // then
        assertEquals(requestPath, "/user/create");
        assertEquals(queryString, "userId=maeng0315&password=40492923aA%21&name=parkmj&email=dakma%40naver.com");
    }

    @Test
    public void GET_쿼리파라미터_User객체_생성() {
        // Given (준비)
        String queryString = "userId=maeng0315&password=40492923aA%21&name=parkmj&email=dakma%40naver.com";
        User mockUser = new User("maeng0315", "40492923aA!", "parkmj", "dakma@naver.com");

        // When (실행)
        User user = ParserUtils.getUser(queryString);

        // Then (검증)
        assertTrue(user.equals(mockUser));
    }

    @Test
    public void 특수문자_변환() {
        String decodeString = "%40";
        String decodeingString = URLDecoder.decode(decodeString);
        assertEquals(decodeingString, "@");

        String encodeString = "@";
        String encodeingString = URLEncoder.encode(encodeString);
        assertEquals(encodeingString, "%40");
    }

}