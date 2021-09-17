<script>
    import {filteredArcs, layoutDirection, layoutDirections, selectedClient} from "./flow-decorator-store";
    import _ from "lodash";
    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    export let parentEntity

    $: decoratorCall = dataTypeDecoratorStore.findByFlowIds(logicalFlowIds);
    $: decorators = _.orderBy($decoratorCall.data, d => d.decoratorEntity.name);

    $: source = $layoutDirection === layoutDirections.clientToCategory ? $selectedClient.name : parentEntity.name
    $: target = $layoutDirection === layoutDirections.clientToCategory ? parentEntity.name : $selectedClient.name

    $: flowClassificationCall = flowClassificationStore.findAll()
    $: ratingsByCode = _.keyBy($flowClassificationCall.data, d => d.code);
    $: noOpinionRating = ratingsByCode['NO_OPINION'];

    $: logicalFlowIds = _
        .chain($filteredArcs)
        .filter(a => a.clientId === $selectedClient.id)
        .map(a => a.flowId)
        .value();

</script>

<div class="small help-block">
    Data types on logical flows from {source} to {target}:
</div>
<div class:waltz-scroll-region-250={_.size(decorators) > 6}>
    <table class="table table-condensed small">
        <colgroup>
            <col style="width: 50%;">
            <col style="width: 50%;">
        </colgroup>
        <thead>
        <tr>
            <th>Data Type</th>
            <th>Decorator</th>
        </tr>
        </thead>
        <tbody>
        {#each decorators as decorator}
            <tr>
                <td>
                    <EntityLink ref={decorator.decoratorEntity}/>
                </td>
                <td>
                    <div class="rating-indicator-block"
                         style="background-color: {_.get(ratingsByCode, [decorator.rating, 'color'], noOpinionRating.color)}">&nbsp;</div>
                    {_.get(ratingsByCode, [decorator.rating, "name"], noOpinionRating.name)}
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>


<style>
    .rating-indicator-block {
        display: inline-block;
        width: 1em;
        height: 1.1em;
        border: 1px solid #aaa;
        border-radius: 2px;
        position: relative;
        top: 2px;
    }
</style>