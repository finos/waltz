<script>

    import {daysInMonth, dimensions} from "./calendar-heatmap-utils";
    import _ from "lodash";

    export let onSelectWeek = (d) => console.log("selecting week", d);
    export let monthData = [];
    export let hoveredDay = null;

    let hoveredWeek = null;

    let month = monthData.startDate.getMonth() + 1;
    let year = monthData.startDate.getFullYear();
    let day = monthData.startDate.getDay();

    let numberDays = daysInMonth(month, year);
    let totalBlocks = numberDays + day;

    function determineWeeks(totalBlocks) {
        if (totalBlocks <= 28) {
            return [0, 1, 2, 3];
        } else if (totalBlocks <= 35) {
            return [0, 1, 2, 3, 4];
        } else {
            return [0, 1, 2, 3, 4, 5];
        }
    }

    let weeks = determineWeeks(totalBlocks);

    $: weekDayInfo = _
        .chain(monthData.days)
        .map(d => {
            const dayOffset = _.indexOf(monthData.days, d) + day;
            let dy = (Math.floor(dayOffset / 7));
            return {week: dy, day: d}
        })
        .groupBy(d => d.week)
        .mapValues(v => _.map(v, d => d.day.date))
        .value();

    $: highlightedWeek = hoveredDay === null
        ? hoveredWeek
        : Math.floor((day + hoveredDay) / 7)

</script>

{#each weeks as week}
    <g transform={`translate(${dimensions.weekPadding / 2}, ${dimensions.day.width * (week)})`}>
        <rect fill={highlightedWeek === week ? "#eee" : "#fff"}
              class="clickable week"
              on:mouseenter={() => hoveredWeek = week}
              on:mouseleave={() => hoveredWeek = null}
              x={dimensions.weekPadding * -2}
              y="{(dimensions.day.width / 2 * -1)}"
              width={dimensions.day.width * 7 + dimensions.weekPadding}
              height={dimensions.day.width}
              on:click,keydown={() => onSelectWeek(weekDayInfo[week])}>
        </rect>
    </g>
{/each}

