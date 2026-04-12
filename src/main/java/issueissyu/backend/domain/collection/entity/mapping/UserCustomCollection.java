package issueissyu.backend.domain.collection.entity.mapping;

import issueissyu.backend.domain.collection.entity.CustomCollection;
import issueissyu.backend.domain.user.entity.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "user_custom_collection")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UserCustomCollection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_custom_collection_id")
    private Long userCustomCollectionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "custom_collection_id", nullable = false)
    @ToString.Exclude
    private CustomCollection customCollection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    @ToString.Exclude
    private UserEntity user;
}
