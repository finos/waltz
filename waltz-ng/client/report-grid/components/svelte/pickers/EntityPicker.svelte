<script>

    import InvolvementPicker from "../../../../common/svelte/entity-pickers/InvolvementPicker.svelte";
    import CostKindPicker from "../../../../common/svelte/entity-pickers/CostKindPicker.svelte";
    import SurveyQuestionPicker from "./SurveyQuestionPicker.svelte";
    import AssessmentDefinitionPicker from "../../../../common/svelte/entity-pickers/AssessmentDefinitionPicker.svelte";
    import PersonPicker from "../../../../common/svelte/entity-pickers/PersonPicker.svelte";
    import SurveyInstanceFieldPicker from "./SurveyInstanceFieldPicker.svelte";
    import ApplicationFieldPicker from "./ApplicationFieldPicker.svelte";
    import ChangeInitiativeFieldPicker from "./ChangeInitiativeFieldPicker.svelte";
    import AppGroupPicker from "../../../../common/svelte/entity-pickers/AppGroupPicker.svelte";
    import DataTypePicker from "../../../../common/svelte/entity-pickers/DataTypePicker.svelte";
    import AttestationPicker from "./AttestationPicker.svelte";
    import OrgUnitFieldPicker from "./OrgUnitFieldPicker.svelte";
    import TagPicker from "./TagPicker.svelte";
    import AliasPicker from "./AliasPicker.svelte";
    import ComplexityKindPicker from "../../../../common/svelte/entity-pickers/ComplexityKindPicker.svelte";
    import MeasurableCategoryPicker from "./MeasurableCategoryPicker.svelte";
    import EntityStatisticPicker from "../../../../common/svelte/entity-pickers/EntityStatisticPicker.svelte";
    import MeasurablePicker from "./MeasurablePicker.svelte";

    export let onSelect = (d) => console.log("Selecting an entity", d);
    export let onDeselect = (d) => console.log("Deselecting an entity", d);
    export let entityKind;
    export let subjectKindFilter = () => true;
    export let selectionFilter = () => true

    function determineEntityPicker(entityKind) {
        switch (entityKind) {
            case "INVOLVEMENT_KIND":
                return InvolvementPicker;
            case "COST_KIND":
                return CostKindPicker;
            case "COMPLEXITY_KIND":
                return ComplexityKindPicker;
            case "SURVEY_QUESTION":
                return SurveyQuestionPicker;
            case "ASSESSMENT_DEFINITION":
                return AssessmentDefinitionPicker;
            case "MEASURABLE":
                return MeasurablePicker;
            case "SURVEY_INSTANCE":
                return SurveyInstanceFieldPicker;
            case "APPLICATION":
                return ApplicationFieldPicker;
            case "CHANGE_INITIATIVE":
                return ChangeInitiativeFieldPicker;
            case "APP_GROUP":
                return AppGroupPicker;
            case "DATA_TYPE":
                return DataTypePicker;
            case "ATTESTATION":
                return AttestationPicker;
            case "ORG_UNIT":
                return OrgUnitFieldPicker;
            case "TAG":
                return TagPicker;
            case "ENTITY_ALIAS":
                return AliasPicker;
            case "MEASURABLE_CATEGORY":
                return MeasurableCategoryPicker;
            case "ENTITY_STATISTIC":
                return EntityStatisticPicker;
            case "PERSON":
                return PersonPicker;
            default:
                throw "Cannot find picker for kind: " + entityKind;
        }
    }

    $: comp = determineEntityPicker(entityKind);

</script>

<svelte:component {onSelect}
                  {onDeselect}
                  {selectionFilter}
                  {subjectKindFilter}
                  {entityKind}
                  this={comp}/>
