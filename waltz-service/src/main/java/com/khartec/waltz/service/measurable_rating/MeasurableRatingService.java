/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.khartec.waltz.service.measurable_rating;

import com.khartec.waltz.data.application.ApplicationIdSelectorFactory;
import com.khartec.waltz.data.measurable.MeasurableDao;
import com.khartec.waltz.data.measurable.MeasurableIdSelectorFactory;
import com.khartec.waltz.data.measurable_rating.MeasurableRatingDao;
import com.khartec.waltz.data.perspective_rating.PerspectiveRatingDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.measurable.Measurable;
import com.khartec.waltz.model.measurable_rating.MeasurableRating;
import com.khartec.waltz.model.measurable_rating.MeasurableRatingCommand;
import com.khartec.waltz.model.measurable_rating.RemoveMeasurableRatingCommand;
import com.khartec.waltz.model.measurable_rating.SaveMeasurableRatingCommand;
import com.khartec.waltz.model.tally.MeasurableRatingTally;
import com.khartec.waltz.model.tally.Tally;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.Checks.checkTrue;
import static java.lang.String.format;

@Service
public class MeasurableRatingService {

    private final MeasurableRatingDao measurableRatingDao;
    private final MeasurableDao measurableDao;
    private final MeasurableIdSelectorFactory measurableIdSelectorFactory;
    private final PerspectiveRatingDao perspectiveRatingDao;
    private final ApplicationIdSelectorFactory applicationIdSelectorFactory;
    private final ChangeLogService changeLogService;


    @Autowired
    public MeasurableRatingService(MeasurableRatingDao measurableRatingDao,
                                   MeasurableDao measurableDao,
                                   MeasurableIdSelectorFactory measurableIdSelectorFactory,
                                   PerspectiveRatingDao perspectiveRatingDao,
                                   ApplicationIdSelectorFactory applicationIdSelectorFactory,
                                   ChangeLogService changeLogService) {
        checkNotNull(measurableRatingDao, "measurableRatingDao cannot be null");
        checkNotNull(measurableDao, "measurableDao cannot be null");
        checkNotNull(measurableIdSelectorFactory, "measurableIdSelectorFactory cannot be null");
        checkNotNull(perspectiveRatingDao, "perspectiveRatingDao cannot be null");
        checkNotNull(applicationIdSelectorFactory, "applicationIdSelectorFactory cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.measurableRatingDao = measurableRatingDao;
        this.measurableDao = measurableDao;
        this.measurableIdSelectorFactory = measurableIdSelectorFactory;
        this.perspectiveRatingDao = perspectiveRatingDao;
        this.applicationIdSelectorFactory = applicationIdSelectorFactory;
        this.changeLogService = changeLogService;
    }

    // -- READ

    public List<MeasurableRating> findForEntity(EntityReference ref) {
        checkNotNull(ref, "ref cannot be null");
        return measurableRatingDao.findForEntity(ref);
    }


    public List<MeasurableRating> findByMeasurableIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = measurableIdSelectorFactory.apply(options);
        return measurableRatingDao.findByMeasurableIdSelector(selector, options.entityLifecycleStatuses());
    }


    public Collection<MeasurableRating> findByAppIdSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return measurableRatingDao.findByApplicationIdSelector(selector);
    }

    // -- WRITE

    public Collection<MeasurableRating> update(SaveMeasurableRatingCommand command) {
        return save(
                command,
                measurableRatingDao::update,
                "Updated: %s with a rating of: %s",
                Operation.UPDATE);
    }


    public Collection<MeasurableRating> create(SaveMeasurableRatingCommand command) {
        return save(
                command,
                measurableRatingDao::create,
                "Added: %s with a rating of: %s",
                Operation.ADD);
    }


    public Collection<MeasurableRating> remove(RemoveMeasurableRatingCommand command) {
        checkNotNull(command, "command cannot be null");
        Measurable measurable = measurableDao.getById(command.measurableId());

        boolean success = measurableRatingDao.remove(command);
        perspectiveRatingDao.cascadeRemovalOfMeasurableRating(command.entityReference(), command.measurableId());

        if (success && measurable != null) {
            writeChangeLogEntry(
                    command,
                    format("Removed: %s",
                            measurable.name()),
                    Operation.REMOVE);

        }
        return findForEntity(command.entityReference());
    }


    public List<Tally<Long>> tallyByMeasurableId() {
        return measurableRatingDao.tallyByMeasurableId();
    }


    public List<Tally<Long>> tallyByMeasurableCategoryId(long categoryId) {
        return measurableRatingDao.tallyByMeasurableCategoryId(categoryId);
    }

    public Collection<MeasurableRatingTally> statsForRelatedMeasurable(IdSelectionOptions options) {
        Select<Record1<Long>> selector = measurableIdSelectorFactory.apply(options);
        return measurableRatingDao.statsForRelatedMeasurable(selector);
    }


    public List<MeasurableRatingTally> statsByAppSelector(IdSelectionOptions options) {
        checkNotNull(options, "options cannot be null");
        Select<Record1<Long>> selector = applicationIdSelectorFactory.apply(options);
        return measurableRatingDao.statsByAppSelector(selector);
    }


    // -- HELPERS --

    private Collection<MeasurableRating> save(SaveMeasurableRatingCommand command,
                                              Function<SaveMeasurableRatingCommand, Boolean> action,
                                              String messageTemplate,
                                              Operation operation) {
        checkNotNull(command, "command cannot be null");

        Measurable measurable = measurableDao.getById(command.measurableId());
        checkNotNull(measurable, format("Unknown measurable with id: %d", command.measurableId()));
        checkTrue(measurable.concrete(), "Cannot rate against an abstract measurable");

        boolean success = action.apply(command);

        if (success) {
            writeChangeLogEntry(
                    command,
                    format(messageTemplate,
                            measurable.name(),
                            command.rating()),
                    operation);
        }

        return findForEntity(command.entityReference());
    }


    private void writeChangeLogEntry(MeasurableRatingCommand command, String message, Operation operation) {
        changeLogService.write(ImmutableChangeLog.builder()
                .message(message)
                .parentReference(command.entityReference())
                .userId(command.lastUpdate().by())
                .createdAt(command.lastUpdate().at())
                .severity(Severity.INFORMATION)
                .childKind(EntityKind.MEASURABLE)
                .operation(operation)
                .build());
    }


    public Collection<MeasurableRating> findByCategory(long id) {
        return measurableRatingDao.findByCategory(id);
    }

}
