<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html ng-app="uxData">
<head>
    <link rel="stylesheet" type="text/css" href="../uxData/styles/testsuite.css">
    <link rel="stylesheet" type="text/css" href="../bower_components/bootstrap/dist/css/bootstrap.min.css">
    <link rel="stylesheet" type="text/css" href="../bower_components/fontawesome/css/font-awesome.min.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/distribution.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/adminui.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/import-namespace.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/delete-duplicate.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/show-namespace.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/search-namespaced-lists.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/rule-preview-styles.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/rules.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/server.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/diff-columns-view.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/tableSorter.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/diff-distributions.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/default-server.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/switcher.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/stackmanagement.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/expressions.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/urlParams.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/summary.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/model-initializer.css">
    <link rel="stylesheet" type="text/css" href="../uxData/styles/adjusted-input-params.css">
    <link rel="stylesheet" type="text/css" href="../bower_components/angular-dialog-service/dist/dialogs.min.css">
    <link rel="stylesheet" type="text/css" href="../bower_components/angular-toastr/dist/angular-toastr.min.css"/>
    <link rel="stylesheet" type="text/css" href="../bower_components/angular-ui/build/angular-ui.min.css"/>
    <link rel="stylesheet" type="text/css" href="../bower_components/angular-ui-select/dist/select.min.css"/>
    <link rel="stylesheet" type="text/css" href="../bower_components/angular-xeditable/dist/css/xeditable.min.css"/>
    <link rel="stylesheet" type="text/css" href="../bower_components/ng-table/dist/ng-table.min.css"/>
    <link rel="stylesheet" type="text/css" href="../bower_components/bootstrap-toggle/css/bootstrap-toggle.css"/>

</head>
<body style="padding-top: 0px !important;">
<!-- Nav tabs -->
<div class="container">
    <div class="navbar navbar-inverse" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                        data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a href="" class="navbar-brand">Redirector Admin OFFLINE</a>
            </div>
            <div class="navbar-collapse collapse">
                <ul class="nav navbar-nav">
                    <li class="dropdown">
                        <a href="" class="dropdown-toggle" data-toggle="dropdown">Rules<span class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a ui-sref="addNewFlavorRule">Flavor Rules Add</a></li>
                            <li><a ui-sref="showFlavorRules">Flavor Rules Show</a></li>
                            <li><a role="menuitem" tabindex="-1" href="#/templates/templatePathRules/">Template Rules</a></li>
                            <li role="presentation" class="divider"></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="addNewURLRule">URL Rules Add</a></li>
                            <li><a ui-sref="showURLRules">URL Rules Show</a></li>
                            <li><a role="menuitem" tabindex="-1" href="#/templates/templateUrlPathRules/">Template URL Rules</a></li>
                            <li role="presentation" class="divider"></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="distribution">Distribution</a></li>
                        </ul>
                    </li>
                    <li class="dropdown">
                        <a href="" class="dropdown-toggle" data-toggle="dropdown">Stacks<span class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a role="menuitem" tabindex="-1" ui-sref="stacksManagement">Stacks Management</a></li>
                        </ul>
                    </li>
                    <li class="dropdown">
                        <a href="" data-toggle="dropdown" class="dropdown-toggle">Namespaced lists<span
                                class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li class="menu-item dropdown dropdown-submenu"><a role="menuitem" tabindex="-1">Add</a>
                                <ul class="dropdown-menu">
                                    <li><a class="menu-item dropdown dropdown-submenu" role="menuitem" tabindex="-1"
                                           ui-sref="namespacesAdd">Add Text</a></li>
                                    <li><a class="menu-item dropdown dropdown-submenu" role="menuitem" tabindex="-1"
                                           ui-sref="namespacesAddIp">Add IP List</a></li>
                                    <li><a class="menu-item dropdown dropdown-submenu" role="menuitem" tabindex="-1"
                                           ui-sref="namespacesAddEncoded">Add Encoded List</a></li>
                                </ul>
                            </li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="namespacesShow">Show</a></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="namespacesImport">Import</a></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="namespacesFindByItem">Find</a></li>
                        </ul>
                    </li>

                    <li class="dropdown">
                        <a role="menuitem" tabindex="-1"  ui-sref="changes">Changes</a>
                    </li>
                </ul>

                <ul class="nav navbar-nav navbar-right" ng-controller="applicationsCtrl as app">
                    <li style="padding-top: 16px; padding-right: 8px; color: #5e5e5e;">Application: </li>
                    <li style="padding-top: 8px">
                        <ui-select
                            ng-model="currentApplication"
                            theme="bootstrap"
                            ng-disabled="false"
                            reset-search-input="false"
                            style="width: 210px;"
                            on-select="changeApplication(currentApplication)">
                            <ui-select-match placeholder="Enter an application name...">{{$select.selected}}</ui-select-match>
                            <ui-select-choices repeat="application in applications | filter: $select.search track by $index">
                                <div ng-bind-html="application | highlight : $select.search"></div>
                            </ui-select-choices>
                        </ui-select>
                    </li>
                    <li class="dropdown">
                        <a href="" class="dropdown-toggle fa fa-user" data-toggle="dropdown"
                           ng-bind="currentUser.username == null ? 'Unknown': currentUser.username">
                            <span class="caret"></span>
                        </a>
                        <ul class="dropdown-menu" role="menu">
                            <li ng-controller="logoutCtrl as logout" ng-show="showLogoutButton">
                                <a href="" ng-click="goToRedirect()">Logout</a>
                            </li>
                            <li class="dropdown">
                                <a role="menuitem" tabindex="-1"  ui-sref="settings">Settings</a>
                            </li>
                        </ul>
                    </li>
                </ul>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-sm-12" role="main">
            <div ui-view>

            </div>
        </div>
    </div>
