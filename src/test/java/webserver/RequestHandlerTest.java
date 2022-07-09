package webserver;

import org.junit.Test;
import util.SplitUtils;

import static org.junit.Assert.*;

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

}