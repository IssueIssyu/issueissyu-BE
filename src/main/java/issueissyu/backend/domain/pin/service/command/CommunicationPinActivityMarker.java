package issueissyu.backend.domain.pin.service.command;

import issueissyu.backend.domain.pin.repository.CommunicationPinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommunicationPinActivityMarker {

    private final CommunicationPinRepository communicationPinRepository;

    @Transactional
    public void markReactionOrComment(Long pinId) {
        communicationPinRepository.bumpUpdatedAt(pinId, LocalDateTime.now());
    }
}
