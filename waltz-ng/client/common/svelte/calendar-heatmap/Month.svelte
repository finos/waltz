<script>

    import Day from "./Day.svelte";
    import {dimensions} from "./calendar-heatmap-utils";
    import Weeks from "./Weeks.svelte";

    export let monthData = null;
    export let dayFillColorScale;
    export let onSelectDate = (x) => console.log("selecting date", x);
    export let onSelectWeek = (x) => console.log("selecting week", x);

    let hoveredDay;

    $: offset = monthData?.startDate.getDay();
    $: month = monthData?.startDate.getMonth();

    function mkTranslate(i) {

        const dayOffset = i + offset;

        let dx = dayOffset % 7 * dimensions.day.width;
        let dy = (Math.floor(dayOffset / 7)) * dimensions.day.width;

        return `translate(${dx}, ${dy})`
    }

</script>

<g transform="translate(15, 40)">
    <Weeks {onSelectWeek}
           {monthData}
           {hoveredDay}>
    </Weeks>
    {#each monthData.days as day, i}
        <g on:mouseenter={() => hoveredDay = i}
           on:mouseleave={() => hoveredDay = null}
           transform={mkTranslate(i)}>
            <Day data={day}
                 color={dayFillColorScale(day.value)}
                 stroke={hoveredDay === i ? "black" : "#bbb"}
                 onSelect={onSelectDate}/>
        </g>
    {/each}
</g>
