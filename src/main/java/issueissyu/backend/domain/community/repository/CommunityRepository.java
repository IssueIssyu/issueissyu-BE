package issueissyu.backend.domain.community.repository;

import issueissyu.backend.domain.community.entity.Community;
import issueissyu.backend.domain.community.enums.CommunityType;
import issueissyu.backend.domain.location.enums.RegionCode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    @Query("""
            select c
            from Community c
            join fetch c.pin p
            join fetch p.user
            where c.communityId = :id
            """)
    Optional<Community> findDetailById(@Param("id") Long communityId);

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
                      and pl.location.region = :region
              )
              and (
                    :cursorCreatedAt is null
                    or c.createdAt < :cursorCreatedAt
                    or (c.createdAt = :cursorCreatedAt and c.communityId < :cursorId)
              )
            order by c.createdAt desc, c.communityId desc
            """)
    List<Community> findFeedByTypeAndRegion(
            @Param("type") CommunityType type,
            @Param("region") RegionCode region,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

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
                      and pl.location.region = :region
              )
              and (
                    :cursorCreatedAt is null
                    or c.createdAt < :cursorCreatedAt
                    or (c.createdAt = :cursorCreatedAt and c.communityId < :cursorId)
              )
            order by c.createdAt desc, c.communityId desc
            """)
    List<Community> findFeedByTypesAndRegion(
            @Param("types") Collection<CommunityType> types,
            @Param("region") RegionCode region,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );
}
