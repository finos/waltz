<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";

    export let reportGrid;

</script>

<PageHeader icon="cloud"
            name={reportGrid?.name}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <span>{reportGrid?.name}</span>
            </li>
        </ol>
    </div>
</PageHeader>
