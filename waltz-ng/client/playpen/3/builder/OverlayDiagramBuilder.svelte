<script>

    import EntityGroupBox from "./EntityGroupBox.svelte";
    import {scaleLinear} from "d3-scale";
    import {layout} from "./overlay-diagram-builder-utils";

    export let config;

    const dimensions = {
        w: 1000,
        padding: 10,
        labelWidth: 160,
        group: {
            padding: 10
        },
        cell: {
            padding: 10,
            height: 120,
            labelHeight: 40,
            statsHeight: 50
        }
    };

    const data = layout(
        config,
        dimensions);

    const groupColorScale = scaleLinear()
        .domain([0, data.length - 1])
        .range(["#DCEFEB", "#67B9A9"]);


</script>

<svg width="100%"
     height={`${dimensions.height}px`}
     viewBox={`0 0 1000 ${dimensions.height}`}
     style="border: 1px solid #dcefeb">

    {#each data as block, idx}
        <g transform={`translate(10, ${block.layoutData.dy})`}>
            <EntityGroupBox height={ block.layoutData.height }
                            {dimensions}
                            color={groupColorScale(idx)}
                            group={block}/>
        </g>
    {/each}
</svg>