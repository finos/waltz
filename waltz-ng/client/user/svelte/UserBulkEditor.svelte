<script>
    import {userStore} from "../../svelte-stores/user-store";
    import {displayError} from "../../common/error-utils";
    import _ from "lodash";
    import Icon from "../../common/svelte/Icon.svelte";

    const UploadModes = {
        ADD: "ADD_ONLY",
        REMOVE: "REMOVE_ONLY",
        REPLACE: "REPLACE"
    };

    const Modes = {
        EDIT: "EDIT",
        PREVIEW: "PREVIEW",
        RESULT: "RESULT"
    };

    let uploadMode = UploadModes.ADD;
    let rowData = "";
    let preview = [];
    let mode = Modes.EDIT
    let updatedCount = 0;

    function onPreviewRows() {
        userStore
            .bulkUploadPreview(uploadMode, rowData)
            .then(r => {
                preview = r.data;
                mode = Modes.PREVIEW;
            })
            .catch(e => displayError("Could not preview bulk upload", e));
    }

    function onUploadRows() {
        userStore
            .bulkUpload(uploadMode, rowData)
            .then(r => {
                mode = Modes.RESULT;
                updatedCount = r.data;
            })
            .catch(e => displayError("Could not perform bulk upload", e));
    }
</script>

{#if mode === Modes.EDIT}
    <form autocomplete="off"
          on:submit|preventDefault={onPreviewRows}>

        <fieldset>
            <legend>Upload Mode</legend>
            <div class="btn-group">
                <div class="radio">
                    <label>
                        <input style="display: inline-block;"
                               type="radio"
                               bind:group={uploadMode}
                               name="uploadMode"
                               value={UploadModes.ADD}>
                        Add Only
                    </label>
                    <div class="help-block">
                        Add will add the roles specified in the upload section to any existing roles for the user.
                    </div>
                </div>
                <div class="radio">
                    <label>
                        <input style="display: inline-block;"
                               type="radio"
                               bind:group={uploadMode}
                               name="uploadMode"
                               value={UploadModes.REMOVE}>
                        Remove Only
                    </label>
                    <div class="help-block">
                        Remove will only remove the roles specified in the upload section.
                    </div>
                </div>

                <div class="radio">
                    <label>
                        <input style="display: inline-block;"
                               type="radio"
                               bind:group={uploadMode}
                               name="uploadMode"
                               value={UploadModes.REPLACE}>
                        Replace
                    </label>
                    <p class="help-block">
                        Replace will remove all existing roles for the user, and replace them with the uploaded roles.
                    </p>
                </div>
            </div>
        </fieldset>

        <br>

        <fieldset>
            <legend>User/Role Data</legend>
            <div class="form-group">
                <div class="help-block">
                    This section allows you to bulk edit user roles.
                </div>
                <div class="row">
                    <div class="col-md-7">
                        <textarea id="row-data"
                                  placeholder="username, role"
                                  bind:value={rowData}
                                  cols="120"
                                  class="form-control"
                                  rows="10"></textarea>
                    </div>
                    <div class="col-md-5">
                        <div class="help-block">
                            Each row should reflect a user/role combination, using either commas or tabs as delimiters.
                            For example:
                        </div>

                        <pre style="white-space: pre-line">
                            username, role
                            test.user@somewhere.com, BOOKMARK_EDITOR
                            test.user@somewhere.com, ADMIN
                            another.user@somewhere.com, ADMIN
                        </pre>

                    </div>
                </div>

            </div>
        </fieldset>

        <button type="submit"
                class="btn btn-success"
                disabled={!rowData}>
            Preview
        </button>

    </form>
{/if}

{#if mode === Modes.PREVIEW}
    <form autocomplete="off"
          on:submit|preventDefault={onUploadRows}>

        <fieldset>
            <legend>Preview</legend>
            <div class="help-block">
                This section shows a preview of the results of the bulk upload.
                Red cells indicate that the user or role could not be found and the line will be ignored if you proceed.
            </div>

            <label for="mode-preview">
                Upload Mode:
            </label>
            <div id="mode-preview">
                {uploadMode}
            </div>

            <br>

            <label for="preview-table">
                User/Role Data:
            </label>

            <table id="preview-table"
                   class="table table-condensed small">
                <thead>
                <tr>
                    <th>Username</th>
                    <th>Role</th>
                </tr>
                </thead>
                <tbody>
                {#each preview as row}
                    {@const okUser = !_.isNil(row.resolvedUser)}
                    {@const okRole = !_.isNil(row.resolvedRole)}
                    <tr>
                        <td class:danger={!okUser}
                            class:success={okUser}>
                            {row.givenUser}
                            <Icon name={okUser ? "check" : "exclamation-triangle"}/>
                        </td>
                        <td class:danger={!okRole}
                            class:success={okRole}>
                            {row.givenRole}
                            <Icon name={okRole ? "check" : "exclamation-triangle"}/>
                        </td>
                    </tr>
                {/each}
                </tbody>
            </table>
        </fieldset>

        <button type="submit"
                class="btn btn-success"
                disabled={!rowData}>
            Update
        </button>

        <button class="btn btn-skinny"
                on:click={() => mode = Modes.EDIT}>
            Back
        </button>
    </form>
{/if}


{#if mode === Modes.RESULT}
    <fieldset>
        <legend>Result</legend>
        <div>
            Updated: {updatedCount} user/role entries.
        </div>
    </fieldset>

    <br>

    <button class="btn btn-skinny"
            on:click={() => mode = Modes.EDIT}>
        Back
    </button>
{/if}