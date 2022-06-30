<script>
    import EntityLink from "../../../../../common/svelte/EntityLink.svelte";
    import {scaleSqrt} from "d3-scale";
    import _ from "lodash";
    import {RenderModes} from "../../aggregate-overlay-diagram-utils";

    export let cellData = {};
    export let maxCount;
    export let height;
    export let width;
    export let renderMode;

    $: r = scaleSqrt()
        .domain([0, maxCount])
        .range([0, height / 2 - 2]);

    $: cr = r(references.length)

    let references = [];
    let textHeight = 18;

    $: references = cellData?.aggregatedEntityReferences || [];

</script>

<div>
    <svg class="content"
         viewBox="0 0 {width} {height}"
         style="background: none">
        {#if !_.isEmpty(references)}
            <circle r={cr}
                    fill="#a9e4ff"
                    stroke="#25b0ff"
                    stroke-width="0.5"
                    cx={width * 0.2}
                    cy={height / 2}/>
        {/if}
        <text font-size={height/2}
              dy={height * 0.67}
              dx={width / 2}
              text-anchor="middle">
            {_.isEmpty(references)
                ? "-"
                : _.size(references)}
        </text>
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

</div>