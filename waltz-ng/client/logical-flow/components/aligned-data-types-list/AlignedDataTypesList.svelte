<script>

    import _ from "lodash";
    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import {entity} from "../../../common/services/enums/entity";
    import {mkRef} from "../../../common/entity-utils";
    import FlowRatingCell from "../../../common/svelte/FlowRatingCell.svelte";
    import {flowClassificationStore} from "../../../svelte-stores/flow-classification-store";

    export let logicalFlow;

    let decoratorsCall;
    let inboundClassifications;
    let classificationsCall = flowClassificationStore.findAll();

    let outboundClassifications;

    $: classifications = $classificationsCall?.data || [];
    $: [inboundClassifications, outboundClassifications] = _.partition(classifications, d => d.direction === "INBOUND");

    $: inboundByCode = _.keyBy(inboundClassifications, d => d.code);
    $: outboundByCode = _.keyBy(outboundClassifications, d => d.code);

    $: {

        if (!_.isEmpty(logicalFlow)){
            const opts = mkSelectionOptions(mkRef(entity.LOGICAL_DATA_FLOW.key, logicalFlow.id));
            decoratorsCall = dataTypeDecoratorStore.findBySelector(entity.LOGICAL_DATA_FLOW.key, opts);
        }
    }

    $: decorators = _.orderBy($decoratorsCall?.data || [], d => d.decoratorEntity.name);

</script>

<ul class="list-inline">
    {#each decorators as decorator}
        <li>
            <FlowRatingCell sourceOutboundClassification={outboundByCode[decorator.rating]}
                            targetInboundClassification={inboundByCode[decorator.targetInboundRating]}/>
            {decorator.decoratorEntity.name}
        </li>
    {/each}
</ul>