</div>

<%--========================== Libs ===========================================--%>
<script type="text/javascript" src='../bower_components/jquery/dist/jquery.min.js'></script>
<script type="text/javascript" src='../bower_components/jquery-autosize/jquery.autosize.min.js'></script>
<script type="text/javascript" src='../bower_components/angular/angular.js'></script>
<script type="text/javascript" src='../bower_components/angular-route/angular-route.js'></script>
<script type="text/javascript" src='../bower_components/angular-animate/angular-animate.min.js'></script>
<script type="text/javascript" src='../bower_components/angular-sanitize/angular-sanitize.js'></script>
<script type="text/javascript" src='../bower_components/angular-ui-router/release/angular-ui-router.js'></script>
<script type="text/javascript" src='../bower_components/angular-ui-select/dist/select.min.js'></script>
<script type="text/javascript" src='../bower_components/bootstrap/dist/js/bootstrap.min.js'></script>
<script type="text/javascript" src='../bower_components/angular-bootstrap/ui-bootstrap.js'></script>
<script type="text/javascript" src='../bower_components/angular-bootstrap/ui-bootstrap-tpls.js'></script>
<script type="text/javascript" src='../bower_components/angular-ui/build/angular-ui.js'></script>
<script type="text/javascript" src='../bower_components/angular-ui-utils/ui-utils.js'></script>
<script type="text/javascript" src='../bower_components/angular-dialog-service/dist/dialogs.min.js'></script>
<script type="text/javascript" src='../bower_components/angular-dialog-service/example/js/dialogs-default-translations.js'></script>
<script type="text/javascript" src='../bower_components/angular-toastr/dist/angular-toastr.min.js'></script>
<script type="text/javascript" src='../bower_components/angular-xeditable/dist/js/xeditable.js'></script>
<script type="text/javascript" src='../bower_components/ng-table/dist/ng-table.js'></script>
<script type="text/javascript" src='../bower_components/sprintf/dist/sprintf.min.js'></script>
<script type="text/javascript" src='../bower_components/bower-javascript-ipv6/lib/browser/jsbn.js'></script>
<script type="text/javascript" src='../bower_components/bower-javascript-ipv6/lib/browser/jsbn2.js'></script>
<script type="text/javascript" src='../bower_components/bower-javascript-ipv6/ipv6.js'></script>
<script type="text/javascript" src='../bower_components/x2js/xml2json.min.js'></script>
<script type="text/javascript" src='../bower_components/IndexedDBShim/dist/indexeddbshim.js'></script>
<script type="text/javascript" src='../bower_components/dropzone/dist/dropzone.js'></script>
<script type="text/javascript" src='../bower_components/dropzone/dist/dropzone-amd-module.js'></script>
<script type="text/javascript" src='../bower_components/angular-numeraljs/dist/angular-numeraljs.js'></script>
<script type="text/javascript" src='../bower_components/numeral/min/numeral.min.js'></script>
<script type="text/javascript" src='../bower_components/bootstrap-toggle/js/bootstrap-toggle.js'></script>
<script type="text/javascript" src='../bower_components/ngstorage/ngStorage.js'></script>

