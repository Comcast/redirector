<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html ng-app="uxData">
<head>
    <link rel="stylesheet" type="text/css" href="../styles/vendor.css">
    <link rel="stylesheet" type="text/css" href="../styles/redirector.css">
</head>
<body style="padding-top: 0px !important;">
<!-- Nav tabs -->
<div>
    <header class="navbar navbar-default" role="banner">
        <div class="container">
            <div class="navbar-header">
                <a href="#" class="navbar-brand">Decider Web Service Admin</a>
            </div>
            <nav class="collapse navbar-collapse bs-navbar-collapse" role="navigation">
                <ul class="nav navbar-nav">
                    <li class="dropdown">
                        <a href="" data-toggle="dropdown" class="dropdown-toggle">Rules<span class="caret"></span></a>
                        <ul class="dropdown-menu" role="menu">
                            <li><a ui-sref="addDeciderRule">Add</a></li>
                            <li><a ui-sref="showDeciderRules">Show</a></li>
                            <li><a ui-sref="importDeciderRules">Import</a></li>
                        </ul>
                        </li>
                   <li><a ui-sref="partners">Partners</a></li>
                   <li class="dropdown">
                       <a href="" data-toggle="dropdown" class="dropdown-toggle">Namespaced lists<span class="caret"></span></a>
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
                           <li><a ui-sref="namespacesShow">Show</a></li>
                           <li><a ui-sref="namespacesImport">Import</a></li>
                           <li><a ui-sref="namespacesFindByItem">Find</a></li>
                       </ul>
                   </li>
                </ul>
                <ul ng-controller="logoutCtrl as logout" ng-show="showLogoutButton">
                    <li class="nav navbar-nav navbar-right">
                        <a href="" ng-click="goToRedirect()">
                            <span class="label label-default">Logout</span>
                        </a>
                    </li>
                </ul>
            </nav>
        </div>
    </header>
</div>
<div class="container bs-docs-container">
    <div class="row">
        <div class="col-md-13" role="main">
            <div ui-view>

            </div>
        </div>
    </div>
</div>

<%--========================== Libs ===========================================--%>
<script type="text/javascript" src='../scripts/vendor.js'></script>
<script type="text/javascript" src='../scripts/deciderUI.min.js'></script>

</body>
</html>
