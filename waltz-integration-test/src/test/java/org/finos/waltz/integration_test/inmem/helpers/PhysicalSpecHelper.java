package org.finos.waltz.integration_test.inmem.helpers;

import org.finos.waltz.common.DateTimeUtilities;
import org.finos.waltz.model.EntityReference;
import org.finos.waltz.model.UserTimestamp;
import org.finos.waltz.model.physical_specification.DataFormatKindValue;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecification;
import org.finos.waltz.model.physical_specification.ImmutablePhysicalSpecificationDeleteCommand;
import org.finos.waltz.service.physical_specification.PhysicalSpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static org.finos.waltz.integration_test.inmem.helpers.NameHelper.mkName;

@Service
public class PhysicalSpecHelper {

    private final PhysicalSpecificationService physicalSpecificationService;

    @Autowired
    public PhysicalSpecHelper(PhysicalSpecificationService physicalSpecificationService) {
        this.physicalSpecificationService = physicalSpecificationService;
    }

    public Long createPhysicalSpec(EntityReference owningEntity, String name) {
        String specName = mkName(name);
        String user = NameHelper.mkUserId(name);
        return physicalSpecificationService.create(ImmutablePhysicalSpecification.builder()
                .externalId(specName)
                .owningEntity(owningEntity)
                .name(specName)
                .description(name)
                .format(DataFormatKindValue.UNKNOWN)
                .lastUpdatedBy(user)
                .isRemoved(false)
                .created(UserTimestamp.mkForUser(user, DateTimeUtilities.nowUtcTimestamp()))
                .build());
    }


    public void removeSpec(Long specId) {
        physicalSpecificationService.markRemovedIfUnused(
                ImmutablePhysicalSpecificationDeleteCommand
                        .builder()
                        .specificationId(specId)
                        .build(),
                mkName("deletingSpec"));
    }


}