<script src="../uxData/js/jquery.format.js"></script>
<script src="../uxData/js/q-all-settled.js"></script>
<script src="../uxData/js/multiselect-tpls.js"></script>


<!-- inject:js -->
<!-- Bootstrapping -->
<script src="../uxData/scripts/application.js"></script>

<script src="../uxData/scripts/config/config.module.js"></script>
<script src="../uxData/scripts/core/core.module.js"></script>
<script src="../redirectorAdminOffline/config/appOfflineMode.config.js"></script>
<script src="../redirectorAdmin/config/state.config.js"></script>
<script src="../uxData/scripts/core/core.js"></script>

<%--========================== Configs ========================================--%>
<script src="../uxData/scripts/config/FormValidation.js"></script>

<%--========================== Services =======================================--%>
<script src="../uxData/scripts/services/services.module.js"></script>
<script src="../uxData/scripts/services/Mediator.js"></script>

<%--========================== Constants =======================================--%>
<script src="../uxData/scripts/constants/constants.module.js"></script>
<script src="../uxData/scripts/constants/permissions.js"></script>
<script src="../uxData/scripts/constants/auth_events.js"></script>
<script src="../uxData/scripts/constants/regexp_constants.js"></script>
<script src="../uxData/scripts/constants/typeahead-data.js"></script>
<script src="../uxData/scripts/constants/rules_constants.js"></script>
<script src="../uxData/scripts/constants/expressions_constants.js"></script>
<script src="../uxData/scripts/constants/server_constants.js"></script>
<script src="../uxData/scripts/constants/common_constants.js"></script>
<script src="../uxData/scripts/constants/localStoragePageNames.js"></script>
<script src="../uxData/scripts/constants/entity_types_constants.js"></script>
<script src="../uxData/scripts/constants/states_constants.js"></script>
<script src="../uxData/scripts/constants/summary_constants.js"></script>
<script src="../uxData/scripts/constants/traffic_constants.js"></script>

<%--========================== Services =======================================--%>
<script src="../uxData/scripts/services/MessageService.js"></script>
<script src="../redirectorAdmin/RequestsService.js"></script>
<script src="../redirectorAdmin/IndexedDBDataSource.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBCommon.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBProvider.js"></script>
<script src="../redirectorAdmin/indexedDB/DataRequesterIndexed.js"></script>
<script src="../redirectorAdmin/indexedDB/DataRequesterSimple.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBNamespaces.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBDistribution.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBDistributionWithDefaultServer.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBWhitelist.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBRules.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBUrlRules.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBRulesTemplates.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBUrlRulesTemplates.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBDefaultUrlParams.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBChanges.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBServicePaths.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBDefaultServer.js"></script>
<script src="../redirectorAdmin/indexedDB/IndexedDBSettings.js"></script>
<script src="../redirectorAdmin/WebServiceDataSource.js"></script>
<script src="../uxData/scripts/services/LocalStorageService.js"></script>
<script src="../uxData/scripts/services/UtilsService.js"></script>
<script src="../uxData/scripts/services/AuthService.js"></script>
<script src="../uxData/scripts/services/ConstantsProviderService.js"></script>
<script src="../uxData/scripts/services/TokenService.js"></script>
<script src="../uxData/scripts/services/LogoutService.js"></script>
<script src="../uxData/scripts/services/global.validation.service.js"></script>

<script src="../uxData/scripts/import/import.service.js"></script>

