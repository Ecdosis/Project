#!/bin/bash
service tomcat6 stop
cp project.war /var/lib/tomcat6/webapps/
rm -rf /var/lib/tomcat6/webapps/project
rm -rf /var/lib/tomcat6/work/Catalina/localhost/
service tomcat6 start
