<script>

    import Day from "./Day.svelte";

    export let monthData = null;
    export let colorScale;

    $: offset = monthData?.startDate.getDay();

    $: month = monthData.startDate.getMonth();

    $: console.log({monthData, offset})

    function mkTranslate(i) {

        const dayOffset = i + offset;

        let dx = dayOffset % 7 * 20;
        let dy = (Math.floor(dayOffset / 7)) * 20;

        console.log({i, dayOffset, dx, dy})

        return `translate(${dx}, ${dy})`
    }

</script>


<g transform="translate(50, 50)">
    {#each monthData.days as day, i}
        <g transform={mkTranslate(i)}>
            <Day data={day} color={colorScale(day.value)}/>
        </g>
    {/each}
</g>
