<script>

    import Day from "./Day.svelte";
    import {dimensions} from "./calendar-heatmap-utils";
    import Weeks from "./Weeks.svelte";

    export let monthData = null;
    export let colorScale;
    export let onSelectDate = (x) => console.log("selecting date", x);
    export let onSelectWeek = (x) => console.log("selecting week", x);

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
           {monthData}>
    </Weeks>
    {#each monthData.days as day, i}
        <g transform={mkTranslate(i)}>
            <Day data={day}
                 color={colorScale(day.value)}
                 stroke="#bbb"
                 onSelect={onSelectDate}/>
        </g>
    {/each}
</g>
