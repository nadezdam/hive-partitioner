FROM nadiam17/hadoop-base:1.0

MAINTAINER nadiam17 <nadezdamilojkovic@gmail.com>

WORKDIR /root

# install the rest of deps
RUN apt-get update && apt-get install -y expect vim libmysql-java

RUN DEBIAN_FRONTEND=noninteractive apt-get install -y mysql-server
#RUN expect -c 'spawn apt-get install -y mysql-server; sleep 5; expect { "*New password for the MySQL*" { send "root\rroot\r" } }'

#install hive
RUN wget http://archive.apache.org/dist/hive/hive-2.1.0/apache-hive-2.1.0-bin.tar.gz && \
	tar -xzf apache-hive-2.1.0-bin.tar.gz && \
	mv apache-hive-2.1.0-bin /usr/local/hive && \
	rm apache-hive-2.1.0-bin.tar.gz

# set environment variable
ENV PATH=$PATH:/usr/local/hive/bin
ENV HIVE_HOME=/usr/local/hive 


COPY config/* /tmp/

RUN mv /tmp/slaves $HADOOP_HOME/etc/hadoop/slaves && \
    mv /tmp/start-all.sh ~/start-all.sh && \
    mv /tmp/run-wordcount.sh ~/run-wordcount.sh && \
	mv /tmp/hive-env.sh $HIVE_HOME/conf/hive-env.sh && \
	mv /tmp/hive-site.xml $HIVE_HOME/conf/hive-site.xml && \
	mv /tmp/expect-start-sql.exp ~/expect-start-sql.exp && \
	mv /tmp/expect-set-root-password.exp ~/expect-set-root-password.exp && \
	mv /tmp/parking-citations-preprocessed.tsv ~/parking-citations-preprocessed.tsv && \
	dos2unix $HADOOP_HOME/etc/hadoop/slaves && \
    dos2unix ~/start-all.sh && \
    dos2unix ~/run-wordcount.sh && \
	dos2unix $HIVE_HOME/conf/hive-env.sh && \
	dos2unix ~/expect-start-sql.exp && \
	dos2unix ~/expect-set-root-password.exp

RUN chmod +x ~/start-all.sh && \
    chmod +x ~/run-wordcount.sh && \
	chmod +x ~/expect-start-sql.exp && \
	chmod +x ~/expect-set-root-password.exp && \
    chmod +x $HADOOP_HOME/sbin/start-dfs.sh && \
    chmod +x $HADOOP_HOME/sbin/start-yarn.sh

# format namenode
RUN /usr/local/hadoop/bin/hdfs namenode -format

RUN ln -s /usr/share/java/mysql-connector-java.jar $HIVE_HOME/lib/mysql-connector-java.jar

RUN rm $HIVE_HOME/lib/log4j-slf4j-impl-2.4.1.jar
	
CMD ["sh", "-c", "service ssh start; ~/start-all.sh; bash"]

