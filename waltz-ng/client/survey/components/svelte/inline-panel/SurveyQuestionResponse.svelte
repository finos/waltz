<script>

    import StringResponseRenderer from "./StringResponseRenderer.svelte";
    import NumberResponseRenderer from "./NumberResponseRenderer.svelte";
    import DateResponseRenderer from "./DateResponseRenderer.svelte";
    import EntityResponseRenderer from "./EntityResponseRenderer.svelte";
    import BooleanResponseRenderer from "./BooleanResponseRenderer.svelte";
    import DropdownMultiResponseRenderer from "./DropdownMultiResponseRenderer.svelte";
    import MeasurableMultiResponseRenderer from "./MeasurableMultiResponseRenderer.svelte";
    import LegalEntityResponseRenderer from "./LegalEntityResponseRenderer.svelte";

    export let response;
    export let question;

    function determineResponseRender(question) {
        switch (question.fieldType) {
            case "NUMBER":
                return NumberResponseRenderer;
            case "DATE":
                return DateResponseRenderer;
            case "PERSON":
            case "APPLICATION":
                return EntityResponseRenderer;
            case "BOOLEAN":
                return BooleanResponseRenderer;
            case "DROPDOWN_MULTI_SELECT":
            case "STRING_LIST":
                return DropdownMultiResponseRenderer;
            case "MEASURABLE_MULTI_SELECT":
                return MeasurableMultiResponseRenderer;
            case "LEGAL_ENTITY":
                return LegalEntityResponseRenderer;
            case "DROPDOWN":
            case "TEXTAREA":
            case "TEXT":
                return StringResponseRenderer;
            default:
                return StringResponseRenderer;
        }
    }

    $: comp = determineResponseRender(question);
</script>


<svelte:component {response} {question} this={comp}/>
{#if response?.comment}
    <div class="small">Comment: {response?.comment}</div>
{/if}