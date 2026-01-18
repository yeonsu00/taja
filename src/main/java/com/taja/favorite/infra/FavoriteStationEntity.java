package com.taja.favorite.infra;

import com.taja.favorite.domain.FavoriteStation;
import com.taja.member.domain.Member;
import com.taja.station.domain.Station;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.List;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        name = "favorite_stations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_member_station", columnNames = {"member_id", "station_id"})
        }
)
@RequiredArgsConstructor
public class FavoriteStationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favoriteStationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "station_id")
    private Station station;

    @Builder
    private FavoriteStationEntity(Long favoriteStationId, Member member, Station station) {
        this.favoriteStationId = favoriteStationId;
        this.member = member;
        this.station = station;
    }

    public static FavoriteStationEntity fromFavoriteStation(FavoriteStation favoriteStation) {
        return FavoriteStationEntity.builder()
                .member(favoriteStation.getMember())
                .station(favoriteStation.getStation())
                .build();
    }

    public static List<Station> toStations(List<FavoriteStationEntity> favoriteStationEntities) {
        return favoriteStationEntities.stream()
                .map(FavoriteStationEntity::getStation)
                .toList();
    }

    private Station getStation() {
        return this.station;
    }
}
