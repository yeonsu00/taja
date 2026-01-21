package com.taja.domain.favorite;

import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
public class FavoriteStation {

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
    private FavoriteStation(Long favoriteStationId, Member member, Station station) {
        this.favoriteStationId = favoriteStationId;
        this.member = member;
        this.station = station;
    }

    public static FavoriteStation of(Member member, Station station) {
        return FavoriteStation.builder()
                .member(member)
                .station(station)
                .build();
    }
}
