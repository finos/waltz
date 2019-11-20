<script>
    import Comp2 from "./Comp2.svelte";
    import Dance from "./Dance.svelte";
    import {onMount} from "svelte";
    import {CORE_API} from "../../common/services/core-api-utils";

    export let name;
    export let serviceBroker;

    let counter = 1;
    let dbl = 0;
    let show = false;
    let app;

    function inc() {
        counter ++;
    }

    const load = async (id) => {
        const foo = await serviceBroker.loadViewData(CORE_API.ApplicationStore.getById, [id]);
        app = foo.data;
        console.log(app)
    }

    $: {
        dbl = counter * 2;
        console.log({dbl, serviceBroker});
        show = counter > 5;
        load(counter);
    }

</script>

<style>
    h1 {
        color: purple;
    }
</style>

<h1>Hello {name}!</h1>

<button on:click={inc}>Click me {counter}, {dbl}</button>
<Comp2 serviceBroker={serviceBroker}
       {app}
       name={"dumbo:" + dbl}></Comp2>

{#if show}

{serviceBroker}
{/if}


<hr>

<Dance id={counter}/>