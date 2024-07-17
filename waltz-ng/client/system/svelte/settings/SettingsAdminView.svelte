
<script>

    import {settingsStore} from "../../../svelte-stores/settings-store";
    import NoData from "../../../common/svelte/NoData.svelte";
    import Icon from "../../../common/svelte/Icon.svelte";
    import {userStore} from "../../../svelte-stores/user-store";
    import toasts from "../../../svelte-stores/toast-store";
    import _ from "lodash";
    import SearchInput from "../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../common";
    import ViewLink from "../../../common/svelte/ViewLink.svelte";
    import PageHeader from "../../../common/svelte/PageHeader.svelte";
    import Section from "../../../common/svelte/Section.svelte";
    import CreateSettingPanel from "./CreateSettingPanel.svelte";
    import {displayError} from "../../../common/error-utils";

    $: settingsCall = settingsStore.loadAll();
    $: settings = $settingsCall.data;

    $: userCall = userStore.load();
    $: user = $userCall.data;

    let editing = false;
    let workingSetting = null;
    let qry = "";

    $: displayedSettings  = _.isEmpty(qry)
        ? settings
        : termSearch(settings, qry, ["name", "value", "description"]);

    $: canEdit = _.includes(user?.roles, 'ADMIN');

    function editSetting(setting) {
        editing = true;
        workingSetting = Object.assign({}, setting);
    }

    function updateSetting() {
        const cmd = {
            name: workingSetting.name,
            value: workingSetting.value,
            description: workingSetting.description
        };

        let updatePromise = settingsStore.update(cmd);

        Promise.resolve(updatePromise)
            .then(() => {
                toasts.success("Successfully updated setting");
                editing = false;
                workingSetting = null;
                settingsCall = settingsStore.loadAll(true);
            })
            .catch(e => toasts.error("Failed to update setting: " + e.error));
    }

    function determineTitle(editing, canEdit) {
        if (editing) {
            return "Already editing, click save to continue";
        } else if (!canEdit) {
            return "You do not have permission to edit this setting";
        } else {
            return "Click to edit this setting"
        }
    }

    function cancel() {
        editing = false;
        workingSetting = null;
    }

    function saveSetting(evt) {
        settingsStore
            .create(evt.detail)
            .then(() => {
                toasts.success("Successfully created setting");
                settingsCall = settingsStore.loadAll(true);
            })
            .catch(e => displayError("Failed to create setting", e))
    }

</script>

<PageHeader icon="cogs"
            name="Settings">
    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li><ViewLink state="main">Home</ViewLink></li>
            <li><ViewLink state="main.system.list">System Admin</ViewLink></li>
            <li>Settings</li>
        </ol>
    </div>
</PageHeader>


<div class="waltz-page-summary waltz-page-summary-attach">

    <p class="waltz-paragraph">
        Settings are used to configure and tune waltz.  The table below shows the currently configured settings.
    </p>

    {#if _.size(settings) > 10}
        <SearchInput bind:value={qry}
                     placeholder="Search settings"/>
        <br>
    {/if}

    <div class:waltz-scroll-region-500={_.size(settings) > 12}>
        <table class="table force-wrap table-condensed table-striped small">
            <colgroup>
                <col width="35%">
                <col width="30%">
                <col width="30%">
                <col width="5%">
            </colgroup>
            <thead>
            <tr>
                <th>Name</th>
                <th>Value</th>
                <th>Description</th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            {#each _.orderBy(displayedSettings, d => d.name) as setting}
                <tr class:editing={editing && workingSetting?.name === setting?.name}>
                    <td class="text-muted">
                        {setting.name}
                    </td>
                    <td>
                        {#if editing && workingSetting?.name === setting?.name}
                            <input class="form-control"
                                   id="value"
                                   maxlength="4000"
                                   placeholder="Value for this setting"
                                   bind:value={workingSetting.value}/>
                            <div style="padding-top: 1em">
                                <button class="btn btn-success btn-xs"
                                        disabled={workingSetting?.value === setting?.value}
                                        on:click={() => updateSetting()}>
                                    <Icon name="floppy-o"/> Save
                                </button>
                                <button class="btn btn-skinny btn-xs"
                                        on:click={() => cancel()}>
                                    Cancel
                                </button>
                            </div>
                        {:else}
                            {setting.value}
                        {/if}
                    </td>
                    <td class="text-muted">
                        {#if editing && workingSetting?.name === setting?.name}
                            <input class="form-control"
                                   id="description"
                                   maxlength="4000"
                                   placeholder="Description for this setting"
                                   bind:value={workingSetting.description}/>
                            <div style="padding-top: 1em">
                                <button class="btn btn-success btn-xs"
                                        disabled={workingSetting?.description === setting?.description}
                                        on:click={() => updateSetting()}>
                                    <Icon name="floppy-o"/> Save
                                </button>
                                <button class="btn btn-skinny btn-xs"
                                        on:click={() => cancel()}>
                                    Cancel
                                </button>
                            </div>
                        {:else}
                            {setting.description || ""}
                        {/if}
                    </td>
                    <td>
                        {#if setting.restricted}
                            <div class="text-muted"
                                 title="This setting is restricted and cannot be edited">
                                <Icon name="lock"/>
                            </div>
                        {:else }
                            <button class="btn btn-skinny"
                                    disabled={!canEdit || editing}
                                    title={determineTitle(editing, canEdit)}
                                    on:click={() => editSetting(setting)}>
                                <Icon name="pencil"/>
                            </button>
                        {/if}
                    </td>
                </tr>
            {:else}
                <tr>
                    <td colspan="4">
                        <NoData>
                            There are no settings.
                        </NoData>
                    </td>
                </tr>
            {/each}
            </tbody>
        </table>
    </div>
</div>
<br>
<Section name="Create a Setting"
         icon="pencil-square-o">
    <CreateSettingPanel on:save={saveSetting}/>
</Section>

<style>
    .editing {
        background-color: rgba(226, 237, 255, 0.73);
    }
</style>