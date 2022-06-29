<script>
    import _ from "lodash";
    import {scaleBand, scaleLinear} from "d3-scale";
    import {mkChunks} from "../../../../../common/list-utils";
    import {determineForegroundColor} from "../../../../../common/colors";
    import RatingIndicatorCell
        from "../../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";
    import {RenderModes} from "../../aggregate-overlay-diagram-utils";

    export let cellData = [];
    export let renderMode;

    const rowHeight = 18;
    const cellPadding = 4;

    let counts = [];

    $: counts = _.orderBy(cellData?.counts, c => c.rating.name);
    $: rows = mkChunks(counts, 6);
    $: height = Math.max(rows.length * (rowHeight + cellPadding), rowHeight) + cellPadding * 2;
</script>

<div>
    <svg width="100%"
         {height}
         shape-rendering="crispEdges"
         style="background: white">
        <g transform={`translate(${cellPadding} , ${cellPadding})`}>
            {#each rows as row, idx}
                <g class="box-row"
                   transform={`translate(0, ${idx * (rowHeight + cellPadding)})`}>
                    {#each row as box, idx}
                        <g transform="translate({idx * (rowHeight + cellPadding)})">
                            <rect height={rowHeight}
                                  stroke="black"
                                  width={rowHeight}
                                  fill={box.rating.color}/>
                            <text text-anchor="middle"
                                  font-size={rowHeight - 2}
                                  fill={determineForegroundColor(box.rating.color)}
                                  y={rowHeight - cellPadding / 2}
                                  x={rowHeight/2}>
                                {box.count}
                            </text>
                            <title>
                                {box.rating.name}
                            </title>
                        </g>
                    {/each}
                </g>
            {/each}
        </g>
    </svg>

    {#if renderMode === RenderModes.FOCUSED}
        <table class="small table table-condensed">
            {#each counts as row}
                <tr>
                    <td>
                        <RatingIndicatorCell {...row.rating}/>
                        ({row.count})
                    </td>
                </tr>
            {/each}
        </table>
    {/if}
</div>
