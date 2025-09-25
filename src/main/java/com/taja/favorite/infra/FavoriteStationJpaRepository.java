package com.taja.favorite.infra;

import com.taja.member.infra.MemberEntity;
import com.taja.station.infra.StationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteStationJpaRepository extends JpaRepository<FavoriteStationEntity, Long> {
    long deleteByMemberAndStation(MemberEntity memberEntity, StationEntity stationEntity);

    boolean existsByMemberAndStation(MemberEntity memberEntity, StationEntity stationEntity);
}
