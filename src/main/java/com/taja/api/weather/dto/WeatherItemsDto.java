package com.taja.api.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record WeatherItemsDto(
        @JsonProperty("item") List<WeatherItemDto> item
) {

    public double findValueByCategory(String category) {
        return item.stream()
                .filter(i -> i.category().equals(category))
                .findFirst()
                .map(i -> {
                    try {
                        return Double.parseDouble(i.obsrValue());
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .orElse(0.0);
    }

}
