package edu.illinois.cs425;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.stream.Collectors;

@Component
@Getter
@Setter
@Slf4j
public class Introducer implements Runnable{

    private final ServerSocket serverSocket;
    @Autowired
    private MembershipList membershipList;
    @Value("${introducerPort}")
    int port;

    public Introducer(@Value("${introducerPort}") int port) throws Exception {
        log.debug("Creating intoducer on port: "+ port);
        this.serverSocket = new ServerSocket(port);
    }

    //Current IpAddressOfMachine + TimeStamp
    public static String createId(final String ipAddress) {
        return new StringBuilder()
                .append(ipAddress)
                .append(":")
                .append(Instant.now().getEpochSecond())
                .toString();
    }

    public void handle(final Socket introducerSocket) {
        try (final ObjectOutputStream writer = new ObjectOutputStream(introducerSocket.getOutputStream())) {
            log.debug("Join request from "+introducerSocket.getInetAddress().getHostAddress());
            var nodeid = createId(introducerSocket.getInetAddress().getHostAddress());
            membershipList.add(nodeid);
            System.out.println("Adding nodeId: "+ nodeid + " Membership Size: " + membershipList.getMembersMap().size());
            writer.writeObject(
                    IntroducerMembershipDetails.builder()
                            .id(nodeid)
                            .membersList(membershipList.getMembersMap().values()
                                    .parallelStream().collect(Collectors.toList()))
                            .build());
            //introducerSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run(){
        log.debug("Introducer listening for new join requests");
        while (true) {
            try {
                System.out.println("Waiting for call on port: " + port);
                this.handle(serverSocket.accept());
            } catch (final Exception e){
                e.printStackTrace();
            }
        }
    }
}
