# PermissionTrial

**PermissionTrial** is a simple permissions management plugin with features like group prefixes, inheritance, and a powerful command structure.

## Features
- **Group Prefixes**: Assign custom prefixes to groups.
- **Inheritance**: Groups can inherit permissions from other groups.
- **Command Management**: Create and assign groups entirely in-game via commands, no configuration files needed.
- **Automatic Saving**: Data will be uploaded periodically and when the plugin closes to prevent lost data.

## Command Structure

### Group Management
- **Permission**: *permissions.commands.group*
- `/groupperms group create GROUP_NAME`  
  *Creates a new group.*
  
- `/groupperms group remove GROUP_NAME`  
  *Removes the group.*

- `/groupperms group setprefix GROUP_NAME prefix`  
  *Sets the prefix for a group.*

- `/groupperms group addperm GROUP_NAME permission.uwu`  
  *Adds a permission to the group.*

- `/groupperms group remperm GROUP_NAME permission.uwu`  
  *Removes a permission from the group.*

- `/groupperms group addinherit GROUP_NAME inheritGroup`  
  *Adds inheritance from another group, providing all of its permissions as well.*

- `/groupperms group reminherit GROUP_NAME inheritGroup`  
  *Removes inheritance from another group.*

- `/groupperms group setdefault GROUP_NAME`  
  *Sets this group as the default for new players who join.*

### Player Management
- **Permission**: *permissions.commands.player*
- `/groupperms player setgroup [player] [group]`  
- `/groupperms player setgroup [player] [group] <expiration> <reverts to this group>`  
  *Assigns a player to a group with optional expiration and the ability to select the new group post-expiration.*

### Query and Info
- `/groupperms query`  
  *Displays the player's current group and status.*
  *permissions.commands.query*

- `/groupperms info`  
  *Displays detailed information about a group.*
  *permissions.commands.info*

- `/groupperms save`  
  *Triggers a manual save.*
  *permissions.commands.save*