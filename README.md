1. Za izvršenje je potreban Docker for Windows

2. Klonirati hive-partitioner repozitorijum:
	* `git clone https://github.com/nadezdam/hive-partitioner.git`

3. Kreirati hadoop mrezu izvršenjem sledeće skripte:
	* `./create-network.sh`

4. Dodati fajl sa test podacima u `./hadoop-master/config` folder

5. Izbildovati docker image hadoop-base i hadoop-master:
	* `cd ./hadoop-base && ./build-image.sh`
	* `cd ./hadoop-master && ./build-image.sh`

6. Pokrenuti Hadoop klaster:
	* `./start-containers.sh`

7. Izvršiti program hive-partitioner sa sledećim parametrima:
	* putanja do fajla sa naredbama za kreiranje baze i tabele;
	* naziv fajla sa podacima (fajl je prethodno učitan u master kontejner na root lokaciji)
	* putanja do fajla sa test upitima
	
Web interfejsi:

* NameNode: http://localhost:50070
* ApplicationManager: http://localhost:8088
* JobHistory: http://localhost:19888
* HiveServer2: http://localhost:10002