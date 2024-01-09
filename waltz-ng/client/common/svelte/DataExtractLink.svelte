<script>
    import Icon from "./Icon.svelte";
    import toasts from "../../svelte-stores/toast-store";
    import {displayError} from "../error-utils";
    import {downloadFile} from "../file-utils";
    import {$http as http} from "../WaltzHttp";

    export let format = null;
    export let name = "Export";
    export let method = "GET";
    export let styling = "button";  // button || link
    export let filename = "extract";
    export let requestBody = null;
    export let extractUrl;

    const base = "data-extract";

    function getFileNameFromHttpResponse(httpResponse) {
        const contentDispositionHeader = httpResponse.headers["Content-Disposition"];
        if(!contentDispositionHeader) {
            return null;
        }
        const result = contentDispositionHeader.split(";")[1].trim().split("=")[1];
        return result.replace(/"/g, "");
    }

    function calcClasses(styling = "button") {
        switch (styling) {
            case "link":
                return ["btn-link"];
            default:
                return ["btn", "btn-xs", "btn-default"];
        }
    }

    function mkRequestOptions(method, format, requestBody) {
        switch (method) {
            case "GET":
                return {method};
            case "POST":
            case "PUT":
                return {method, body: JSON.stringify(requestBody)};
            default:
                throw `Unsupported extract method: ${method}`;
        }
    }

    function mkFilename(filename, fmt) {
        return `${filename}.${_.toLower(fmt)}`;
    }

    function invokeExport(fmt) {
        const options = {
            params : { format: fmt }
        };
        if (fmt === "XLSX") {
            options.responseType = "arraybuffer";
        }

        const requestOptions = mkRequestOptions(method, format, requestBody);

        return http
            .doFetch(`${url}?format=${fmt}`, requestOptions)
            .then(r => fmt === 'XLSX'
                ? r.blob()
                : r.text())
            .then(data => downloadFile(data, mkFilename(filename, fmt), fmt));
    }

    function doExport(format) {
        toasts.info("Exporting data");
        extracting = true;
        invokeExport(format)
            .then(() => toasts.success("Data exported"))
            .catch(e => displayError("Data export failure", e))
            .finally(() => extracting = false);
    }

    function exportAs(fmt) {
        doExport(fmt);
    }

    function exportCsv() {
        doExport("CSV");
    }

    function exportXlsx() {
        doExport("XLSX");
    }

    let extracting = false;

    $: url = `${base}/${extractUrl}`;
    $: classes = calcClasses(styling);
</script>


{#if !format}
        <ul>
            <li>
                <button class={classes}>
                    <Icon name={extracting ? "refresh" : "cloud-download"}
                          spin={extracting}/>
                    {name}
                    <span class="caret"></span>
                </button>
                <ul class="dropdown"
                    role="menu">
                    <li role="menuitem">
                        <button class="btn-link"
                                on:mousedown|preventDefault={exportCsv}>
                            <Icon name="file-text-o"/>
                            Export as csv
                        </button>
                    </li>
                    <li role="menuitem">
                        <button class="btn-link"
                                on:click|preventDefault={exportXlsx}>
                            <Icon name="file-excel-o"/>
                            Export as xlsx
                        </button>
                    </li>
                </ul>
            </li>

        </ul>
{:else}
    <!-- format specified, eg: SVG -->
        <button class={classes}
                on:click={() => exportAs(format)}>
            <Icon name={extracting ? "refresh" : "cloud-download"}
                  spin={extracting}/>
            {name}
        </button>
{/if}

<style>

    ul {
        display: inline-block;
        list-style: none;
        margin: 0;
        padding-left: 0;
    }

    li {
        display: block;
        position: relative;
        text-decoration: none;
        transition-duration: 0.5s;
    }


    li:hover {
        background-color: #fafafa;
        cursor: pointer;
    }


    ul li ul {
        background: white;
        visibility: hidden;
        opacity: 0;
        min-width: 20rem;
        position: absolute;
        left: 0.1em;
        transition: all 0.5s ease;
        display: none;
    }

    ul li:hover > ul,
    ul li ul:hover {
        visibility: visible;
        opacity: 1;
        display: block;
        z-index: 100000;
        border: 1px solid #666;
    }

    ul li ul li {
        clear: both;
        width: 100%;
    }
</style>