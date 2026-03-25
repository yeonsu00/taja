package com.taja.infrastructure.favorite;

import com.taja.domain.favorite.FavoriteStation;
import com.taja.domain.member.Member;
import com.taja.domain.station.Station;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FavoriteStationJpaRepository extends JpaRepository<FavoriteStation, Long> {
    long deleteByMemberAndStation(Member member, Station station);

    boolean existsByMemberAndStation(Member member, Station station);

    void deleteByMember(Member member);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM FavoriteStation fs WHERE fs.member.memberId IN :memberIds")
    void deleteByMemberMemberIdIn(@Param("memberIds") List<Long> memberIds);
}
