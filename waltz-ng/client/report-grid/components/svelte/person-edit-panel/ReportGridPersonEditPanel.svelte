<script>
    import {reportGridMember} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";
    import _ from "lodash";
    import AddNewSubscriberPanel from "./AddNewSubscriberPanel.svelte";
    import toasts from "../../../../svelte-stores/toast-store";
    import ReportGridPersonOverview from "./ReportGridPersonOverview.svelte";
    import {gridService} from "../report-grid-service";
    import {displayError} from "../../../../common/error-utils";

    const Modes = {
        VIEW: "VIEW",
        CREATE: "CREATE"
    };

    let activeMode = Modes.VIEW;
    let selectedMember = null;
    let qry = "";

    const {gridMembers} = gridService;

    function editRole(person, role) {
        return gridService
            .updateMember(person.email, role)
            .then(() => {
                toasts.success("Updated user role to:" + role)
                activeMode = Modes.VIEW;
                selectedMember = null;
            })
            .catch(e => displayError("Could not update role.", e))
    }

    function deleteMember(member) {
        return gridService
            .removeMember(member.user.email)
            .then(() => {
                selectedMember = null;
                toasts.success(`Removed ${member.user.displayName} from members list`)
            });
    }

    function selectPerson(p) {
        return gridService
            .updateMember(p.email, reportGridMember.VIEWER.key)
            .then(() => {
                toasts.success(`Successfully saved ${p.name} as a subscriber to this grid`);
                activeMode = Modes.VIEW;
            })
            .catch(e => toasts.error(`Could not save ${p.name} as a subscriber to this grid.` + e.error))
    }

    function selectMember(member) {
        selectedMember = member;
    }

    function cancel() {
        activeMode = Modes.VIEW;
    }

    function addSubscriber() {
        activeMode = Modes.CREATE;
    }

    $: membersList = _.isEmpty(qry)
        ? $gridMembers
        : termSearch($gridMembers, qry, ["user.displayName", "role"]);

</script>

<div class="row">
    <div class="col-sm-5">
        <div class="help-block small">
            Listed below are the owners and viewers of this report grid.
        </div>
        {#if _.size($gridMembers) > 10}
            <SearchInput bind:value={qry}
                         placeholder="Search people"/>
            <br>
        {/if}
        <table class="table table-condensed table-hover small">
            <colgroup>
                <col width="70%">
                <col width="30%">
            </colgroup>
            <thead>
                <tr>
                    <th>User</th>
                    <th>Role</th>
                </tr>
            </thead>
            <tbody>
            {#each membersList as member}
                <tr class="clickable"
                    class:selected={selectedMember?.user.id === member?.user.id}
                    on:click={() => selectMember(member)}>
                    <td class:memberInactive={member.user?.isRemoved}>
                        {member.user?.displayName}
                    </td>
                    <td>
                        {reportGridMember[member.role].name}
                    </td>
                </tr>
            {/each}
            <tr class="clickable"
                on:click={() => addSubscriber()}>
                <td colspan="2">
                    <button class="btn btn-skinny"
                            on:click={() => addSubscriber()}>
                        <Icon name="plus"/>Add a subscriber
                    </button>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
    <div class="col-sm-7">
        {#if activeMode === Modes.VIEW}
            <ReportGridPersonOverview members={$gridMembers}
                                      selectedMember={selectedMember}
                                      onDelete={deleteMember}
                                      onEdit={editRole}/>
        {:else if activeMode === Modes.CREATE}
            <AddNewSubscriberPanel onCancel={cancel}
                                   onSelect={selectPerson}/>
        {/if}
    </div>
</div>


<style>
    .selected{
        background: #f3f9ff;
    }

    .memberInactive {
        text-decoration: line-through;
    }
</style>