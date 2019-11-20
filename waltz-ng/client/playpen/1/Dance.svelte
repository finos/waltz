<script>
    import {CORE_API} from "../../common/services/core-api-utils";

    export let id;
    export let serviceBroker;

    let promise;

    async function loadViaFetch() {
        let p = await fetch("http://localhost:8443/api/app/id/" + id);
        return await p.json();
    }

    async function loadViaSB() {
        console.log({serviceBroker, id});
        let p = await serviceBroker.loadViewData(
            CORE_API.ApplicationStore.getById,
            [id]);
        return await p.data;
    }

    $: promise = loadViaSB(id);

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
