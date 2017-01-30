module.exports = function (grunt) {

    require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

    grunt.initConfig({
        pkg: grunt.file.readJSON('package.json'),
        shell: {
            options: {
                stdout: true
            },
            npm_install: {
                command: 'npm install'
            }
        },

        jshint: {
            options: {
                jshintrc: '.jshintrc'
            },
            all: [
                'Gruntfile.js',
                'scripts/{,*/}*.js'
            ]
        },

        uglify: {
            redirectorOnline: {
                src: 'scripts/redirectorUI.js',
                dest: 'scripts/redirectorUI.min.js'
            },
            redirectorOffline: {
                src: 'scripts/redirectorOfflineUI.js',
                dest: 'scripts/redirectorOfflineUI.min.js'
            },
            decider: {
                src: 'scripts/deciderUI.js',
                dest: 'scripts/deciderUI.min.js'
            }
        },
        concat: {
            options: {
                stripBanners: false,
                separator: '\n\n',
                banner: ''
            },
            vendor_styles: {
                dest: 'styles/vendor.css',
                src: [
                    'bower_components/bootstrap/dist/css/bootstrap.min.css',
                    'bower_components/angular-dialog-service/dist/dialogs.min.css',
                    'bower_components/angular-toastr/dist/angular-toastr.min.css',
                    'bower_components/angular-ui/build/angular-ui.min.css',
                    'bower_components/angular-motion/dist/angular-motion.min.css',
                    'bower_components/ng-table/dist/ng-table.min.css',
                    'bower_components/fontawesome/css/font-awesome.min.css',
                    'bower_components/angular-ui-select/dist/select.min.css',
                    'bower_components/dropzone/dist/dropzone.css',
                    'bower_components/bootstrap-toggle/css/bootstrap-toggle.css',
                    'bower_components/angular-xeditable/dist/css/xeditable.min.css'

                ],
                nonull: true
            },

            vendor: {
                options: {
                    separator: '\n\n'
                },
                dest: 'scripts/vendor.js',
                src: [
                    'bower_components/jquery/dist/jquery.min.js',
                    'bower_components/bootstrap/dist/js/bootstrap.min.js',
                    'bower_components/jquery-autosize/jquery.autosize.min.js',
                    'bower_components/angular/angular.min.js',
                    'bower_components/angular-route/angular-route.min.js',
                    'bower_components/angular-animate/angular-animate.min.js',
                    'bower_components/angular-sanitize/angular-sanitize.min.js',
                    'bower_components/angular-ui-router/release/angular-ui-router.min.js',
                    'bower_components/angular-bootstrap/ui-bootstrap.min.js',
                    'bower_components/angular-bootstrap/ui-bootstrap-tpls.min.js',
                    'bower_components/angular-dialog-service/dist/dialogs.min.js',
                    'bower_components/angular-dialog-service/dist/dialogs-default-translations.min.js',
                    'bower_components/angular-toastr/dist/angular-toastr.min.js',
                    'bower_components/angular-ui/build/angular-ui.min.js',
                    'bower_components/angular-ui-utils/ui-utils.min.js',
                    'bower_components/angular-xeditable/dist/js/xeditable.min.js',
                    'bower_components/ng-table/dist/ng-table.min.js',
                    'bower_components/sprintf/dist/sprintf.min.js',
                    'bower_components/bower-javascript-ipv6/ipv6.js',
                    'bower_components/x2js/xml2json.min.js',
                    'bower_components/angular-ui-select/dist/select.min.js',
                    'bower_components/dropzone/dist/min/dropzone.min.js',
                    'bower_components/dropzone/dist/min/dropzone-amd-module.min.js',
                    'bower_components/angular-numeraljs/dist/angular-numeraljs.min.js',
                    'bower_components/numeral/min/numeral.min.js',
                    'bower_components/bootstrap-toggle/js/bootstrap-toggle.min.js',
                    'bower_components/ngstorage/ngStorage.min.js'

                ],
                nonull: true
            },

            redirector_styles: {
                dest: 'styles/redirector.css',
                src: [
                    'uxData/styles/*.css'
                ],
                nonull: true
            },

            redirector_offline_ui: {
                options: {
                    separator: '\n\n'
                },
                dest: 'scripts/redirectorOfflineUI.js',
                src: [
                    'uxData/js/jquery.format.js',
                    'uxData/js/q-all-settled.js',
                    'uxData/js/multiselect-tpls.js',
                    'uxData/scripts/application.js',
                    'uxData/scripts/**/*.module.js',
                    'deciderAdmin/decider-rules/*.module.js',
                    'uxData/scripts/services/*.js',
                    'uxData/scripts/**/*.service.js',
                    'uxData/scripts/constants/*.js',
                    'deciderAdmin/decider-rules/*.js',
                    'redirectorAdmin/config/*.js',
                    'redirectorAdminOffline/config/*.js',
                    'uxData/scripts/**/*.js',
                    'redirectorAdmin/indexedDB/*.js',
                    'redirectorAdmin/RequestsService.js',
                    'redirectorAdmin/IndexedDBDataSource.js',
                    'redirectorAdmin/WebServiceDataSource.js'
                ],
                nonull: true
            },

            redirector_ui: {
                options: {
                    separator: '\n\n'
                },
                dest: 'scripts/redirectorUI.js',
                src: [
                    'uxData/js/jquery.format.js',
                    'uxData/js/q-all-settled.js',
                    'uxData/js/multiselect-tpls.js',
                    'uxData/scripts/application.js',
                    'uxData/scripts/**/*.module.js',
                    'deciderAdmin/decider-rules/*.module.js',
                    'uxData/scripts/services/*.js',
                    'uxData/scripts/**/*.service.js',
                    'uxData/scripts/constants/*.js',
                    'deciderAdmin/decider-rules/*.js',
                    'redirectorAdmin/config/*.js',
                    'uxData/scripts/**/*.js',
                    'redirectorAdmin/indexedDB/*.js',
                    'redirectorAdmin/RequestsService.js',
                    'redirectorAdmin/IndexedDBDataSource.js',
                    'redirectorAdmin/WebServiceDataSource.js'
                ],
                nonull: true
            },

            decider_ui: {
                options: {
                    separator: '\n\n'
                },
                dest: 'scripts/deciderUI.js',
                src: [
                    'uxData/js/jquery.format.js',
                    'uxData/js/q-all-settled.js',
                    'uxData/js/multiselect-tpls.js',

                    'uxData/scripts/application.js',
                    'uxData/scripts/**/*.module.js',
                    'deciderAdmin/decider-rules/*.module.js',
                    'uxData/scripts/services/*.js',
                    'uxData/scripts/**/*.service.js',
                    'uxData/scripts/constants/*.js',
                    'deciderAdmin/decider-rules/*.js',
                    'deciderAdmin/config/*.js',
                    'uxData/scripts/**/*.js',
                    'deciderAdmin/RequestsService.js'
                ],
                nonull: true
            }
        }
    });

    //installation-related
    grunt.registerTask('install', ['update']);
    grunt.registerTask('update', ['shell:npm_install', 'compilebootstrap', 'concat', 'uglify']);

    //defaults
    grunt.registerTask('default', ['dev']);

    //development
    grunt.registerTask('dev', ['update', 'connect:devserver', 'open:devserver', 'watch:assets']);

    //server daemon
    grunt.registerTask('serve', ['connect:webserver']);

    grunt.registerTask('compilebootstrap', function() {
        grunt.file.copy('bootstrap.less', 'bower_components/bootstrap/less/bootstrap.less');

        var cb = this.async();
        grunt.util.spawn({
            grunt: true,
            args: ['dist'],
            opts: {
                cwd: 'bower_components/bootstrap'
            }
        }, function(error, result, code) {
            cb();
        });
    });
};
