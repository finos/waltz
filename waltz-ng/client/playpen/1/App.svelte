<script>
    import Comp2 from "./Comp2.svelte";
    import Dance from "./Dance.svelte";
    import {onMount} from "svelte";
    import {CORE_API} from "../../common/services/core-api-utils";

    export let name;
    export let serviceBroker;

    let counter = 1;
    let dbl; // num
    let show; // bool
    let app; // obj

    function inc() {
        counter ++;
    }

    $: {
        dbl = counter * 2;
        show = counter > 5;
    }

</script>

<style>
    h1 {
        color: purple;
    }
</style>

<h1>Hello {name}!</h1>

<button on:click={inc}>Click me {counter}, {dbl}</button>

{#if show}
<Comp2 name={"dumbo:" + dbl}/>
{/if}



<hr>

<Dance serviceBroker={serviceBroker}
       id={counter}>
</Dance>