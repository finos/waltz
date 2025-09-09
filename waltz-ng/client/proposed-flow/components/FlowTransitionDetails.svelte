<script>
    import Icon from "../../common/svelte/Icon.svelte";
    import NoData from "../../common/svelte/NoData.svelte";
    import { formatDate, stateMeta } from "../utils";

    export let proposedFlow;
    $: workflowTransitionList = proposedFlow?.workflowTransitionList || [];

    let openIdx = null;
</script>

<div class="accordion-container">
  {#if workflowTransitionList.length === 0}
    <NoData type="warning">
        <Icon name="exclamation-triangle" />
        No workflow transitions.
    </NoData>
  {:else}
    {#each workflowTransitionList as transition, idx}
        <div class="accordion-item">
            <button
                class="accordion-header"
                on:click={() => openIdx = openIdx === idx ? null : idx}
                aria-expanded={openIdx === idx}
                aria-controls={`panel-${idx}`}
            >
                <span class="step-icon" style="background:{stateMeta[transition.toState]?.color || '#d9d9d9'}">
                  <Icon name={stateMeta[transition.toState]?.icon}/>
                </span>
                <span class="step-label">
                    {stateMeta[transition.toState]?.label || transition.toState}
                </span>
                <span class="step-date">{formatDate(transition.lastUpdatedAt)}</span>
                <span class="accordion-arrow" aria-hidden="true">{openIdx === idx ? "▲" : "▼"}</span>
            </button>
            {#if openIdx === idx}
                <div
                    class="accordion-panel"
                    id={`panel-${idx}`}
                    role="region"
                    aria-labelledby={`header-${idx}`}
                >
                    <table class="meta-table">
                        <tr><th>From State</th><td>{transition.fromState || '-'}</td></tr>
                        <tr><th>To State</th><td>{transition.toState}</td></tr>
                        <tr><th>Last Updated At</th><td>{formatDate(transition.lastUpdatedAt)}</td></tr>
                        <tr><th>Last Updated By</th><td>{transition.lastUpdatedBy}</td></tr>
                        <tr><th>Reason</th><td>{transition.reason}</td></tr>
                        <tr><th>Provenance</th><td>{transition.provenance}</td></tr>
                    </table>
                </div>
            {/if}
        </div>
    {/each}
  {/if}
</div>

<style>
.accordion-container {
    width: 100%;
    max-width: 500px;
    margin: 0 auto;
    background: #fff;
    border-radius: 10px;
    box-shadow: 0 1px 8px rgba(0,0,0,0.04);
    padding: 0.5rem 0.2rem;
}
.accordion-item + .accordion-item {
    border-top: 1px solid #f1f1f1;
}
.accordion-header {
    width: 100%;
    background: none;
    border: none;
    outline: none;
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 10px 6px;
    font-size: 15px;
    cursor: pointer;
    color: #222;
    transition: background 0.18s;
    border-radius: 6px;
}
.accordion-header:hover, .accordion-header[aria-expanded="true"] {
    background: #f5f8ff;
}
.step-icon {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    font-size: 1.5rem;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 6px;
    box-shadow: 0 0 2px #eee;
}
.step-label {
    font-weight: 500;
    color: #222;
    font-size: 14px;
    flex: 1 1 0%;
    text-align: justify;
}
.step-date {
    color: #7e7e7e;
    font-size: 12px;
    margin-right: 10px;
}
.accordion-arrow {
    color: #aaa;
    font-size: 13px;
    padding-left: 6px;
}
.accordion-panel {
    padding-left: 44px;
    padding-right: 10px;
    padding-bottom: 12px;
    padding-top: 12px;
    animation: fadeIn 0.3s;
}
@keyframes fadeIn {
    from { opacity: 0; }
    to { opacity: 1; }
}
.meta-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 12px;
    background: #fcfcfc;
    border-radius: 7px;
    box-shadow: 0 0 0.5px #e0e0e0;
    margin-bottom: 0;
}
.meta-table th,
.meta-table td {
    padding: 6px 10px;
    border-bottom: 1px solid #e9e9e9;
    text-align: left;
    vertical-align: top;
    color: #222;
    font-size: 12px;
    font-weight: 400;
    border-radius: 0;
}
.meta-table th {
    width: 140px;
    color: #705f55;
    font-size: 12px;
    font-weight: normal;
    letter-spacing: 0.04em;
    text-align: left;
    padding-right: 6px;
}

@media (max-width: 600px) {
    .accordion-panel {
        padding-left: 8px;
        padding-right: 2px;
    }
    .accordion-header {
        padding: 8px 2px;
        font-size: 14px;
    }
    .meta-table th, .meta-table td {
        padding: 3px 5px;
        font-size: 11px;
    }
}
</style>