<script>
    import ViewLink from "../common/svelte/ViewLink.svelte";
    import PageHeader from "../common/svelte/PageHeader.svelte";
    import _ from "lodash";
    import Icon from "../common/svelte/Icon.svelte";
    import {surveyTemplateStore} from "../svelte-stores/survey-template-store";
    import toastStore from "../svelte-stores/toast-store";
    import {displayError} from "../common/error-utils";
    import pageInfo from "../svelte-stores/page-navigation-store";

    let jsonStr = "";
    let disabled = true;
    let message = "";

    $: {
        try {
            const json = JSON.parse(jsonStr);
            if (_.isEmpty(json)) {
                disabled = true;
                message = "Nothing to parse";
            } else {
                disabled = false;
                message = "";
            }
        } catch (e) {
            disabled = true;
            message = "Cannot parse json.  Error: " + e;
        }
    }

    function save() {
        return surveyTemplateStore
            .importFromJSON(JSON.parse(jsonStr))
            .then(d => {
                toastStore.success("Imported template from file")
                $pageInfo = {
                    state: "main.survey.template.view",
                    params: {
                        id: d.data
                    }
                };
            })
            .catch(e => displayError("Failed to import template", e));
    }

</script>

<PageHeader icon="user-circle"
            name="Template Import"
            small="JSON file">

    <div slot="breadcrumbs">
        <ol class="waltz-breadcrumbs">
            <li>
                <ViewLink state="main">Home</ViewLink>
            </li>
            <li>
                <ViewLink state="main.survey.template.list">Survey Templates</ViewLink>
            </li>
            <li>
                Import
            </li>
        </ol>
    </div>
</PageHeader>

<div class="waltz-page-summary waltz-page-summary-attach">
    <div class="waltz-display-section">

        <div class="row">
            <div class="col-md-12">
                <div class="help-block">
                    From this screen you can create a new template from the JSON representation of an existing Survey Template.
                    Typically used when moving surveys between different environments which do not share a database.
                </div>


                <form autocomplete="off"
                      on:submit|preventDefault={save}>
                    <div class="row">
                        <div class="col-sm-8">
                            <div class="form-group">
                                <!-- NAME -->
                                <label for="json">
                                    JSON Template
                                    <small class="text-muted">(required)</small>
                                </label>
                                <textarea class="form-control"
                                          id="json"
                                          rows="16"
                                          placeholder="JSON Content"
                                          bind:value={jsonStr}></textarea>
                                <div class="help-block">
                                    Copy the json representation of the template into this text area then click import.
                                </div>
                            </div>
                        </div>
                        <div class="col-sm-4">
                            <div style="padding-top: 2em;">
                                {#if disabled}
                                    <div style="color: orange">
                                        <Icon size="2x"
                                              name="exclamation-triangle"/>
                                    </div>
                                    <div>
                                        {message}
                                    </div>
                                {:else}
                                    <div style="color: green">
                                        <Icon size="2x"
                                              name="check"/>
                                    </div>
                                    <div>
                                        JSON is valid (though it may not match the required schema)
                                    </div>
                                {/if}
                            </div>

                        </div>
                    </div>


                    <button type="submit"
                            class="btn btn-success"
                            disabled={disabled}>
                        Import
                    </button>
                </form>
            </div>
        </div>

    </div>
</div>
