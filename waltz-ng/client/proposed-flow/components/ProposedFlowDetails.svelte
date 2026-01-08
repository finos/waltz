<script>
    import EntityLink from "../../common/svelte/EntityLink.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import { safe,STATES } from "../utils";

    export let proposedFlow = {};

    $: flowDef = proposedFlow?.flowDef || {};

    $: flowDetails = [
        {
            key: "Source App",
            entityRef: flowDef?.source,
            value: safe(flowDef?.source?.name)
        },
        {
            key: "Target App",
            entityRef: flowDef?.target,
            value: safe(flowDef?.target?.name)
        },
        { key: "Source External ID", value: safe(flowDef?.source?.externalId) },
        { key: "Target External ID", value: safe(flowDef?.target?.externalId) },
        {
            key: "Name",
            entityRef: proposedFlow?.physicalFlowId? {
                id:proposedFlow?.physicalFlowId,
                name:safe(flowDef?.flowAttributes?.name) || safe(flowDef?.specification?.name),
                kind:"PHYSICAL_FLOW"
            }:null,
            value: safe(flowDef?.flowAttributes?.name) || safe(flowDef?.specification?.name)
        },
        { key: "Basis Offset", value: safe(flowDef?.flowAttributes?.basisOffset) },
        { key: "Criticality", value: safe(flowDef?.flowAttributes?.criticality) },
        { key: "Transport", value: safe(flowDef?.flowAttributes?.transport) },
        { key: "Frequency", value: safe(flowDef?.flowAttributes?.frequency) },
        { key: "Provenance", value: safe(flowDef?.specification?.provenance) },
        { key: "Justification", value: safe(flowDef?.reason?.description) }
    ]

    $: specDetails = [
        {
            key: "Name",
            value: proposedFlow.workflowState.state === STATES.FULLY_APPROVED? proposedFlow.specificationId: safe(flowDef?.specification?.name),
            entityRef: proposedFlow.workflowState.state === STATES.FULLY_APPROVED?
                {
                    id:proposedFlow.specificationId,
                    name:flowDef?.specification?.name,
                    kind:flowDef?.specification?.kind
                }
                : flowDef?.specification,
        },
        { key: "External Identifier", value: safe(flowDef?.specification?.externalId) },
        { key: "Format", value: safe(flowDef?.specification?.format) },
        { key: "Description", value: safe(flowDef?.specification?.description) },
        { key: "Last Updated At", value: safe(flowDef?.specification?.lastUpdatedAt) },
        { key: "Last Updated By", value: safe(flowDef?.specification?.lastUpdatedBy) },
    ]
</script>

<style>
    .section {
        padding: 20px 0;
        border-bottom: 1px solid #eee;
        margin-bottom: 0;
    }
    .section-title {
        margin-bottom: 12px;
        font-size: 16px;
        line-height: inherit;
        border-bottom: 1px solid #e5e5e5;
    }
    .kv-list {
        display: grid;
        grid-template-columns: 120px 1fr;
        gap: 4px 18px;
        row-gap: 2px;
        align-items: start;
        margin-bottom: 0;
        border-bottom: 1px solid #e9e9e9;
    }
    .kv-key {
        color: #705f55;
        font-size: 12px;
        letter-spacing: 0.04em;
        text-align: left;
        padding-right: 6px;
    }
    .kv-value {
        color: #222;
        font-size: 12px;
        font-weight: 400;
        line-height: 1.5;
        border-radius: 0;
        padding: 2px 0;
        box-sizing: border-box;
        background: none;
    }
    .section:last-child {
        border-bottom: none;
    }
    @media (max-width: 540px) {
        .kv-list {
            grid-template-columns: 1fr;
        }
        .kv-key {
            text-align: left;
            padding-right: 0;
            padding-bottom: 2px;
        }
        .kv-value {
            padding: 4px 0;
        }
    }
</style>

<div class="section">
    <h5 class="section-title">
        <Icon name="table"/>
        Proposed Flow Details
    </h5>
    <div class="kv-list">
        {#each flowDetails as item}
            <div class="kv-key">{item.key}</div>
            <div class="kv-value">
                {#if item.entityRef && item.entityRef.kind && item.entityRef.id}
                    <EntityLink ref={item.entityRef}/>
                {:else}
                    {item.value}
                {/if}
            </div>
        {/each}
    </div>
</div>

<div class="section">
    <h5 class="section-title">
        <Icon name="table"/>
        Physical Specification
    </h5>
    <div class="kv-list">
        {#each specDetails as item}
            <div class="kv-key">{item.key}</div>
            <div class="kv-value">
                {#if item.entityRef && item.entityRef.kind && item.entityRef.id}
                    <EntityLink ref={item.entityRef}/>
                {:else}
                    {item.value}
                {/if}
            </div>
        {/each}
    </div>
</div>