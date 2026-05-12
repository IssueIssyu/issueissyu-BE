package issueissyu.backend.domain.map.exception.code;

import issueissyu.backend.domain.pin.enums.PinType;
import issueissyu.backend.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MapSuccessCode implements BaseSuccessCode {

    MAP_200_1(HttpStatus.OK, "MAP_200_1", "현재 화면의 전체 핀 조회에 성공했습니다."),
    MAP_200_2(HttpStatus.OK, "MAP_200_2", "현재 화면의 이슈 핀 조회에 성공했습니다."),
    MAP_200_3(HttpStatus.OK, "MAP_200_3", "현재 화면의 소통 핀 조회에 성공했습니다."),
    MAP_200_4(HttpStatus.OK, "MAP_200_4", "현재 화면의 가게 핀 조회에 성공했습니다."),
    MAP_200_5(HttpStatus.OK, "MAP_200_5", "현재 화면의 축제 핀 조회에 성공했습니다."),

    MAP_CARD_200_1(HttpStatus.OK, "MAP_CARD_200_1", "이슈 핀 카드 조회에 성공했습니다."),
    MAP_CARD_200_2(HttpStatus.OK, "MAP_CARD_200_2", "소통 핀 카드 조회에 성공했습니다."),
    MAP_CARD_200_3(HttpStatus.OK, "MAP_CARD_200_3", "가게 핀 카드 조회에 성공했습니다."),
    MAP_CARD_200_4(HttpStatus.OK, "MAP_CARD_200_4", "축제 핀 카드 조회에 성공했습니다."),

    MAP_NOTICE_200(HttpStatus.OK, "MAP_NOTICE_200", "공지사항 조회에 성공했습니다."),
    MAP_NOTICE_204(HttpStatus.OK, "MAP_NOTICE_204", "등록된 공지가 없습니다."),

    PATCHNOTE_200(HttpStatus.OK, "PATCHNOTE_200", "현재 지역의 패치노트 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    public static MapSuccessCode forPinCard(PinType type) {
        return switch (type) {
            case ISSUE -> MAP_CARD_200_1;
            case COMMUNICATION -> MAP_CARD_200_2;
            case STORE -> MAP_CARD_200_3;
            case FESTIVAL -> MAP_CARD_200_4;
        };
    }
}
