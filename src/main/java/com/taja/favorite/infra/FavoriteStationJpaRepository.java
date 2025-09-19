package com.taja.favorite.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteStationJpaRepository extends JpaRepository<FavoriteStationEntity, Long> {
}
