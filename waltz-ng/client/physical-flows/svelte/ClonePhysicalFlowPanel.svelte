<script>

    import _ from "lodash";
    import {termSearch} from "../../common";
    import {physicalFlowStore} from "../../svelte-stores/physical-flow-store";
    import {mkSelectionOptions} from "../../common/selector-utils";
    import SearchInput from "../../common/svelte/SearchInput.svelte";
    import {logicalFlowStore} from "../../svelte-stores/logical-flow-store";
    import {physicalSpecStore} from "../../svelte-stores/physical-spec-store";
    import {nestedEnums} from "./physical-flow-editor-store";
    import {createEventDispatcher} from "svelte";
    import {
        toCriticalityName,
        toDataFormatKindName,
        toFrequencyKindName,
        toTransportKindName
    } from "./physical-flow-registration-utils";

    export let primaryEntityRef;

    let dispatch = createEventDispatcher();

    let physicalFlowCall;
    let logicalFlowCall;
    let physicalSpecificationCall;
    let filteredFlows = [];
    let qry = "";

    function selectFlow(flow) {
        dispatch("select", flow);
    }

    $: {
        if (primaryEntityRef) {
            physicalFlowCall = physicalFlowStore.findBySelector(mkSelectionOptions(primaryEntityRef));
            logicalFlowCall = logicalFlowStore.findBySelector(mkSelectionOptions(primaryEntityRef));
            physicalSpecificationCall = physicalSpecStore.findBySelector(mkSelectionOptions(primaryEntityRef));
        }
    }

    $: physicalFlows = $physicalFlowCall?.data;
    $: logicalFlows = $logicalFlowCall?.data;
    $: physicalSpecs = $physicalSpecificationCall?.data;

    $: logicalsById = _.keyBy(logicalFlows, d => d.id);
    $: specsById = _.keyBy(physicalSpecs, d => d.id);

    $: enrichedPhysicalFlows = _.map(
        physicalFlows,
        d => {
            const logicalFlow = _.get(logicalsById, d.logicalFlowId)
            const specification = _.get(specsById, d.specificationId)

            return Object.assign(
                {},
                d,
                {
                    logicalFlow,
                    specification
                })
        });

    $: filteredFlows = _.isEmpty(qry)
        ? enrichedPhysicalFlows
        : termSearch(enrichedPhysicalFlows, qry, ["name", "externalId", "format"]);

</script>


<div class="small">

    <SearchInput bind:value={qry}/>
    <br>
    <div class:waltz-scroll-region-350={_.size(filteredFlows) > 10}>
        <table class="table table-condensed table-hover">
            <colgroup>
                <col width="30%"/>
                <col width="10%"/>
                <col width="15%"/>
                <col width="15%"/>
                <col width="15%"/>
                <col width="15%"/>
            </colgroup>
            <thead>
            <tr>
                <th>Name</th>
                <th>External Id</th>
                <th>Format</th>
                <th>Transport</th>
                <th>Frequency</th>
                <th>Criticality</th>
            </tr>
            </thead>
            <tbody>
            {#each filteredFlows as flow}
                <tr class="clickable"
                    on:click={() => selectFlow(flow)}>
                    <td class="force-wrap">{flow.specification?.name}</td>
                    <td class="force-wrap">{flow.externalId || "-"}</td>
                    <td>{toDataFormatKindName($nestedEnums, flow.specification?.format)}</td>
                    <td>{toTransportKindName($nestedEnums, flow.transport)}</td>
                    <td>{toFrequencyKindName($nestedEnums, flow.frequency)}</td>
                    <td>{toCriticalityName($nestedEnums, flow.criticality)}</td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
</div>

