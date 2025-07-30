package org.finos.waltz.model.proposed_flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProposedFlowDefinition {

    private ApplicationRef source;
    private ApplicationRef target;
    private Long logicalFlowId;
    private Integer reasonCode;
    private Long physicalFlowId;
    private Specification specification;

    /* ── inner classes --------------------------------------------- */

    public static class ApplicationRef {
        private String kind;
        private Long id;

        /* getters & setters */
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
    }

    public static class Specification {
        private OwningEntity owningEntity;
        private String name;
        private String description;
        private String format;
        private String lastUpdatedBy;
        private String externalId;
        private Long id;        // nullable
        private FlowAttributes flowAttributes;
        private List<Long> dataTypeIds;

        /* getters & setters */
        public OwningEntity getOwningEntity() { return owningEntity; }
        public void setOwningEntity(OwningEntity owningEntity) { this.owningEntity = owningEntity; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getFormat() { return format; }
        public void setFormat(String format) { this.format = format; }
        public String getLastUpdatedBy() { return lastUpdatedBy; }
        public void setLastUpdatedBy(String lastUpdatedBy) { this.lastUpdatedBy = lastUpdatedBy; }
        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public FlowAttributes getFlowAttributes() { return flowAttributes; }
        public void setFlowAttributes(FlowAttributes flowAttributes) { this.flowAttributes = flowAttributes; }
        public List<Long> getDataTypeIds() { return dataTypeIds; }
        public void setDataTypeIds(List<Long> dataTypeIds) { this.dataTypeIds = dataTypeIds; }
    }

    public static class OwningEntity {
        private Long id;
        private String kind;
        private String name;          // "AMG" in the sample
        private String externalId;    // "60487-1"
        private String description;   // multi-line text
        private String entityLifecycleStatus; // "ACTIVE"

        /* getters & setters */
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getKind() { return kind; }
        public void setKind(String kind) { this.kind = kind; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getEntityLifecycleStatus() { return entityLifecycleStatus; }
        public void setEntityLifecycleStatus(String entityLifecycleStatus) { this.entityLifecycleStatus = entityLifecycleStatus; }
    }

    public static class FlowAttributes {
        private String name;
        private String transport;
        private String frequency;
        @JsonProperty("basis0ffset")  // typo retained from image
        private Integer basis0ffset;
        private String criticality;
        private String description;
        private String externalId;

        /* getters & setters */
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getTransport() { return transport; }
        public void setTransport(String transport) { this.transport = transport; }
        public String getFrequency() { return frequency; }
        public void setFrequency(String frequency) { this.frequency = frequency; }
        public Integer getBasis0ffset() { return basis0ffset; }
        public void setBasis0ffset(Integer basis0ffset) { this.basis0ffset = basis0ffset; }
        public String getCriticality() { return criticality; }
        public void setCriticality(String criticality) { this.criticality = criticality; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getExternalId() { return externalId; }
        public void setExternalId(String externalId) { this.externalId = externalId; }
    }

    /* ── root getters & setters ------------------------------------ */

    public ApplicationRef getSource() { return source; }
    public void setSource(ApplicationRef source) { this.source = source; }

    public ApplicationRef getTarget() { return target; }
    public void setTarget(ApplicationRef target) { this.target = target; }

    public Long getLogicalFlowId() { return logicalFlowId; }
    public void setLogicalFlowId(Long logicalFlowId) { this.logicalFlowId = logicalFlowId; }

    public Integer getReasonCode() { return reasonCode; }
    public void setReasonCode(Integer reasonCode) { this.reasonCode = reasonCode; }

    public Long getPhysicalFlowId() { return physicalFlowId; }
    public void setPhysicalFlowId(Long physicalFlowId) { this.physicalFlowId = physicalFlowId; }

    public Specification getSpecification() { return specification; }
    public void setSpecification(Specification specification) { this.specification = specification; }
}
