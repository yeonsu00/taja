package com.taja.favorite.application;

import com.taja.favorite.domain.FavoriteStation;

public interface FavoriteStationRepository {
    void saveFavoriteStation(FavoriteStation favoriteStation);
}
