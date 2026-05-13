package issueissyu.backend.domain.collection.entity.mapping;

import issueissyu.backend.domain.collection.entity.CustomCollection;
import issueissyu.backend.domain.user.entity.User;
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
    private User user;

    @Builder.Default
    @Column(name = "is_profile", nullable = false)
    private boolean isProfile = false;

    @Builder.Default
    @Column(name = "is_bookmark", nullable = false)
    private boolean isBookmark = false;

    public void setProfile(boolean profile) {
        this.isProfile = profile;
    }

    public void setBookmark(boolean bookmark) {
        this.isBookmark = bookmark;
    }

    public void markAsProfile() {
        this.isProfile = true;
    }
}
