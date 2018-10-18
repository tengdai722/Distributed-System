#!/bin/bash

# Kill all server

ssh -i ~/vm yuyaow3@fa18-cs425-g20-01.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-02.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-03.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-04.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-05.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-06.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-07.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-08.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-09.cs.illinois.edu pkill java &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-10.cs.illinois.edu pkill java &

# Then start new

ssh -i ~/vm yuyaow3@fa18-cs425-g20-01.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-02.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-03.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-04.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-05.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-06.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-07.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-08.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-09.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
ssh -i ~/vm yuyaow3@fa18-cs425-g20-10.cs.illinois.edu java -jar '/home/yuyaow3/CS425-1.0-jar-with-dependencies.jar' s &
