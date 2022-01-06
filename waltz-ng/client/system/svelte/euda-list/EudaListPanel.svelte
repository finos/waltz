<script>

    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {endUserApplicationStore} from "../../../svelte-stores/end-user-application-store";
    import {termSearch} from "../../../common";
    import _ from "lodash";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import EndUserApplicationInfoPanel from "../../../common/svelte/info-panels/EndUserApplicationInfoPanel.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import SubSection from "../../../common/svelte/SubSection.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import {mkRef} from "../../../common/entity-utils";

    const Modes = {
        VIEW: "VIEW",
        CONFIRM: "CONFIRM"
    }

    let activeMode = Modes.VIEW;

    $: eudaCall = endUserApplicationStore.findAll();
    $: eudas = $eudaCall.data;

    let qry = "";
    let selectedEuda = null;
    let recentlyPromoted = [];
    let comment = null;

    $: eudaList = _.isEmpty(qry)
        ? eudas
        : termSearch(eudas, qry, ["id", "name", "description", "lifecyclePhase", "kind"])

    function promote() {
        const promotePromise = endUserApplicationStore.promoteToApplication(selectedEuda.id, {comment});
        Promise.resolve(promotePromise)
            .then(r => {
                toasts.success("Successfully promoted " + selectedEuda.name + " to an application")
                let promotedEuda = Object.assign({}, selectedEuda, {appId: r.data.id});
                recentlyPromoted = _.concat(recentlyPromoted, promotedEuda)
            })
            .then(r => {
                selectedEuda = null;
                activeMode = Modes.VIEW;
                eudaCall = endUserApplicationStore.findAll(true);
            })
            .catch(e => toasts.error("Could not promote euda:", e.error))
    }

    function selectEuda(euda) {
        activeMode = Modes.VIEW;
        selectedEuda = euda;
    }

</script>


<div class="col-sm-12" style="margin-bottom: 2em">
    End User Developed Applications (EUDAs) in Waltz. Select a EUDA below to promote it to a full application.
</div>
<div>
    <div class="col-sm-8">
        <SearchInput bind:value={qry}/>
        <div class:waltz-scroll-region-350={_.size(eudaList) > 10}>
            <table class="table table-condensed table-striped table-hover small">
                <colgroup>
                    <col width="10%">
                    <col width="60%">
                    <col width="30%">
                </colgroup>
                <thead>
                    <tr>
                        <th>Id</th>
                        <th>Name</th>
                        <th>Kind</th>
                    </tr>
                </thead>
                <tbody>
                {#each eudaList as euda}
                    <tr class="clickable" on:click={() => selectEuda(euda)}>
                        <td>{euda.id}</td>
                        <td>
                            <span class="force-wrap">{euda.name}</span>
                        </td>
                        <td>{euda.applicationKind}</td>
                    </tr>
                {:else }
                    <tr>
                        <td colspan="5">
                            <NoData>
                                There are no eudas to display
                            </NoData>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </div>
    </div>
    <div class="col-sm-4">
        <SubSection>
            <div slot="header">
                <span>Selected Euda</span>
            </div>
            <div slot="content">
                {#if _.isNull(selectedEuda)}
                    <NoData type="info">
                        <Icon name="info-circle"/>
                        Select an End User Application from the list to see more detail or promote it to a full application.
                    </NoData>
                {:else if activeMode === Modes.VIEW}
                    <EndUserApplicationInfoPanel primaryEntityRef={selectedEuda}/>
                    <button class="btn btn-skinny"
                            on:click={() => activeMode = Modes.CONFIRM}>
                        Promote<Icon name="level-up"/>
                    </button>
                {:else if activeMode === Modes.CONFIRM}
                    <h4 class="force-wrap">
                        Are you sure you want to promote:
                        {selectedEuda.name}
                        to a full application in Waltz?
                    </h4>
                    <div>
                        <input class="form-control"
                               id="comment"
                               placeholder="Comment"
                               bind:value={comment}>
                    </div>
                    <div class="help-block small"><Icon name="info-circle"/>
                        Add a comment to the change log entry for this application
                    </div>
                    <br>
                    <span>
                        <button class="btn btn-success"
                                on:click={() => promote()}>
                            Promote
                        </button>
                        <button class="btn btn-skinny"
                                on:click={() => activeMode = Modes.VIEW}>
                            Cancel
                        </button>
                    </span>
                {/if}
            </div>
        </SubSection>
        <br>
        <SubSection>
            <div slot="header">
                Recently Promoted
            </div>
            <div slot="content">
                {#if _.isEmpty(recentlyPromoted)}
                    <NoData>
                        You have no recently promoted applications.
                    </NoData>
                {:else}
                    <ul>
                        {#each recentlyPromoted as promoted }
                            <li>
                                <EntityLink ref={mkRef("APPLICATION", promoted.appId, promoted.name)}/>
                            </li>
                        {/each}
                    </ul>
                {/if}
            </div>
        </SubSection>
    </div>
</div>

<style>

    ul {
        padding-left: 1em;
    }

</style>