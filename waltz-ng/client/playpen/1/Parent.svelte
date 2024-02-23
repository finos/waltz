<script>

    import {onDestroy, onMount} from "svelte";
    import Child from "./Child.svelte";

    onMount(() => console.log("parent onMount"));
    onDestroy(() => console.log("parent onDestroy"));

    let show = true;

    export let counter = 0;

    $: d = counter * counter;

</script>

<h1>Hello Parent: counter is {counter}, sq: {d}</h1>


<button on:click={() => show = !show}>Toggle Child</button>

{#if show}
    <Child/>
{/if}

