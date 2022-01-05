<script>
    import _ from "lodash";
    import {activeMode, Modes, selectedUser} from "./user-management-store";
    import {userStore} from "../../svelte-stores/user-store";
    import toasts from "../../svelte-stores/toast-store";

    let newPassword = null;
    let passwordCheck = null;

    $: disabled = _.isEmpty(newPassword)
        || _.isEmpty(newPassword.trim())
        || newPassword !== passwordCheck;

    function resetPassword() {
        const resetPromise = userStore.resetPassword($selectedUser.userName, newPassword);

        Promise.resolve(resetPromise)
            .then(r => {
                toasts.success("Successfully updated password for user: " + $selectedUser.userName)
                $activeMode = Modes.DETAIL;
            })
            .catch(e => toasts.error("Could not update password for user: " + e.error))
    }

</script>

<h4>Password Update for {$selectedUser.userName}</h4>
<div class="help-block">
    Enter a new password for the user below, password cannot be blank and the tr-typed password must match (case-sensitive).
</div>

<div class="row">
    <label for="new"
           class="col-sm-2">
        New Password
    </label>

    <div class="col-sm-3">
        <input class="form-control"
               id="new"
               maxlength="255"
               placeholder="New password"
               bind:value={newPassword}/>
    </div>
</div>
<br>
<div class="row">
    <label for="check"
           class="col-sm-2">
        Re-type Password
    </label>

    <div class="col-sm-3">
        <input class="form-control"
               id="check"
               maxlength="255"
               placeholder="New password"
               bind:value={passwordCheck}/>
    </div>
</div>

<br>
<div class="row">
    <div class="col-sm-2">
    </div>
    <div class="col-sm-3">
        <button class="btn btn-danger"
                on:click={() => resetPassword()}
                disabled={disabled}>
            Reset Password
        </button>
        <button class="btn btn-skinny"
                on:click={() => $activeMode = Modes.DETAIL}>
            Cancel
        </button>
    </div>
</div>
