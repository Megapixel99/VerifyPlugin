# VerifyPlugin

## Purpose of this Plugin

To make it harder for random people to come on a private Minecraft server
and destroy it (or take it over) this plugin sets the default GameMode to
spectator and requires that players verify who they are with a server which
can be found here: https://github.com/Megapixel99/VerifyPluginServer.

List of commands:
/verify
/reset
/mute
/unmute
/help

List of permissions:
reset: Allows reset command
mute: Allows mute and unmute command

## System (environment) properties

To ensure the plugin builds successfully you will need to change the following in `verify.java`:
* configure the server host to your host and port
* set the help verbiage to whatever you call your players
(i.e. if your player base is university students the help verbiage should be
  set to "you are a university student")
* configure the name of your world file

## How to build this plugin

This plugin was developed and built with NetBeans. A tutorial on how to do
this can be found here:

https://www.spigotmc.org/wiki/spigot-plugin-development/

If you wish to use an IDE other that NetBeans please see the
"Creating a blank plugin" section here:

https://www.spigotmc.org/wiki/spigot-plugin-development/

## License
```
Copyright 2020 Seth Wheeler

Licensed under the MIT License.
```
