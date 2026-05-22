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

    /**
     * 상세 조회.
     *
     * 모든 Community는 Pin 기반이므로 pin/user를 함께 조회한다.
     * CARDNEWS는 cardnewsImages도 함께 조회한다.
     */
    @Query("""
            select c
            from Community c
            join fetch c.pin p
            join fetch p.user
            left join fetch c.cardnewsImages
            where c.communityId = :id
            """)
    Optional<Community> findDetailById(@Param("id") Long communityId);

    /**
     * 지역 기반 단일 타입 피드 조회.
     *
     * ISSUE / STORE / FESTIVAL / COMMUNICATION처럼
     * 지역 필터가 필요한 타입에서 사용한다.
     */
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

    /**
     * 지역 기반 복수 타입 피드 조회.
     *
     * HOME 동네 최근 소식 등에서
     * ISSUE / STORE / FESTIVAL / COMMUNICATION만 조회할 때 사용한다.
     */
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
     * 전역 단일 타입 피드 조회.
     *
     * POLICY / CONTEST / CARDNEWS처럼
     * 지역 필터를 사용하지 않는 타입에서 사용한다.
     *
     * 단, 모든 Community는 Pin 기반이므로 pin/user는 함께 조회한다.
     */
    @Query("""
            select c
            from Community c
            join fetch c.pin p
            join fetch p.user
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

    /**
     * ALL 피드 조회.
     *
     * 지역 기반 타입:
     * - ISSUE / STORE / FESTIVAL / COMMUNICATION
     * - PinLocation.region = regionCode 조건 적용
     *
     * 전역 타입:
     * - POLICY / CONTEST / CARDNEWS
     * - 지역 조건 없이 함께 조회
     */
    @Query("""
            select c
            from Community c
            join fetch c.pin p
            join fetch p.user
            where (
                    (
                        c.communityType in :regionTypes
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
            @Param("regionTypes") Collection<CommunityType> regionTypes,
            @Param("globalTypes") Collection<CommunityType> globalTypes,
            @Param("regionCode") String regionCode,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /**
     * HOT 피드 조회.
     *
     * 지역 기반 타입:
     * - ISSUE / STORE / FESTIVAL / COMMUNICATION
     * - PinLocation.region = regionCode 조건 적용
     *
     * 전역 타입:
     * - POLICY / CONTEST / CARDNEWS
     * - 지역 조건 없이 함께 조회
     *
     * popularity는 스케줄러에서 주기적으로 재계산된 값을 사용한다.
     */
    @Query("""
            select c
            from Community c
            join fetch c.pin p
            join fetch p.user
            where (
                    (
                        c.communityType in :regionTypes
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
            @Param("regionTypes") Collection<CommunityType> regionTypes,
            @Param("globalTypes") Collection<CommunityType> globalTypes,
            @Param("regionCode") String regionCode,
            @Param("since") LocalDateTime since,
            @Param("cursorPopularity") Double cursorPopularity,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    /**
     * 인기도 재계산 스케줄러에서 사용할 후보 조회.
     *
     * 1차 구현에서는 HOT 노출 대상과 동일하게 최근 7일 게시글만 재계산한다.
     * 모든 Community는 Pin 기반이므로 pin/user를 함께 조회한다.
     */
    @Query("""
            select c
            from Community c
            join fetch c.pin p
            join fetch p.user
            where c.createdAt >= :since
            """)
    List<Community> findPopularityUpdateTargets(@Param("since") LocalDateTime since);
}