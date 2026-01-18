package com.taja.favorite.infra;

import com.taja.member.domain.Member;
import com.taja.station.domain.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteStationJpaRepository extends JpaRepository<FavoriteStationEntity, Long> {
    long deleteByMemberAndStation(Member member, Station station);

    boolean existsByMemberAndStation(Member member, Station station);
}
