package org.vividframework.samples;

import org.vividframework.beans.annotation.ComponentScan;
import org.vividframework.boot.SpringApplication;
import org.vividframework.boot.VividApplication;

@ComponentScan
public class SampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleApplication.class, args);
    }
}
