<script>
    import {CORE_API} from "../../common/services/core-api-utils";

    export let serviceBroker;
    export let primaryEntityRef;

    let bookmarks = [];

    const load = async (ref) => {
        console.log("Loading", { ref });
        const r = await serviceBroker
            .loadViewData(
                CORE_API.BookmarkStore.findByParent,
                [ref]);
        bookmarks = await r.data;
    };

    $: load(primaryEntityRef);

</script>


<style>
	h1 {
		color: red;
	}
</style>


<h1>Hello2!</h1>

<p>
    {console.log(bookmarks) || bookmarks.length}
</p>


<ul>
    {#each bookmarks as bookmark, idx}
        <li>{idx} / {bookmark.kind} / {bookmark.title}</li>
    {/each}
</ul>