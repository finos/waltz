<script>
    import moment from "moment";

    export let dateTime;
    export let relative = true;
    export let formatStr = "yyyy-MM-DD hh:mm:ss";

    const timeAgo = t => moment.utc(t).fromNow();
    const format = t => moment.utc(t).local().format(formatStr)
</script>

{#if relative}
    <span title={format(dateTime)}>
        {timeAgo(dateTime)}
    </span>
{:else}
    <span title={timeAgo(dateTime)}>
        {format(dateTime)}
    </span>
{/if}
