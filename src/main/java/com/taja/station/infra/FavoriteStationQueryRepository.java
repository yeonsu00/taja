package com.taja.station.infra;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.taja.favorite.infra.FavoriteStationEntity;
import com.taja.favorite.infra.QFavoriteStationEntity;
import com.taja.member.domain.Member;
import com.taja.member.domain.QMember;
import com.taja.station.domain.QStation;
import com.taja.station.domain.Station;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FavoriteStationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<Station> findFavoriteStationsByMemberEmail(String email) {
        QFavoriteStationEntity favorite = QFavoriteStationEntity.favoriteStationEntity;
        QMember member = QMember.member;
        QStation station = QStation.station;

        List<FavoriteStationEntity> favoriteStationEntities = jpaQueryFactory
                .select(favorite)
                .from(favorite)
                .join(favorite.member, member)
                .join(favorite.station, station).fetchJoin()
                .where(member.email.eq(email))
                .fetch();

        return FavoriteStationEntity.toStations(favoriteStationEntities);
    }

}
