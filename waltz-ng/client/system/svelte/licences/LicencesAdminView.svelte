<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";
    import _ from "lodash";
    import {licenceStore} from "../../../svelte-stores/licence-store";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";

    let licencesCall = licenceStore.findAll();
    let qry;

    $: licences = $licencesCall?.data;

    $: visibleLicences = _.isEmpty(qry)
        ? licences
        : termSearch(licences, qry, ["name", "externalId", "provenance"]);

    $: console.log({licences});
</script>


<PageHeader icon="id-card-o"
            name="Licences">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Licences</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="row">
        <div class="col-md-12">
            <p></p>
        </div>
    </div>


    <SearchInput bind:value={qry}
                 placeholder="Search licences"/>
    <div class:waltz-scroll-region-350={_.size(licences) > 10}>
        <table class="table table-condensed">
            <thead>
            <tr>
                <th>Name</th>
                <th>External ID</th>
                <th>Last Updated</th>
                <th>Provenance</th>
            </tr>
            </thead>
            <tbody>
                {#each visibleLicences as licence}
                    <tr>
                        <td>{licence.name}</td>
                        <td>{licence.externalId}</td>
                        <td>
                            <LastEdited entity={licence}/>
                        </td>
                        <td>{licence.provenance}</td>
                    </tr>
                {/each}
            </tbody>
        </table>

    </div>


</div>
