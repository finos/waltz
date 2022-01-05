<script>

    import UserSelectList from "./UserSelectList.svelte";
    import {activeMode, Modes} from "./user-management-store";
    import UserRolesList from "./UserRolesList.svelte";
    import UserCreatePanel from "./UserCreatePanel.svelte";
    import PasswordUpdatePanel from "./PasswordUpdatePanel.svelte";
    import DeleteUserConfirmationPanel from "./DeleteUserConfirmationPanel.svelte";

</script>

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