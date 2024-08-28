* [About](#about)
* [Enabling](#enabling)
* [Configuration changes](#configuration-changes)
* [Data conversion](#data-conversion)
    * [Downloading from MySQL to files](#downloading-from-mysql-to-files)
    * [Uploading from files to MySQL](#uploading-from-files-to-mysql)

# About
By default, groups and users are stored in separate files (groups.yml and users.yml).  
Enabling MySQL will use it as storage of groups and users instead of the files.

playerdata.yml file (for toggling scoreboard/bossbar and remembering decision) is not affected and will still be used instead of MySQL. It may be added in the future.

All tables from TAB are prefixed with `tab_`, currently `tab_groups` for groups and `tab_users` for users.

# Enabling
Find this section in **config.yml**:
```
mysql:
  enabled: false
  host: 127.0.0.1
  port: 3306
  database: tab
  username: user
  password: password
  useSSL: true
```
Enabling it will enable MySQL. The other settings are pretty straight forward.

# Configuration changes
Once enabled, configured values in groups.yml and users.yml will not be visible anymore, which is expected, because your database is empty when created. This is not a bug. All you need to do is [upload your data to the database](#uploading-from-files-to-mysql).

Since groups.yml and users.yml files will no longer be used, making any changes to them will take no effect. If you wish to perform any changes, you have 2 options:
* Use in-game commands, which will upload changes to the database
* Upload the file to database after every change (will not work for removing though) (useful when enabling for the first time).

# Data conversion
## Downloading from MySQL to files
This can be done using `/tab mysql download` command (permission is `tab.mysql.download`). It will download all data from the MySQL and save into groups.yml / users.yml.  
This command requires the MySQL function enabled.  
Existing data in files will not intentionally be deleted, but may get overwritten.

## Uploading from files to MySQL
This can be done using `/tab mysql upload` command (permission is `tab.mysql.upload`). It will upload all content from groups.yml and users.yml into MySQL.  
This command requires the MySQL function enabled.  
Existing data in database will not intentionally be deleted, but may get overwritten.  