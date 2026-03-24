package com.taja.simulator.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "simulator")
public class SimulatorProperties {

    private String baseUrl = "https://taja.myvnc.com";
}
