package com.taja.favorite.infra;

import com.taja.favorite.application.FavoriteStationRepository;
import com.taja.favorite.domain.FavoriteStation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FavoriteStationRepositoryImpl implements FavoriteStationRepository {

    private final FavoriteStationJpaRepository favoriteStationJpaRepository;

    @Override
    public void saveFavoriteStation(FavoriteStation favoriteStation) {
        FavoriteStationEntity favoriteStationEntity = FavoriteStationEntity.fromFavoriteStation(favoriteStation);
        favoriteStationJpaRepository.save(favoriteStationEntity);
    }
}
