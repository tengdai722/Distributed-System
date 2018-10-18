package edu.illinois.cs425;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Getter
@Setter
@Component
public class MembershipList {

    private String hostId;
    private List<Member> lastPinged;
    private ConcurrentSkipListMap<String, Member> membersMap;
    private LoadingCache<String, CS425Messages.Message.NodeEvent> recentUpdatesCache;

    public MembershipList() {
        membersMap = new ConcurrentSkipListMap<>();
        this.hostId = ""; //get ip through socket api and read port from appconfig
        recentUpdatesCache = CacheBuilder.newBuilder().expireAfterWrite(2, TimeUnit.SECONDS).build(new CacheLoader<String, CS425Messages.Message.NodeEvent>() {
            @Override
            public CS425Messages.Message.NodeEvent load(String s) throws Exception {
                return null;
            }
        });
    }

    public synchronized void update(CS425Messages.Message message) {
        if (message.hasData()) {
            for (CS425Messages.Message.NodeEvent event : message.getNodeEventsList()) {
                switch (event.getEvent()) {
                    case JOIN:
                        if (membersMap.containsKey(event.getId())) {
                            break;
                        }
                        membersMap.put(event.getId() + "", new Member(event.getId() + "", Instant.now()));
                        recentUpdatesCache.put(event.getId(), event);
                        break;
                    case LEAVE:
                        log.debug("UPDATE Node with id " + event.getId() + " has left the node");
                        if (!membersMap.containsKey(event.getId())) {
                            break;
                        }
                        membersMap.remove(event.getId() + "");
                        recentUpdatesCache.put(event.getId(), event);
                        break;
                    case FAIL:
                        log.debug("UPDATE Node with id " + event.getId() + " has failed");
                        if (!membersMap.containsKey(event.getId())) {
                            break;
                        }
                        membersMap.remove(event.getId() + "");
                        recentUpdatesCache.put(event.getId(), event);
                        break;
                    default:
                        log.debug("Undefined Event  " + event.getEvent());
                }
            }
        }
    }

    public synchronized void incrementLastAlive(String id) {
        Member member = membersMap.get(id);
        member.setLastAlive(Instant.now());
    }

    public synchronized List<Member> getNextNEntries(int N) {
        String current = hostId;
        List<Member> nextNMembers = new ArrayList<>();
        if (membersMap.size() <= N + 1) {
            for (Map.Entry<String, Member> member : membersMap.entrySet()) {
                if (!member.getKey().equals(hostId)) {
                    nextNMembers.add(member.getValue());
                }
            }
            return nextNMembers;
        }
        while (nextNMembers.size() < N) {
            var nextEntry = membersMap.higherEntry(current);
            if (nextEntry == null) {
                nextEntry = membersMap.firstEntry();
            }
            nextNMembers.add(nextEntry.getValue());
            current = nextEntry.getKey();
        }
        return nextNMembers;
    }

    public synchronized Member getRandomNeighbour(final List<Member> neighbours){
        return membersMap.values().stream().filter(member -> (member.getId()!=hostId && !neighbours.contains(member))).findAny().get();
    }

    public synchronized void remove(String id) {
        log.debug("FAILURE Node with id " + id + " has failed");
        System.out.println("Removing Node with id " + id );
        membersMap.remove(id);
        recentUpdatesCache.put(id, CS425Messages.Message.NodeEvent.newBuilder().setEvent(CS425Messages.Message.Event.FAIL).setId(id).build());
    }

    public synchronized Collection<CS425Messages.Message.NodeEvent> getRecentUpdates() {
        return recentUpdatesCache.asMap().values();
    }

    public synchronized void add(final String id){
        membersMap.putIfAbsent(id, new Member(id, Instant.now()));
        recentUpdatesCache.put(id, CS425Messages.Message.NodeEvent.newBuilder().setEvent(CS425Messages.Message.Event.JOIN).setId(id).build());
    }
    //used for bootstrapping
    public synchronized void addAll(final List<Member> members){
        members.forEach(member -> {
            member.setLastAlive(Instant.now());
            membersMap.putIfAbsent(member.getId(),member);
        });
    }

    public String toString(){
        StringBuilder builder = new StringBuilder("Membership List (Size "+this.getMembersMap().size()+ ") : \n");
        this.membersMap.keySet().stream().forEach(key -> builder.append(key + "\n"));
        return builder.toString();
    }
}
