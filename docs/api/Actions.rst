==============
Custom Actions
==============

Plugins that implement the API can add custom actions that can be inserted in the Prism Data (by the plugin).

Prism will not monitor custom plugin events, that requires you to hook a listener.

To initialize your plugin would initially hook the api and then register the new Action
see :ref:`usage`

You may need a custom handler to define what happens with this action

.. code-block:: java

    public class CustomHandler implements Handler {
         // implement methods
         // Please review the main Prism codebase to see how handlers are created.
    }

Create a new Action class

.. code-block:: java

    public class CustomAction implements Action {

        private Handler customHandler;

        public CustomAction() {
            this.customHandler = new CustomHandler();
        }

        @Override
        public ActionType getActionType() {
            return ActionType.CUSTOM_ACTION;
        }

        @Override
        public boolean canRollback() {
            return false;
        }

        @Override
        public boolean canRestore() {
            return false;
        }

        @Override
        public Class<? extends Handler> getHandler() {
            return null;
        }

        @Override
        public boolean requiresHandler(Class<? extends Handler> handler) {
            return false;
        }

        @Override
        public boolean doesCreateBlock() {
            return true;
        }

        @Override
        public String getName() {
            return "custom-action-block-create";
        }

        @Override
        public String getShortName() {
            return "block-create";
        }

        @Override
        public String getFamilyName() {
            return "custom-action";
        }
    }

You then need to register it

.. code-block::java

   onEnable(){

        ../

        api.getActionRegistry().registerCustomAction(yourPlugin, new CustomAction());

        /...

   }

Then use the action somewhere.

.. code-block:: java

    /** On some event **/
    customHandler = new CustomHandler();
    api.handleCustomHandler(customHandler);



