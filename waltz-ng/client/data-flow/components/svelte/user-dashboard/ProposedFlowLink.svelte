<script>
import Icon from "../../../../common/svelte/Icon.svelte";
import ViewLinkLabelled from "../../../../common/svelte/ViewLinkLabelled.svelte";
import { filters } from "./filter-store";
import Tooltip from "../../../../common/svelte/Tooltip.svelte";
import ProposedFlowLinkTooltipContent from "./ProposedFlowLinkTooltipContent.svelte";

export let flow = {};
export let showExclamation = false;
export let currentTab;

const handleExclamationClick = (e) => {
    e.stopPropagation();
    e.preventDefault();

    $filters[currentTab].action.includes("ACTIONABLE")
        ? $filters[currentTab].action = $filters[currentTab].action.filter(f => f !== "ACTIONABLE")
        : $filters[currentTab].action = [...$filters[currentTab].action, "ACTIONABLE"];
}
</script>

<div>
    <ViewLinkLabelled state="main.proposed-flow.view"
        ctx={{id: flow?.id}}
        openInNewTab={false}
        label={`${flow?.flowDef?.source?.name} → ${flow?.flowDef?.target?.name}`}/>
    
    {#if showExclamation}
        <Tooltip content={ProposedFlowLinkTooltipContent}>
            <span class="exclamation"
                  on:click={e => handleExclamationClick(e)}
                  on:keypress={e => handleExclamationClick(e)}
                  slot="target">
                <Icon name="exclamation-circle" size="lg"/>
            </span>
        </Tooltip>
    {/if}
</div>

<style>
    .exclamation {
        color: #ff4500;
        cursor: pointer;
    }
</style>