package org.finos.waltz.data;

import org.finos.waltz.model.CommonTableFields;
import org.finos.waltz.model.EntityKind;
import org.finos.waltz.model.EntityLifecycleStatus;
import org.finos.waltz.model.ImmutableCommonTableFields;
import org.finos.waltz.schema.Tables;
import org.finos.waltz.schema.tables.Actor;
import org.finos.waltz.schema.tables.AllocationScheme;
import org.finos.waltz.schema.tables.Application;
import org.finos.waltz.schema.tables.ApplicationGroup;
import org.finos.waltz.schema.tables.AssessmentDefinition;
import org.finos.waltz.schema.tables.ChangeInitiative;
import org.finos.waltz.schema.tables.ChangeSet;
import org.finos.waltz.schema.tables.ComplexityKind;
import org.finos.waltz.schema.tables.CostKind;
import org.finos.waltz.schema.tables.DataType;
import org.finos.waltz.schema.tables.EndUserApplication;
import org.finos.waltz.schema.tables.EntityRelationship;
import org.finos.waltz.schema.tables.EntityStatisticDefinition;
import org.finos.waltz.schema.tables.FlowClassification;
import org.finos.waltz.schema.tables.FlowClassificationRule;
import org.finos.waltz.schema.tables.FlowDiagram;
import org.finos.waltz.schema.tables.InvolvementKind;
import org.finos.waltz.schema.tables.LegalEntity;
import org.finos.waltz.schema.tables.LegalEntityRelationship;
import org.finos.waltz.schema.tables.Licence;
import org.finos.waltz.schema.tables.LogicalDataElement;
import org.finos.waltz.schema.tables.LogicalFlow;
import org.finos.waltz.schema.tables.Measurable;
import org.finos.waltz.schema.tables.MeasurableCategory;
import org.finos.waltz.schema.tables.MeasurableRating;
import org.finos.waltz.schema.tables.OrganisationalUnit;
import org.finos.waltz.schema.tables.Person;
import org.finos.waltz.schema.tables.PhysicalFlow;
import org.finos.waltz.schema.tables.PhysicalSpecification;
import org.finos.waltz.schema.tables.ServerInformation;
import org.finos.waltz.schema.tables.SoftwarePackage;
import org.finos.waltz.schema.tables.SurveyQuestion;
import org.finos.waltz.schema.tables.SurveyTemplate;
import org.jooq.impl.DSL;

import static org.finos.waltz.model.EntityKind.ACTOR;

public class CommonTableFieldsRegistry {

