<script>
    import {CORE_API} from "../../common/services/core-api-utils";

    export let id;
    export let serviceBroker;
    export let app = {name: "snoozer"};

    let promise;

    async function load() {
        let p = await fetch("http://localhost:8443/api/app/id/" + id);
        return await p.json();
    }

    async function load2() {
        console.log({serviceBroker, id})
        let p = await serviceBroker.loadViewData(
            CORE_API.ApplicationStore.getById,
            [id]);
        return await p.json().data;
    }

    $: promise = load2(id);

</script>

<style>
    .zzz {
        color: lightgrey;
    }
    .yay {
        color: darkgreen;
    }
    .boo {
        color: red;
    }
</style>

{#await promise}
    <h1 class="zzz">Snooze</h1>
{:then v}
    <h1 class="yay">Hello {console.log(v) || v.name}, {v.applicationKind}!</h1>
{:catch err}
    <h1 class="boo">Oh no</h1>
    <pre>{err}</pre>
{/await}


<h3>{app}</h3>