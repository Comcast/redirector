<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <script src="uxData/js/angular.js"></script>
        <title>Redirector Web Service Admin</title>
        <link rel="stylesheet" href="uxData/styles/bootstrap.min.css">
        <link rel="stylesheet" href="uxData/styles/adminui.css">
    </head>
    <body>
        <div class="panel panel-default login-form">
            <div class="panel-heading">Please enter you <strong>CROWD</strong> credentials</div>
            <div class="panel-body">
                <span class="error-label"><c:out value="${error}" /></span>
                <form method="POST" action="auth">
                    <input type="hidden" name="return_to" value="${param.return_to}" />
                    <div class="form-group">
                        <label for="username">Username:</label>
                        <input class="form-control" type="text" name="username" id="username" />
                    </div>
                    <div class="form-group">
                        <label for="password">Password:</label>
                        <input class="form-control" type="password" name="password" id="password" />
                    </div>
                    <button type="submit" class="btn btn-primary">Login</button>
                </form>
            </div>
        </div>

        <div ng-app="uxData" ng-controller="tokenCleanerCtrl">
            <script>
                angular.module('uxData', [])
                       .controller('tokenCleanerCtrl', ['tokenService',
                    function (tokenService) {
                        tokenService.removeGlobalUser();
                    }
                ]);
            </script>
        </div>
    </body>
</html>
<script src="uxData/scripts/services/LocalStorageService.js"></script>
<script src="uxData/scripts/services/TokenService.js"></script>