<script>
    import {
        clearSelections,
        clientScale,
        clientScrollOffset,
        contextPanelMode,
        displayedClients,
        highlightClass,
        layoutDirection,
        layoutDirections,
        Modes,
        selectedClient
    } from "./flow-decorator-store";
    import {dimensions, getNodeColors} from "./flow-decorator-utils"
    import {truncateMiddle} from "../../../common/string-utils";

    import _ from "lodash";
    import {getSymbol} from "../../../common/svg-icon";

    function onMouseEnter(client) {
        $highlightClass = `client_${client.id}`;
    }

    function onMouseLeave() {
        $highlightClass = null;
    }

    function mkClasses(client) {
        return client.kind + " clickable no-text-select";
    }

    function selectClient(client) {
        clearSelections();
        $selectedClient = client;
        $contextPanelMode = Modes.FLOW_SUMMARY;
    }

</script>


<g transform={`translate(0, ${$clientScrollOffset})`}>
    {#each $displayedClients as client}
        <g transform={`translate(0, ${$clientScale(client.id)})`}
           class={mkClasses(client)}
           on:click|stopPropagation={() => selectClient(client)}
           on:keydown|stopPropagation={() => selectClient(client)}>
            <rect stroke={getNodeColors(client.kind).stroke}
                  fill={getNodeColors(client.kind).fill}
                  on:mouseenter={() => onMouseEnter(client)}
                  on:mouseleave={() => onMouseLeave()}
                  rx={dimensions.client.height / 2}
                  width={dimensions.client.width - dimensions.client.iconPadding}
                  height={dimensions.client.height}
                  transform={`translate(${$layoutDirection === layoutDirections.clientToCategory ? 0 : dimensions.client.iconPadding}, ${$clientScale.bandwidth() / 2 - 13})`}/>
            <text dx={$layoutDirection === layoutDirections.clientToCategory ? 10 : dimensions.client.iconPadding + 10}
                  pointer-events="none"
                  dy={$clientScale.bandwidth() / 2 + 5}>
                {truncateMiddle(client.name, 22)}
            </text>
            <g class="svg-fa-icon"
               transform={`translate(${$layoutDirection === layoutDirections.clientToCategory
                    ? dimensions.client.width - dimensions.client.iconPadding + dimensions.client.iconPadding / 2
                    : dimensions.client.iconPadding / 2}, ${$clientScale.bandwidth() / 2})`}>
                {#if _.size(client.physicalFlows) === 0}
                    <path d={getSymbol("questionCircle")} fill="none" stroke="#000"></path>
                {:else if _.size(client.physicalFlows) === 1}
                    <path d={getSymbol("page")} fill="none" stroke="#000"></path>
                {:else if _.size(client.physicalFlows) === 2}
                    <path d={getSymbol("pages")} fill="none" stroke="#000"></path>
                {:else}
                    <path d={getSymbol("folder")} fill="none" stroke="#000"></path>
                {/if}
            </g>
        </g>
    {/each}
</g>


<style>
    .svg-fa-icon text {
        font-family: FontAwesome;
    }

</style>