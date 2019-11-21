<script>
    import {CORE_API} from "../../common/services/core-api-utils";

    function sleep(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    export let serviceBroker;
    export let primaryEntityRef;

    const load = async (ref) => {
        console.log("Loading", { ref });
        await sleep(500);
        const r = await serviceBroker
            .loadViewData(
                CORE_API.BookmarkStore.findByParent,
                [ref]);


        return r.data;
    };

    $: promise = load(primaryEntityRef);

</script>


<style>
	h1 {
		color: red;
	}
</style>


<h1>Hello2!</h1>

{#await promise}
    Loading
{:then bookmarks}
    <ul>
        {#each bookmarks as bookmark, idx}
            <li>{idx} / {bookmark.kind} / {bookmark.title}</li>
        {/each}
    </ul>
{/await}
