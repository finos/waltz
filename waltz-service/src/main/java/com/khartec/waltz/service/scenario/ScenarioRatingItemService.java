/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

package com.khartec.waltz.service.scenario;

import com.khartec.waltz.data.scenario.ScenarioRatingItemDao;
import com.khartec.waltz.model.application.Application;
import com.khartec.waltz.model.rating.RagName;
import com.khartec.waltz.model.scenario.ChangeScenarioCommand;
import com.khartec.waltz.model.scenario.Scenario;
import com.khartec.waltz.model.scenario.ScenarioRatingItem;
import com.khartec.waltz.service.application.ApplicationService;
import com.khartec.waltz.service.changelog.ChangeLogService;
import com.khartec.waltz.service.rating_scheme.RatingSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.service.scenario.ScenarioUtilities.mkBasicLogEntry;

@Service
public class ScenarioRatingItemService {

    private final ScenarioRatingItemDao scenarioRatingItemDao;
    private final ChangeLogService changeLogService;
    private final ApplicationService applicationService;
    private final RatingSchemeService ratingSchemeService;
    private final ScenarioService scenarioService;


    @Autowired
    public ScenarioRatingItemService(ScenarioRatingItemDao scenarioRatingItemDao,
                                     ChangeLogService changeLogService, ApplicationService applicationService, RatingSchemeService ratingSchemeService, ScenarioService scenarioService) {
        this.applicationService = applicationService;
        this.ratingSchemeService = ratingSchemeService;
        this.scenarioService = scenarioService;
        checkNotNull(scenarioRatingItemDao, "scenarioRatingItemDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.scenarioRatingItemDao = scenarioRatingItemDao;
        this.changeLogService = changeLogService;
    }


    public Collection<ScenarioRatingItem> findForScenarioId(long scenarioId) {
        return scenarioRatingItemDao.findForScenarioId(scenarioId);
    }


    public boolean remove(ChangeScenarioCommand command, String userId) {
        boolean result = scenarioRatingItemDao.remove(command, userId);

        if (result) {
            writeApplicationChangeLog(command, userId, "Application %s (%s) was removed from %s");
        }

        return result;
    }


    public boolean add(ChangeScenarioCommand command, String userId) {
        boolean result = scenarioRatingItemDao.add(command, userId);
        if (result) {
            writeApplicationChangeLog(command, userId, "Application %s (%s) was added to %s");
        }

        return result;
    }


    public boolean updateRating(ChangeScenarioCommand command, String userId) {
        boolean result = scenarioRatingItemDao.updateRating(command, userId);

        if (result) {
            if(command.rating() != command.previousRating()) {
                writeUpdateRatingLog(command, userId);
            } else {
                 writeApplicationChangeLog(command, userId,
                         "Updated rating/description for app %s (%s), a comment was added to %s ");
            }
        }

        return result;
    }

    private void writeUpdateRatingLog(ChangeScenarioCommand command, String userId) {
        String message;
        Application application = applicationService.getById(command.appId());
        Scenario scenario = scenarioService.getById(command.scenarioId());
        List<RagName> ratings = ratingSchemeService.getById(command.ratingSchemeId()).ratings();
        message = String.format(
                "Application %s (%s), moved from %s to %s for %s",
                application.assetCode().orElse("Unknown"),
                application.name(),
                getRatingName(ratings, command.previousRating()),
                getRatingName(ratings, command.rating()),
                scenario.name());

        changeLogService.write(mkBasicLogEntry(command.scenarioId(), message, userId));
    }

    private void writeApplicationChangeLog(ChangeScenarioCommand command, String userId, String messageFormat) {
        Application application = applicationService.getById(command.appId());
        Scenario scenario = scenarioService.getById(command.scenarioId());
        String message = String.format(
                messageFormat,
                application.assetCode().orElse("Unknown"),
                application.name(),
                scenario.name());
        changeLogService.write(mkBasicLogEntry(command.scenarioId(), message, userId));
    }

    private String getRatingName(List<RagName> ratings, char rating) {
        Optional<RagName> ratingOptional = ratings.stream().filter(r -> r.rating() == rating).findFirst();
        return ratingOptional.isPresent() ? ratingOptional.get().name() : "Unknown";
    }
}
