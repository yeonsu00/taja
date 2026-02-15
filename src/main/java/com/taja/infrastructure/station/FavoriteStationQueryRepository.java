package com.taja.infrastructure.station;

import com.taja.domain.station.Station;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FavoriteStationQueryRepository {

    private final EntityManager entityManager;

    public List<Station> findFavoriteStationsByMemberEmail(String email) {
        return entityManager
                .createQuery(
                        """
                        select fs.station
                        from FavoriteStation fs
                        join fs.member m
                        join fetch fs.station s
                        where m.email = :email
                        """,
                        Station.class
                )
                .setParameter("email", email)
                .getResultList();
    }

}
