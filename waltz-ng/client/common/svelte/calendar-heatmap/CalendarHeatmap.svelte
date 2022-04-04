<script>

    import {chunkMonths} from "./calendar-heatmap-utils";
    import Day from "./Day.svelte";
    import Month from "./Month.svelte";
    import {scaleLinear} from "d3";
    import _ from "lodash";

    export let data = [];
    export let onSelectDate = (x) => console.log("selecting date", x);


    $: console.log({data, onSelectDate});
    let today = new Date();
    chunkMonths(data, today, today)


    function mkVal() {
        if (Math.random() > 0.7) {
            return 0;
        } else {
            return Math.random() * 10;
        }
    }

    const colorScale = scaleLinear().domain([0, 10]).range(["yellow", "red"]);

    function mkDays(numDays) {
        return _.map(_.range(numDays), x => ({date: new Date(`2022-04-${x + 1}`), value: mkVal()}));
    }


    function daysInMonth(month, year) {
        return new Date(year, month, 0).getDate();
    }

    const months = _.map(_.range(12), d => ({
        startDate: new Date(`2022-${d + 1}-01`),
        days: mkDays(daysInMonth(d + 1, 2022))
    }));

    $: console.log(_.map(months, d => d.days.length))
</script>

<h4>Hello there!</h4>

<svg width="1000" height="100" viewBox="0 0 1800 200">
    <g>
        {#each months as monthData, idx}
            <g transform={`translate(${idx * 150}, 0)`}>
                <Month monthData={monthData} {colorScale}>
                </Month>
            </g>
        {/each}
    </g>
</svg>
