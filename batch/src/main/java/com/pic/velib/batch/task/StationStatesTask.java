package com.pic.velib.batch.task;

import com.pic.velib.entity.Station;
import com.pic.velib.service.dto.StationService;
import com.pic.velib.entity.StationState;
import com.pic.velib.service.opendata.StationsAPI;
import com.pic.velib.service.opendata.StationsAPIImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class StationStatesTask {

    @Value("${stationState.deepHistoryToKeepInDays}")
    private long deepHistoryToKeepInDays;

    private final StationService stationService;

    private final StationsAPI stationsApi;

    @Autowired
    public StationStatesTask(StationService stationService, StationsAPI stationsApi) {
        this.stationService = stationService;
        this.stationsApi = stationsApi;
    }

    @Scheduled(fixedDelay = 5, timeUnit = TimeUnit.MINUTES)
    public void getStations() {

        List<Station> stations = stationsApi.getStations();

        stationService.saveAllStation(stations);

        List<StationState> stationStates = stationsApi.getCurrentStates();
        for (int i = 0 ; i < stationStates.size(); i++)
        {
            stationService.updateStationState(stationStates.get(i));
        }

    }


    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.DAYS)
    public void dropHistory() {

        stationService.deleteStationStatesBefore(System.currentTimeMillis() - deepHistoryToKeepInDays * 24 * 60 * 60 * 1000);

    }


}