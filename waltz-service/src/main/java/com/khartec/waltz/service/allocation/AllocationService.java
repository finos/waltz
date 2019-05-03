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

package com.khartec.waltz.service.allocation;

import com.khartec.waltz.common.ListUtilities;
import com.khartec.waltz.data.EntityReferenceNameResolver;
import com.khartec.waltz.data.allocation.AllocationDao;
import com.khartec.waltz.data.allocation_scheme.AllocationSchemeDao;
import com.khartec.waltz.model.EntityReference;
import com.khartec.waltz.model.allocation.Allocation;
import com.khartec.waltz.model.allocation.MeasurablePercentageChange;
import com.khartec.waltz.service.allocation.AllocationUtilities.ValidationResult;
import com.khartec.waltz.service.changelog.ChangeLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.khartec.waltz.common.Checks.checkNotNull;
import static com.khartec.waltz.model.EntityKind.ALLOCATION_SCHEME;
import static com.khartec.waltz.model.EntityKind.MEASURABLE;
import static com.khartec.waltz.model.EntityReference.mkRef;
import static com.khartec.waltz.service.allocation.AllocationUtilities.mkBasicLogEntry;
import static com.khartec.waltz.service.allocation.AllocationUtilities.validateAllocationChanges;

@Service
public class AllocationService {

    private static final Logger LOG = LoggerFactory.getLogger(AllocationService.class);

    private final AllocationDao allocationDao;
    private final ChangeLogService changeLogService;
    private final EntityReferenceNameResolver nameResolver;


    @Autowired
    public AllocationService(AllocationDao allocationDao,
                             EntityReferenceNameResolver nameResolver,
                             ChangeLogService changeLogService) {
        this.nameResolver = nameResolver;
        checkNotNull(allocationDao, "allocationDao cannot be null");
        checkNotNull(changeLogService, "changeLogService cannot be null");

        this.allocationDao = allocationDao;
        this.changeLogService = changeLogService;
    }


    public Collection<Allocation> findByEntity(EntityReference ref) {
        return allocationDao.findByEntity(ref);
    }


    public List<Allocation> findByEntityAndScheme(EntityReference ref,
                                                  long schemeId) {
        return allocationDao.findByEntityAndScheme(ref, schemeId);
    }


    public List<Allocation> findByMeasurableAndScheme(long measurableId,
                                                      long schemeId){
        return allocationDao.findByMeasurableIdAndScheme(measurableId, schemeId);
    }


    public Boolean updateAllocations(EntityReference ref,
                                     long schemeId,
                                     Collection<MeasurablePercentageChange> changes,
                                     String username){
        validateChanges(
                ref,
                schemeId,
                changes,
                username);

        Boolean success = allocationDao.updateAllocations(ref, schemeId, changes, username);

        if (success) {
            writeChangeLogEntries(ref, schemeId, changes, username);
        }

        return success;
    }


    // -- HELPERS ---

    private void validateChanges(EntityReference ref,
                                 long schemeId,
                                 Collection<MeasurablePercentageChange> changes,
                                 String username) {
        List<Allocation> currentAllocations = findByEntityAndScheme(ref, schemeId);

        ValidationResult validationResult = validateAllocationChanges(currentAllocations, changes);
        if (validationResult.failed()) {
            String reason = String.format("Cannot update allocations because: %s", validationResult.message());
            LOG.error("Cannot update allocations for entity: {}, scheme: {}, changes:{}, for user: {}, because: {}",
                    ref,
                    schemeId,
                    changes,
                    username,
                    reason);

            throw new IllegalArgumentException(reason);
        }
    }

    private void writeChangeLogEntries(EntityReference ref, long schemeId, Collection<MeasurablePercentageChange> changes, String username) {
        List<EntityReference> refs = ListUtilities.map(
                changes,
                c -> mkRef(MEASURABLE, c.measurablePercentage().measurableId()));

        Map<Long, Optional<String>> measurableIdToName = nameResolver.resolve(refs)
                .stream()
                .collect(Collectors.toMap(EntityReference::id, EntityReference::name));

        Optional<EntityReference> schemeRef = nameResolver.resolve(mkRef(ALLOCATION_SCHEME, schemeId));

        String msgPrefix = String.format(
                "Modifying %s allocations",
                schemeRef
                    .flatMap(EntityReference::name)
                    .orElse("Unknown"));

        String msgBody = changes.stream()
                .map(c -> describeChange(measurableIdToName, c))
                .collect(Collectors.joining(", "));

        String msg = String.format(
                "%s: %s",
                msgPrefix,
                msgBody);

        changeLogService.write(mkBasicLogEntry(ref, msg, username));
    }


    private String describeChange(Map<Long, Optional<String>> measurableIdToName,
                                  MeasurablePercentageChange c) {
        switch (c.operation()) {
            case UPDATE:
            case ADD:
                return String.format(
                        "Set allocation for measurable '%s' to %d %%",
                        measurableIdToName.get(c.measurablePercentage().measurableId()).orElse("Unknown"),
                        c.measurablePercentage().percentage());
            case REMOVE:
                return String.format(
                        "Unallocated measurable '%s'",
                        measurableIdToName.get(c.measurablePercentage().measurableId()).orElse("Unknown"));
            default:
                return "";
        }
    }

}
