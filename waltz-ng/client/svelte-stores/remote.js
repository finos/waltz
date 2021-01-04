import {$http} from "../common/WaltzHttp";
import stringify from "json-stable-stringify";
import {onDestroy} from "svelte";
import {writable} from "svelte/store";


class Cache {

    constructor(name) {
        this.name = name;
        this.data = new Map();
        this.listeners = new Map();
        //onDestroy(() => this.clear());
    }

    clear() {
        this.data.clear();
        this.listeners.clear();
    }

    name() {
        return name;
    }

}

function mkPromise(method, url, config) {
    switch (method) {
        case "GET":
            return $http.get(url);
        case "POST":
            return $http.post(url, config.data);
        case "PUT":
            return $http.put(url, config.data);
        case "DELETE":
            return $http.delete(url);
        default:
            Promise.reject(`Unknown verb: ${method}`);
    }
}


function mkKey(method, url, config) {
    return `${method}_${url}_${stringify(config)}`;
}


function _fetchData(cache, method, url, config, init = []) {
    const key = mkKey(method, url, config);

    if (! cache.data.has(key)) {
        cache.data.set(key, writable(init));
    }

    const store = cache.data.get(key);

    mkPromise(method, url, config)
        .then(r => store.set(r.data));

    return store;
}


const appCache = new Cache();


export function initRemote(name = "defaultRemote") {
    const cache = new Cache(name);

    return {
        fetchViewData: (method, url, config) => _fetchData(cache, method, url, config),
        fetchAppData: (method, url, config) => _fetchData(appCache, method, url, config)
    };
}