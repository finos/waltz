<script>

    import UserSelectList from "./UserSelectList.svelte";
    import {activeMode, Modes} from "./user-management-store";
    import UserRolesList from "./UserRolesList.svelte";
    import UserCreatePanel from "./UserCreatePanel.svelte";
    import PasswordUpdatePanel from "./PasswordUpdatePanel.svelte";
    import DeleteUserConfirmationPanel from "./DeleteUserConfirmationPanel.svelte";
    import Icon from "../../common/svelte/Icon.svelte";
    import UserBulkEditor from "./UserBulkEditor.svelte";

    const Tabs = {
        SINGLE: 'single',
        BULK: 'bulk'
    };

    let selectedTab = Tabs.SINGLE;

</script>


<div class="waltz-tabs"
     style="padding-top: 1em">
    <!-- TAB HEADERS -->
    <input type="radio"
           bind:group={selectedTab}
           value="single"
           id="single">
    <label class="wt-label"
           for="single">
        <span><Icon name="pencil-square-o"/> Individual User Admin</span>
    </label>

    <input type="radio"
           bind:group={selectedTab}
           value="bulk"
           id="bulk">
    <label class="wt-label"
           for="bulk">
        <span><Icon name="list"/> Bulk User Admin</span>
    </label>

    <div class="wt-tab wt-active">
        {#if selectedTab === 'single'}
            {#if $activeMode === Modes.LIST}
                <UserSelectList/>
            {:else if $activeMode === Modes.DETAIL}
                <UserRolesList/>
            {:else if $activeMode === Modes.ADD}
                <UserCreatePanel/>
            {:else if $activeMode === Modes.PASSWORD}
                <PasswordUpdatePanel/>
            {:else if $activeMode === Modes.DELETE}
                <DeleteUserConfirmationPanel/>
            {/if}
        {:else if selectedTab === 'bulk'}
            <UserBulkEditor/>
        {/if}
    </div>
</div>
