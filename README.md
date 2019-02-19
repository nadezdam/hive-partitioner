1. Klonirati hive-partitioner repozitorijum:
	git clone https://github.com/nadezdam/hive-partitioner.git
2. Kreirati hadoop mrezu izvršenjem sledeće skripte:
	./create-network.sh
3. Izbildovati docker image hadoop-base i hadoop-master:
	./hadoop-base/build-image.sh
	./hadoop-master/build-image.sh
4. Pokrenuti Hadoop klaster
	./start-containers.sh

5. Izvršiti program hive-partitioner sa sledećim parametrima:
	-putanja do fajla sa naredbama za kreiranje baze i tabele;
	-naziv fajla sa podacima (fajl je prethodno učitan u master kontejner na root lokaciji)
	-putanja do fajla sa test upitima
	
Web interfejsi:

* NameNode: http://localhost:50070
* ApplicationManager: http://localhost:8088
* JobHistory: http://localhost:19888
* HiveServer2: http://localhost:10002