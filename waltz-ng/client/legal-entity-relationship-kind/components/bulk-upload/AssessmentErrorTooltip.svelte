<script>

    import _ from "lodash";

    export let assessmentRating = null;

    $: errorMessages = _
        .chain(assessmentRating?.ratings)
        .flatMap(d => d.errors)
        .map(d => d.errorMessage)
        .uniq()
        .value()

</script>

<strong>This assessment has the following errors:</strong>
<ul>
    {#each errorMessages as errorMsg}
        <li>{errorMsg}</li>
    {/each}
</ul>