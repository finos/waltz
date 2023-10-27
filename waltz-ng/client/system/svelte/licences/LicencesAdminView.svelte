<script>
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";
    import _ from "lodash";
    import {licenceStore} from "../../../svelte-stores/licence-store";
    import LastEdited from "../../../common/svelte/LastEdited.svelte";
    import Section from "../../../common/svelte/Section.svelte";
    import EditLicencePanel from "./EditLicencePanel.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {displayError} from "../../../common/error-utils";
    import toasts from "../../../svelte-stores/toast-store";
    import RemoveLicencePanel from "./RemoveLicencePanel.svelte";
    import NoData from "../../../common/svelte/NoData.svelte";


    const Modes = {
        VIEW: "VIEW",
        EDIT: "EDIT",
        REMOVE: "REMOVE"
    }

    let licencesCall = licenceStore.findAll();
    let qry;
    let activeMode = Modes.VIEW;
    let selectedLicence;

    function saveLicence(evt) {
        licenceStore.save(evt.detail)
            .then(() => {
                licencesCall = licenceStore.findAll(true);
                toasts.success("Successfully saved licence");
                activeMode = Modes.VIEW;
            })
            .catch(e => displayError("Could not save licence", e));
    }

    function edit(licence) {
        selectedLicence = licence;
        activeMode = Modes.EDIT;
    }

    function remove(licence) {
        selectedLicence = licence;
        activeMode = Modes.REMOVE;
    }

    function cancel() {
        selectedLicence = null;
        activeMode = Modes.VIEW;
    }

    function removeLicence(evt) {
        const licence = evt.detail;
        licenceStore.remove(licence.id)
            .then(() => {
                licencesCall = licenceStore.findAll(true);
                toasts.success("Successfully removed licence");
                activeMode = Modes.VIEW;
            })
                .catch(e => displayError("Could not remove licence", e));
    }

    $: licences = $licencesCall?.data;

    $: visibleLicences = _.isEmpty(qry)
        ? licences
        : termSearch(licences, qry, ["name", "externalId", "provenance"]);

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

    {#if activeMode === Modes.VIEW}
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
                    <th>Actions</th>
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
                            <td>
                                <span>
                                    <button class="btn btn-skinny"
                                            on:click={() => edit(licence)}>
                                        <Icon name="pencil"/> Edit
                                    </button>
                                    |
                                    <button class="btn btn-skinny"
                                            on:click={() => remove(licence)}>
                                        <Icon name="trash"/> Remove
                                    </button>
                                </span>
                            </td>
                        </tr>
                    {:else}
                        <tr>
                            <td colspan="5">
                                <NoData>There are no licences, use the section below to add one</NoData>
                            </td>
                        </tr>
                    {/each}
                </tbody>
            </table>
        </div>
    {:else if activeMode === Modes.EDIT}
        <EditLicencePanel on:save={saveLicence}
                          on:cancel={cancel}
                          licence={selectedLicence}/>
    {:else if activeMode === Modes.REMOVE}
        <RemoveLicencePanel on:remove={removeLicence}
                            on:cancel={cancel}
                            licence={selectedLicence}/>
    {/if}
</div>

{#if activeMode === Modes.VIEW}
    <br>

    <Section name="Create a licence"
             icon="pencil-square-o">
        <EditLicencePanel on:save={saveLicence}
                          showCancel={false}/>
    </Section>
{/if}
