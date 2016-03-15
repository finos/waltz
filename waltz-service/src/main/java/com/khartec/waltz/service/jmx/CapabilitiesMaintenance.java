package com.khartec.waltz.service.jmx;

import com.khartec.waltz.service.capability.CapabilityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

@ManagedResource(description = "Maintenance functions for the Waltz Capability model")
public class CapabilitiesMaintenance {

    private static final Logger LOG = LoggerFactory.getLogger(CapabilitiesMaintenance.class);

    private final CapabilityService capabilityService;


    @Autowired
    public CapabilitiesMaintenance(CapabilityService capabilityService) {
        this.capabilityService = capabilityService;
    }

    @ManagedAttribute
    public String getName() {
        return "Capabilities";
    }


    @ManagedOperation(description = "Rebuild capability hierarchy using parentId field")
    public boolean rebuildHierarchy() {
        LOG.warn("Rebuilding capability hierarchy (via jmx)");
        return capabilityService.rebuildHierarchy();
    }

}
