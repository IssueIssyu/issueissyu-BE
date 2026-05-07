package issueissyu.backend.domain.location.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "population_density")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PopulationDensity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "population_density_id")
    private Long populationDensityId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false, unique = true)
    @ToString.Exclude
    private Location location;

    @Builder.Default
    @Column(name = "target_petition", nullable = false)
    private int targetPetition = 30;

    @Builder.Default
    @Column(name = "target_community", nullable = false)
    private int targetCommunity = 10;

    @Builder.Default
    @Column(name = "population_density", nullable = false)
    private float density = 0f;
}
