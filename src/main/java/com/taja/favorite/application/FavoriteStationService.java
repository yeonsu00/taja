package com.taja.favorite.application;

import com.taja.favorite.domain.FavoriteStation;
import com.taja.member.application.MemberRepository;
import com.taja.member.domain.Member;
import com.taja.station.application.StationRepository;
import com.taja.station.domain.Station;
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
}
