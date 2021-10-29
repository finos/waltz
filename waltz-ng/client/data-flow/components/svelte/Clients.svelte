<script>
    import {clientScale,
        clientScrollOffset,
        filteredClients,
        highlightClass,
        selectedClient,
        layoutDirection,
        layoutDirections,
        contextPanelMode,
        Modes
    } from "./flow-decorator-store";
    import {dimensions} from "./flow-decorator-utils"
    import {truncateMiddle} from "../../../common/string-utils";

    import _ from "lodash";

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
        $selectedClient = client;
        $contextPanelMode = Modes.ENTITY;
    }

</script>

<g transform={`translate(0, ${$clientScrollOffset})`}>
    {#each $filteredClients as client}
        <g transform={`translate(0, ${$clientScale(client.id)})`} class={mkClasses(client)}
           on:click|stopPropagation={() => selectClient(client)}>
            <rect stroke="#999"
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
                    ? dimensions.client.width - dimensions.client.iconPadding + dimensions.client.iconPadding / 4
                    : dimensions.client.iconPadding / 4}, ${$clientScale.bandwidth() / 2 + 5})`}>
                {#if _.size(client.physicalFlows) === 0}
                    <text class="svg-fa-icon" font-family="FontAwesome">&#xf29c;</text>
                {:else if _.size(client.physicalFlows) === 1}
                    <text class="svg-fa-icon" font-family="FontAwesome">&#xf016;</text>
                {:else if _.size(client.physicalFlows) === 2}
                    <text class="svg-fa-icon" font-family="FontAwesome">&#xf0c5;</text>
                {:else}
                    <text class="svg-fa-icon" font-family="FontAwesome">&#xf115;</text>
                {/if}
            </g>

<!-- TRANSLATE FOR SWITCH IN DIRECTION!!!! -->

        </g>
    {/each}
</g>


<style>


    .ACTOR rect {
        fill: #f0e9ff;
    }


    .APPLICATION rect {
        fill: #eef8ff;
    }


    .svg-fa-icon text {
        font-family: FontAwesome;
    }

</style>