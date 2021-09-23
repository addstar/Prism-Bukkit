Configuration Default
---------------------

.. code-block::yaml

  # Prism Config : Generated: 14/3/21, 6:01 pm

    prism:
      debug: false
      preload-materials: false
      paste:
        enable: true
        api-key: API key from http://paste.gg
      wand:
        default-mode: HAND
        default-mode-item: STICK
        default-mode-block: SPRUCE_LOG
        auto-equip: true
        allow-user-override: true
        inspect-ignore-actions:
        - PLAYER_CHAT
        - PLAYER_COMMAND
        - PLAYER_JOIN
        - PLAYER_QUIT
      queries:
        default-radius: 5
        default-time-since: 2d
        max-lookup-radius: 100
        max-applier-radius: 100
        never-use-defaults: false
        lookup-max-results: 1000
        default-results-per-page: 5
        lookup-auto-group: true
        always-show-extended: false
      near:
        default-radius: 5
        max-results: 100
        max-radius: 100
      drain:
        default-radius: 5
        max-results: 100
        max-radius: 10
      ex:
        default-radius: 5
        max-results: 100
        max-radius: 10
      ignore:
        enable-perm-nodes: false
        players-in-creative: false
        players: []
        players_whitelist: false
        worlds: []
        worlds_whitelist: false
      purge:
        rules:
        - before:8w
        - a:water-flow before:4w
        batch-tick-delay: 30
        records-per-batch: 10000
      applier:
        notify-nearby-enabled: true
        notify-nearby-additional-radius: 20
        remove-fire-on-burn-rollback: true
        remove-drops-on-explode-rollback: true
        never-spawn-entity:
        - CREEPER
        never-place-block:
        - WATER
        - LAVA
        allow-rollback-items-removed-from-container: true
      tracking:
        trackers:
          ITEM_REMOVE: true
          LEAF_DECAY: true
          ENTITY_UNLEASH: true
          WATER_BREAK: true
          SHEEP_EAT: true
          MUSHROOM_GROW: true
          PRISM_RESTORE: true
          PLAYER_COMMAND: false
          BLOCK_SPREAD: true
          PRISM_DELETE: true
          ENTITY_BREAK: true
          ENTITY_EXPLODE: true
          ENCHANT_ITEM: false
          ITEM_INSERT: true
          PLAYER_JOIN: false
          CUSTOM_ACTION: true
          CONTAINER_ACCESS: true
          TREE_GROW: true
          SPAWNEGG_USE: true
          BLOCK_USE: true
          VEHICLE_PLACE: true
          BLOCK_FALL: true
          TNT_EXPLODE: true
          PLAYER_DEATH: true
          ENTITY_KILL: true
          ITEM_RECEIVE: true
          CROP_TRAMPLE: true
          BLOCK_BREAK: true
          ITEM_BREAK: false
          HANGINGITEM_PLACE: true
          ENDERMAN_PLACE: true
          CRAFT_ITEM: false
          ITEM_ROTATE: true
          LAVA_FLOW: true
          PORTAL_CREATE: true
          WATER_BUCKET: true
          BUCKET_FILL: true
          BLOCK_DISPENSE: true
          VEHICLE_EXIT: true
          PLAYER_QUIT: false
          PLAYER_KILL: true
          BED_EXPLODE: true
          BLOCK_PLACE: true
          LAVA_BUCKET: true
          PRISM_ROLLBACK: true
          ITEM_PICKUP: true
          ENTITY_SHEAR: true
          PRISM_UNDO: true
          BLOCK_FADE: true
          ENDERMAN_PICKUP: true
          PRISM_EXTINGUISH: true
          CREEPER_EXPLODE: true
          LAVA_BREAK: true
          BLOCK_SHIFT: true
          PLAYER_CHAT: false
          PLAYER_TELEPORT: false
          POTION_SPLASH: true
          VEHICLE_ENTER: true
          DRAGON_EAT: true
          BLOCK_BURN: true
          WATER_FLOW: false
          ENTITY_DYE: false
          HANGINGITEM_BREAK: true
          PLAYER_TRADE: false
          WORLD_EDIT: false
          ITEM_DROP: true
          FIRE_SPREAD: false
          ENTITY_FOLLOW: true
          PRISM_DRAIN: true
          VEHICLE_BREAK: true
          FIREWORK_LAUNCH: true
          BONEMEAL_USE: true
          PRISM_PROCESS: true
          ENTITY_LEASH: true
          ENTITY_SPAWN: true
          SIGN_CHANGE: true
          TNT_PRIME: true
          XP_PICKUP: false
          CAKE_EAT: true
          ENTITY_FORM: true
          BLOCK_FORM: true
          PLAYER_GAMEMODECHANGE: false
          TARGET_HIT: false
          LAVA_IGNITE: true
        player-ip-on-join: false
        hopper-item-events: false
        api-enabled: true
        api-allowed-plugins: []
      do-not-track-commands:
      - vanish
      - v
      - login
      - changepassword
      - register
      - unregister
      alerts:
        alert-staff-to-applied-process: true
        alert-player-about-self: true
        ores:
          blocks:
            IRON_ORE: '#444444'
            GOLD_ORE: '#ffe17d'
            LAPIS_ORE: '#0670cc'
            DIAMOND_ORE: '#04babd'
            EMERALD_ORE: '#21bf60'
            NETHER_GOLD_ORE: '#ff7308'
            ANCIENT_DEBRIS: '#856d3e'
          enabled: true
          log-to-console: true
          log-commands:
          - examplecommand <alert>
        illegal-commands:
          commands:
          - op
          - deop
          - stop
          - reload
          - bukkit:op
          - bukkit:deop
          - bukkit:stop
          - bukkit:reload
          - minecraft:op
          - minecraft:deop
          - minecraft:stop
          - minecraft:reload
          enabled: false
          log-to-console: true
          log-commands:
          - examplecommand <alert>
        uses:
          lighter: true
          lava: true
          item-placement:
          - BEDROCK
          - STICKY_PISTON
          - TNT
          - LAVA
          item-break:
          - BEDROCK
          - STICKY_PISTON
          - TNT
          - LAVA
          ignore-staff: false
          enabled: true
          log-to-console: true
          log-commands:
          - examplecommand <alert>
        vanilla-xray-enabled: true
      queue:
        empty-tick-delay: 3
        max-failures-before-wait: 5
        actions-per-insert-batch: 1000
        force-write-queue-on-shutdown: true
    datasource:
      type: derby
      properties:
        database-name: derby
        user-name: PRISM
        password: PRISM
        prefix: prism_

