package com.taja.interfaces.api.station;

import com.taja.global.exception.InvalidSortTypeException;
import java.util.Arrays;
import lombok.Getter;

@Getter
public enum PostSort {
    LATEST("latest", "최신순"),
    POPULAR("popular", "인기순");

    private final String value;
    private final String description;

    PostSort(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static PostSort fromValue(String value) {
        if (value == null) {
            return LATEST;
        }
        return Arrays.stream(PostSort.values())
                .filter(sort -> sort.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new InvalidSortTypeException("지원하지 않는 정렬 기준입니다: " + value));
    }
}
