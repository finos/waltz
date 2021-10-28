<script>
    import {
        contextPanelMode,
        filteredArcs,
        layoutDirection,
        layoutDirections,
        Modes,
        selectedClient,
        selectedDecorator
    } from "./flow-decorator-store";
    import _ from "lodash";
    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import {mkRef} from "../../../common/entity-utils";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {createEventDispatcher} from "svelte";

    export let parentEntity

    let logicalFlowId = null;

    let dispatch = createEventDispatcher();

    $: decoratorCall = dataTypeDecoratorStore.findByFlowIds([logicalFlowId]);
    $: decorators = _.chain($decoratorCall.data)
        .map(d => Object.assign({}, d, {info: _.get(decoratorInfoByDtId, d.dataTypeId)}))
        .orderBy(d => d.decoratorEntity.name)
        .value();

    $: source = $layoutDirection === layoutDirections.clientToCategory ? $selectedClient.name : parentEntity.name
    $: target = $layoutDirection === layoutDirections.clientToCategory ? parentEntity.name : $selectedClient.name

    $: flowClassificationCall = flowClassificationStore.findAll()
    $: ratingsByCode = _.keyBy($flowClassificationCall.data, d => d.code);
    $: noOpinionRating = ratingsByCode['NO_OPINION'];

    $: logicalFlowId = _
        .chain($filteredArcs)
        .filter(a => a.clientId === $selectedClient.id)
        .map(a => a.flowId)
        .first()
        .value();

    $: decoratorInfoCall = dataTypeDecoratorStore.findDatatypeUsageCharacteristics(mkRef("LOGICAL_DATA_FLOW", logicalFlowId));
    $: decoratorInfo = $decoratorInfoCall?.data
    $: decoratorInfoByDtId = _.keyBy(decoratorInfo, d => d.dataTypeId);

    function selectDecorator(decorator) {
        $selectedDecorator = decorator;
        $contextPanelMode = Modes.DECORATOR;
    }

    function mkHoverText(decorator) {
        return _
            .chain([decorator.info?.warningMessageForViewers, decorator.info?.warningMessageForEditors])
            .compact()
            .join(' ')
            .value()
    }

</script>

<div class="small help-block">
    Data types on logical flows from {source} to {target}:
</div>
<div class:waltz-scroll-region-250={_.size(decorators) > 6}>
    <table class="table table-condensed table-hover small">
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
            <tr class="clickable"
                title={mkHoverText(decorator)}
                on:click={() => selectDecorator(decorator)}>
                <td>
                    <EntityLink ref={decorator.decoratorEntity}/>
                </td>
                <td>
                    <div class="rating-indicator-block"
                         style="background-color: {_.get(ratingsByCode, [decorator.rating, 'color'], noOpinionRating.color)}">&nbsp;</div>
                    {_.get(ratingsByCode, [decorator.rating, "name"], noOpinionRating.name)}
                    {#if !decorator.info?.isRemovable}
                        <Icon name="lock"/>
                    {/if}
                    {#if decorator.info?.warningMessageForViewers}
                        <Icon name="exclamation-triangle"/>
                    {/if}
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