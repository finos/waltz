<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";

</script>

<PageHeader icon="cubes"
            name="Applications"
            small="All">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <span>Application</span>
            </li>
        </ol>
    </div>
</PageHeader>
