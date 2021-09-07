<script>
    import {filteredClients, clientScale, clientScrollOffset, highlightClass} from "./scroll-store";
    import {dimensions} from "./scroll-utils"
    import {truncateMiddle} from "../../../common/string-utils";


    function onMouseEnter(client) {
        $highlightClass = `client_${client.id}`;
    }

    function onMouseLeave() {
        $highlightClass = null;
    }

</script>

<g transform={`translate(0, ${$clientScrollOffset})`}>
    {#each $filteredClients as client}
        <g transform={`translate(0, ${$clientScale(client.id)})`}>
            <rect fill="#eef8ff"
                  stroke="#999"
                  on:mouseenter={() => onMouseEnter(client)}
                  on:mouseleave={() => onMouseLeave()}
                  rx={dimensions.client.height / 2}
                  width={dimensions.client.width}
                  height={$clientScale.bandwidth() / 2}
                  transform={`translate(0, ${$clientScale.bandwidth() / 4 - 2})`}/>
            <text dx="10"
                  pointer-events="none"
                  dy={$clientScale.bandwidth() / 2 + 4}>
                {truncateMiddle(client.name, 24)}
            </text>
        </g>
    {/each}
</g>