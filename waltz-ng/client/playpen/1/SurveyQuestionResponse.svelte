<script>

    import StringResponseRenderer from "./StringResponseRenderer.svelte";
    import NumberResponseRenderer from "./NumberResponseRenderer.svelte";
    import DateResponseRenderer from "./DateResponseRenderer.svelte";

    export let response;
    export let question;

    function determineResponseRender(question) {
        switch (question.fieldType) {
            case "NUMBER":
                return NumberResponseRenderer;
            case "APPLICATION":
            case "BOOLEAN":
            case "DATE":
                return DateResponseRenderer;
            case "DROPDOWN":
            case "DROPDOWN_MULTI_SELECT":
            case "MEASURABLE_MULTI_SELECT":
            case "PERSON":
            case "TEXT":
                return StringResponseRenderer;
            case "TEXTAREA":
            default:
                return StringResponseRenderer;
        }
    }

    $: comp = determineResponseRender(question);

</script>


<svelte:component {response} this={comp}/>
{#if response?.comment}
    <div class="small">Comment: {response?.comment}</div>
{/if}