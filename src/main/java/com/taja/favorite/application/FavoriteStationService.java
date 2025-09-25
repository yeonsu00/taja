package com.taja.favorite.application;

import com.taja.favorite.domain.FavoriteStation;
import com.taja.member.application.MemberRepository;
import com.taja.member.domain.Member;
import com.taja.station.application.StationRedisRepository;
import com.taja.station.application.StationRepository;
import com.taja.station.domain.Station;
import com.taja.station.presentation.response.MapStationResponse;
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
    private final StationRedisRepository stationRedisRepository;

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

    @Transactional(readOnly = true)
    public List<MapStationResponse> findFavoriteStationsByMemberEmail(String email) {
        List<Station> favoriteStations = favoriteStationRepository.findFavoriteStationsByMemberEmail(email);
        return stationRedisRepository.findStationStatus(favoriteStations);
    }
}
