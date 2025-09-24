package com.taja.station.infra;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.taja.favorite.infra.FavoriteStationEntity;
import com.taja.favorite.infra.QFavoriteStationEntity;
import com.taja.member.infra.QMemberEntity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FavoriteStationQueryRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public List<StationEntity> findFavoriteStationsByMemberEmail(String email) {
        QFavoriteStationEntity favorite = QFavoriteStationEntity.favoriteStationEntity;
        QMemberEntity member = QMemberEntity.memberEntity;
        QStationEntity station = QStationEntity.stationEntity;

        List<FavoriteStationEntity> favoriteStationEntities = jpaQueryFactory
                .select(favorite)
                .from(favorite)
                .join(favorite.member, member)
                .join(favorite.station, station).fetchJoin()
                .where(member.email.eq(email))
                .fetch();

        return FavoriteStationEntity.toStationEntities(favoriteStationEntities);
    }

}
