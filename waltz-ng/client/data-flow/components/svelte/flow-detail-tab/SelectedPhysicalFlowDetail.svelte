<script>
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import {selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import {
        toCriticalityName,
        toDataFormatKindName, toFrequencyKindName, toTransportKindName
    } from "../../../../physical-flows/svelte/physical-flow-registration-utils";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import {enumValueStore} from "../../../../svelte-stores/enum-value-store";
    import {nestEnums} from "../../../../common/svelte/enum-utils";
    import pageInfo from "../../../../svelte-stores/page-navigation-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import DescriptionFade from "../../../../common/svelte/DescriptionFade.svelte";
    import DataTypeMiniTable from "./DataTypeMiniTable.svelte";
    import AssessmentsTable from "./widgets/AssessmentsTable.svelte";

    function goToPhysicalFlowPage(flow) {
        $pageInfo = {
            state: "main.physical-flow.view",
            params: {
                id: flow.physicalFlow.id
            }
        }
    }

    export let flowClassifications = [];
    export let assessmentDefinitions = [];

    let enumsCall = enumValueStore.load();
    let permissionsCall = null;
    let hasEditPermission = false;

    $: permissionsCall = logicalFlowStore.findPermissionsForFlow($selectedLogicalFlow?.logicalFlow.id);
    $: permissions = $permissionsCall?.data;
    $: hasEditPermission = _.some(permissions, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));

    $: nestedEnums = nestEnums($enumsCall.data);
    $: logicalFlow = $selectedPhysicalFlow.logicalFlow;
    $: physicalFlow = $selectedPhysicalFlow.physicalFlow;
    $: specification = $selectedPhysicalFlow.specification;
    $: dataTypesForSpecification = $selectedPhysicalFlow.dataTypesForSpecification;
    $: assessmentDefinitionsById = _.keyBy(assessmentDefinitions, d => d.id);
    $: ratingsByDefId = _.merge(
        $selectedLogicalFlow.physicalFlowRatingsByDefId,
        $selectedLogicalFlow.physicalSpecRatingsByDefId);

    $: ref = {
        id: physicalFlow.id,
        name: physicalFlow.name || specification.name,
        kind: "PHYSICAL_FLOW"
    };
</script>


<h4>
    <span>
        Physical Flow
        <Icon name={_.get(entity, [logicalFlow.source.kind, "icon"])}/>
        <Icon name="arrow-right"/>
        <Icon name={_.get(entity, [logicalFlow.target.kind, "icon"])}/>
    </span>
</h4>

<table class="table table-condensed small">
    <tbody>
    <tr>
        <td style="width: 20%">Name</td>
        <td>
            <EntityLink ref={ref}/>
        </td>
    </tr>
    <tr>
        <td>External Id</td>
        <td>{physicalFlow.externalId || "-"}</td>
    </tr>
    <tr>
        <td>Specification</td>
        <td>
            <EntityLink ref={specification}
                        isSecondaryLink={true}/>
        </td>
    </tr>
    <tr>
        <td>Data Types</td>
        <td>
            <DataTypeMiniTable decorators={dataTypesForSpecification}
                               {flowClassifications}/>
        </td>
    </tr>
    <tr>
        <td>Specification Format</td>
        <td>
            {toDataFormatKindName(nestedEnums, specification.format)}
        </td>
    </tr>
    <tr>
    <tr>
        <td>Criticality</td>
        <td>
            {toCriticalityName(nestedEnums, physicalFlow.criticality)}
        </td>
    </tr>
    <tr>
        <td>Frequency</td>
        <td>
            {toFrequencyKindName(nestedEnums, physicalFlow.frequency)}
        </td>
    </tr>
    <tr>
        <td>Transport Kind</td>
        <td>
            {toTransportKindName(nestedEnums, physicalFlow.transport)}
        </td>
    </tr>
    <tr>
        <td>Basis Offset</td>
        <td>
            {physicalFlow.basisOffset}
        </td>
    </tr>
    {#if physicalFlow.description}
        <tr>
            <td>Description</td>
            <td>
                <DescriptionFade expanderAlignment={"right"}
                                 text={physicalFlow.description}/>
            </td>
        </tr>
    {/if}
    </tbody>
</table>


{#if !_.isEmpty(ratingsByDefId)}
    <AssessmentsTable {ratingsByDefId}
                      {assessmentDefinitionsById}/>
{/if}

<details>
    <summary>Actions</summary>
    <menu>
        <li>
            <button class="btn btn-skinny"
                    on:click={() => goToPhysicalFlowPage($selectedPhysicalFlow)}>
                Visit the physical flow page
            </button>
            {#if hasEditPermission}
                <span class="help-block">To remove the flow or edit it's attributes</span>
            {/if}
        </li>
    </menu>
</details>


<style>
    menu {
        padding-left: 1em;
    }

    menu li {
        list-style: none;
    }
</style>