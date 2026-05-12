package org.vividframework.boot;

import org.vividframework.context.GenericApplicationContext;
import org.vividframework.beans.DefaultListableBeanFactory;
import org.vividframework.web.DispatcherHandler;
import org.vividframework.http.server.NettyHttpServer;

import java.util.Properties;

/**
 * Vivid Application entry point
 * @author Jon Fisher
 */
public class VividApplication {

    public static void run(Class<?> primarySource, String... args) {
        SpringApplication application = new SpringApplication(primarySource);
        application.run(args);
    }

    public static GenericApplicationContext run(Class<?> primarySource, Properties properties, String... args) {
        SpringApplication application = new SpringApplication(primarySource);
        application.setDefaultProperties(properties);
        return (GenericApplicationContext) application.run(args);
    }
}
