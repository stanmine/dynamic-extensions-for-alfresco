// Generated by CoffeeScript 1.3.3
(function() {
  var App;

  App = (typeof exports !== "undefined" && exports !== null ? exports : this).App;

  App.Router = Em.Router.extend({
    root: Em.Route.extend({
      index: Em.Route.extend({
        route: '/',
        redirectsTo: 'dashboard'
      }),
      dashboard: Em.Route.extend({
        route: '/dashboard',
        connectOutlets: function(router) {
          return router.get('applicationController').connectOutlet('dashboard');
        }
      }),
      dictionary: Em.Route.extend({
        route: '/dictionary',
        models: Em.Route.extend({
          route: '/models',
          initialState: 'index',
          index: Em.Route.extend({
            route: '/',
            connectOutlets: function(router) {
              var controller;
              router.get('applicationController').connectOutlet('dictionary');
              controller = router.get('dictionaryController');
              controller.set('selection', App.Dictionary.MODELS);
              return controller.connectOutlet('modelIndex', Dictionary.ModelDefinition.findAll());
            }
          }),
          show: Em.Route.extend({
            route: '/:name',
            deserialize: function(router, params) {
              return Dictionary.ModelDefinition.findByName(params.name);
            },
            connectOutlets: function(router, context) {
              App.set('title', context.get('name'));
              return context.expand().done(function() {
                router.get('modelDetailController').connectOutlet('navigation', 'modelNavigation', context);
                router.get('modelDetailController').connectOutlet('modelDefinition', context);
                router.set('modelNavigationController.selection', context);
                return router.get('applicationController').connectOutlet('modelDetail', context);
              });
            },
            exit: function() {
              return App.set('title', null);
            }
          })
        }),
        dataTypes: Em.Route.extend({
          route: '/data-types',
          connectOutlets: function(router) {
            var controller;
            controller = router.get('dictionaryController');
            controller.set('selection', App.Dictionary.DATA_TYPES);
            controller.connectOutlet('dataTypes', Dictionary.DataType.findAll());
            return router.get('applicationController').connectOutlet('dictionary');
          }
        }),
        namespaces: Em.Route.extend({
          route: '/namespaces',
          connectOutlets: function(router) {
            var controller;
            controller = router.get('dictionaryController');
            controller.set('selection', App.Dictionary.NAMESPACES);
            controller.connectOutlet('namespaces', Dictionary.Namespace.findAll());
            return router.get('applicationController').connectOutlet('dictionary');
          }
        }),
        classDefinition: Em.Route.extend({
          route: '/classes/:name',
          deserialize: function(router, params) {
            return Dictionary.ClassDefinition.findByName(params.name);
          },
          connectOutlets: function(router, context) {
            App.set('title', context.get('name'));
            return context.expand().done(function() {
              var modelDefinition;
              modelDefinition = Dictionary.ModelDefinition.findByName(context.get('model.name'));
              return modelDefinition.done(function() {
                router.get('modelDetailController').connectOutlet('navigation', 'modelNavigation', modelDefinition);
                router.get('modelDetailController').connectOutlet('classDefinition', context);
                router.set('modelNavigationController.selection', context);
                return router.get('applicationController').connectOutlet('modelDetail', context);
              });
            });
          },
          exit: function() {
            return App.set('title', null);
          }
        })
      }),
      loading: Em.State.extend(),
      gotoDashboard: Em.Route.transitionTo('dashboard'),
      gotoDictionary: Em.Route.transitionTo('dictionary.models'),
      showModelDefinitions: Em.Route.transitionTo('dictionary.models.index'),
      showModelDefinition: Em.Route.transitionTo('dictionary.models.show'),
      showClassDefinition: Em.Route.transitionTo('dictionary.classDefinition'),
      showDataTypes: Em.Route.transitionTo('dictionary.dataTypes'),
      showNamespaces: Em.Route.transitionTo('dictionary.namespaces')
    })
  });

}).call(this);