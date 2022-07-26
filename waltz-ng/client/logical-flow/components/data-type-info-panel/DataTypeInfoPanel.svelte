<script>

    import {dataTypeDecoratorStore} from "../../../svelte-stores/data-type-decorator-store";
    import {entity} from "../../../common/services/enums/entity";
    import {mkSelectionOptions} from "../../../common/selector-utils";
    import {physicalFlowStore} from "../../../svelte-stores/physical-flow-store";
    import _ from "lodash";
    import {truncateMiddle} from "../../../common/string-utils";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import DescriptionFade from "../../../common/svelte/DescriptionFade.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";

    export let datatype;
    export let logicalFlow;
    export let primaryEntityRef;

    let specDecoratorCall;
    let physFlowCall;
    let qry = "";

    $: {
        if (primaryEntityRef) {
            specDecoratorCall = dataTypeDecoratorStore.findBySelector(entity.PHYSICAL_SPECIFICATION.key, mkSelectionOptions(primaryEntityRef))
            physFlowCall = physicalFlowStore.findBySelector(mkSelectionOptions(logicalFlow));
        }
    }

    $: specsById = _
        .chain($specDecoratorCall.data)
        .filter(d => d.dataTypeId === datatype.dataType.id)
        .keyBy(d => d.entityReference.id)
        .value();

    $: physicalFlows = _
        .chain($physFlowCall.data)
        .map(d => Object.assign({}, d, {specification: specsById[d.specificationId]}))
        .filter(d => !_.isNil(d.specification))
        .value();

    $: filteredPhysicalFlows = _.isEmpty(qry)
        ? physicalFlows
        : termSearch(physicalFlows, qry, ["externalId", "frequency", "transport", "criticality"]);

</script>


<h4>{datatype?.dataType.name}</h4>
<DescriptionFade text={datatype?.dataType.description}/>

{#if !_.isEmpty(physicalFlows)}
    <br>
    <div>Physical flows sharing this data type:</div>

    {#if _.size(physicalFlows) > 10}
        <SearchInput bind:value={qry}/>
    {/if}
    <div class:waltz-scroll-region-350={_.size(physicalFlows) > 10}>
        <table class="table table-condensed small">
            <colgroup>
                <col width="40%"/>
                <col width="20%"/>
                <col width="20%"/>
                <col width="20%"/>
            </colgroup>
            <thead>
            <tr>
                <th>External Id</th>
                <th>Frequency</th>
                <th>Transport</th>
                <th>Criticality</th>
            </tr>
            </thead>
            <tbody>
            {#each filteredPhysicalFlows as flow}
                <tr>
                    <td title={flow.externalId}>
                        <EntityLink ref={flow}>
                            {truncateMiddle(flow.externalId, 30)}
                        </EntityLink>
                    </td>
                    <td>{flow.frequency}</td>
                    <td>{flow.transport}</td>
                    <td>{flow.criticality}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
{/if}