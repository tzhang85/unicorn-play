define(
    ["dojo",
    "coweb/main",
    "dijit/registry",
    "dojox/grid/DataGrid",
    "dojo/data/ItemFileWriteStore",
    "cowebx/dojo/BusyDialog/BusyDialog",
    "dojo/_base/array",
    "dijit/form/Button",
    "dijit/layout/BorderContainer",
    "dijit/layout/ContentPane"
    ],
    function(dojo, coweb, dijit, DataGrid,
      ItemFileWriteStore, BusyDialog, arrays) {

      var ColistApp = function() {
        return;
      };
      var proto = ColistApp.prototype;

      proto.init = function() {
        console.log("ColistApp init called!");
        dojo.parser.parse();
        this.grid = dijit.byId("grid"); // not dojo.byId, which would give
        //the DOM object
        this.grid.canSort = function() {return false;};
        this.dataStore = null;
        this.dsHandles = {};

        this.initCollab();

        this.localListData = [];
        this.buildList();

        //we're using this like a hashmap, where the keys are the "id"s of the
        //rows
        this.removed = {};
        var addButton = dijit.byId("addButton");
        var removeButton = dijit.byId("remButton");
        dojo.connect(addButton, "onClick", this, "onAddRow");
        dojo.connect(removeButton, "onClick", this, "onRemoveRow");

        //Session management stuff
        var session = coweb.initSession();
        session.onStatusChange(function(stat) {
          console.log(stat);
        });
        //BusyDialog.createBusy(session);
        session.prepare();
      };
      proto.initCollab = function() {
        console.log("initCollab called!");
        //temporary stub
      };

      proto.buildList = function() {
        console.log("buildList called!");
        var emptyData = {data:{identifier:"id", label:"name", items:[]}};
        var store = new ItemFileWriteStore(emptyData);
        arrays.forEach(this.bgData, function(at) {
          store.newItem(at);
        });

        this.dataStore = store;
        console.log(this.grid);
        this.grid.setStore(store);
      };

      proto.onAddRow = function() {
        var date = new Date();
        var id = String(Math.random()).substr(2) + String(date.getTime());
        var toBeAdded = {
          id: id,
          name: "New item",
          amount: 0
        };
        this.dataStore.newItem(toBeAdded);
        console.log("onAddRow called! added: " + toBeAdded.id);
      };

      proto.onRemoveRow = function() {
        console.log("onRemoveRow called!");
        var selected = this.grid.selection.getSelected();
        // Remember the positions of the removed elements in the "removed" object
        arrays.forEach(selected, function(item) {
          this.removed[this.dataStore.getIdentity(item)] = this.grid.getItemIndex(item);
        }, this);
        this.grid.removeSelectedRows();
      };

      var app = new ColistApp();
      dojo.ready(function() {
        app.init();
      });

    }
);