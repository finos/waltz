package org.finos.waltz.test_common.helpers;

import org.finos.waltz.model.allocation_scheme.ImmutableAllocationScheme;
import org.finos.waltz.service.allocation_schemes.AllocationSchemeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AllocationSchemeHelper {

    @Autowired
    private AllocationSchemeService allocationSchemeService;

    public long createAllocationScheme(String description, long categoryId, String name, String externalId) {

        ImmutableAllocationScheme allocationScheme = ImmutableAllocationScheme.builder()
                .description(description)
                .measurableCategoryId(categoryId)
                .name(name)
                .externalId(externalId)
                .build();

        return allocationSchemeService.create(allocationScheme);
    }
}
