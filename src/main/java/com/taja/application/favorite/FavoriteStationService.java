package com.taja.application.favorite;

import com.taja.application.station.StationInfo;
import com.taja.domain.favorite.FavoriteStation;
import com.taja.application.member.MemberRepository;
import com.taja.domain.member.Member;
import com.taja.application.station.StationRedisRepository;
import com.taja.application.station.StationRepository;
import com.taja.domain.station.Station;
import com.taja.interfaces.api.station.response.MapStationResponse;
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
        List<StationInfo.StationFullInfo> stationInfos = stationRedisRepository.findStationStatus(favoriteStations);
        return StationInfo.StationFullInfo.toMapStationResponses(stationInfos);
    }
}
