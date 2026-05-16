package org.vividframework.samples;

import org.vividframework.beans.annotation.Service;

@Service
public class GreetingService {

    public GreetingDto greet(String name) {
        return new GreetingDto("Hello, " + name + "!");
    }
}
