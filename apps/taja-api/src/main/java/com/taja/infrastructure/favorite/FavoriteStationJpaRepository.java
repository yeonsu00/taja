package com.taja.infrastructure.favorite;

import com.taja.domain.favorite.FavoriteStation;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteStationJpaRepository extends JpaRepository<FavoriteStation, Long> {
    long deleteByMemberAndStation(Member member, Station station);

    boolean existsByMemberAndStation(Member member, Station station);

    void deleteByMember(Member member);
}
