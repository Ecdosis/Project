#!/bin/bash
if [ ! -d project ]; then
  mkdir project
  if [ $? -ne 0 ] ; then
    echo "couldn't create project directory"
    exit
  fi
fi
if [ ! -d project/WEB-INF ]; then
  mkdir project/WEB-INF
  if [ $? -ne 0 ] ; then
    echo "couldn't create project/WEB-INF directory"
    exit
  fi
fi
if [ ! -d project/WEB-INF/lib ]; then
  mkdir project/WEB-INF/lib
  if [ $? -ne 0 ] ; then
    echo "couldn't create project/WEB-INF/lib directory"
    exit
  fi
fi
rm -f project/WEB-INF/lib/*.jar
cp dist/Project.jar project/WEB-INF/lib/
cp lib/*.jar project/WEB-INF/lib/
cp web.xml project/WEB-INF/
jar cf project.war -C project WEB-INF 
echo "NB: you MUST copy the contents of tomcat-bin to \$tomcat_home/bin"
