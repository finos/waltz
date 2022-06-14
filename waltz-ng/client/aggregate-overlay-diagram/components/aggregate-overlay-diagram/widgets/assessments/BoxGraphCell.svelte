<script>
    import _ from "lodash";
    import {scaleBand, scaleLinear} from "d3-scale";
    import {mkChunks} from "../../../../../common/list-utils";
    import {determineForegroundColor} from "../../../../../common/colors";
    import RatingIndicatorCell
        from "../../../../../ratings/components/rating-indicator-cell/RatingIndicatorCell.svelte";

    export let cellData = [];

    const rowHeight = 18;

    let counts = [];

    $: counts = _.orderBy(cellData?.counts, c => c.rating.name);
    $: rows = mkChunks(counts, 6);
    $: height = Math.max(rows.length * rowHeight, rowHeight);
</script>


<svg width="100%"
     height={height + 10}
     style="background: white">
    <g transform="translate(5, 5)">
        {#each rows as row, idx}
            <g class="box-row"
               transform={`translate(0, ${idx * rowHeight})`}>
                {#each row as box, idx}
                    <g transform="translate({idx * (rowHeight + 4)})">
                        <rect height={rowHeight}
                              stroke="black"
                              width={rowHeight}
                              fill={box.rating.color}/>
                        <text dy={rowHeight - 4}
                              text-anchor="middle"
                              fill={determineForegroundColor(box.rating.color)}
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
<br>
<br>
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