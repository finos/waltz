<script>
    import _ from "lodash";
    import {entity} from "../../../../common/services/enums/entity";
    import {selectedLogicalFlow, selectedPhysicalFlow} from "./flow-details-store";
    import {
        toCriticalityName,
        toDataFormatKindName, toFrequencyKindName, toTransportKindName
    } from "../propose-data-flow/propose-data-flow-utils";
    import EntityLink from "../../../../common/svelte/EntityLink.svelte";
    import {enumValueStore} from "../../../../svelte-stores/enum-value-store";
    import {nestEnums} from "../../../../common/svelte/enum-utils";
    import pageInfo from "../../../../svelte-stores/page-navigation-store";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import {logicalFlowStore} from "../../../../svelte-stores/logical-flow-store";
    import DescriptionFade from "../../../../common/svelte/DescriptionFade.svelte";
    import DataTypeMiniTable from "./DataTypeMiniTable.svelte";
    import AssessmentsTable from "./widgets/AssessmentsTable.svelte";
    import {createEventDispatcher} from "svelte";
    import EntityLabel from "../../../../common/svelte/EntityLabel.svelte";
    import toastStore from "../../../../svelte-stores/toast-store";
    import {physicalFlowStore} from "../../../../svelte-stores/physical-flow-store";
    import {settingsStore} from "../../../../svelte-stores/settings-store";
    import {isDataFlowProposalsEnabled} from "../../../../common/utils/settings-util";

    const ActionSectionStates = {
        LIST: "LIST",
        REMOVAL_CONFIRMATION: "REMOVAL_CONFIRMATION"
    };

    const dispatch = createEventDispatcher();


    function goToPhysicalFlowPage(flow) {
        $pageInfo = {
            state: "main.physical-flow.view",
            params: {
                id: flow.physicalFlow.id
            }
        }
    }

    function doRemove(flow) {
        physicalFlowStore
            .removeFlow(flow.id)
            .then(() => {
                toastStore.info("Physical Flow removed, reloading");
                dispatch("reload");
                dispatch("clearSelected");
            });
    }

    export let flowClassifications = [];
    export let assessmentDefinitions = [];

    let enumsCall = enumValueStore.load();
    let permissionsCall = null;
    let hasAddPermission = false;
    let hasUpdatePermission = false;
    let hasRemovePermission = false;
    let actionSectionState = ActionSectionStates.LIST;

    let settingsCall=settingsStore.loadAll();
    let isSettingsLoaded = false;

    $: if ($settingsCall?.data && Object.keys($settingsCall.data).length > 0) {
        isSettingsLoaded = true;
    }
    $: dataFlowProposalsEnabled = isSettingsLoaded?isDataFlowProposalsEnabled($settingsCall.data):undefined;

    $: permissionsCall = logicalFlowStore.findPermissionsForFlow($selectedLogicalFlow?.logicalFlow.id);
    $: permissions = $permissionsCall?.data;
    $: hasAddPermission = _.includes(permissions, "ADD");
    $: hasUpdatePermission = _.includes(permissions, "UPDATE");
    $: hasRemovePermission = _.includes(permissions, "REMOVE");

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
        name: _.get(physicalFlow, "name") || _.get(specification, "name") || "??",
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
        <td>Specification External Id</td>
        <td>
            {specification.externalId}
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
            {toDataFormatKindName(nestedEnums, specification?.format)}
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

{#if dataFlowProposalsEnabled !== undefined && !dataFlowProposalsEnabled}
    <details>
        <summary>Actions</summary>
        <menu>
            {#if actionSectionState === ActionSectionStates.LIST }
                {#if hasRemovePermission && ! physicalFlow.isReadOnly}
                    <li>
                        <button class="btn btn-skinny btn-danger"
                                on:click={() => actionSectionState = ActionSectionStates.REMOVAL_CONFIRMATION}>
                            <Icon name="trash"/>
                            Remove physical flow
                        </button>
                        <p class="help-block small">
                            This will remove the physical flow.
                            Note: removed flows may be restored via the link in the changelog.
                        </p>
                    </li>
                {/if}
                <li>
                    <button class="btn btn-skinny"
                            on:click={() => goToPhysicalFlowPage($selectedPhysicalFlow)}>
                        <Icon name="qrcode"/>
                        Visit the physical flow page
                    </button>
                    {#if hasUpdatePermission}
                        <span class="help-block small">
                            To edit the flow attributes
                        </span>
                    {/if}
                </li>
            {/if}
            {#if actionSectionState === ActionSectionStates.REMOVAL_CONFIRMATION }
                <div class="removal-box">
                    <h4>Removal Confirmation:</h4>
                    <p>
                        Are you sure you want to remove this physical flow ?
                    </p>
                    <p>
                        Flow: <b><EntityLabel {ref}/></b>
                    </p>
                    <p>
                        <EntityLabel ref={logicalFlow.source}/>
                        <Icon name="arrow-right"/>
                        <EntityLabel ref={logicalFlow.target}/>
                    </p>

                    <button class="btn btn-skinny"
                            on:click={() => doRemove(physicalFlow)}>
                        <Icon name="trash"/>
                        Remove
                    </button>
                    <button class="btn btn-skinny"
                            on:click={() => actionSectionState = ActionSectionStates.LIST}>
                        Cancel
                    </button>
                </div>
            {/if}
        </menu>
    </details>
{/if}

<style>
    menu {
        padding-left: 1em;
    }

    menu li {
        list-style: none;
    }


    .removal-box{
        border-width: 1px;
        border-style: solid;
        border-color: #d93f44;
        background-color: #fae9ee;
        padding-left: 2em;
        padding-right: 2em;
        padding-bottom: 1.5em;
        padding-top: 1.5em;
        border-radius: 2px;
    }
</style>