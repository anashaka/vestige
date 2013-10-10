package com.googlecode.vestige.platform.test;

import java.lang.reflect.Field;
import java.net.URL;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Enumeration;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.googlecode.vestige.platform.system.VestigeSystem;
import com.googlecode.vestige.platform.system.VestigeSystemAction;

/**
 * @author gaellalire
 */
public class TestVestigeDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestVestigeDriver.class);

    public static void giveDirectStreamAccessToLogback() {
        try {
            Field streamField = ConsoleTarget.class.getDeclaredField("stream");
            streamField.setAccessible(true);
            streamField.set(ConsoleTarget.SystemOut, System.out);
            streamField.set(ConsoleTarget.SystemErr, System.err);
            streamField.setAccessible(false);

            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            ContextInitializer ci = new ContextInitializer(loggerContext);
            URL url = ci.findURLOfDefaultConfigurationFile(true);
            try {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(loggerContext);
                loggerContext.reset();
                configurator.doConfigure(url);
            } catch (JoranException je) {
                // StatusPrinter will handle this
            }
            StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
        } catch (NoSuchFieldException e) {
            LOGGER.error("Logback appender changes", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Logback appender changes", e);
        }
    }

    @Test
    @Ignore
    public void testname() throws Exception {
        // System.out.println("par ici" + ProxySelector.getDefault());
        giveDirectStreamAccessToLogback();
        new VestigeSystemAction() {

            @Override
            public void vestigeSystemRun() throws Exception {
                System.err.println("par la");
                Thread thread1 = new Thread() {

                    @Override
                    public void run() {
                        try {
                            VestigeSystem vestigeSystem = new VestigeSystem(VestigeSystem.getSystem());
                            VestigeSystem.pushSystem(vestigeSystem);
                            System.out.println(VestigeSystem.getSystem());
                            System.out.println(vestigeSystem.getWriteDrivers());
                            MockDriver driver = new MockDriver();
                            Enumeration<Driver> drivers = DriverManager.getDrivers();
                            Assert.assertFalse(drivers.hasMoreElements());
                            DriverManager.registerDriver(driver);
                            drivers = DriverManager.getDrivers();
                            Assert.assertEquals(driver, drivers.nextElement());
                            Assert.assertFalse(drivers.hasMoreElements());
                            System.out.println("OK T1");

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };
                thread1.start();
                Thread.sleep(100);
                Thread thread2 = new Thread() {

                    @Override
                    public void run() {
                        try {
                            VestigeSystem vestigeSystem = new VestigeSystem(VestigeSystem.getSystem());
                            VestigeSystem.pushSystem(vestigeSystem);
                            System.out.println(VestigeSystem.getSystem());
                            System.out.println(vestigeSystem.getWriteDrivers());
                            MockDriver driver = new MockDriver();
                            Enumeration<Driver> drivers = DriverManager.getDrivers();
                            Assert.assertFalse(drivers.hasMoreElements());
                            DriverManager.registerDriver(driver);
                            drivers = DriverManager.getDrivers();
                            Assert.assertEquals(driver, drivers.nextElement());
                            Assert.assertFalse(drivers.hasMoreElements());
                            System.out.println("OK T2");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                };
                thread2.start();
                thread1.join();
                thread2.join();
                System.out.println("pre fin");
            }
        }.execute();
        System.out.println("fin");
    }

}
