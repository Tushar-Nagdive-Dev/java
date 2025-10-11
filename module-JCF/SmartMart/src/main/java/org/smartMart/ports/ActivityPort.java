package org.smartMart.ports;

import org.smartMart.domain.activity.Activity;

import java.util.List;

public interface ActivityPort {

    void record(Activity activity);

    List<Activity> latest();

    List<String> audit();
}
