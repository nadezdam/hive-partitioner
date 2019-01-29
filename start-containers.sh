#!/bin/bash

# the default node number is 1
N=2

# start hadoop slave 
N=$(( $N + 1 ))
i=1
while [ $i -lt $N ]
do
	docker rm -f hadoop-slave$i
	echo "start hadoop-slave$i container..."
	docker run -itd \
		--net=hadoop \
		--name hadoop-slave$i \
		--hostname hadoop-slave$i \
		--memory=2000m \
		--memory-swap=6000m \
		--oom-kill-disable \
		nadiam17/hadoop-base:1.0
	i=$(( $i + 1 ))
done

# start hadoop master container
docker rm -f hadoop-master
echo "start hadoop-master container..."
winpty docker run -td \
	--net=hadoop \
	-p 50070:50070 \
	-p 19888:19888 \
	-p 8088:8088 \
	-p 10002:10002 \
	-p 10000:10000 \
	-p 2222:22 \
	--name hadoop-master \
	--hostname hadoop-master \
	--memory=2000m \
	--memory-swap=6000m \
	--oom-kill-disable \
	nadiam17/hadoop-master:1.0

# copy ssh key from `hadoop-master` container
docker cp hadoop-master:/root/.ssh/id_rsa ./id_rsa.pem
