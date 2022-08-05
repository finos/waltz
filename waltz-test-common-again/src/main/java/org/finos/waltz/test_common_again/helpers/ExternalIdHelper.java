package org.finos.waltz.test_common_again.helpers;

import org.finos.waltz.model.EntityReference;
import org.finos.waltz.service.external_identifier.ExternalIdentifierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ExternalIdHelper {

    private final ExternalIdentifierService externalIdentifierService;

    @Autowired
    public ExternalIdHelper(ExternalIdentifierService externalIdentifierService) {
        this.externalIdentifierService = externalIdentifierService;
    }

    public int createExtId(EntityReference ref, String extId, String name) {
        return externalIdentifierService.create(ref, extId, name);
    }

}
