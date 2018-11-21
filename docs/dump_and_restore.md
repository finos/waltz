# Taking and restoring database backups

Status: DRAFT

## Creating Backups Example
````
 mysqldump -u root demodb > dump.sql
````
On linux you may need to modify `/usr/local/etc/my.cnf`. 


## Restoring Backup Example

````
 mysql -u root demodb < dump.sql
````
