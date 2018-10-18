#!/bin/bash

#Login into 10 hosts and setup the server
HOSTS=()
FILES=()

filePrefix="vm"
fileExtension=".log"
hostPrefix="anain2@fa18-cs425-g02-"
hostPostFix=".cs.illinois.edu"
directory=":~"
zero=0

for i in `seq 1 2`;
        do
        	if [ $i -eq 10 ]
				then
					HOSTS+=($hostPrefix$i$hostPostFix)
				else
					HOSTS+=($hostPrefix$zero$i$hostPostFix)
			fi
			FILES+=($filePrefix$i$fileExtension)
        done

jarFile="cs425.jar"
appConfig="mp2AppConfig"

for i in `seq 0 $((${#HOSTS[@]}-1))`;
	do
		# echo "Copying"  ${FILES[i]} ${HOSTS[i]} $directory
		#Copy log files to host machine
		# echo ${HOSTS[i]}$directory

		echo "Copying Files to "${HOSTS[i]}

		#Kill the host server
		ssh ${HOSTS[i]} 'kill -9 $(/usr/sbin/lsof -t -i:8090 -sTCP:LISTEN)'
		
		# echo ${FILES[i]} ${HOSTS[i]}$directory
		# scp ${FILES[i]} ${HOSTS[i]}$directory

		scp $appConfig ${HOSTS[i]}$directory
		# ssh ${HOSTS[i]} "echo $'\nlogFileName="${FILES[i]}"'>>appConfig"

		#copy the jar in the host
		scp $jarFile ${HOSTS[i]}$directory

		#Run the host server
		# ssh ${HOSTS[i]} "java -Xmx1G -cp cs425.jar edu.illinois.cs425.Server "${FILES[i]}" &>/dev/null &"

	done