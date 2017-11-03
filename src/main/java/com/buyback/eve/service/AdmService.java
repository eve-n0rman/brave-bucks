package com.buyback.eve.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;

import com.buyback.eve.domain.SolarSystem;
import com.buyback.eve.repository.SolarSystemRepository;
import com.mashape.unirest.http.JsonNode;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * AdmService
 *
 * Created on 06.10.2017
 *
 * Copyright (C) 2017 Volkswagen AG, All rights reserved.
 */
@Service
public class AdmService {

    private static final Logger LOG = LoggerFactory.getLogger(AdmService.class);

    private final JsonRequestService service;
    private final SolarSystemRepository solarSystemRepository;
    private final Map<Long, Integer> systemAdms = new HashMap<>();

    public AdmService(final JsonRequestService service,
                      final SolarSystemRepository solarSystemRepository) {
        this.service = service;
        this.solarSystemRepository = solarSystemRepository;
    }

    public Integer getAdm(final long systemId) {
        if (!systemAdms.containsKey(systemId)) {
            update();
        }
        return systemAdms.get(systemId);
    }

    @PostConstruct
    public void init() {
        update();
    }

    @Scheduled(cron = "0 15 * * * *")
    public void update() {
        Collection<Long> systemIds = new ArrayList<>();
        solarSystemRepository.findAll().stream().mapToLong(SolarSystem::getSystemId).forEach(systemIds::add);
        final Optional<JsonNode> optional = service.getAdms();
        optional.ifPresent(jsonNode -> {
            final JSONArray array = jsonNode.getArray();
            for (int i = 0; i < array.length(); i++) {
                final JSONObject jsonObject = array.getJSONObject(i);
                final long solarSystemId = jsonObject.getLong("solar_system_id");
                final int adm;
                if (jsonObject.has("vulnerability_occupancy_level")) {
                    adm = jsonObject.getInt("vulnerability_occupancy_level");
                } else {
                    if (systemIds.contains(solarSystemId)) {
                        LOG.warn("Could not load ADM for hunting area system {}", solarSystemId);
                    }
                    adm = 5;
                }
                systemAdms.put(solarSystemId, adm);
            }
        });
        LOG.info("ADM update complete.");
    }
}
