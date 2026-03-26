package com.taja.domain.weather;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QWeatherHistory is a Querydsl query type for WeatherHistory
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QWeatherHistory extends EntityPathBase<WeatherHistory> {

    private static final long serialVersionUID = 89065287L;

    public static final QWeatherHistory weatherHistory = new QWeatherHistory("weatherHistory");

    public final DatePath<java.time.LocalDate> baseDate = createDate("baseDate", java.time.LocalDate.class);

    public final TimePath<java.time.LocalTime> baseTime = createTime("baseTime", java.time.LocalTime.class);

    public final StringPath district = createString("district");

    public final NumberPath<Double> hourlyRain = createNumber("hourlyRain", Double.class);

    public final DateTimePath<java.time.LocalDateTime> requestedAt = createDateTime("requestedAt", java.time.LocalDateTime.class);

    public final NumberPath<Double> temperature = createNumber("temperature", Double.class);

    public final NumberPath<Long> weatherHistoryId = createNumber("weatherHistoryId", Long.class);

    public final NumberPath<Double> windSpeed = createNumber("windSpeed", Double.class);

    public QWeatherHistory(String variable) {
        super(WeatherHistory.class, forVariable(variable));
    }

    public QWeatherHistory(Path<? extends WeatherHistory> path) {
        super(path.getType(), path.getMetadata());
    }

    public QWeatherHistory(PathMetadata metadata) {
        super(WeatherHistory.class, metadata);
    }

}

