package issueissyu.backend.domain.user.service.command;

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

@Service
@RequiredArgsConstructor
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

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
        TermResDTO result = buildResult(isTerm, marketingTerm);

        // 필수 약관 미동의 시 실패 응답(result)만 반환하고 DB 반영은 하지 않는다.
        if (!isTerm) {
            return result;
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() -> GeneralException.of(GeneralErrorCode.USER_NOT_FOUND));

        upsertUserTerm(user, TermName.SERVICE, serviceTerm);
        upsertUserTerm(user, TermName.PRIVACY, privacyTerm);
        upsertUserTerm(user, TermName.LOCATION, locationTerm);
        upsertUserTerm(user, TermName.MARKETING, marketingTerm);

        // MARKETING 동의 여부에 따라 4개 알람 상태를 동일하게 반영
        user.updateAlarmAgreement(marketingTerm);

        return result;
    }

    private void upsertUserTerm(User user, TermName termName, boolean agreed) {
        Term term = termRepository.findByTermName(termName)
                .orElseGet(() -> termRepository.save(
                        Term.builder().termName(termName).build()
                ));

        UserTerm userTerm = userTermRepository.findByUserAndTerm(user, term)
                .orElseGet(() -> UserTerm.builder()
                        .user(user)
                        .term(term)
                        .build());
        userTerm.changeAgreement(agreed);
        userTermRepository.save(userTerm);
    }

    private static TermResDTO buildResult(boolean isTerm, boolean marketingTerm) {
        return TermResDTO.builder()
                .isTerm(isTerm)
                .eventAlarmActive(marketingTerm)
                .likeAlarmActive(marketingTerm)
                .hotAlarmActive(marketingTerm)
                .storeAlarmActive(marketingTerm)
                .build();
    }
}
