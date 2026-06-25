package com.tourismsv.repository;

import com.tourismsv.config.TestcontainersConfiguration;
import com.tourismsv.domain.entity.Destination;
import com.tourismsv.domain.entity.DestinationType;
import com.tourismsv.domain.enums.DestinationState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
@ActiveProfiles("test")
class DestinationRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private DestinationRepository repository;

    private DestinationType beachType;
    private DestinationType mountainType;

    @BeforeEach
    void setUp() {
        beachType = em.persist(DestinationType.builder()
                .name("Beach")
                .description("Beach destinations")
                .build());
        mountainType = em.persist(DestinationType.builder()
                .name("Mountain")
                .description("Mountain destinations")
                .build());
    }

    @Test
    void saveAndFindById() {
        var dest = em.persistFlushFind(Destination.builder()
                .name("El Tunco")
                .description("Surf beach")
                .country("El Salvador")
                .city("La Libertad")
                .latitude(new BigDecimal("13.48"))
                .longitude(new BigDecimal("-89.32"))
                .destinationType(beachType)
                .state(DestinationState.ACTIVE)
                .build());

        var found = repository.findById(dest.getId()).orElseThrow();

        assertThat(found.getName()).isEqualTo("El Tunco");
        assertThat(found.getCountry()).isEqualTo("El Salvador");
        assertThat(found.getState()).isEqualTo(DestinationState.ACTIVE);
        assertThat(found.getLatitude().compareTo(new BigDecimal("13.48"))).isZero();
    }

    @Test
    void findAll() {
        em.persist(Destination.builder()
                .name("El Tunco").description("Surf beach")
                .country("El Salvador").city("La Libertad")
                .latitude(new BigDecimal("13.48")).longitude(new BigDecimal("-89.32"))
                .destinationType(beachType).state(DestinationState.ACTIVE)
                .build());
        em.persist(Destination.builder()
                .name("Santa Ana Volcano").description("Hike")
                .country("El Salvador").city("Santa Ana")
                .latitude(new BigDecimal("13.85")).longitude(new BigDecimal("-89.62"))
                .destinationType(mountainType).state(DestinationState.ACTIVE)
                .build());

        var all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById() {
        var dest = em.persist(Destination.builder()
                .name("El Tunco").description("Surf beach")
                .country("El Salvador").city("La Libertad")
                .latitude(new BigDecimal("13.48")).longitude(new BigDecimal("-89.32"))
                .destinationType(beachType).state(DestinationState.ACTIVE)
                .build());

        assertThat(repository.count()).isEqualTo(1);
        repository.deleteById(dest.getId());
        assertThat(repository.count()).isZero();
    }

    @Test
    void existsByName() {
        em.persist(Destination.builder()
                .name("El Tunco").description("Surf beach")
                .country("El Salvador").city("La Libertad")
                .latitude(new BigDecimal("13.48")).longitude(new BigDecimal("-89.32"))
                .destinationType(beachType).state(DestinationState.ACTIVE)
                .build());

        assertThat(repository.existsByName("El Tunco")).isTrue();
        assertThat(repository.existsByName("Non Existent")).isFalse();
    }

    @Test
    void search_withAllFilters() {
        em.persist(Destination.builder()
                .name("El Tunco").description("Surf beach")
                .country("El Salvador").city("La Libertad")
                .latitude(new BigDecimal("13.48")).longitude(new BigDecimal("-89.32"))
                .destinationType(beachType).state(DestinationState.ACTIVE)
                .build());

        Page<Destination> result = repository.search(
                "El Tunco", "ACTIVE", beachType.getId(),
                "El Salvador", "La Libertad", PageRequest.of(0, 10));

        assertThat(result).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("El Tunco");
    }

    @Test
    void search_withNoFilters() {
        em.persist(Destination.builder()
                .name("El Tunco").description("Surf beach")
                .country("El Salvador").city("La Libertad")
                .latitude(new BigDecimal("13.48")).longitude(new BigDecimal("-89.32"))
                .destinationType(beachType).state(DestinationState.ACTIVE)
                .build());
        em.persist(Destination.builder()
                .name("Santa Ana Volcano").description("Hike")
                .country("El Salvador").city("Santa Ana")
                .latitude(new BigDecimal("13.85")).longitude(new BigDecimal("-89.62"))
                .destinationType(mountainType).state(DestinationState.ACTIVE)
                .build());

        Page<Destination> result = repository.search(
                null, null, null, null, null, PageRequest.of(0, 10));

        assertThat(result).hasSize(2);
    }

    @Test
    void search_withNoResults() {
        em.persist(Destination.builder()
                .name("El Tunco").description("Surf beach")
                .country("El Salvador").city("La Libertad")
                .latitude(new BigDecimal("13.48")).longitude(new BigDecimal("-89.32"))
                .destinationType(beachType).state(DestinationState.ACTIVE)
                .build());

        Page<Destination> result = repository.search(
                "NonExistent", null, null, null, null, PageRequest.of(0, 10));

        assertThat(result).isEmpty();
    }
}
