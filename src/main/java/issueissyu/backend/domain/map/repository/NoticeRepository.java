package issueissyu.backend.domain.map.repository;

import issueissyu.backend.domain.map.entity.Notice;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("""
            select n from Notice n
            join fetch n.pin
            where :now >= n.noticeStartTime and :now <= n.noticeEndTime
            order by n.noticeId asc""")
    List<Notice> findActiveAt(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Notice n WHERE n.pin.pinId = :pinId")
    void deleteByPin_PinId(@Param("pinId") Long pinId);
}
