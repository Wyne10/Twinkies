# Twinkies
![GitHub](https://img.shields.io/badge/Version-1.2-green)
![GitHub](https://img.shields.io/badge/Paper-1.19.2%2B-lightgrey)
![GitHub](https://img.shields.io/github/license/Wyne10/Twinkies)

Twinkies is a small plugin for Paper 1.19.2+ that will help you to keep track of player's twinks.

## Twnkies is not translated to English right now. Translation will be available later but you can still use Russian version or translate it yourself.

## Requirements
 - Paper 1.19.2+
 - PlaceholderAPI

## Features
 - Saving nicknames and IP history
 - Sending notifications to admins
 - Individual notification settings for each administrator
 - Full logging of notifications and plugin messages and writing to log file
 - Detection of nick and IP change, as well as determination of duplicate nicknames and IPs
 - Ability to manipulate saved player data via commands
 - Logging, saving data, executing commands, sending notifications are processed in different threads
 - Ability to predict similar player nicknames


## Permissions
- twinkies.*
- twinkies.notifications: Allows player to receive notifications and change notifications settings
- twinkies.playerData: Allows player to lookup saved player data
   - twinkies.playerDataMod: Allows player to delete saved player data
- twinkies.logging: Allows player to change logging settings (However to make settings persist after plugin reload you should change them directly in config file)
- twinkies.bypass: Will not send notifications about twinks of this player

## Commands
- twinkies
- - twinkies reload - Reload config (Available only to OP)
- - twinkies logging [logName] - Logging settings (twinkies.logging)
- - twinkies notif [notifName] - Notifications settings (twinkies.notifications)
- - twinkies data
- - - twinkies data search - Search twinks of all players
- - - twinkies data player
- - - - twinkies data player [playerNick] - Show saved information about player [playerNick]
- - - - twinkies data player [playerNick] delete - Delete all information about player [playerNick]
- - - - twinkies data player [playerNick] nick [nick] - Search twinks of player [playerNick] by [nick]
- - - - twinkies data player [playerNick] nick [nick] delete - Delete saved [nick] of player [playerNick]
- - - - twinkies data player [playerNick] ip [ip] - Search twinks of player [playerNick] by [ip]
- - - - twinkies data player [playerNick] ip [ip] delete - Delete saved [ip] of player [playerNick]
- - - - twinkies data player [playerNick] search - Search twinks of player [playerNick] by all saved nicknames and IPs
- - - twinkies data nick
- - - - twinkies data nick [nick] - Search players that used [nick]
- - - twinkies data ip
- - - - twinkies data ip [ip] - Search players that used [ip]
