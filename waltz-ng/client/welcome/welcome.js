/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

function controller(staticPanelStore, appGroupStore) {
    const vm = this;

    staticPanelStore.findByGroup('HOME')
        .then(panels => vm.panels = panels);

    appGroupStore.findMyGroupSubscriptions()
        .then(groupSubscriptions => vm.appGroupSubscriptions = groupSubscriptions);

}

controller.$inject = ['StaticPanelStore', 'AppGroupStore'];


export default {
    controller,
    controllerAs: 'ctrl',
    template: require('./welcome.html')
}