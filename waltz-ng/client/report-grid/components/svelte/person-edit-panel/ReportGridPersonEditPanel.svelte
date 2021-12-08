<script>

    import {selectedGrid} from "../report-grid-store";
    import {reportGridMemberStore} from "../../../../svelte-stores/report-grid-member-store";
    import {reportGridMember} from "../report-grid-utils";
    import Icon from "../../../../common/svelte/Icon.svelte";
    import SearchInput from "../../../../common/svelte/SearchInput.svelte";
    import {termSearch} from "../../../../common";
    import _ from "lodash";
    import AddNewSubscriberPanel from "./AddNewSubscriberPanel.svelte";
    import toasts from "../../../../svelte-stores/toast-store";
    import ReportGridPersonOverview from "./ReportGridPersonOverview.svelte";

    const Modes = {
        VIEW: "VIEW",
        CREATE: "CREATE"
    }

    let activeMode = Modes.VIEW;
    let selectedMember = null;

    let qry = null;

    $: membersCall = $selectedGrid?.definition?.id && reportGridMemberStore.findByGridId($selectedGrid?.definition.id, true);
    $: members = $membersCall?.data || [];

    $: peopleCall = $selectedGrid?.definition?.id && reportGridMemberStore.findPeopleByGridId($selectedGrid?.definition.id, true);
    $: people = $peopleCall?.data || [];

    $: membersList = _.isEmpty(qry)
        ? members
        : termSearch(members, qry, ["userId", "role"]);


    $: encrichedMembersList = _.map(
        membersList,
        d => Object.assign({}, d, {person: _.find(people, p => p.email === d.userId)}));

    function selectMember(member) {
        selectedMember = member;
    }

    function editRole(member, role) {
        const updateCmd = {
            userId: member.userId,
            role
        }

        let updatePromise = reportGridMemberStore.updateRole($selectedGrid?.definition?.id, updateCmd);

        Promise.resolve(updatePromise)
            .then(r => {
                toasts.success("Updated user role to:" + role)
                reloadMembers();
                activeMode = Modes.VIEW;
                selectedMember = null;
            })
            .catch(e => toasts.error("Could not update role: " + e))
    }

    function reloadMembers(){
        membersCall = reportGridMemberStore.findByGridId($selectedGrid?.definition.id, true);
        peopleCall = reportGridMemberStore.findPeopleByGridId($selectedGrid?.definition.id, true);
    }

    function deleteMember(member) {

        const reportGridMember = {
            gridId: member.gridId,
            userId: member.userId,
            role: member.role
        }

        reportGridMemberStore.deleteRole(reportGridMember);
        reloadMembers();
        selectedMember = null;
    }

    function cancel(){
        activeMode = Modes.VIEW;
    }

    function addSubscriber() {
        activeMode = Modes.CREATE;
    }

    function selectPerson(p) {
        const cmd = {
            gridId: $selectedGrid?.definition?.id,
            userId: p.email,
            role: reportGridMember.VIEWER.key
        }

        let createPromise = reportGridMemberStore.create(cmd);

        Promise.resolve(createPromise)
            .then(r => {
                toasts.success(`Successfully added ${p.name} as a subscriber to ${$selectedGrid?.definition.name} grid`);
                reloadMembers();
                activeMode = Modes.VIEW;
            })
            .catch(e => toasts.error(`Could not add ${p.name} as a subscriber to this grid.`))
    }

</script>

<div class="row">
    <div class="col-sm-6">
        {#if _.size(membersList) > 10}
            <SearchInput bind:value={qry}
                         placeholder="Search people"/>
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
            {#each encrichedMembersList as member}
                <tr class="clickable"
                    class:selected={selectedMember?.userId === member?.userId}
                    on:click={() => selectMember(member)}>
                    <td>{member?.person?.displayName}</td>
                    <td>{reportGridMember[member.role].name}</td>
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
    <div class="col-sm-6">
        {#if activeMode === Modes.VIEW}
            <ReportGridPersonOverview members={members}
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
</style>