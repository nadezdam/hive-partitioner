#!/bin/bash

# setup mysql

find /var/lib/mysql -type f -exec touch {} \;
service mysql start

TEMP_USER=$(grep 'user' /etc/mysql/debian.cnf | awk '{ print $NF }' | sed -n 1p)
TEMP_PASS=$(grep 'password' /etc/mysql/debian.cnf | awk '{ print $NF }' | sed -n 1p)

cd /usr/local/hive/scripts/metastore/upgrade/mysql

~/expect-set-root-password.exp $TEMP_USER $TEMP_PASS
~/expect-start-sql.exp

cd ~


echo -e "\n"

#start hadoop

echo -e "\n"

$HADOOP_HOME/sbin/start-dfs.sh

echo -e "\n"

$HADOOP_HOME/sbin/start-yarn.sh

echo -e "\n"

#make hive dirs in HDFS

hdfs dfs -mkdir -p /user/hive/warehouse
hdfs dfs -mkdir /tmp
hdfs dfs -chmod g+w /user/hive/warehouse
hdfs dfs -chmod g+w /tmp

#Start jobhistory-daemon
$HADOOP_HOME/sbin/mr-jobhistory-daemon.sh start historyserver

echo -e "\n"


#start HiveServer2

hive --service hiveserver2



