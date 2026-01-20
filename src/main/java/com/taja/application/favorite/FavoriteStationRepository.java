package com.taja.application.favorite;

import com.taja.domain.favorite.FavoriteStation;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import java.util.List;

public interface FavoriteStationRepository {
    void saveFavoriteStation(FavoriteStation favoriteStation);

    void deleteFavoriteStation(Member member, Station station);

    boolean existsByMemberAndStation(Member member, Station station);

    List<Station> findFavoriteStationsByMemberEmail(String email);
}
