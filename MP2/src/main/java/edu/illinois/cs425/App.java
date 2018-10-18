package edu.illinois.cs425;

import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import spring.SpringConfig;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 */
@Slf4j
public class App {
    private static Server server;
    private static PingWorker pingWorker;
    private static Introducer introducer;
    private static MembershipList membershipList;

    public static void main(String[] args) throws Exception {
        final ApplicationContext ctx = new AnnotationConfigApplicationContext(SpringConfig.class);
        server = ctx.getBean(Server.class);
        pingWorker = ctx.getBean(PingWorker.class);
        membershipList = ctx.getBean(MembershipList.class);
        var inGroup = false;

        final String introducerHostId = ctx.getEnvironment().getProperty("introducerHostId");
        final int port = Integer.valueOf(ctx.getEnvironment().getProperty("introducerPort"));

        System.out.println(introducerHostId + " " + port);

        final Thread serverThread = new Thread(server);
        final Thread pingThread = new Thread(pingWorker);

        if (introducerHostId.equals(InetAddress.getLocalHost().getHostName())) {
            introducer = ctx.getBean(Introducer.class);
            final Thread introducerThread = new Thread(introducer);

            var hostId = Introducer.createId(InetAddress.getLocalHost().getHostAddress());
            membershipList.setHostId(hostId);
            membershipList.add(hostId);
            serverThread.start();
            pingThread.start();
            introducerThread.start();
        }

        var message = "Please select an option from 1 -4: \n" +
                "1- List the membership list\n" +
                "2 - List self's id\n" +
                "3 - Join the group\n" +
                "4 - Voluntarily leave the group (different from a failure)";


        int option;

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println(message);
                String userInput = reader.readLine();
                if (userInput == null)
                    break;
                option = Integer.parseInt(userInput);
                switch (option) {
                    case 1:
                        System.out.println(membershipList);
                        break;
                    case 2:
                        System.out.println("SystemId: " + membershipList.getHostId());
                        break;
                    case 3:
                        if (inGroup) {
                            break;
                        }
                        //TODO send info to introducer before starting pinging.
                        inGroup = true;
                        Socket socket = new Socket(introducerHostId, port);
                        ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                        IntroducerMembershipDetails details = (IntroducerMembershipDetails) inputStream.readObject();
                        log.debug("Joining group with id " + details.getId());
                        membershipList.setHostId(details.getId());
                        membershipList.addAll(details.getMembersList());
                        pingThread.start();
                        serverThread.start();
                        break;
                    case 4:
                        //TODO leave logic
                        if (inGroup) {
                            server.setLeave(true);
                            pingWorker.setLeave(true);
                            inGroup = false;
                        }
                        serverThread.join();
                        pingThread.join();
                        System.exit(0);
                        break;
                    default:
                        System.out.println("Invalid input");
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
