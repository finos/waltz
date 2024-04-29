<script>
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    import _ from "lodash";
    import {flowDirection as FlowDirection} from "../../../common/services/enums/flow-direction";

    export let ratingDirection;
    let outboundClassifications = [];
    let inboundClassifications = [];

    let flowClassificationCall = flowClassificationStore.findAll();

    $: classifications = _
        .chain($flowClassificationCall.data)
        .filter(d => _.isEmpty(ratingDirection) ? true : d.direction === ratingDirection)
        .orderBy(["position", "name"])
        .value();

    $: [outboundClassifications, inboundClassifications] = _.partition(classifications, d => d.direction === FlowDirection.OUTBOUND.key);

</script>

{#if !_.isEmpty(outboundClassifications)}
    <div>
        <strong>Producer Ratings: </strong>
        <ul style="display: inline"
            class="list-inline help-block">
            {#each outboundClassifications as classification}
                <li>
        <span class="indicator"
              style={`background-color: ${classification.color}`}>
        </span>
                    <span title={classification.description}>
            {classification.name}
        </span>
                </li>
            {/each}
        </ul>
    </div>
{/if}

{#if !_.isEmpty(outboundClassifications) && !_.isEmpty(inboundClassifications)}
    <br>
{/if}

{#if !_.isEmpty(inboundClassifications)}
    <div>
        <strong>Consumer Ratings: </strong>
        <ul style="display: inline"
            class="list-inline help-block">
            {#each inboundClassifications as classification}
            <li>
                <span class="indicator"
                      style={`background-color: ${classification.color}`}>
                </span>
                <span title={classification.description}>
                    {classification.name}
                </span>
            </li>
            {/each}
        </ul>
    </div>
{/if}

<style>
    .indicator {
        display: inline-block;
        border: 1px solid #ccc;
        height: 0.9em;
        width: 1em;
    }
</style>
