/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import './app/app.js';
import './app/app.controller.js';
import './app/handsontable/handsonHelper.js';
import './app/notebook/notebook.controller.js';

/** start: global variable `zeppelin` related files */
import './app/tabledata/tabledata.js';
import './app/tabledata/transformation.js';
import './app/tabledata/pivot.js';
import './app/tabledata/passthrough.js';
import './app/tabledata/columnselector.js';
import './app/visualization/visualization.js';
import './app/visualization/builtins/visualization-table.js';
import './app/visualization/builtins/visualization-nvd3chart.js';
import './app/visualization/builtins/visualization-barchart.js';
import './app/visualization/builtins/visualization-piechart.js';
import './app/visualization/builtins/visualization-areachart.js';
import './app/visualization/builtins/visualization-linechart.js';
import './app/visualization/builtins/visualization-scatterchart.js';
import './app/visualization/builtins/storage-areachart.js';
/** end: global variable `zeppelin` related files */

import './app/credential/credential.controller.js';
import './app/configuration/configuration.controller.js';
import './app/configuration/configuration.filter.js';
import './app/notebook/paragraph/paragraph.controller.js';
import './app/notebook/paragraph/result/result.controller.js';
import './app/search/result-list.controller.js';
import './app/notebookRepos/notebookRepos.controller.js';
import './app/helium/helium.controller.js';
import './components/arrayOrderingSrv/arrayOrdering.service.js';
import './components/clipboard/clipboard.controller.js';
import './components/navbar/navbar.controller.js';
import './components/ngescape/ngescape.directive.js';
import './components/interpreter/interpreter.directive.js';
import './components/expandCollapse/expandCollapse.directive.js';
import './components/popover-html-unsafe/popover-html-unsafe.directive.js';
import './components/popover-html-unsafe/popover-html-unsafe-popup.directive.js';
import './components/editor/codeEditor.directive.js';
import './components/ngenter/ngenter.directive.js';
import './components/dropdowninput/dropdowninput.directive.js';
import './components/resizable/resizable.directive.js';
import './components/baseUrl/baseUrl.service.js';
import './components/browser-detect/browserDetect.service.js';
import './components/saveAs/saveAs.service.js';
import './components/searchService/search.service.js';
import './components/login/login.controller.js';
import './components/elasticInputCtrl/elasticInput.controller.js';
import './components/notevarshareService/notevarshare.service.js';
import './components/helium/helium.service.js';
import './app/dashboard/i18n.js';
import './app/dashing/tables/smart-table.js';
import './app/dashing/dashing.js';
import './app/dashboard/services/locator.js';
import './app/dashboard/services/restapi.js';
import './app/dashboard/services/health_check_service.js';
import './app/dashboard/services/models/models.js';
import './app/dashboard/views/cluster/cluster_hottestFiles.js';
import './app/dashboard/views/cluster/cluster_fileInCache.js';
import './app/dashboard/views/cluster/storage/storage.js';
import './app/dashboard/views/cluster/storage/bigChart.js'
import './app/dashboard/views/cluster/nodeinfo/nodes.js';
import './app/dashboard/views/cluster/storage/storages.js';
import './app/dashboard/views/actions/actions.js';
import './app/dashboard/views/actions/action/action.js';
import './app/dashboard/views/actions/submit/submit.js';
import './app/dashboard/views/rules/rules.js';
import './app/dashboard/views/rules/rule/rule.js';
import './app/dashboard/views/rules/submit/submit.js';
import './app/dashboard/views/rules/rule/alerts_table.js';
import './app/dashboard/views/rules/rule/cmdlets_table.js';
import './app/dashboard/views/helper.js';
import './app/dashboard/views/mover/mover.js';
import './app/dashboard/views/copy/copy.js';
import './app/dashboard/views/mover/detail/moverActions.js';
import './app/dashboard/views/copy/detail/copyActions.js';
