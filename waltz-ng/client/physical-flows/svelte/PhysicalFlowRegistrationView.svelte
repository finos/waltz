<script>
    import PageHeader from "../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../common/svelte/ViewLink.svelte";
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import toasts from "../../svelte-stores/toast-store";

    import {expandedSections, logicalFlow, physicalFlow, physicalSpecification} from "./physical-flow-editor-store";

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

    export let primaryEntityRef = {};

    onMount(() => {
        $expandedSections = [sections.ROUTE];
    })

    function createFlow() {

        const specification = {
            owningEntity: toEntityRef(primaryEntityRef),
            name: $physicalSpecification.name,
            description: $physicalSpecification.description,
            format: $physicalSpecification.format,
            lastUpdatedBy: "waltz"
        }

        const flowAttributes = {
            transport: $physicalFlow.transport,
            frequency: $physicalFlow.frequency,
            basisOffset: $physicalFlow.basisOffset,
            criticality: $physicalFlow.criticality
        }

        const command = {
            specification,
            flowAttributes,
            logicalFlowId: $logicalFlow.id
        }

        physicalFlowStore.create(command)
            .then(() => {
                toasts.success("Successfully added physical flow");
                history.back();
            })
            .catch(e => displayError("Could not create physical flow", e));
    }

    $: incompleteRecord = !($logicalFlow && $physicalFlow && $physicalSpecification);

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

        <LogicalFlowSelectionStep {primaryEntityRef}/>

        <PhysicalSpecificationStep {primaryEntityRef}/>

        <PhysicalFlowCharacteristicsStep {primaryEntityRef}/>

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
    </div>
</PageHeader>

{/if}


<style type="text/scss">
    @import "../../../style/_variables.scss";

    .incomplete-warning {
        color: $waltz-amber;
    }
</style>