package com.taja.interfaces.scheduler;

import lombok.Getter;
import org.springframework.stereotype.Component;

@Getter
@Component
public class StationInitializationHolder {

    private volatile boolean initialized = false;

    public void setInitialized() {
        this.initialized = true;
    }

}
