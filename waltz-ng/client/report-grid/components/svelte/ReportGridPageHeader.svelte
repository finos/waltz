<script>

    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";

    export let reportGrid;
    export let parentEntityRef;

</script>

<PageHeader icon="cloud"
            name={reportGrid?.name}>
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <span>
                    <EntityLink ref={parentEntityRef} showIcon="false"/>
                </span>
            </li>
            <li>
                <span>{reportGrid?.name}</span>
            </li>
        </ol>
    </div>
</PageHeader>
