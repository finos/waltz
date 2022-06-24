<script>
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import {scaleLinear} from "d3-scale";
    import _ from "lodash";
    import {RenderModes} from "../../aggregate-overlay-diagram-utils";

    export let cellData = {};
    export let maxCount;
    export let height;
    export let renderMode;

    $: r = scaleLinear()
        .domain([0, maxCount])
        .range([0, height / 2 - 2]);

    $: cr = r(references.length)

    let references = [];
    let textHeight = 18;

    $: references = cellData?.aggregatedEntityReferences || [];

    $: svgHeight = renderMode === RenderModes.FOCUSED
        ? Math.max((references.length + additionalLines) * textHeight + height, height)
        : height;

    $: additionalLines = _
        .chain(references)
        .map(r => r.name)
        .map(name => _.floor(_.size(name) / 47))
        .sum()
        .value();

</script>


<svg class="content"
     width="100%"
     {height}
     style="background: white">
    {#if _.isEmpty(references)}
        <text font-size="16"
              dy="26"
              dx="60">
            -
        </text>
    {:else}
        <circle r={cr}
                fill="#a9e4ff"
                stroke="#25b0ff"
                stroke-width="2"
                cx={height * 0.75}
                cy={height / 2}/>

        <!-- Count label-->
        <text font-size="16"
              dy={height / 2 + 6}
              dx={height * 1.5}>
            {_.size(references)}
        </text>
    {/if}
</svg>


{#if renderMode === RenderModes.FOCUSED}
    <!--  List of entities-->
    <table class="table table-condensed table-hover small">
        <tbody>
        {#each _.sortBy(references, r => r.name) as ref}
            <tr>
                <td>
                    <EntityLink {ref}/>
                </td>
                <td>{ref.externalId}</td>
            </tr>
        {/each}
        </tbody>
    </table>
{/if}

<style>
    ul {
        padding: 0.1em 0 0 0;
        margin: 0 0 0 0;
        list-style: none;
        font-size: 12px
    }

    li {
        padding-top: 0.1em;
    }
</style>
