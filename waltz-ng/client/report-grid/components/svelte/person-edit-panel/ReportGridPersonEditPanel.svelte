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

    $: membersList = _.isEmpty(qry)
        ? members
        : termSearch(members, qry, ["userId", "role"]);


    function selectMember(member) {
        selectedMember = member;
    }

    function editRole(member, role) {
        const updateCmd = {
            userId: member.userId,
            role
        }

        reportGridMemberStore.updateRole($selectedGrid?.definition?.id, updateCmd);
        reloadMembers();
        activeMode = Modes.VIEW;
        selectedMember = null;
    }

    function reloadMembers(){
        membersCall = reportGridMemberStore.findByGridId($selectedGrid?.definition.id, true);
    }

    function deleteMember(member) {
        reportGridMemberStore.deleteRole(member);
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
        <table class="table table-condensed table-hover">
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
                    on:click={() => selectMember(member)}>
                    <td>{member.userId}</td>
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
