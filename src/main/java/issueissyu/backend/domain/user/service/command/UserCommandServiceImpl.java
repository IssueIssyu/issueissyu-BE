package issueissyu.backend.domain.user.service.command;

import issueissyu.backend.domain.auth.exception.AuthException;
import issueissyu.backend.domain.auth.exception.code.AuthErrorCode;
import issueissyu.backend.domain.user.dto.req.TermReqDTO;
import issueissyu.backend.domain.user.dto.res.TermResDTO;
import issueissyu.backend.domain.user.entity.Term;
import issueissyu.backend.domain.user.entity.User;
import issueissyu.backend.domain.user.entity.mapping.UserTerm;
import issueissyu.backend.domain.user.enums.TermName;
import issueissyu.backend.domain.user.repository.TermRepository;
import issueissyu.backend.domain.user.repository.UserRepository;
import issueissyu.backend.domain.user.repository.UserTermRepository;
import issueissyu.backend.global.api.code.GeneralErrorCode;
import issueissyu.backend.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

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

    private static TermResDTO buildResult(boolean marketingTerm) {
        return TermResDTO.builder()
                .eventAlarmActive(marketingTerm)
                .likeAlarmActive(marketingTerm)
                .hotAlarmActive(marketingTerm)
                .storeAlarmActive(marketingTerm)
                .build();
    }
}
