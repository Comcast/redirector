/**
 * Authored and shared by Aaron Hardy (https://github.com/Aaronius)
 * Link to the source:
 * https://gist.github.com/Aaronius/46ae4a0f8ff052cd24f0
 */
angular.module('qAllSettled', []).config(["$provide", function($provide) {
    $provide.decorator('$q', ["$delegate", function($delegate) {
        var $q = $delegate;
        $q.allSettled = function(promises) {
            return $q.all(promises.map(function(promise) {
                return promise.then(function(value) {
                    return { state: 'fulfilled', value: value };
                }, function(reason) {
                    return { state: 'rejected', reason: reason };
                });
            }));
        };
        return $q;
    }]);
}]);