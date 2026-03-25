package com.taja.application.favorite;

import com.taja.domain.favorite.FavoriteStation;
import com.taja.application.member.MemberRepository;
import com.taja.domain.member.Member;
import com.taja.application.station.StationRepository;
import com.taja.domain.station.Station;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FavoriteStationService {

    private final MemberRepository memberRepository;
    private final StationRepository stationRepository;
    private final FavoriteStationRepository favoriteStationRepository;

    @Transactional
    public void addFavoriteStationToMember(String email, Long stationId) {
        Member member = memberRepository.findByEmail(email);
        Station station = stationRepository.findById(stationId);

        FavoriteStation favoriteStation = FavoriteStation.of(member, station);
        favoriteStationRepository.saveFavoriteStation(favoriteStation);
    }

    @Transactional
    public void deleteMemberFavoriteStation(String email, Long stationId) {
        Member member = memberRepository.findByEmail(email);
        Station station = stationRepository.findById(stationId);

        favoriteStationRepository.deleteFavoriteStation(member, station);
    }

    @Transactional
    public boolean isFavoriteStation(String email, Long stationId) {
        Member member = memberRepository.findByEmail(email);
        Station station = stationRepository.findById(stationId);

        return favoriteStationRepository.existsByMemberAndStation(member, station);
    }

    public List<Station> findFavoriteStationsByMemberEmail(String email) {
        return favoriteStationRepository.findFavoriteStationsByMemberEmail(email);
    }

    public void deleteByMemberIdIn(List<Long> memberIds) {
        favoriteStationRepository.deleteByMemberIdIn(memberIds);
    }
}
