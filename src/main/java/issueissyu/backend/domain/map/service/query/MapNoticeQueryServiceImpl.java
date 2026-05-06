package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.map.dto.res.MapNoticeItemResDTO;
import issueissyu.backend.domain.map.dto.res.MapNoticeListResDTO;
import issueissyu.backend.domain.map.entity.Notice;
import issueissyu.backend.domain.map.repository.NoticeRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapNoticeQueryServiceImpl implements MapNoticeQueryService {

    private final NoticeRepository noticeRepository;

    @Override
    public MapNoticeListResDTO getActiveNotices() {
        LocalDateTime now = LocalDateTime.now();
        List<MapNoticeItemResDTO> notices = noticeRepository.findActiveAt(now).stream().map(this::toItem).toList();
        return MapNoticeListResDTO.builder().notices(notices).build();
    }

    private MapNoticeItemResDTO toItem(Notice n) {
        return MapNoticeItemResDTO.builder()
                .noticeId(n.getNoticeId())
                .pinId(n.getPin().getPinId())
                .noticeContent(n.getNoticeContent())
                .build();
    }
}
