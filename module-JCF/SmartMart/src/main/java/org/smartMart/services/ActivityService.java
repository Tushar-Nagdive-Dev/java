package org.smartMart.services;

import org.smartMart.domain.activity.Activity;
import java.util.*;

public final class ActivityService {
    private final Deque<Activity> recent = new ArrayDeque<>();
    private final int limit; private final Set<String> auditOrderedUnique = new LinkedHashSet<>();

    public ActivityService(int limit){
        this.limit = limit;
    }

    public void record(Activity a){
        recent.addFirst(a);
        if (recent.size() > limit) recent.removeLast();
        auditOrderedUnique.add(a.userId()+"|"+a.action()+"|"+a.at());
    }
    public List<Activity> latest(){
        return List.copyOf(recent);
    }

    public List<String> audit(){
        return List.copyOf(auditOrderedUnique);
    }
}
