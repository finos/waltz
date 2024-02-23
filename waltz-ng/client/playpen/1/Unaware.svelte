<script>
    import {onDestroy, onMount} from "svelte";

    export let counter = 0;

    onMount(() => console.log("unaware onMount"));
    onDestroy(() => console.log("unaware onDestroy"));
</script>

<h1>Hello Unaware: counter is {counter}</h1>


