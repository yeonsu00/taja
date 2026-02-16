package com.taja.infrastructure.favorite;

import com.taja.domain.favorite.FavoriteStation;
import com.taja.application.favorite.FavoriteStationRepository;
import com.taja.global.exception.DuplicateFavoriteStationException;
import com.taja.global.exception.FavoriteStationNotFoundException;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import com.taja.infrastructure.station.FavoriteStationQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FavoriteStationRepositoryImpl implements FavoriteStationRepository {

    private final FavoriteStationJpaRepository favoriteStationJpaRepository;
    private final FavoriteStationQueryRepository favoriteStationQueryRepository;

    @Override
    public void saveFavoriteStation(FavoriteStation favoriteStation) {
        try {
            favoriteStationJpaRepository.save(favoriteStation);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateFavoriteStationException("이미 즐겨찾기 등록된 대여소입니다.");
        }
    }

    @Override
    public void deleteFavoriteStation(Member member, Station station) {
        long count = favoriteStationJpaRepository.deleteByMemberAndStation(member, station);

        if (count == 0) {
            throw new FavoriteStationNotFoundException("즐겨찾기 대여소를 찾을 수 없습니다.");
        }
    }

    @Override
    public boolean existsByMemberAndStation(Member member, Station station) {
        return favoriteStationJpaRepository.existsByMemberAndStation(member, station);
    }

    @Override
    public List<Station> findFavoriteStationsByMemberEmail(String email) {
        return favoriteStationQueryRepository.findFavoriteStationsByMemberEmail(email);
    }

    @Override
    public void deleteByMember(Member member) {
        favoriteStationJpaRepository.deleteByMember(member);
    }
}
