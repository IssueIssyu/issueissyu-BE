package issueissyu.backend.domain.map.service.query;

import issueissyu.backend.domain.issue.enums.IssuePinState;
import issueissyu.backend.domain.location.exception.LocationException;
import issueissyu.backend.domain.location.repository.LocationRepository;
import issueissyu.backend.domain.location.service.LocationService;
import issueissyu.backend.domain.map.dto.res.PatchNotePageInfoResDTO;
import issueissyu.backend.domain.map.dto.res.PatchNotePinItemResDTO;
import issueissyu.backend.domain.map.dto.res.PatchNoteResDTO;
import issueissyu.backend.domain.map.exception.MapException;
import issueissyu.backend.domain.map.exception.code.MapErrorCode;
import issueissyu.backend.domain.map.repository.PatchNotePinRow;
import issueissyu.backend.domain.map.repository.PatchNoteRepository;
import issueissyu.backend.domain.map.support.PatchNoteCursorCodec;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatchNoteQueryServiceImpl implements PatchNoteQueryService {

    private static final int SIZE_DEFAULT = 10;
    private static final int SIZE_MIN = 1;
    private static final int SIZE_MAX = 100;

    // 커서 미적용 시 ROW 비교용 더미 값(실제로는 applyCursor=false 이므로 비교되지 않음)
    private static final LocalDateTime CURSOR_DUMMY_TIME = LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    private final PatchNoteRepository patchNoteRepository;
    private final LocationRepository locationRepository;
    private final LocationService locationService;
    private final PatchNoteCursorCodec patchNoteCursorCodec;

    @Override
    public PatchNoteResDTO getPatchNotes(String uid, String region, Integer size, String cursor) {
        try {
            int pageSize = resolveSize(size);
            String resolvedRegion = resolveRegion(uid, region);

            boolean applyCursor = StringUtils.hasText(cursor);
            int cursorRank = 0;
            LocalDateTime cursorCreatedAt = CURSOR_DUMMY_TIME;
            long cursorPinId = 0L;

            if (applyCursor) {
                PatchNoteCursorCodec.Decoded decoded = patchNoteCursorCodec.decode(cursor);
                cursorRank = decoded.rank();
                cursorCreatedAt = decoded.createdAt();
                cursorPinId = decoded.pinId();
            }

            List<PatchNotePinRow> rows =
                    patchNoteRepository.findPatchNotes(
                            resolvedRegion,
                            applyCursor,
                            cursorRank,
                            cursorCreatedAt,
                            cursorPinId,
                            pageSize + 1);

            boolean hasNext = rows.size() > pageSize;
            List<PatchNotePinRow> pageRows = hasNext ? rows.subList(0, pageSize) : rows;

            List<PatchNotePinItemResDTO> pins =
                    pageRows.stream()
                            .map(
                                    r ->
                                            new PatchNotePinItemResDTO(
                                                    r.getPinId(),
                                                    r.getPinType(),
                                                    r.getPinDetailAddress(),
                                                    r.getIssuePinState(),
                                                    r.getCreatedAt()))
                            .toList();

            String nextCursor = null;
            if (hasNext && !pageRows.isEmpty()) {
                PatchNotePinRow last = pageRows.get(pageRows.size() - 1);
                IssuePinState state = IssuePinState.valueOf(last.getIssuePinState());
                nextCursor =
                        patchNoteCursorCodec.encode(state, last.getCreatedAt(), last.getPinId());
            }

            return new PatchNoteResDTO(pins, new PatchNotePageInfoResDTO(hasNext, nextCursor));
        } catch (MapException e) {
            throw e;
        } catch (Exception e) {
            throw MapException.of(MapErrorCode.PATCHNOTE_400_5);
        }
    }

    private int resolveSize(Integer size) {
        int s = size == null ? SIZE_DEFAULT : size;
        if (s < SIZE_MIN || s > SIZE_MAX) {
            throw MapException.of(MapErrorCode.PATCHNOTE_400_2);
        }
        return s;
    }

    private String resolveRegion(String uid, String region) {
        if (StringUtils.hasText(region)) {
            String trimmed = region.trim();
            if (!locationRepository.existsByRegion(trimmed)) {
                throw MapException.of(MapErrorCode.PATCHNOTE_400_1);
            }
            return trimmed;
        }
        try {
            return locationService.getUserLocation(uid).getAddress();
        } catch (LocationException e) {
            throw MapException.of(MapErrorCode.PATCHNOTE_400_4);
        }
    }
}