<%--========================== Directives =====================================--%>
<script src="../uxData/scripts/directives/OptionsDisabled.js"></script>
<script src="../uxData/scripts/directives/Server.js"></script>
<script src="../uxData/scripts/directives/Expression.js"></script>
<script src="../uxData/scripts/directives/URLExpression.js"></script>
<script src="../uxData/scripts/directives/RuleXmlView.js"></script>
<script src="../uxData/scripts/directives/RulePreview.js"></script>
<script src="../uxData/scripts/directives/ProgressButton.js"></script>
<script src="../uxData/scripts/directives/aDisabled.js"></script>
<script src="../uxData/scripts/directives/MaxLength.js"></script>
<script src="../uxData/scripts/directives/Paginator.js"></script>
<script src="../uxData/scripts/directives/PendingChangesIcon.js"></script>
<script src="../uxData/scripts/directives/TableSorter.js"></script>
<script src="../uxData/scripts/directives/BtnSwitch.js"></script>
<script src="../uxData/scripts/directives/rules/RuleFilterSort-directive.js"></script>
<script type="text/javascript" src='../uxData/scripts/directives/bootstrap-toggle.directive.js'></script>

<%--========================== Filters ========================================--%>
<script src="../uxData/scripts/filters/OrderRuleObjectBy.js"></script>
<script src="../uxData/scripts/filters/FindObjectByFirstOrderProperty.js"></script>
<script src="../uxData/scripts/filters/HostnameAndIp.js"></script>

<%--========================== Controllers ====================================--%>
<script src="../uxData/scripts/controllers/ServerCtrl.js"></script>
<script src="../uxData/scripts/controllers/BackupCtrl.js"></script>
<script src="../uxData/scripts/controllers/ApplicationsCtrl.js"></script>
<script src="../uxData/scripts/controllers/RulePreviewController.js"></script>
<script src="../uxData/scripts/controllers/PaginatorCtrl.js"></script>
<script src="../uxData/scripts/controllers/LogoutCtrl.js"></script>


<%--========================== Namespaced ====================================--%>
<script src="../uxData/scripts/namespaced/namespaced.module.js"></script>
<script src="../uxData/scripts/namespaced/namespaced.service.js"></script>
<script src="../uxData/scripts/namespaced/namespaced-alerts.service.js"></script>
<script src="../uxData/scripts/namespaced/namespaced-edit.controller.js"></script>
<script src="../uxData/scripts/namespaced/namespaced-list.controller.js"></script>
<script src="../uxData/scripts/namespaced/namespaced-import.controller.js"></script>
<script src="../uxData/scripts/namespaced/namespaced-modal.controller.js"></script>
<script src="../uxData/scripts/namespaced/namespaced-search.controller.js"></script>
<script src="../uxData/scripts/namespaced/namespaced-delete-entities.controller.js"></script>


<%--========================== StacksManagement ====================================--%>
<script src="../uxData/scripts/stacks-management/stacksManagement.module.js"></script>
<script src="../uxData/scripts/stacks-management/stacksManagement.service.js"></script>
<script src="../uxData/scripts/stacks-management/stacksManagement.controller.js"></script>


<%--========================== Distribution ====================================--%>
<script src="../uxData/scripts/distribution/distribution.module.js"></script>
<script src="../uxData/scripts/distribution/distribution.controller.js"></script>
<script src="../uxData/scripts/distribution/distribution-edit.controller.js"></script>
<script src="../uxData/scripts/distribution/distribution.service.js"></script>
<script src="../uxData/scripts/distribution/distribution-alerts.service.js"></script>
<script src="../uxData/scripts/distribution/distribution-requests.service.js"></script>
<script src="../uxData/scripts/distribution/distribution-validation.service.js"></script>

<%--========================== Diff Distribution ====================================--%>
<script src="../uxData/scripts/distribution-diff/distribution-diff.module.js"></script>
<script src="../uxData/scripts/distribution-diff/distribution-diff.directive.js"></script>
<script src="../uxData/scripts/distribution-diff/distribution-diff.controller.js"></script>

