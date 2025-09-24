package com.taja.favorite.application;

import com.taja.favorite.domain.FavoriteStation;
import com.taja.member.domain.Member;
import com.taja.station.domain.Station;
import java.util.List;

public interface FavoriteStationRepository {
    void saveFavoriteStation(FavoriteStation favoriteStation);

    void deleteFavoriteStation(Member member, Station station);

    boolean existsByMemberAndStation(Member member, Station station);

    List<Station> findFavoriteStationsByMemberEmail(String email);
}
