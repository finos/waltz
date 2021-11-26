<script>

    import InvolvementPicker from "./InvolvementPicker.svelte";
    import CostKindPicker from "./CostKindPicker.svelte";
    import SurveyQuestionPicker from "./SurveyQuestionPicker.svelte";
    import AssessmentDefinitionPicker from "./AssessmentDefinitionPicker.svelte";
    import MeasurablePicker from "./MeasurablePicker.svelte";

    export let onSelect = (d) => console.log("Selecting an entity", d);
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
            default:
                throw "Cannot find picker for kind: " + entityKind;
        }
    }

    $: comp = determineEntityPicker(entityKind);

</script>

<svelte:component {onSelect} {selectionFilter} this={comp}/>
