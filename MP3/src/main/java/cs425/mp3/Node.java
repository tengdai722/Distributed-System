package cs425.mp3;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class Node {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String hostName;
    private final int port = Config.UDP_PORT;
    private final int TCPPort = Config.TCP_PORT;
    private boolean isIntroducer;
    private AtomicBoolean inGroup = new AtomicBoolean(false);
    private AtomicBoolean electionAcked = new AtomicBoolean(false);
    private final long joinPeriod = Config.JOIN_PERIOD;
    private Thread receive;
    private Thread elector;
    //header for different msg types
    private final int ack = 20;
    private final int gossip = 10;
    private final int join = 0;
    private final int leave = 1;
    private final int isAlive = 2;
    private final int update = 3;
    private final int elected = 4;
    private final int election = 5;
    private final int gossipRound = Config.GOSSIP_ROUND;
    private final int electionPeriod = Config.ELECTION_PERIOD;
    private Instant lastGossipTime;
    private String leader = "";
    private ConcurrentHashMap<String, String> memberList = new ConcurrentHashMap<>();
    private Thread FD;
    private DatagramSocket ds;
    private ConcurrentHashMap<String, String> ackList = new ConcurrentHashMap<>();
    public ConcurrentHashMap<String, String> crashedNode = new ConcurrentHashMap<>();
    public AtomicBoolean isLeaderCrashed = new AtomicBoolean(false);
    public AtomicBoolean isLeaderChanged = new AtomicBoolean(false);


    public Node() throws UnknownHostException {
        this.hostName = Util.getCurrentHostname();
        this.lastGossipTime = Instant.now();
        if (hostName.equals(Config.DEFAULT_MASTER_HOSTNAME)) {
            this.isIntroducer = true;
        }
        logger.info("Hostname: {}", this.hostName);
    }


    //print id for each node
    public void printId() throws UnknownHostException {
        if (this.inGroup.get()) {
            logger.info("id:{}  $$  ip:{}  $$  timestamp:{}   ", this.hostName, Util.getIpFromHostname(this.hostName), this.memberList.get(this.hostName));
        } else {
            logger.info("join the group first");
        }
    }


    //print the current leader
    public void printLeader() {
        if (!this.leader.isEmpty()) {
            logger.info("current leader:{}", this.leader);
        } else {
            logger.info("no leader selected");
        }
    }

    //print current membership list for each node
    public void printList() throws UnknownHostException {
        if (this.inGroup.get()) {
            logger.info("Membership list of " + this.hostName + ":");
            for (String host : this.memberList.keySet()) {
                logger.info("id:{}  $$  ip:{}  $$  timestamp:{}   ", host, Util.getIpFromHostname(host), this.memberList.get(host));
            }
        } else {
            logger.error("join the group first");
        }
    }


    private Runnable receiveWorker() {
        return () -> {
            Thread.currentThread().setName("Receiver");
            byte[] buf = new byte[1024];
            try {
                this.ds = new DatagramSocket(port);
                DatagramPacket dp_receive = new DatagramPacket(buf, 1024);
                while (this.inGroup.get()) {
                    this.ds.receive(dp_receive);
                    String raw = new String(dp_receive.getData(), 0, dp_receive.getLength());
                    String[] message = raw.split("\\|");
                    if (message.length != 4) {
                        throw new RuntimeException("UDP packet error");
                    }
                    String header = message[0];
                    String content = message[1];
                    String source = Util.getHostnameFromIp(message[2]);
                    String timestamp = message[3].trim();
                    //if (! header.equals(Integer.toString(isAlive))  && ! header.equals(Integer.toString(isAlive+ack))){
                    //  logger.info("receive from <{}> with <{}>---------------------",source,raw);
                    //}
                    switch (header) {
                        case "0":
                            joinHandler(source, timestamp);
                            break;
                        case "1":
                            leaveHandler(content, timestamp);
                            break;
                        case "2":
                            isAliveHandler(source);
                            break;
                        case "5":
                            electionHandler(content);
                            break;
                        case "11":
                            gossipHandler(11, content, timestamp);
                            break;
                        case "13":
                            gossipHandler(13, content, timestamp);
                            break;
                        case "14":
                            gossipHandler(14, content, timestamp);
                            break;
                        case "21":
                            ackHandler(21, source);
                            break;
                        case "22":
                            ackHandler(22, source);
                            break;
                        case "25":
                            ackHandler(25, source);
                            break;
                        default:
                            logger.error("Received header: {}", header);
                            throw new RuntimeException("Invalid header");
                    }
                }
                this.ds.close();
            } catch (Exception e) {
                logger.error("Receiver worker error", e);
            }
        };
    }

    private Runnable FDWorker() {
        return () -> {
            Thread.currentThread().setName("failure_detector");
            while (this.inGroup.get()) {
                if (this.memberList.size() > 3) {
                    List<String> keys = new ArrayList<>(this.memberList.keySet());
                    int index = keys.indexOf(this.hostName);
                    int next1, next2, next3;
                    next1 = (index + 1) % keys.size();
                    next2 = (index + 2) % keys.size();
                    next3 = (index + 3) % keys.size();
                    this.ackList.clear();
                    this.ackList.put(keys.get(next1), "f");
                    this.ackList.put(keys.get(next2), "f");
                    this.ackList.put(keys.get(next3), "f");
                    send(keys.get(next1), this.port, isAlive, "", Instant.now().toString());
                    send(keys.get(next2), this.port, isAlive, "", Instant.now().toString());
                    send(keys.get(next3), this.port, isAlive, "", Instant.now().toString());
                    Util.noExceptionSleep(1000);
                    int i = 0;
                    for (; i < 8; i++) {
                        for (String host : this.ackList.keySet()) {
                            if (this.ackList.get(host).equals("f")) {
                                send(host, this.port, isAlive, "", Instant.now().toString());
                                // logger.info("{}th ping <{}> at <{}>", Integer.toString(i), host, Instant.now());
                            }
                        }
                        Util.noExceptionSleep(500);
                    }
                    boolean needUpdate = false;
                    for (String host : this.ackList.keySet()) {
                        if (this.ackList.get(host).equals("f")) {
                            this.memberList.remove(host);
                            logger.info("failure of <{}> detected at <{}>", host, Instant.now());
                            if (host.equals(this.leader)) {
                                logger.info("start leader election protocol");
                                election();
                            }
                            //add crashed node, avoid duplicated add
                            if (!this.crashedNode.contains(host)) {
                                this.crashedNode.put(host, "");
                                logger.info("Crashed node <{}> saved to map", host);
                            }
                            needUpdate = true;
                        }
                    }
                    if (needUpdate) {
                        gossip(gossip + update, listToString(), Instant.now().toString(), this.gossipRound);
                    }
                }
            }
        };
    }

    //bully algorithm for leader election
    private Runnable electionWorker() {
        return () -> {
            Thread.currentThread().setName("electionWorker");
            while (this.leader.equals("")) {
                this.isLeaderCrashed.set(true);
                int electionSendCnt = 0;
                for (String host : this.memberList.keySet()) {
                    if (Integer.parseInt(this.hostName.substring(15, 17)) < Integer.parseInt(host.substring(15, 17))) {
                        send(host, this.port, election, this.hostName, Instant.now().toString());
                        electionSendCnt += 1;
                    }
                }
                this.electionAcked.set(false);
                Util.noExceptionSleep(this.electionPeriod);
                if (this.electionAcked.get() && electionSendCnt != 0) {
                    Util.noExceptionSleep(this.electionPeriod);
                } else {
                    this.leader = this.hostName;
                    this.isLeaderCrashed.set(false);
                    this.isLeaderChanged.set(true);
                    logger.info("self elected at <{}>", this.hostName);
                    gossip(elected + gossip, this.leader, Instant.now().toString(), this.gossipRound);
                }
            }
            this.elector = null;
        };
    }

    public void join() {
        if (this.inGroup.get()) {
            logger.warn("already in the group");
        } else {
            if (this.receive == null) {
                this.receive = new Thread(this.receiveWorker());
                this.inGroup.set(true);
                this.receive.start();
                logger.info("receive thread start at <{}>", this.hostName);
            }
            if (this.isIntroducer) {
                this.memberList.put(this.hostName, Instant.now().toString());
                this.leader = this.hostName;
                logger.warn("introducer ready");
            } else {
                while (this.memberList.isEmpty()) {
                    send(Config.DEFAULT_MASTER_HOSTNAME, this.port, join, "", Instant.now().toString());
                    Util.noExceptionSleep(this.joinPeriod);
                }
            }
            if (this.FD == null) {
                this.FD = new Thread(this.FDWorker());
                this.FD.start();
                logger.info("failure detector thread at <{}>", this.hostName);
            }

        }
    }


    public void leave() {
        if (this.inGroup.get()) {
            this.memberList.remove(this.hostName);
            gossip(gossip + leave, this.hostName, Instant.now().toString(), this.gossipRound);
            while (!this.memberList.isEmpty()) {
                Util.noExceptionSleep(500);
                this.memberList.forEach((host, time) -> {
                    send(host, this.port, leave, this.hostName, Instant.now().toString());
                });
            }
            this.inGroup.set(false);
            this.receive = null;
            this.ds.close();
            this.FD = null;
        } else {
            System.out.println("join the group first");
        }
    }


    private void send(String host, int port, int header, String content, String requestTime) {
        try {
            String msg = String.format("%d|%s|%s|%s", header, content, Util.getIpFromHostname(this.hostName), requestTime);
            //logger.info("send to <{}> with <{}>--------------------------", host, msg);

            byte[] buf = msg.getBytes();
            DatagramSocket ds = new DatagramSocket();
            DatagramPacket DpSend = new DatagramPacket(buf, buf.length, InetAddress.getByName(host), port);
            ds.send(DpSend);
            ds.close();
        } catch (Exception e) {
            logger.error("Send error", e);
        }
    }

    // gossip message
    private void gossip(int header, String content, String requestTime, int cnt) {
        //get two random hosts from the list except for itself
        for (int i = 0; i < cnt; i++) {
            List<String> keys = new ArrayList<>(this.memberList.keySet());
            if (keys.size() < 3) {
                for (String s : keys) {
                    if (!s.equals(this.hostName)) {
                        send(s, this.port, header, content, requestTime);
                    }
                }
            } else {
                Random generator = new Random();
                int randomIndex1 = generator.nextInt(keys.size());
                int randomIndex2 = generator.nextInt(keys.size());
                while (randomIndex1 == randomIndex2 || keys.get(randomIndex1).equals(this.hostName) || keys.get(randomIndex2).equals(this.hostName)) {
                    randomIndex1 = generator.nextInt(keys.size());
                    randomIndex2 = generator.nextInt(keys.size());
                }
                String host1 = keys.get(randomIndex1);
                String host2 = keys.get(randomIndex2);
                send(host1, this.port, header, content, requestTime);
                send(host2, this.port, header, content, requestTime);
            }

        }

    }

    private String listToString() {
        if (this.memberList.isEmpty()) {
            return "";
        } else {
            List<String> copy = new ArrayList<>();
            this.memberList.forEach((host, time) -> {
                copy.add(host + "$$" + time);
            });
            return String.join(",", copy);
        }
    }

    private void joinHandler(String source, String requestTime) {
        if (this.isIntroducer) {
            logger.info("Node <{}> request join at <{}>.", source, requestTime);
            // Join it only when it's new
            if (!this.memberList.containsKey(source)) {
                this.memberList.put(source, requestTime);
                gossip(update + gossip, listToString(), Instant.now().toString(), this.gossipRound);
                gossip(elected + gossip, this.leader, Instant.now().toString(), this.gossipRound);
            }
        } else {
            logger.warn("Only introducer can let node join the group.");
        }
    }

    private void gossipHandler(int header, String content, String requestTime) {
        //leave gossip
        if (header == gossip + leave) {
            //has the leaver in the list, delete it and gossip the message
            if (this.memberList.containsKey(content)) {
                gossip(header, content, requestTime, this.gossipRound);
                leaveHandler(content, requestTime);
            }
        }
        //update gossip
        else if (header == gossip + update) {
            if (update(content, requestTime)) {
                gossip(header, content, requestTime, this.gossipRound);
            }
        }
        //elected gossip
        else if (header == gossip + elected) {
            if (!this.leader.equals(content)) {
                this.leader = content;
                this.isLeaderCrashed.set(false);
                gossip(header, content, requestTime, this.gossipRound);
            }
        }
    }

    private boolean update(String content, String requestTime) {
        String[] nodes = content.split(",");
        ConcurrentHashMap<String, String> newMap = new ConcurrentHashMap<>();
        for (String s : nodes) {
            String[] pair = s.split("\\$\\$");
            if (pair.length != 2) {
                throw new RuntimeException("update content error");
            } else {
                newMap.put(pair[0], pair[1]);
            }
        }
        if (newMap.equals(this.memberList) || this.lastGossipTime.isAfter(Instant.parse(requestTime))) {
            return false;
        } else {
            //logger.info("request update at <{}>", requestTime);
            this.memberList = newMap;
            this.lastGossipTime = Instant.parse(requestTime);
            return true;
        }
    }

    private void isAliveHandler(String source) {
        send(source, this.port, ack + isAlive, "", Instant.now().toString());
    }

    private void leaveHandler(String source, String requestTime) {
        if (this.memberList.containsKey(source)) {
            logger.info("Node <{}> request leave at <{}>.", source, requestTime);
            this.memberList.remove(source);
            send(source, this.port, ack + leave, "", Instant.now().toString());
        }
    }

    private void ackHandler(int header, String source) {
        if (header == ack + leave) {
            this.memberList.remove(source);
        } else if (header == ack + isAlive) {
            if (this.ackList.containsKey(source)) {
                this.ackList.put(source, "t");
            }
        } else if (header == ack + election) {
            if (Integer.parseInt(this.hostName.substring(15, 17)) < Integer.parseInt(source.substring(15, 17))) {
                this.electionAcked.set(true);
            }
        }
    }

    private void election() {
        if (this.elector == null) {
            this.leader = "";
            this.elector = new Thread(electionWorker());
            logger.info("election thread start at <{}>", this.hostName);
            this.elector.start();
        }
    }

    private void electionHandler(String content) {
        send(content, this.port, election + ack, "", Instant.now().toString());
        election();
    }

    public String getLeader() {
        return leader;
    }

    public String[] getNodesArray() {
        return this.memberList.keySet().toArray(new String[0]);
    }

    public ConcurrentHashMap<String, String> getMemberList() {
        return memberList;
    }

    public String getHostName() {
        return hostName;
    }
}