<%--========================== Partners ====================================--%>
<script src="../uxData/scripts/partners/partners.module.js"></script>
<script src="../uxData/scripts/partners/partners-edit-controller.js"></script>
<script src="../uxData/scripts/partners/partners.controller.js"></script>

<%--=========================== FileSelect =====================================--%>
<script src="../uxData/scripts/file-select/file-select.directive.js"></script>
<script src="../uxData/scripts/file-select/file-reader.service.js"></script>

<%--=========================== testsuite =====================================--%>
<script src="../uxData/scripts/testsuite/testsuite.module.js"></script>
<script src="../uxData/scripts/testsuite/testsuite-edit.controller.js"></script>
<script src="../uxData/scripts/testsuite/testsuite-requests.service.js"></script>
<script src="../uxData/scripts/testsuite/testsuite.service.js"></script>
<script src="../uxData/scripts/testsuite/testsuite-show.controller.js"></script>
<script src="../uxData/scripts/testsuite/testsuite-validation.service.js"></script>
<script src="../uxData/scripts/testsuite/testsuite-alerts.service.js"></script>
<script src="../uxData/scripts/testsuite/testsuite-run.controller.js"></script>
<script src="../uxData/scripts/testsuite/testsuite-import.controller.js"></script>

<%--=========================== active-whitelisted =====================================--%>
<script src="../uxData/scripts/active-whitelisted/active-whitelisted.module.js"></script>
<script src="../uxData/scripts/active-whitelisted/active-whitelisted.controller.js"></script>
<script src="../uxData/scripts/active-whitelisted/active-whitelisted.directive.js"></script>

<%--========================== Changes ====================================--%>
<script src="../uxData/scripts/changes/changes.module.js"></script>
<script src="../uxData/scripts/changes/changes.service.js"></script>
<script src="../uxData/scripts/changes/changes-requests.service.js"></script>
<script src="../uxData/scripts/changes/changes.controller.js"></script>

<%--========================== OFFLINE Changes=============================--%>
<script src="../uxData/scripts/changesOffline/changesOffline.module.js"></script>
<script src="../uxData/scripts/changesOffline/changesOffline.service.js"></script>
<script src="../uxData/scripts/changesOffline/changesOffline-requests.service.js"></script>
<script src="../uxData/scripts/changesOffline/changesOffline.controller.js"></script>

<%--========================== Templates ====================================--%>
<script src="../uxData/scripts/templates/templates.module.js"></script>
<script src="../uxData/scripts/templates/templates.service.js"></script>
<script src="../uxData/scripts/templates/templates-requests.service.js"></script>
<script src="../uxData/scripts/templates/templates.controller.js"></script>

<%--========================== Rules ====================================--%>
<script src="../uxData/scripts/rules/rules.module.js"></script>
<script src="../uxData/scripts/rules/rules-show.controller.js"></script>
<script src="../uxData/scripts/rules/rules-edit.controller.js"></script>
<script src="../uxData/scripts/rules/rules.service.js"></script>
<script src="../uxData/scripts/rules/rules-edit.service.js"></script>
<script src="../uxData/scripts/rules/rules-request.service.js"></script>
<script src="../uxData/scripts/rules/rules-builder.service.js"></script>
<script src="../uxData/scripts/rules/rules-validation.service.js"></script>
<script src="../uxData/scripts/rules/rules-validation-common.service.js"></script>
<script src="../uxData/scripts/rules/rules-alert.service.js"></script>
<script src="../uxData/scripts/rules/pathRules-builder.service.js"></script>

<script src="../uxData/scripts/rules/rules-marshaller.service.js"></script>
<script src="../uxData/scripts/rules/rules-unmarshaller.service.js"></script>

<%--========================== URL-Rules ================================--%>
<script src="../uxData/scripts/url-rules/urlRules.module.js"></script>
<script src="../uxData/scripts/url-rules/urlRules.service.js"></script>
<script src="../uxData/scripts/url-rules/urlRules-edit.controller.js"></script>
<script src="../uxData/scripts/url-rules/urlRules-show.controller.js"></script>
<script src="../uxData/scripts/url-rules/urlRules-request.service.js"></script>
<script src="../uxData/scripts/url-rules/urlRules-validation.service.js"></script>
<script src="../uxData/scripts/url-rules/urlRules-builder.service.js"></script>

