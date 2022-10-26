<script>
    import PageHeader from "../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import toasts from "../../svelte-stores/toast-store";
    import pageInfo from "../../svelte-stores/page-navigation-store";

    import {
        dataTypes,
        expandedSections,
        logicalFlow,
        nestedEnums,
        physicalFlow,
        physicalSpecification,
        skipDataTypes,
        viewMode,
        ViewMode
    } from "./physical-flow-editor-store";

    import _ from "lodash";
    import {onMount} from "svelte";
    import LogicalFlowSelectionStep from "./LogicalFlowSelectionStep.svelte";
    import PhysicalFlowCharacteristicsStep from "./PhysicalFlowCharacteristicsStep.svelte";
    import PhysicalSpecificationStep from "./PhysicalSpecificationStep.svelte";
    import {sections} from "./physical-flow-registration-utils";
    import {physicalFlowStore} from "../../svelte-stores/physical-flow-store";
    import {toEntityRef} from "../../common/entity-utils";
    import {displayError} from "../../common/error-utils";
    import Icon from "../../common/svelte/Icon.svelte";
    import ClonePhysicalFlowPanel from "./ClonePhysicalFlowPanel.svelte";
    import {enumValueStore} from "../../svelte-stores/enum-value-store";
    import {nestEnums} from "../../common/svelte/enum-utils";
    import Toggle from "../../common/svelte/Toggle.svelte";
    import DataTypeSelectionStep from "./DataTypeSelectionStep.svelte";

    export let primaryEntityRef = {};

    const Modes = {
        CREATE: "CREATE",
        CLONE: "CLONE"
    }

    let activeMode = Modes.CREATE;
    let enumsCall = enumValueStore.load();

    $: $nestedEnums = nestEnums($enumsCall.data);

    onMount(() => {
        $expandedSections = [sections.ROUTE];

        // clear off any previous store values, logicals are handled when a target is read from the state params
        $physicalSpecification = null;
        $physicalFlow = null;
    })


    function createFlow() {

        const specification = {
            owningEntity: toEntityRef(primaryEntityRef),
            name: $physicalSpecification.name,
            description: $physicalSpecification.description,
            format: $physicalSpecification.format,
            lastUpdatedBy: "waltz",
            externalId: $physicalSpecification.externalId,
            id: $physicalSpecification.id ? $physicalSpecification.id : null
        }

        const flowAttributes = {
            transport: $physicalFlow.transport,
            frequency: $physicalFlow.frequency,
            basisOffset: $physicalFlow.basisOffset,
            criticality: $physicalFlow.criticality,
            description: $physicalFlow.description,
            externalId: $physicalFlow.externalId
        }

        const command = {
            specification,
            flowAttributes,
            logicalFlowId: $logicalFlow.id,
            dataTypeIds: $dataTypes
        }

        physicalFlowStore.create(command)
            .then(() => toasts.success("Successfully added physical flow"))
            .then(() => {
                $pageInfo = {
                    state: "main.app.view",
                    params: {
                        id: primaryEntityRef.id
                    }
                };
            })
            .catch(e => displayError("Could not create physical flow", e));
    }

    function selectFlowToClone(flow) {
        $logicalFlow = flow.logicalFlow;
        $physicalSpecification = flow.specification
        $physicalFlow = flow;
        activeMode = Modes.CREATE;
        $expandedSections = [sections.ROUTE, sections.SPECIFICATION, sections.FLOW];
    }

    function toggleViewMode() {
        $viewMode = $viewMode === ViewMode.SECTION
            ? ViewMode.FLOW
            : ViewMode.SECTION;
    }

    $: incompleteRecord = !($logicalFlow && $physicalFlow && $physicalSpecification && (!_.isEmpty($dataTypes) || $skipDataTypes));

</script>


{#if primaryEntityRef}
<PageHeader name="Register new Physical Flow"
            icon="dot-circle-o"
            small={_.get(primaryEntityRef, ["name"], "-")}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><EntityLink ref={primaryEntityRef}/></li>
            <li>Register Physical Flow</li>
        </ol>
    </div>

    <div slot="summary">
        <div class="pull-right">
            <Toggle labelOn="Show old view"
                    labelOff="Show new flows"
                    state={$viewMode === ViewMode.SECTION}
                    onToggle={() => toggleViewMode()}/>
        </div>

        {#if activeMode === Modes.CLONE}

            <ClonePhysicalFlowPanel {primaryEntityRef}
                                    on:select={(evt) => selectFlowToClone(evt.detail)}/>
            <br>
            <button class="btn btn-skinny"
                    on:click={() => activeMode = Modes.CREATE}>
                Cancel
            </button>

        {:else if activeMode = Modes.CREATE}

            <div class="help-block">
                <Icon name="info-circle"/>
                Complete the form below to register a new physical flow, or
                <button class="btn btn-skinny"
                        on:click={() => activeMode = Modes.CLONE}>
                    clone an existing flow
                </button>
                and modify it's details.
            </div>

            <div class="selection-step">
                <LogicalFlowSelectionStep {primaryEntityRef}/>
            </div>

            <div class="selection-step">
                <PhysicalSpecificationStep {primaryEntityRef}/>
            </div>

            <div class="selection-step">
                <PhysicalFlowCharacteristicsStep {primaryEntityRef}/>
            </div>

            <div class="selection-step">
                <DataTypeSelectionStep {primaryEntityRef}/>
            </div>

            <br>

            <span>
                <button class="btn btn-success"
                        disabled={incompleteRecord}
                        on:click={() => createFlow()}>
                    Create
                </button>

                {#if incompleteRecord}
                    <span class="incomplete-warning">
                        <Icon name="exclamation-triangle"/>You must complete all sections
                    </span>
                {/if}
            </span>
        {/if}
    </div>
</PageHeader>

{/if}


<style type="text/scss">
    @import "../../../style/_variables.scss";

    .incomplete-warning {
        color: $waltz-amber;
    }

    .selection-step {
        border: #EEEEEE 1px solid;
        padding-bottom: 1em;
        padding-left: 1em;
        padding-right: 1em;
        margin-bottom: 0.25em;
    }
</style>