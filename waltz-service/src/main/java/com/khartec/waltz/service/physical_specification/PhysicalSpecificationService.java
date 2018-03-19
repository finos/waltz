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

package com.khartec.waltz.service.physical_specification;

import com.khartec.waltz.data.physical_specification.PhysicalSpecificationDao;
import com.khartec.waltz.data.physical_specification.PhysicalSpecificationIdSelectorFactory;
import com.khartec.waltz.data.physical_specification.search.PhysicalSpecificationSearchDao;
import com.khartec.waltz.model.*;
import com.khartec.waltz.model.changelog.ChangeLog;
import com.khartec.waltz.model.changelog.ImmutableChangeLog;
import com.khartec.waltz.model.command.CommandOutcome;
import com.khartec.waltz.model.command.CommandResponse;
import com.khartec.waltz.model.command.ImmutableCommandResponse;
import com.khartec.waltz.model.entity_search.EntitySearchOptions;
import com.khartec.waltz.model.physical_specification.PhysicalSpecification;
import com.khartec.waltz.model.physical_specification.PhysicalSpecificationDeleteCommand;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.jooq.Record1;
import org.jooq.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.common.StringUtilities.isEmpty;
import static java.util.Collections.emptyList;


@Service
public class PhysicalSpecificationService {

    private final ChangeLogService changeLogService;
    private final PhysicalSpecificationDao specificationDao;
    private final PhysicalSpecificationSearchDao specificationSearchDao;
    private final PhysicalSpecificationIdSelectorFactory idSelectorFactory;


    @Autowired
    public PhysicalSpecificationService(ChangeLogService changeLogService,
                                        PhysicalSpecificationDao specificationDao,
                                        PhysicalSpecificationSearchDao specificationSearchDao,
                                        PhysicalSpecificationIdSelectorFactory idSelectorFactory)
    {
        checkNotNull(changeLogService, "changeLogService cannot be null");
        checkNotNull(specificationDao, "specificationDao cannot be null");
        checkNotNull(specificationSearchDao, "specificationSearchDao cannot be null");
        checkNotNull(idSelectorFactory, "idSelectorFactory cannot be null");

        this.changeLogService = changeLogService;
        this.specificationDao = specificationDao;
        this.specificationSearchDao = specificationSearchDao;
        this.idSelectorFactory = idSelectorFactory;
    }


    public Set<PhysicalSpecification> findByEntityReference(EntityReference ref) {
        return specificationDao.findByEntityReference(ref);
    }


    public PhysicalSpecification getById(long id) {
        return specificationDao.getById(id);
    }


    public Collection<PhysicalSpecification> findBySelector(IdSelectionOptions options) {
        Select<Record1<Long>> selector = idSelectorFactory.apply(options);
        return specificationDao.findBySelector(selector);
    }


    public CommandResponse<PhysicalSpecificationDeleteCommand> delete(PhysicalSpecificationDeleteCommand command, String username) {
        checkNotNull(command, "command cannot be null");

        CommandOutcome commandOutcome = CommandOutcome.SUCCESS;
        String responseMessage = null;

        PhysicalSpecification specification = specificationDao.getById(command.specificationId());

        if (specification == null) {
            commandOutcome = CommandOutcome.FAILURE;
            responseMessage = "Specification not found";
        } else {
            int deleteCount = specificationDao.delete(command.specificationId());

            if (deleteCount == 0) {
                commandOutcome = CommandOutcome.FAILURE;
                responseMessage = "This specification cannot be deleted as it is being referenced by one or more physical flows";
            }
        }

        if (commandOutcome == CommandOutcome.SUCCESS) {
            logChange(username,
                    specification.owningEntity(),
                    String.format("Specification: %s removed",
                            specification.name()),
                    Operation.REMOVE);
        }

        return ImmutableCommandResponse.<PhysicalSpecificationDeleteCommand>builder()
                .entityReference(EntityReference.mkRef(EntityKind.PHYSICAL_SPECIFICATION, command.specificationId()))
                .originalCommand(command)
                .outcome(commandOutcome)
                .message(Optional.ofNullable(responseMessage))
                .build();
    }


    public List<PhysicalSpecification> search(String query, EntitySearchOptions options) {
        if (isEmpty(query)) return emptyList();
        return specificationSearchDao.search(query, options);
    }



    private void logChange(String userId,
                           EntityReference ref,
                           String message,
                           Operation operation) {

        ChangeLog logEntry = ImmutableChangeLog.builder()
                .parentReference(ref)
                .message(message)
                .severity(Severity.INFORMATION)
                .userId(userId)
                .childKind(EntityKind.PHYSICAL_SPECIFICATION)
                .operation(operation)
                .build();

        changeLogService.write(logEntry);
    }

}
