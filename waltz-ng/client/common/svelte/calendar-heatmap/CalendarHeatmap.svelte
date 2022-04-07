<script>

    import {dimensions, monthNames, prepareMonthData} from "./calendar-heatmap-utils";
    import Month from "./Month.svelte";
    import {scaleSqrt} from "d3-scale";
    import _ from "lodash";

    export let data = [];
    export let onSelectDate = (x) => console.log("selecting date", x);
    export let onSelectWeek = (x) => console.log("selecting week", x);
    export let onSelectMonth = (x) => console.log("selecting month", x);

    $: colorScale = scaleSqrt().domain([0, maxValue?.count]).range(["#e7fae2", "#07ed4a"]);

    let startDate = new Date("2021-08-01");
    let endDate = new Date("2022-10-01");

    $: maxValue = _.maxBy(data, d => d.count);

    $: months = prepareMonthData(data, startDate, endDate);

    function determineRow(idx) {
        return Math.floor(idx / dimensions.monthsPerLine);
    }

    function determineColumn(idx) {
        return idx % dimensions.monthsPerLine + 1;
    }


</script>

<h4>Hello there!</h4>

<svg width="2400" height="800" viewBox="0 0 2400 800">
    <g>
        {#each months as monthData, idx}
            <g transform={`translate(${determineColumn(idx) * dimensions.monthWidth}, ${determineRow(idx) * dimensions.monthWidth})`}>
                <text transform={`translate(${7 * dimensions.dayWidth / 2})`}
                      class="clickable"
                      text-anchor="middle"
                      dx={dimensions.dayWidth}
                      dy="25"
                      fill="#aaa"
                      on:click={() => onSelectMonth(_.map(monthData.days, d => d.date))}>
                    {monthNames[monthData?.startDate.getMonth()]}
                </text>
                <Month monthData={monthData}
                       {colorScale}
                       {onSelectDate}
                       {onSelectWeek}>
                </Month>
            </g>
        {/each}
    </g>
</svg>
