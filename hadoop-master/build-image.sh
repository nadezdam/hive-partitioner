#!/bin/bash

echo ""

echo -e "\nbuild docker hadoop image\n"
docker build --force-rm -t nadiam17/hadoop-master:1.0 .

echo ""