<%--========================== URL-Params ===============================--%>
<script src="../uxData/scripts/url-params/urlParams.module.js"></script>
<script src="../uxData/scripts/url-params/urlParams.directive.js"></script>
<script src="../uxData/scripts/url-params/urlParams.controller.js"></script>

<%--========================== Expressions ================================--%>
<script src="../uxData/scripts/expressions/expression.module.js"></script>
<script src="../uxData/scripts/expressions/expression.service.js"></script>
<script src="../uxData/scripts/expressions/expression-directive.js"></script>
<script src="../uxData/scripts/expressions/expression.controller.js"></script>
<script src="../uxData/scripts/expressions/expression-builder.service.js"></script>

<%--========================== Server =====================================--%>
<script src="../uxData/scripts/server/server.module.js"></script>
<script src="../uxData/scripts/server/server.directive.js"></script>
<script src="../uxData/scripts/server/server.controller.js"></script>
<script src="../uxData/scripts/server/server.service.js"></script>

<%--========================== DeciderRules ====================================--%>
<script src="../deciderAdmin/decider-rules/deciderRules.module.js"></script>
<script src="../deciderAdmin/decider-rules/deciderRules.service.js"></script>
<script src="../deciderAdmin/decider-rules/deciderRules-request.service.js"></script>
<script src="../deciderAdmin/decider-rules/deciderRules-builder.service.js"></script>
<script src="../deciderAdmin/decider-rules/deciderRules-edit.controller.js"></script>
<script src="../deciderAdmin/decider-rules/deciderRules-show.controller.js"></script>

<%--========================== Summary ====================================--%>
<script src="../uxData/scripts/summary/summary.module.js"></script>
<script src="../uxData/scripts/summary/summary.service.js"></script>
<script src="../uxData/scripts/summary/summary.controller.js"></script>
<script src="../uxData/scripts/summary/summary-request.service.js"></script>

<%--========================== Default server ====================================--%>
<script src="../uxData/scripts/default-server/default-server.module.js"></script>
<script src="../uxData/scripts/default-server/default-server.directive.js"></script>
<script src="../uxData/scripts/default-server/default-server.controller.js"></script>

<%--========================== Traffic ====================================--%>
<script src="../uxData/scripts/traffic/traffic.module.js"></script>
<script src="../uxData/scripts/traffic/traffic.directive.js"></script>
<script src="../uxData/scripts/traffic/traffic.config.js"></script>
<script src="../uxData/scripts/traffic/traffic.controller.js"></script>
<script src="../uxData/scripts/traffic/traffic.service.js"></script>
<script src="../uxData/scripts/traffic/traffic-request.service.js"></script>
<script src="../uxData/scripts/traffic/traffic-indexdb.service.js"></script>
<script src="../uxData/scripts/traffic/traffic-webservice.service.js"></script>

<%--========================== Settings ====================================--%>
<script src="../uxData/scripts/settings/settings.module.js"></script>
<script src="../uxData/scripts/settings/settings.controller.js"></script>
<script src="../uxData/scripts/settings/settings.service.js"></script>
<script src="../uxData/scripts/settings/settings-request.service.js"></script>

<%--========================== Diff====================================--%>
<script src="../uxData/scripts/diff/diff.module.js"></script>
<script src="../uxData/scripts/diff/diff.directive.js"></script>
<script src="../uxData/scripts/diff/diff.controller.js"></script>
<script src="../uxData/scripts/diff/diff.service.js"></script>
<script src="../uxData/scripts/diff/diff-line.service.js"></script>
<script src="../uxData/scripts/diff/diff-testcase.service.js"></script>

<%--========================== Model Initializer =============================--%>
<script src="../uxData/scripts/model-initializer/model-initializer.module.js"></script>
<script src="../uxData/scripts/model-initializer/model-initializer.service.js"></script>
<script src="../uxData/scripts/model-initializer/model-initializer-request.service.js"></script>
<script src="../uxData/scripts/model-initializer/model-initializer.controller.js"></script>

</body>
</html>
