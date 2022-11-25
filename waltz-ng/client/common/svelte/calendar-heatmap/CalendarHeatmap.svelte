<script>

    import {dimensions, monthNames, prepareMonthData} from "./calendar-heatmap-utils";
    import Month from "./Month.svelte";
    import {scaleSqrt} from "d3-scale";
    import _ from "lodash";
    import CalendarHeatmapControlPanel from "./CalendarHeatmapControlPanel.svelte";
    import {timeFormat} from "d3-time-format";

    const format = timeFormat("%Y-%m");    export let data = [];

    export let onSelectDate = (x) => console.log("selecting date", x);
    export let onSelectWeek = (x) => console.log("selecting week", x);
    export let onSelectMonth = (x) => console.log("selecting month", x);

    $: dayFillColorScale = scaleSqrt().domain([0, maxValue?.count]).range(["#e7fae2", "#07ed4a"]);
    $: monthLabelColorScale = scaleSqrt().domain([0, months.length]).range(["#b7b7b7", "#666"]);

    const today = new Date();

    let startDate = new Date(today.getFullYear() - 1, today.getMonth() + 1, 1);
    let endDate = today;
    let hoveredMonth;

    $: maxValue = _.maxBy(data, d => d.count);
    $: months = prepareMonthData(data, startDate, endDate);
    $: diagramRows = Math.ceil(months.length / dimensions.monthsPerLine);

    function determineRow(idx) {
        return Math.floor(idx / dimensions.monthsPerLine);
    }

    function determineColumn(idx) {
        return idx % dimensions.monthsPerLine;
    }

    function setStartDate(date) {
        startDate = new Date(date);
    }

    function setEndDate(date) {
        endDate = new Date(date);
    }


</script>

<svg width={dimensions.diagram.width}
     height={dimensions.month.height * diagramRows}
     viewBox={`0 0 ${dimensions.diagram.width * (1 + 1/dimensions.monthsPerLine)} ${dimensions.month.height * diagramRows}`}>
    <g>
        {#each months as monthData, idx}
            <g transform={`translate(${determineColumn(idx) * dimensions.month.width}, ${determineRow(idx) * dimensions.month.height})`}>

                <g on:click={() => onSelectMonth(_.map(monthData.days, d => d.date))}
                   on:keydown={() => onSelectMonth(_.map(monthData.days, d => d.date))}>
                    <title>{format(monthData?.startDate)}</title>

                    <rect width={dimensions.month.width}
                          height={30}
                          class="clickable"
                          on:mouseenter={() => hoveredMonth = idx}
                          on:mouseleave={() => hoveredMonth = null}
                          fill={hoveredMonth === idx ? "#eee" : "#fff"}>
                    </rect>
                </g>
                <text transform={`translate(${7 * dimensions.day.width / 2})`}
                      text-anchor="middle"
                      dx={dimensions.day.width /4}
                      dy="20"
                      fill={monthLabelColorScale(idx)}
                      pointer-events="none">
                    {monthNames[monthData?.startDate.getMonth()]}
                </text>
                <Month monthData={monthData}
                       {dayFillColorScale}
                       {onSelectDate}
                       {onSelectWeek}>
                </Month>
                <rect stroke={hoveredMonth === idx ? "#ddd" : "none"}
                      fill="none"
                      x="1"
                      y="1"
                      rx="2"
                      ry="2"
                      width={dimensions.month.width - 2}
                      height={dimensions.month.height - 2}>
                </rect>
            </g>
        {/each}
    </g>
</svg>

<CalendarHeatmapControlPanel {startDate} {endDate} {setStartDate} {setEndDate}/>
