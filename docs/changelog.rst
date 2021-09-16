ChangeLog
=========

New Features
^^^^^^^^^^^^

- Internationalization of most messages - submit language files on github
- Highlight colours are customizable.
- Banners now store the pattern and painting should store the Art type.
- Bed Explosions should be tracked
- Help message provide clickable links to this wiki.
- Improved entity serializations.  this is a major change and will need testing
- Save shulker box content - (watch for very large contents that might exceed db storage max (32000 bytes)
- These docs!
- New Paper specific event handlers.
- Add Hikari db connector
- Major db structure rewrite.
- Add support for Apache Derby
- Create new Prism-Api
- Paste results are clickable
- Added testing framework and integration framework
- Database access is now inside the db classes modularising prism further.
- A new Task manager handles all async task execution rather than bukkit.
- Move to Configurate to manage configuration files
- Improve debug driver class reporting.



Bug Fixes
^^^^^^^^^

- Fix NPE on command registration when the plugin fails to install
- Player properly logged on EntityBlockFormEvent.
- Fix NPE when a handler has no source.
- Fix double beacon bug(#229)
- Fix Purge command handling
- Fix to Ore Naming
- Name handling improved in handlers
- Fix to vanilla potions
- Fix to player name change bugs
- Builds now include jdk 11
- Fix `-overwrite` so it doesnt skip as often
- Remove sensitive data from debug paste.
- Remove extensive use of static methods.



Deprecations
^^^^^^^^^^^^

Removals
^^^^^^^^

- Travis has been retired

Dependency Changes
^^^^^^^^^^^^^^^^^^

- Adventure library updated to 4.2.0
- Junit5
- Derby 10.14.2
- Paper 1.16.5


