package issueissyu.backend.domain.user.service.command;

import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.auth.service.AuthService;
import issueissyu.backend.domain.location.dto.res.UserLocationCertResDto;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.domain.location.exception.code.LocationErrorCode;
import issueissyu.backend.domain.location.service.LocationService;
import issueissyu.backend.domain.user.dto.req.TermReqDTO;
import issueissyu.backend.domain.user.dto.res.TermResDTO;
import issueissyu.backend.domain.user.dto.res.UserAlarmToggleOutcome;
import issueissyu.backend.domain.user.dto.res.UserAlarmToggleResDTO;
import issueissyu.backend.domain.user.entity.Term;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.entity.mapping.UserTerm;
import issueissyu.backend.domain.user.enums.TermName;
import issueissyu.backend.domain.user.enums.UserAlarmType;
import issueissyu.backend.domain.user.exception.code.UserSuccessCode;
import issueissyu.backend.domain.user.repository.TermRepository;
import issueissyu.backend.domain.user.exception.UserException;
import issueissyu.backend.domain.user.exception.code.UserErrorCode;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.repository.UserTermRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.postgresql.geometric.PGpoint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private static final List<TermName> TARGET_TERMS = List.of(
            TermName.SERVICE,
            TermName.PRIVACY,
            TermName.LOCATION,
            TermName.MARKETING
    );

    private final UserRepository userRepository;
    private final TermRepository termRepository;
    private final UserTermRepository userTermRepository;
    private final AuthService authService;
    private final LocationService locationService;

    @Override
    public TermResDTO agreeTerms(String uid, TermReqDTO request) {
        boolean serviceTerm = Boolean.TRUE.equals(request.getServiceTerm());
        boolean privacyTerm = Boolean.TRUE.equals(request.getPrivacyTerm());
        boolean locationTerm = Boolean.TRUE.equals(request.getLocationTerm());
        boolean marketingTerm = Boolean.TRUE.equals(request.getMarketingTerm());

        boolean isTerm = serviceTerm && privacyTerm;

        // 필수 약관 미동의 시 에러 응답(result=null) 반환
        if (!isTerm) {
            throw AuthException.of(AuthErrorCode.TERM_400);
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        Map<TermName, Boolean> agreements = new EnumMap<>(TermName.class);
        agreements.put(TermName.SERVICE, serviceTerm);
        agreements.put(TermName.PRIVACY, privacyTerm);
        agreements.put(TermName.LOCATION, locationTerm);
        agreements.put(TermName.MARKETING, marketingTerm);

        // term 4종류를 IN 조회로 한 번에 로드
        List<Term> terms = termRepository.findAllByTermNameIn(TARGET_TERMS);
        Map<TermName, Term> termByName = new EnumMap<>(TermName.class);
        for (Term term : terms) {
            termByName.put(term.getTermName(), term);
        }

        // term 마스터 데이터가 비어있는 경우를 대비해 누락된 항목만 생성
        List<Term> missingTerms = TARGET_TERMS.stream()
                .filter(termName -> !termByName.containsKey(termName))
                .map(termName -> Term.builder().termName(termName).build())
                .toList();
        if (!missingTerms.isEmpty()) {
            List<Term> savedTerms = termRepository.saveAll(missingTerms);
            for (Term term : savedTerms) {
                termByName.put(term.getTermName(), term);
            }
        }

        // user_term도 IN 조회로 한 번에 로드
        List<UserTerm> existingUserTerms = userTermRepository.findAllByUserAndTermIn(user, termByName.values());
        Map<TermName, UserTerm> userTermByName = new EnumMap<>(TermName.class);
        for (UserTerm userTerm : existingUserTerms) {
            userTermByName.put(userTerm.getTerm().getTermName(), userTerm);
        }

        // 메모리 상에서 upsert 대상 구성 후 batch save
        List<UserTerm> toSave = new ArrayList<>();
        for (TermName termName : TARGET_TERMS) {
            UserTerm userTerm = userTermByName.get(termName);
            if (userTerm == null) {
                userTerm = UserTerm.builder()
                        .user(user)
                        .term(termByName.get(termName))
                        .build();
            }
            userTerm.changeAgreement(Boolean.TRUE.equals(agreements.get(termName)));
            toSave.add(userTerm);
        }
        userTermRepository.saveAll(toSave);

        // MARKETING 동의 여부에 따라 4개 알람 상태를 동일하게 반영
        user.updateAlarmAgreement(marketingTerm);

        return buildResult(marketingTerm);
    }

    @Override
    public void changeNickname(String uid, String nickname) {
        String trimmed = nickname == null ? "" : nickname.trim();
        if (!authService.isValidNicknameFormat(trimmed)) {
            throw UserException.of(UserErrorCode.USER_NICKNAME_400);
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        if (Objects.equals(trimmed, user.getNickname())) {
            return;
        }
        if (userRepository.existsByNickname(trimmed)) {
            throw UserException.of(UserErrorCode.USER_NICKNAME_409);
        }

        user.updateNickname(trimmed);
    }

    @Override
    public UserLocationCertResDto changeUserRegion(String uid, double lat, double lng) {
        validateLatLngForRegion(lat, lng);

        User user =
                userRepository
                        .findById(uid)
                        .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));
        assertEligibleForMonthlyRegionChange(user);

        return locationService.userLocationCert(uid, new PGpoint(lng, lat));
    }

    @Override
    public UserAlarmToggleOutcome toggleUserAlarm(String uid, UserAlarmType alarmType) {
        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        return switch (alarmType) {
            case LIKE -> {
                user.toggleLikeAlarm();
                yield new UserAlarmToggleOutcome(
                        UserSuccessCode.USER_ALARM_200_1,
                        UserAlarmToggleResDTO.builder().likeAlarmActive(user.isLikeAlarmActive()).build());
            }
            case EVENT -> {
                user.toggleEventAlarm();
                yield new UserAlarmToggleOutcome(
                        UserSuccessCode.USER_ALARM_200_2,
                        UserAlarmToggleResDTO.builder().eventAlarmActive(user.isEventAlarmActive()).build());
            }
            case HOT -> {
                user.toggleHotAlarm();
                yield new UserAlarmToggleOutcome(
                        UserSuccessCode.USER_ALARM_200_3,
                        UserAlarmToggleResDTO.builder().hotAlarmActive(user.isHotAlarmActive()).build());
            }
            case STORE -> {
                user.toggleStoreAlarm();
                yield new UserAlarmToggleOutcome(
                        UserSuccessCode.USER_ALARM_200_4,
                        UserAlarmToggleResDTO.builder().storeAlarmActive(user.isStoreAlarmActive()).build());
            }
        };
    }

    private static void assertEligibleForMonthlyRegionChange(User user) {
        LocalDateTime last = user.getUserPointUpdated();
        if (last == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (last.plusMonths(1).isAfter(now)) {
            throw LocationException.of(LocationErrorCode.LOCATION_REGION_CHANGE_TOO_SOON);
        }
    }

    private static void validateLatLngForRegion(double lat, double lng) {
        if (Double.isNaN(lat)
                || Double.isNaN(lng)
                || Double.isInfinite(lat)
                || Double.isInfinite(lng)
                || lat < -90
                || lat > 90
                || lng < -180
                || lng > 180) {
            throw LocationException.of(LocationErrorCode.LOCATION_INVALID_REQUEST);
        }
    }

    private static TermResDTO buildResult(boolean marketingTerm) {
        return TermResDTO.builder()
                .eventAlarmActive(marketingTerm)
                .likeAlarmActive(marketingTerm)
                .hotAlarmActive(marketingTerm)
                .storeAlarmActive(marketingTerm)
                .build();
    }
}
