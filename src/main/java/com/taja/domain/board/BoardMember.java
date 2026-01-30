package com.taja.domain.board;

import com.taja.global.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Entity
@Table(
        name = "board_members",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_board_member_station", columnNames = {"station_id", "member_id"})
        }
)
@Getter
@RequiredArgsConstructor
public class BoardMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardMemberId;

    @Column(name = "station_id", nullable = false)
    private Long stationId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Builder
    private BoardMember(Long boardMemberId, Long stationId, Long memberId) {
        this.boardMemberId = boardMemberId;
        this.stationId = stationId;
        this.memberId = memberId;
    }

    public static BoardMember of(Long stationId, Long memberId) {
        return BoardMember.builder()
                .stationId(stationId)
                .memberId(memberId)
                .build();
    }
}
