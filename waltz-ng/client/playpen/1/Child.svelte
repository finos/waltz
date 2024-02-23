<script>
    import {onDestroy, onMount} from "svelte";

    onMount(() => console.log("child onMount"));
    onDestroy(() => console.log("child onDestroy"));

</script>

<h1>
    Hello child
</h1>