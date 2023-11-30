<script context="module">
    import {writable} from "svelte/store";
    let hideRatings = writable(true);
</script>

<script>
    import _ from "lodash";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";
    import Toggle from "../../../../common/svelte/Toggle.svelte";

    export let decorators = [];
    export let flowClassifications = []


    $: flowClassificationsByCode = _.keyBy(flowClassifications, d => d.code);

    $: hasRatings =  !_.isEmpty(flowClassifications)
    $: showRatings = !$hideRatings && hasRatings;

</script>

<table class="">
    <tbody class="small">
    {#each _.orderBy(decorators, d => d.decoratorEntity.name) as type}
        <tr>
            <td>
                <div class="rating-icon"
                     style={`background-color: ${flowClassificationsByCode[type.rating]?.color}`}>
                </div>
                <EntityLabel ref={type.decoratorEntity}
                             showIcon={false}>
                </EntityLabel>
            </td>
            {#if showRatings}
                <td>
                    {_.get(flowClassificationsByCode, [type.rating, "name"], "-")}
                </td>
            {/if}
        </tr>
    {/each}
    </tbody>
</table>

{#if hasRatings}
    <div class="smaller">
        <Toggle state={showRatings}
                labelOn="Hide Ratings"
                onToggle={() => $hideRatings = !$hideRatings}
                labelOff="Show Ratings"/>
    </div>
{/if}



<style>

    .rating-icon {
        display: inline-block;
        height: 1em;
        width: 1em;
        border:1px solid #ccc;
        border-radius: 2px;
    }

    table {
        width: 100%;
    }

    table td {
        padding-bottom: 0.2em;
        padding-right: 0.6em;
        border-bottom: 1px solid #eee;
        vertical-align: top;
    }
</style>