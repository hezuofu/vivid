package org.vividframework.samples;

import org.vividframework.beans.annotation.ComponentScan;
import org.vividframework.boot.VividApplication;
import org.vividframework.boot.VividApplication;

@ComponentScan
public class SampleApplication {

    public static void main(String[] args) {
        VividApplication.run(SampleApplication.class, args);
    }
}
