package com.tourismsv.domain.entity;

import com.tourismsv.domain.enums.DestinationState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "id")
@ToString(exclude = {"destinationType", "images", "reviews", "likes", "saves"})
@Entity
@Table(name = "destinations")
public class Destination {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 255)
    private String name;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String country;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal longitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_type_id", nullable = false)
    private DestinationType destinationType;

    @Column(name = "url_banner", columnDefinition = "TEXT")
    private String urlBanner;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    @Builder.Default
    private DestinationState state = DestinationState.DRAFT;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DestinationImage> images = new ArrayList<>();

    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY)
    @Builder.Default
    private List<DestinationReview> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY)
    @Builder.Default
    private List<DestinationLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "destination", fetch = FetchType.LAZY)
    @Builder.Default
    private List<DestinationSave> saves = new ArrayList<>();
}
