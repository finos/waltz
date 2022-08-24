<script>

    import InvolvementPicker from "./InvolvementPicker.svelte";
    import CostKindPicker from "./CostKindPicker.svelte";
    import SurveyQuestionPicker from "./SurveyQuestionPicker.svelte";
    import AssessmentDefinitionPicker from "./AssessmentDefinitionPicker.svelte";
    import MeasurablePicker from "./MeasurablePicker.svelte";
    import SurveyInstanceFieldPicker from "./SurveyInstanceFieldPicker.svelte";
    import ApplicationFieldPicker from "./ApplicationFieldPicker.svelte";
    import ChangeInitiativeFieldPicker from "./ChangeInitiativeFieldPicker.svelte";
    import AppGroupPicker from "./AppGroupPicker.svelte";
    import DataTypePicker from "./DataTypePicker.svelte";
    import AttestationPicker from "./AttestationPicker.svelte";
    import OrgUnitFieldPicker from "./OrgUnitFieldPicker.svelte";

    export let onSelect = (d) => console.log("Selecting an entity", d);
    export let onDeselect = (d) => console.log("Deselecting an entity", d);
    export let entityKind;
    export let selectionFilter = () => true

    function determineEntityPicker(entityKind) {
        switch (entityKind) {
            case "INVOLVEMENT_KIND":
                return InvolvementPicker;
            case "COST_KIND":
                return CostKindPicker;
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
            default:
                throw "Cannot find picker for kind: " + entityKind;
        }
    }

    $: comp = determineEntityPicker(entityKind);

</script>

<svelte:component {onSelect}
                  {onDeselect}
                  {selectionFilter}
                  this={comp}/>
