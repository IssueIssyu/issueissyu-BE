package issueissyu.backend.domain.community.repository;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    boolean existsByPin_PinId(Long pinId);

    Optional<Community> findByPin_PinId(Long pinId);

    @Query("""
            select c
            from Community c
            left join fetch c.pin p
            left join fetch p.user
            left join fetch c.cardnewsImages
            where c.communityId = :id
            """)
    Optional<Community> findDetailById(@Param("id") Long communityId);

    // 지역 기반 단일 타입 피드 조회.
    @Query("""
            select c
            from Community c
            join fetch c.pin p
            join fetch p.user
            where c.communityType = :type
              and exists (
                    select 1
                    from PinLocation pl
                    where pl.pin = p
                      and pl.location.region = :regionCode
              )
              and (
                    cast(:cursorCreatedAt as LocalDateTime) is null
                    or c.createdAt < :cursorCreatedAt
                    or (c.createdAt = :cursorCreatedAt and c.communityId < :cursorId)
              )
            order by c.createdAt desc, c.communityId desc
            """)
    List<Community> findFeedByTypeAndRegion(
            @Param("type") CommunityType type,
            @Param("regionCode") String regionCode,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // 전역 단일 타입 피드 조회
    @Query("""
            select c
            from Community c
            where c.communityType = :type
              and (
                    cast(:cursorCreatedAt as LocalDateTime) is null
                    or c.createdAt < :cursorCreatedAt
                    or (c.createdAt = :cursorCreatedAt and c.communityId < :cursorId)
              )
            order by c.createdAt desc, c.communityId desc
            """)
    List<Community> findFeedByType(
            @Param("type") CommunityType type,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // 지역 기반 여러 타입 피드 조회.
    @Query("""
            select c
            from Community c
            join fetch c.pin p
            join fetch p.user
            where c.communityType in :types
              and exists (
                    select 1
                    from PinLocation pl
                    where pl.pin = p
                      and pl.location.region = :regionCode
              )
              and (
                    cast(:cursorCreatedAt as LocalDateTime) is null
                    or c.createdAt < :cursorCreatedAt
                    or (c.createdAt = :cursorCreatedAt and c.communityId < :cursorId)
              )
            order by c.createdAt desc, c.communityId desc
            """)
    List<Community> findFeedByTypesAndRegion(
            @Param("types") Collection<CommunityType> types,
            @Param("regionCode") String regionCode,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /**
     * ALL 피드 조회.
     *
     * 지역 기반 게시글:
     * - ISSUE / STORE / FESTIVAL / COMMUNICATION
     * - pin이 있고, PinLocation.region이 regionCode와 일치해야 한다.
     *
     * 전역 게시글:
     * - POLICY / CONTEST / CARDNEWS
     * - 지역 필터 없이 함께 조회한다.
     */
    @Query("""
            select c
            from Community c
            left join fetch c.pin p
            left join fetch p.user
            where (
                    (
                        c.communityType in :pinBasedTypes
                        and p is not null
                        and exists (
                            select 1
                            from PinLocation pl
                            where pl.pin = p
                              and pl.location.region = :regionCode
                        )
                    )
                    or c.communityType in :globalTypes
              )
              and (
                    cast(:cursorCreatedAt as LocalDateTime) is null
                    or c.createdAt < :cursorCreatedAt
                    or (c.createdAt = :cursorCreatedAt and c.communityId < :cursorId)
              )
            order by c.createdAt desc, c.communityId desc
            """)
    List<Community> findFeedByRegionOrGlobalTypes(
            @Param("pinBasedTypes") Collection<CommunityType> pinBasedTypes,
            @Param("globalTypes") Collection<CommunityType> globalTypes,
            @Param("regionCode") String regionCode,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /**
     * HOT 피드 조회.
     *
     * 지역 기반 HOT:
     * - ISSUE / STORE / FESTIVAL / COMMUNICATION
     * - 해당 지역에 속한 pin 기반 게시글
     *
     * 전역 HOT:
     * - POLICY / CONTEST / CARDNEWS
     * - 지역 필터 없이 함께 조회
     *
     * popularity는 스케줄러에서 주기적으로 재계산된 값을 사용한다.
     */
    @Query("""
            select c
            from Community c
            left join fetch c.pin p
            left join fetch p.user
            where (
                    (
                        c.communityType in :pinBasedTypes
                        and p is not null
                        and exists (
                            select 1
                            from PinLocation pl
                            where pl.pin = p
                              and pl.location.region = :regionCode
                        )
                    )
                    or c.communityType in :globalTypes
              )
              and c.createdAt >= :since
              and (
                    cast(:cursorPopularity as double) is null
                    or c.popularity < :cursorPopularity
                    or (c.popularity = :cursorPopularity and c.communityId < :cursorId)
              )
            order by c.popularity desc, c.communityId desc
            """)
    List<Community> findHotFeedByRegionOrGlobalTypes(
            @Param("pinBasedTypes") Collection<CommunityType> pinBasedTypes,
            @Param("globalTypes") Collection<CommunityType> globalTypes,
            @Param("regionCode") String regionCode,
            @Param("since") LocalDateTime since,
            @Param("cursorPopularity") Double cursorPopularity,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    // 인기도 재계산 스케줄러에서 사용할 후보 조회
    @Query("""
            select distinct c
            from Community c
            left join fetch c.pin p
            left join fetch p.user
            where c.createdAt >= :since
            """)
    List<Community> findPopularityUpdateTargets(@Param("since") LocalDateTime since);
}