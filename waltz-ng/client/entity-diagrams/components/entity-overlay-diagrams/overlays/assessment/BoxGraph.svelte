<script>
    import _ from "lodash";
    import {mkChunks} from "../../../../../common/list-utils";
    import {determineForegroundColor} from "../../../../../common/colors";

    export let cellData = [];
    export let width;
    export let height;

    $: cellPadding = height / 2 * 0.1;

    let counts = [];

    $: counts = _.orderBy(cellData?.counts, c => c.rating.name);
    $: chunksPerRow = _.floor(width / (height / 2));
    $: rows = mkChunks(counts, chunksPerRow);
    $: rowHeight = height / 2 * 0.9;

</script>

<div>
    <svg viewBox={`0 0 ${width} ${height}`}
         shape-rendering="crispEdges"
         width="100%"
         style="background: white">
        <g transform={`translate(${cellPadding} , ${cellPadding})`}>
            {#each rows as row, idx}
                <g class="box-row"
                   transform={`translate(0, ${idx * (rowHeight + cellPadding)})`}>
                    {#each row as box, idx}
                        <g transform="translate({idx * (rowHeight + cellPadding)})">
                            <rect height={rowHeight}
                                  width={rowHeight}
                                  fill={box.rating.color}/>
                            <text text-anchor="middle"
                                  font-size={rowHeight * 0.6}
                                  textLength={box.count >= 100 ? rowHeight : null}
                                  dominant-baseline="middle"
                                  lengthAdjust="spacingAndGlyphs"
                                  fill={determineForegroundColor(box.rating.color)}
                                  y={(rowHeight + cellPadding) / 2}
                                  x={rowHeight / 2}>
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
</div>

<style>

    svg {
        display: block;
    }

</style>
