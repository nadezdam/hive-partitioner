To properly configure `hadoop-master`, add file `library-collection-inventory.csv` to `hadoop-master/config` folder and rebuild `hadoop-master` docker image.

The `id_rsa.pem` is a private key generated during `hadoop-base` image building that can be used for ssh-ing into the machine.
