<!DOCTYPE html>
<html ng-app="uxData">
<head>
    <link rel="stylesheet" type="text/css" href="../styles/vendor.css">
    <link rel="stylesheet" type="text/css" href="../styles/redirector.css">

</head>
<body style="padding-top: 0px !important;">
<!-- Nav tabs -->
<div class="container">
    <div class="navbar navbar-default" role="navigation">
        <div class="container-fluid">
            <div class="navbar-header">
                <button type="button" class="navbar-toggle collapsed" data-toggle="collapse"
                        data-target=".navbar-collapse">
                    <span class="sr-only">Toggle navigation</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a href="" class="navbar-brand">Redirector Admin</a>
            </div>
            <div class="navbar-collapse collapse">
                <ul class="nav navbar-nav">
                    <li class="dropdown">
                        <a href="" class="dropdown-toggle" data-toggle="dropdown">Rules<span class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a ui-sref="addNewFlavorRule">Flavor Rules Add</a></li>
                            <li><a ui-sref="showFlavorRules">Flavor Rules Show</a></li>
                            <li><a ui-sref="importFlavorRules">Flavor Rules Import</a></li>
                            <li><a role="menuitem" tabindex="-1" href="#/templates/templatePathRules/">Template Rules</a></li>
                            <li role="presentation" class="divider"></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="addNewURLRule">URL Rules Add</a></li>
                            <li><a ui-sref="showURLRules">URL Rules Show</a></li>
                            <li><a ui-sref="importURLRules">URL Rules Import</a></li>
                            <li><a role="menuitem" tabindex="-1" href="#/templates/templateUrlPathRules/">Template URL Rules</a></li>
                            <li role="presentation" class="divider"></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="distribution">Distribution</a></li>
                        </ul>
                    </li>
                    <li class="dropdown">
                        <a href="" class="dropdown-toggle" data-toggle="dropdown">Test Suite<span
                                class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a role="menuitem" tabindex="-1" ui-sref="testsuite-show">Show</a></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="testsuite-add">Add</a></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="testsuite-import">Import</a></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="testsuite-showauto">Generate and Run Tests</a></li>
                        </ul>
                    </li>
                    <li class="dropdown">
                        <a href="" class="dropdown-toggle" data-toggle="dropdown">Stacks<span class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a role="menuitem" tabindex="-1" ui-sref="stacksManagement">Stacks Management</a></li>
                            <li><a role="menuitem" tabindex="-1" ui-sref="summary">Summary</a></li>
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
                        <a href="" data-toggle="dropdown" class="dropdown-toggle">Changes<span class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a role="menuitem" tabindex="-1"  ui-sref="changes">Changes</a></li>
                            <li><a role="menuitem" tabindex="-1"  ui-sref="changesOffline">Offline Mode Changes</a></li>
                            <li><a role="menuitem" tabindex="-1"  ui-sref="modelInitializer">Model Initializer</a></li>
                        </ul>

                    </li>
                </ul>

                <ul class="nav navbar-nav navbar-right" ng-controller="applicationsCtrl as app">
                    <li style="padding-top: 16px; padding-right: 8px; color: #5e5e5e;">Application: </li>
                    <li style="padding-top: 8px">
                        <ui-select
                            ng-model="appsChooseHolder.currentApplication"
                            theme="bootstrap"
                            ng-disabled="false"
                            reset-search-input="false"
                            style="width: 210px;"
                            on-select="changeApplication(appsChooseHolder.currentApplication)">
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
<script type="text/javascript" src='../scripts/vendor.js'></script>
<script type="text/javascript" src='../scripts/redirectorUI.min.js'></script>

</body>
</html>
