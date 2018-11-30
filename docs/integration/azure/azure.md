# Azure Deployment


<!-- toc -->

<!-- tocstop -->

## Status: DEVELOPMENT

This guide is currently being written.  It will almost contain inaccuracies.  PR's welcome.


## Overview

This guide will walk users through a basic setup of Waltz on the Microsoft Azure 
cloud platform.


## Prerequisites

I will assume:

- you have an active Azure account (trial subscription is fine)
- you have a basic understanding of Java/Maven/Tomcat


## Installation

We need:
- Webapp container
- Database


Creating a web-app, using tomcat 8.5

Add database

Add elastic pool

Setup FTP/SFTP using the details given on the app page (`FTP/deployment username`)
and a password from the `deployment center/ftp/user credentials` page.

Copy the war file into the `<ftpsite>/site/wwwroot/bin/apache-tomcat-8.5.24/webapps`

**TO BE IMPROVED:** Wait for it to expand then copy a `waltz.properties` file into the expanded war.

If using sample data generators you will need to ensure that a person has the admin user name