    public static CommonTableFields<?> determineCommonTableFields(EntityKind kind, String alias) {
        switch (kind) {
            case ACTOR:
                Actor actor = alias == null ? Tables.ACTOR : Tables.ACTOR.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(ACTOR)
                        .table(actor)
                        .idField(actor.ID)
                        .parentIdField(null)
                        .nameField(actor.NAME)
                        .descriptionField(actor.DESCRIPTION)
                        .externalIdField(actor.EXTERNAL_ID)
                        .build();
            case ALLOCATION_SCHEME:
                AllocationScheme allocScheme = alias == null ? Tables.ALLOCATION_SCHEME : Tables.ALLOCATION_SCHEME.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.ALLOCATION_SCHEME)
                        .table(allocScheme)
                        .idField(allocScheme.ID)
                        .parentIdField(null)
                        .nameField(allocScheme.NAME)
                        .descriptionField(allocScheme.DESCRIPTION)
                        .externalIdField(allocScheme.EXTERNAL_ID)
                        .build();
            case APPLICATION:
                Application app = alias == null ? Tables.APPLICATION : Tables.APPLICATION.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.APPLICATION)
                        .table(app)
                        .idField(app.ID)
                        .parentIdField(null)
                        .nameField(app.NAME)
                        .descriptionField(app.DESCRIPTION)
                        .externalIdField(app.ASSET_CODE)
                        .lifecycleField(app.ENTITY_LIFECYCLE_STATUS)
                        .build();
            case APP_GROUP:
                ApplicationGroup ag = alias == null ? Tables.APPLICATION_GROUP : Tables.APPLICATION_GROUP.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.APP_GROUP)
                        .table(ag)
                        .idField(ag.ID)
                        .parentIdField(null)
                        .nameField(ag.NAME)
                        .descriptionField(ag.DESCRIPTION)
                        .externalIdField(ag.EXTERNAL_ID)
                        .build();
            case ASSESSMENT_DEFINITION:
                AssessmentDefinition ad = alias == null ? Tables.ASSESSMENT_DEFINITION : Tables.ASSESSMENT_DEFINITION.as("alias");
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.ASSESSMENT_DEFINITION)
                        .table(ad)
                        .idField(ad.ID)
                        .parentIdField(null)
                        .nameField(ad.NAME)
                        .descriptionField(ad.DESCRIPTION)
                        .externalIdField(ad.EXTERNAL_ID)
                        .build();
            case CHANGE_INITIATIVE:
                ChangeInitiative ci = alias == null ? Tables.CHANGE_INITIATIVE : Tables.CHANGE_INITIATIVE.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.CHANGE_INITIATIVE)
                        .table(ci)
                        .idField(ci.ID)
                        .parentIdField(ci.PARENT_ID)
                        .nameField(ci.NAME)
                        .descriptionField(ci.DESCRIPTION)
                        .externalIdField(ci.EXTERNAL_ID)
                        .build();
            case CHANGE_SET:
                ChangeSet cs = alias == null ? Tables.CHANGE_SET : Tables.CHANGE_SET.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.CHANGE_SET)
                        .table(cs)
                        .idField(cs.ID)
                        .nameField(cs.NAME)
                        .descriptionField(cs.DESCRIPTION)
                        .externalIdField(cs.EXTERNAL_ID)
                        .lifecycleField(cs.ENTITY_LIFECYCLE_STATUS)
                        .build();
            case COMPLEXITY_KIND:
                ComplexityKind cxk = alias == null ? Tables.COMPLEXITY_KIND : Tables.COMPLEXITY_KIND.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.COMPLEXITY_KIND)
                        .table(cxk)
                        .idField(cxk.ID)
                        .parentIdField(null)
                        .nameField(cxk.NAME)
                        .descriptionField(cxk.DESCRIPTION)
                        .externalIdField(cxk.EXTERNAL_ID)
                        .build();
            case COST_KIND:
                CostKind ck = alias == null ? Tables.COST_KIND : Tables.COST_KIND.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.COST_KIND)
                        .table(ck)
                        .idField(ck.ID)
                        .parentIdField(null)
                        .nameField(ck.NAME)
                        .descriptionField(ck.DESCRIPTION)
                        .externalIdField(ck.EXTERNAL_ID)
                        .build();
            case DATA_TYPE:
                DataType dt = alias == null ? Tables.DATA_TYPE : Tables.DATA_TYPE.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.DATA_TYPE)
                        .table(dt)
                        .idField(dt.ID)
                        .parentIdField(dt.PARENT_ID)
                        .nameField(dt.NAME)
                        .descriptionField(dt.DESCRIPTION)
                        .externalIdField(dt.CODE)
                        .build();
            case END_USER_APPLICATION:
                EndUserApplication euda = alias == null ? Tables.END_USER_APPLICATION : Tables.END_USER_APPLICATION.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.END_USER_APPLICATION)
                        .table(euda)
                        .idField(euda.ID)
                        .nameField(euda.NAME)
                        .descriptionField(euda.DESCRIPTION)
                        .externalIdField(euda.EXTERNAL_ID)
                        .build();
            case ENTITY_RELATIONSHIP:
                EntityRelationship er = alias == null ? Tables.ENTITY_RELATIONSHIP : Tables.ENTITY_RELATIONSHIP.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.ENTITY_RELATIONSHIP)
                        .table(er)
                        .idField(er.ID)
                        .nameField(CommonTableFields.NA_FIELD_VAL)
                        .descriptionField(er.DESCRIPTION)
                        .externalIdField(CommonTableFields.NA_FIELD_VAL)
                        .build();
            case ENTITY_STATISTIC:
                EntityStatisticDefinition esd = alias == null ? Tables.ENTITY_STATISTIC_DEFINITION : Tables.ENTITY_STATISTIC_DEFINITION.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.ENTITY_STATISTIC)
                        .table(esd)
                        .idField(esd.ID)
                        .parentIdField(esd.PARENT_ID)
                        .nameField(esd.NAME)
                        .descriptionField(esd.DESCRIPTION)
                        .externalIdField(esd.EXTERNAL_ID)
                        .build();
            case FLOW_CLASSIFICATION_RULE:
                FlowClassificationRule fcr = alias == null ? Tables.FLOW_CLASSIFICATION_RULE : Tables.FLOW_CLASSIFICATION_RULE.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.FLOW_CLASSIFICATION_RULE)
                        .table(fcr)
                        .idField(fcr.ID)
                        .parentIdField(null)
                        .nameField(CommonTableFields.NA_FIELD_VAL)
                        .descriptionField(null)
                        .externalIdField(fcr.EXTERNAL_ID)
                        .build();
            case FLOW_CLASSIFICATION:
                FlowClassification fc = alias == null ? Tables.FLOW_CLASSIFICATION : Tables.FLOW_CLASSIFICATION.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.FLOW_CLASSIFICATION)
                        .table(fc)
                        .idField(fc.ID)
                        .parentIdField(null)
                        .nameField(fc.NAME)
                        .descriptionField(fc.DESCRIPTION)
                        .externalIdField(fc.CODE)
                        .build();
            case FLOW_DIAGRAM:
                FlowDiagram fd = alias == null ? Tables.FLOW_DIAGRAM : Tables.FLOW_DIAGRAM.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.FLOW_DIAGRAM)
                        .table(fd)
                        .idField(fd.ID)
                        .parentIdField(null)
                        .nameField(fd.NAME)
                        .descriptionField(fd.DESCRIPTION)
                        .externalIdField(CommonTableFields.NA_FIELD_VAL)
                        .build();
            case INVOLVEMENT_KIND:
                InvolvementKind ik = alias == null ? Tables.INVOLVEMENT_KIND : Tables.INVOLVEMENT_KIND.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.INVOLVEMENT_KIND)
                        .table(ik)
                        .idField(ik.ID)
                        .parentIdField(null)
                        .nameField(ik.NAME)
                        .descriptionField(ik.DESCRIPTION)
                        .externalIdField(ik.EXTERNAL_ID)
                        .build();
            case LEGAL_ENTITY:
                LegalEntity le = alias == null ? Tables.LEGAL_ENTITY : Tables.LEGAL_ENTITY.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.LEGAL_ENTITY)
                        .table(le)
                        .idField(le.ID)
                        .nameField(le.NAME)
                        .descriptionField(le.DESCRIPTION)
                        .externalIdField(le.EXTERNAL_ID)
                        .lifecycleField(le.ENTITY_LIFECYCLE_STATUS)
                        .build();
            case LEGAL_ENTITY_RELATIONSHIP:
                LegalEntityRelationship ler = alias == null ? Tables.LEGAL_ENTITY_RELATIONSHIP : Tables.LEGAL_ENTITY_RELATIONSHIP.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.LEGAL_ENTITY_RELATIONSHIP)
                        .table(ler)
                        .idField(ler.ID)
                        .nameField(CommonTableFields.NA_FIELD_VAL)
                        .descriptionField(ler.DESCRIPTION)
                        .externalIdField(ler.EXTERNAL_ID)
                        .build();
            case LICENCE:
                Licence lic = alias == null ? Tables.LICENCE : Tables.LICENCE.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.LICENCE)
                        .table(lic)
                        .idField(lic.ID)
                        .nameField(lic.NAME)
                        .descriptionField(lic.DESCRIPTION)
                        .externalIdField(lic.EXTERNAL_ID)
                        .build();
            case LOGICAL_DATA_ELEMENT:
                LogicalDataElement lde = alias == null ? Tables.LOGICAL_DATA_ELEMENT : Tables.LOGICAL_DATA_ELEMENT.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.LOGICAL_DATA_ELEMENT)
                        .table(lde)
                        .idField(lde.ID)
                        .nameField(lde.NAME)
                        .descriptionField(lde.DESCRIPTION)
                        .externalIdField(lde.EXTERNAL_ID)
                        .lifecycleField(lde.ENTITY_LIFECYCLE_STATUS)
                        .build();
            case LOGICAL_DATA_FLOW:
                LogicalFlow lf = alias == null ? Tables.LOGICAL_FLOW : Tables.LOGICAL_FLOW.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.LOGICAL_DATA_FLOW)
                        .table(lf)
                        .idField(lf.ID)
                        .nameField(CommonTableFields.NA_FIELD_VAL)
                        .descriptionField(CommonTableFields.NA_FIELD_VAL)
                        .externalIdField(lf.EXTERNAL_ID)
                        .lifecycleField(lf.ENTITY_LIFECYCLE_STATUS)
                        .build();
            case MEASURABLE:
                Measurable m = alias == null ? Tables.MEASURABLE : Tables.MEASURABLE.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.MEASURABLE)
                        .table(m)
                        .idField(m.ID)
                        .parentIdField(m.PARENT_ID)
                        .nameField(m.NAME)
                        .descriptionField(m.DESCRIPTION)
                        .externalIdField(m.EXTERNAL_ID)
                        .lifecycleField(m.ENTITY_LIFECYCLE_STATUS)
                        .build();
            case MEASURABLE_CATEGORY:
                MeasurableCategory mc = alias == null ? Tables.MEASURABLE_CATEGORY : Tables.MEASURABLE_CATEGORY.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.MEASURABLE_CATEGORY)
                        .table(mc)
                        .idField(mc.ID)
                        .parentIdField(null)
                        .nameField(mc.NAME)
                        .descriptionField(mc.DESCRIPTION)
                        .externalIdField(mc.EXTERNAL_ID)
                        .build();
            case MEASURABLE_RATING:
                MeasurableRating mr = alias == null ? Tables.MEASURABLE_RATING : Tables.MEASURABLE_RATING.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.MEASURABLE_RATING)
                        .table(mr)
                        .idField(mr.ID)
                        .parentIdField(null)
                        .nameField(CommonTableFields.NA_FIELD_VAL)
                        .descriptionField(mr.DESCRIPTION)
                        .externalIdField(null)
                        .build();
            case ORG_UNIT:
                OrganisationalUnit ou = alias == null ? Tables.ORGANISATIONAL_UNIT : Tables.ORGANISATIONAL_UNIT.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.ORG_UNIT)
                        .table(ou)
                        .idField(ou.ID)
                        .parentIdField(ou.PARENT_ID)
                        .nameField(ou.NAME)
                        .descriptionField(ou.DESCRIPTION)
                        .externalIdField(ou.EXTERNAL_ID)
                        .build();
            case PERSON:
                Person p = alias == null ? Tables.PERSON : Tables.PERSON.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.PERSON)
                        .table(p)
                        .idField(p.ID)
                        .parentIdField(null)
                        .nameField(p.DISPLAY_NAME)
                        .descriptionField(p.EMAIL)
                        .externalIdField(p.EMPLOYEE_ID)
                        .lifecycleField(DSL.when(p.IS_REMOVED.isTrue(), EntityLifecycleStatus.REMOVED.name()).otherwise(EntityLifecycleStatus.ACTIVE.name()))
                        .build();
            case PHYSICAL_FLOW:
                PhysicalFlow pf = alias == null ? Tables.PHYSICAL_FLOW : Tables.PHYSICAL_FLOW.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.PHYSICAL_FLOW)
                        .table(pf)
                        .idField(pf.ID)
                        .parentIdField(null)
                        .nameField(pf.NAME)
                        .descriptionField(pf.DESCRIPTION)
                        .externalIdField(pf.EXTERNAL_ID)
                        .lifecycleField(pf.ENTITY_LIFECYCLE_STATUS)
                        .build();
            case PHYSICAL_SPECIFICATION:
                PhysicalSpecification ps = alias == null ? Tables.PHYSICAL_SPECIFICATION : Tables.PHYSICAL_SPECIFICATION.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.PHYSICAL_FLOW)
                        .table(ps)
                        .idField(ps.ID)
                        .parentIdField(null)
                        .nameField(ps.NAME)
                        .descriptionField(ps.DESCRIPTION)
                        .externalIdField(ps.EXTERNAL_ID)
                        .lifecycleField(DSL.when(ps.IS_REMOVED.isTrue(), EntityLifecycleStatus.REMOVED.name()).otherwise(EntityLifecycleStatus.ACTIVE.name()))
                        .build();
            case SERVER:
                ServerInformation srv = alias == null ? Tables.SERVER_INFORMATION : Tables.SERVER_INFORMATION.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.SERVER)
                        .table(srv)
                        .idField(srv.ID)
                        .parentIdField(null)
                        .nameField(srv.HOSTNAME)
                        .descriptionField(srv.OPERATING_SYSTEM)
                        .externalIdField(srv.EXTERNAL_ID)
                        .build();
            case SOFTWARE:
                SoftwarePackage sp = alias == null ? Tables.SOFTWARE_PACKAGE : Tables.SOFTWARE_PACKAGE.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.SOFTWARE)
                        .table(sp)
                        .idField(sp.ID)
                        .parentIdField(null)
                        .nameField(sp.NAME)
                        .descriptionField(sp.GROUP)
                        .externalIdField(sp.EXTERNAL_ID)
                        .build();
            case SURVEY_QUESTION:
                SurveyQuestion sq = alias == null ? Tables.SURVEY_QUESTION : Tables.SURVEY_QUESTION.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.SURVEY_QUESTION)
                        .table(sq)
                        .idField(sq.ID)
                        .parentIdField(null)
                        .nameField(sq.LABEL)
                        .descriptionField(sq.HELP_TEXT)
                        .externalIdField(sq.EXTERNAL_ID)
                        .build();
            case SURVEY_TEMPLATE:
                SurveyTemplate st = alias == null ? Tables.SURVEY_TEMPLATE : Tables.SURVEY_TEMPLATE.as(alias);
                return ImmutableCommonTableFields
                        .builder()
                        .entityKind(EntityKind.SURVEY_TEMPLATE)
                        .table(st)
                        .idField(st.ID)
                        .parentIdField(null)
                        .nameField(st.NAME)
                        .descriptionField(st.DESCRIPTION)
                        .externalIdField(st.EXTERNAL_ID)
                        .build();
            default:
                throw new UnsupportedOperationException("Cannot determine table fields for entity kind:" + kind);
        }
    }
}
