<script>

    import {involvementKindStore} from "../../../svelte-stores/involvement-kind-store";
    import _ from "lodash";
    import InvolvementBreakdown from "./InvolvementBreakdown.svelte";
    import EntityLink from "../../../common/svelte/EntityLink.svelte";
    import toasts from "../../../svelte-stores/toast-store";
    import {displayError} from "../../../common/error-utils";
    import pageInfo from "../../../svelte-stores/page-navigation-store";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {termSearch} from "../../../common";
    import DropdownPicker
        from "../../../report-grid/components/svelte/column-definition-edit-panel/DropdownPicker.svelte";
    import {entity} from "../../../common/services/enums/entity";

    const Modes = {
        ADD: "ADD",
        VIEW: "VIEW"
    }

    let activeMode = Modes.VIEW;

    let working = {
        name: null,
        description: null,
        subjectKind: null,
        externalId: null,
        permittedRole: null
    }

    let searchStr = "";

    let entityList = [
        entity.ACTOR,
        entity.APP_GROUP,
        entity.APPLICATION,
        entity.CHANGE_INITIATIVE,
        entity.DATA_TYPE,
        entity.END_USER_APPLICATION,
        entity.MEASURABLE,
        entity.MEASURABLE_CATEGORY,
        entity.ORG_UNIT,
        entity.PHYSICAL_FLOW,
        entity.ROLE
    ];

    $: involvementStatCall = involvementKindStore.findUsageStats();
    $: usageStats = _.orderBy($involvementStatCall?.data, d => _.toUpper(d.involvementKind.name));

    $: displayedStats = _.isEmpty(searchStr)
        ? usageStats
        : termSearch(usageStats, searchStr, ["involvementKind.name", "involvementKind.externalId"]);

    function createNewInvolvementKind() {
        involvementKindStore.create(working)
            .then(r => {
                toasts.success("Successfully created involvement kind");
                involvementKindStore.findUsageStats(true);
                $pageInfo = {
                    state: "main.involvement-kind.view",
                    params: {
                        id: r.data
                    }
                }
                cancel();
            })
            .catch(e => displayError("Could not create involvement kind", e));
    }

    function cancel() {
        activeMode = Modes.VIEW;
        working = {
            name: null,
            description: null,
        }
    }


</script>


{#if _.size(usageStats) > 20}
    <SearchInput bind:value={searchStr}
                 placeholder="Search for an involvement kind..."/>
{/if}

<div class:waltz-scroll-region-400={_.size(usageStats) > 20}>
    <table class="table table-condensed">
        <thead>
        <tr>
            <th>Involvement Kind</th>
            <th>External ID</th>
            <th>Transitive</th>
            <th>Usage Stats By Entity (Active / Removed People)</th>
        </tr>
        </thead>
        <tbody>
        {#each displayedStats as statInfo}
            <tr>
                <td>
                    <EntityLink ref={statInfo.involvementKind}/>
                </td>
                <td>
                    {statInfo.involvementKind.externalId || "-"}
                </td>
                <td>
                    {statInfo.involvementKind.transitive}
                </td>
                <td>
                    <InvolvementBreakdown breakdownStats={statInfo.breakdown}/>
                </td>
            </tr>
        {/each}
        </tbody>
    </table>
</div>

<div style="padding-top: 2em">
    {#if activeMode === Modes.VIEW}
        <button class="btn btn-skinny"
                on:click={() => activeMode = Modes.ADD}>
            <Icon name="plus"/>
            Add new Involvement Kind
        </button>

    {:else if activeMode === Modes.ADD}

        <hr>

        <h4>Create new involvement kind:</h4>

        <form autocomplete="off"
              on:submit|preventDefault={createNewInvolvementKind}>

            <input class="form-control"
                   id="name"
                   maxlength="255"
                   required="required"
                   placeholder="Name"
                   bind:value={working.name}/>
            <div class="help-block">
                Name of the Involvement Kind
            </div>

            <textarea class="form-control"
                      id="description"
                      placeholder="Description"
                      bind:value={working.description}></textarea>
            <div class="help-block">
                Description of this Involvement Kind
            </div>

            <DropdownPicker items={entityList}
                            onSelect={d => working.subjectKind = d.key}
                            defaultMessage="Select an entity kind for these involvements"
                            selectedItem={_.find(entityList, d => d.key === working.subjectKind)}/>
            <div class="help-block">
                Entity kind these involvements are associated to
            </div>

            <button on:click|preventDefault={createNewInvolvementKind}
                    class="btn btn-success"
                    disabled={working.name === null || working.description == null || working.subjectKind == null}>
                Save
            </button>

            <button class="btn skinny"
                    on:click|preventDefault={cancel}>
                Cancel
            </button>
        </form>
    {/if}
</div>
