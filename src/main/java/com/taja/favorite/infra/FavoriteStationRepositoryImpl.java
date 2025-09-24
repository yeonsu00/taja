package com.taja.favorite.infra;

import com.taja.favorite.application.FavoriteStationRepository;
import com.taja.favorite.domain.FavoriteStation;
import com.taja.global.exception.DuplicateFavoriteStationException;
import com.taja.global.exception.FavoriteStationNotFoundException;
import com.taja.member.domain.Member;
import com.taja.member.infra.MemberEntity;
import com.taja.station.domain.Station;
import com.taja.station.infra.FavoriteStationQueryRepository;
import com.taja.station.infra.StationEntity;
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
        FavoriteStationEntity favoriteStationEntity = FavoriteStationEntity.fromFavoriteStation(favoriteStation);
        try {
            favoriteStationJpaRepository.save(favoriteStationEntity);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateFavoriteStationException("이미 즐겨찾기 등록된 대여소입니다.");
        }
    }

    @Override
    public void deleteFavoriteStation(Member member, Station station) {
        MemberEntity memberEntity = MemberEntity.fromMember(member);
        StationEntity stationEntity = StationEntity.fromStation(station);

        long count = favoriteStationJpaRepository.deleteByMemberAndStation(memberEntity, stationEntity);

        if (count == 0) {
            throw new FavoriteStationNotFoundException("즐겨찾기 대여소를 찾을 수 없습니다.");
        }
    }

    @Override
    public boolean existsByMemberAndStation(Member member, Station station) {
        MemberEntity memberEntity = MemberEntity.fromMember(member);
        StationEntity stationEntity = StationEntity.fromStation(station);
        return favoriteStationJpaRepository.existsByMemberAndStation(memberEntity, stationEntity);
    }

    @Override
    public List<Station> findFavoriteStationsByMemberEmail(String email) {
        List<StationEntity> stationEntities = favoriteStationQueryRepository.findFavoriteStationsByMemberEmail(email);
        return stationEntities.stream()
                .map(StationEntity::toStation)
                .toList();
    }
}
