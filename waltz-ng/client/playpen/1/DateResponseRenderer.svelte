<script>

    import {timeFormat} from "d3-time-format";

    export let response;

    const dateFormat = timeFormat("%Y-%m-%d");

</script>

<span>{dateFormat(new Date(response?.dateResponse))}</span>