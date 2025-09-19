package com.taja.favorite.domain;

import com.taja.member.domain.Member;
import com.taja.station.domain.Station;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FavoriteStation {

    private Long favoriteStationId;

    private Member member;

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
