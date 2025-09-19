package com.taja.favorite.infra;

import com.taja.favorite.domain.FavoriteStation;
import com.taja.member.infra.MemberEntity;
import com.taja.station.infra.StationEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "favorite_stations")
@RequiredArgsConstructor
public class FavoriteStationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favoriteStationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private StationEntity station;

    @Builder
    private FavoriteStationEntity(Long favoriteStationId, MemberEntity member, StationEntity station) {
        this.favoriteStationId = favoriteStationId;
        this.member = member;
        this.station = station;
    }

    public static FavoriteStationEntity fromFavoriteStation(FavoriteStation favoriteStation) {
        return FavoriteStationEntity.builder()
                .member(MemberEntity.fromMember(favoriteStation.getMember()))
                .station(StationEntity.fromStation(favoriteStation.getStation()))
                .build();
    }
}
