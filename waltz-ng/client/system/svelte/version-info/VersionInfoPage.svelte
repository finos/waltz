<script>

    import {onMount} from "svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    const endpoint = "api/system-version";

    let packageVersion = "";
    let databaseVersions = [];

    onMount(async function () {
        const response = await fetch(endpoint);
        const data = await response.json();
        packageVersion = data.packageVersion;
        databaseVersions = data.databaseVersions;
    });

</script>



<PageHeader icon="bullseye"
            name="System Version Information">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>System Version Info</li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <div class="help-block">
                This page shows version information for this installation of Waltz.
            </div>
            <h3>
                <Icon name="code"/>
                Package Version
            </h3>
            <div class="help-block">
                This version information is read from the jar manifest.
                If it shows nulls you are most likely running an older (pre 1.48) build or direclty from a dev env.
            </div>
            <div class="version-info">{packageVersion}</div>

            <h3>
                <Icon name="database"/>
                Database Versions
            </h3>
            <div class="help-block">
                This information is taken from the liquibase changelog.
                It reads the table <code>databasechangelog</code> and shows the unique filenames that have been applied.
            </div>
            {#each databaseVersions as dbv}
                <div class="version-info">
                    {dbv}
                </div>
            {/each}
        </div>
    </div>
</div>

<style>
    .version-info {
        font-family: monospace;
    }
</style>