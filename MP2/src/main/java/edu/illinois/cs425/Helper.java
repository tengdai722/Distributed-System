package edu.illinois.cs425;

import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Helper {

    /**
     * Adds recent updates to Message.
     * @param builder
     * @return
     */
    @Autowired
    private  MembershipList membershipList;

    public  CS425Messages.Message.Builder addEventUpdates(CS425Messages.Message.Builder builder){
        var recentUpdates = membershipList.getRecentUpdates();
        builder.setData(false);
        if(recentUpdates.size() > 0){
            builder.setData(true);
            builder.addAllNodeEvents(recentUpdates);
        }
        return builder;
    }
}
