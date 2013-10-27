/*
 * This file is part of Vestige.
 *
 * Vestige is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Vestige is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Vestige.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.googlecode.vestige.edition.standard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.sshd.SshServer;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.util.ContextInitializer;
import ch.qos.logback.core.joran.spi.ConsoleTarget;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

import com.googlecode.vestige.application.ApplicationDescriptorFactory;
import com.googlecode.vestige.application.ApplicationException;
import com.googlecode.vestige.application.DefaultApplicationManager;
import com.googlecode.vestige.application.SynchronizedApplicationManager;
import com.googlecode.vestige.application.descriptor.xml.XMLApplicationDescriptorFactory;
import com.googlecode.vestige.core.VestigeExecutor;
import com.googlecode.vestige.edition.standard.schema.Admin;
import com.googlecode.vestige.edition.standard.schema.ObjectFactory;
import com.googlecode.vestige.edition.standard.schema.SSH;
import com.googlecode.vestige.edition.standard.schema.Settings;
import com.googlecode.vestige.edition.standard.schema.Web;
import com.googlecode.vestige.platform.DefaultVestigePlatform;
import com.googlecode.vestige.platform.VestigePlatform;
import com.googlecode.vestige.platform.system.JVMVestigeSystemActionExecutor;
import com.googlecode.vestige.platform.system.PrivateVestigePolicy;
import com.googlecode.vestige.platform.system.PrivateVestigeSecurityManager;
import com.googlecode.vestige.platform.system.PrivateWhiteListVestigePolicy;
import com.googlecode.vestige.platform.system.PublicVestigeSystem;
import com.googlecode.vestige.platform.system.VestigeSystemAction;
import com.googlecode.vestige.platform.system.VestigeSystemActionExecutor;
import com.googlecode.vestige.resolver.maven.MavenArtifactResolver;

/**
 * @author Gael Lalire
 */
public class StandardEditionVestige implements VestigeSystemAction, Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(StandardEditionVestige.class);

    private VestigePlatform vestigePlatform;

    private DefaultApplicationManager defaultApplicationManager;

    private SshServer sshServer;

    private Server webServer;

    private VestigeExecutor vestigeExecutor;

    private Thread workerThread;

    private ApplicationDescriptorFactory applicationDescriptorFactory;

    private File resolverFile;

    private VestigeSystemActionExecutor vestigeSystemActionExecutor;

    private boolean securityEnabled;

    @SuppressWarnings("unchecked")
    public StandardEditionVestige(final File homeFile, final File baseFile, final VestigeExecutor vestigeExecutor, final VestigePlatform vestigePlatform) throws Exception {
        this.vestigeExecutor = vestigeExecutor;
        this.vestigePlatform = vestigePlatform;

        File settingsFile = new File(baseFile, "settings.xml");
        if (!settingsFile.exists()) {
            settingsFile = new File(homeFile, "settings.xml");
            if (!settingsFile.exists()) {
                throw new Exception("Unable to locate settings.xml");
            }
        }
        LOGGER.info("Use {} for vestige settings file", settingsFile);

        Unmarshaller unMarshaller = null;
        try {
            JAXBContext jc = JAXBContext.newInstance(ObjectFactory.class.getPackage().getName());
            unMarshaller = jc.createUnmarshaller();

            URL xsdURL = StandardEditionVestige.class.getResource("settings.xsd");
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            Schema schema = schemaFactory.newSchema(xsdURL);
            unMarshaller.setSchema(schema);
        } catch (Exception e) {
            throw new Exception("Unable to initialize settings parser", e);
        }

        Settings settings;
        try {
            settings = ((JAXBElement<Settings>) unMarshaller.unmarshal(settingsFile)).getValue();
        } catch (JAXBException e) {
            throw new ApplicationException("unable to unmarshall settings.xml", e);
        }

        File appFile = new File(baseFile, "app");
        if (!appFile.exists()) {
            appFile.mkdir();
        }

        try {
            resolverFile = new File(appFile, "application-manager.ser");
            if (resolverFile.isFile()) {
                LOGGER.trace("Restoring application-manager.ser");
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(resolverFile));
                try {
                    defaultApplicationManager = (DefaultApplicationManager) objectInputStream.readObject();
                } finally {
                    objectInputStream.close();
                }
                LOGGER.trace("application-manager.ser restored");
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to restore application manager", e);
        }

        File appHomeFile = new File(appFile, "home");
        if (!appHomeFile.exists()) {
            appHomeFile.mkdir();
        }
        if (defaultApplicationManager == null) {
            defaultApplicationManager = new DefaultApplicationManager(appHomeFile);
        }
        SynchronizedApplicationManager synchronizedApplicationManager = new SynchronizedApplicationManager(defaultApplicationManager);

        File m2Home = new File(homeFile, "m2");
        File mavenSettingsFile = new File(System.getProperty("user.home"), ".m2" + File.separator + "settings.xml");
        if (!mavenSettingsFile.isFile()) {
            mavenSettingsFile = new File(m2Home, "settings.xml");
        }

        LOGGER.info("Use {} for maven settings file", mavenSettingsFile);

        applicationDescriptorFactory = new XMLApplicationDescriptorFactory(new MavenArtifactResolver(mavenSettingsFile));

        securityEnabled = settings.getSecurity().isEnabled();
        vestigeSystemActionExecutor = new JVMVestigeSystemActionExecutor(securityEnabled);

        Admin admin = settings.getAdmin();
        SSH ssh = admin.getSsh();

        Future<SshServer> futureSshServer = null;
        if (ssh.isEnabled()) {
            vestigeExecutor.createWorker("ssh-factory-worker", true, 1);
            File sshBase = new File(baseFile, "ssh");
            File sshHome = new File(homeFile, "ssh");
            futureSshServer = vestigeExecutor.submit(new SSHServerFactory(sshBase, sshHome, ssh, appHomeFile, synchronizedApplicationManager, vestigePlatform));
        }

        Future<Server> futureServer = null;
        Web web = admin.getWeb();
        if (web.isEnabled()) {
            vestigeExecutor.createWorker("web-factory-worker", true, 1);
            futureServer = vestigeExecutor.submit(new WebServerFactory(web, synchronizedApplicationManager, appHomeFile));
        }

        if (futureSshServer != null) {
            sshServer = futureSshServer.get();
        }
        if (futureServer != null) {
            webServer = futureServer.get();
        }
    }

    @Override
    public void run() {
        vestigeSystemActionExecutor.execute(this);
    }

    @Override
    public void vestigeSystemRun(final PublicVestigeSystem vestigeSystem) {
        final long startTimeMillis;
        if (LOGGER.isInfoEnabled()) {
            startTimeMillis = System.currentTimeMillis();
            String implementationVersion = null;
            Package cPackage = StandardEditionVestige.class.getPackage();
            if (cPackage != null) {
                implementationVersion = cPackage.getImplementationVersion();
            }
            if (implementationVersion == null) {
                LOGGER.info("Starting Vestige SE");
            } else {
                LOGGER.info("Starting Vestige SE version {}", implementationVersion);
            }
        } else {
            startTimeMillis = 0;
        }

        PrivateVestigePolicy vestigePolicy = null;
        PrivateVestigeSecurityManager vestigeSecurityManager = null;

        if (securityEnabled) {
            vestigePolicy = new PrivateVestigePolicy();
            vestigeSystem.setPolicy(vestigePolicy);

            PrivateWhiteListVestigePolicy whiteListVestigePolicy = new PrivateWhiteListVestigePolicy();
            // the launch class (before application code)
            whiteListVestigePolicy.addSafeClassLoader(DefaultApplicationManager.class.getClassLoader());
            // the log class (after application code)
            whiteListVestigePolicy.addSafeClassLoader(JoranConfigurator.class.getClassLoader());
            whiteListVestigePolicy.addSafeClassLoader(ConsoleTarget.class.getClassLoader());
            vestigeSystem.setWhiteListPolicy(whiteListVestigePolicy);

            vestigeSecurityManager = new PrivateVestigeSecurityManager();
            vestigeSystem.setSecurityManager(vestigeSecurityManager);
        }

        try {
            start(vestigeSystem, vestigeSecurityManager, vestigePolicy);
        } catch (Exception e) {
            LOGGER.error("Unable to start Vestige SE", e);
        }

        if (LOGGER.isInfoEnabled()) {
            long currentTimeMillis = System.currentTimeMillis();
            long jvmStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
            LOGGER.info("Vestige SE started in {} ms ({} ms since JVM started)", currentTimeMillis - startTimeMillis, currentTimeMillis - jvmStartTime);
        }
        synchronized (this) {
            try {
                wait();
            } catch (InterruptedException e) {
                LOGGER.trace("Vestige SE interrupted", e);
            }
        }
        try {
            stop();
            Thread workerThread = vestigeExecutor.createWorker("se-shutdown-worker", true, 0);
            for (Integer id : vestigePlatform.getAttachments()) {
                vestigePlatform.detach(id);
            }
            workerThread.interrupt();
            workerThread.join();
            LOGGER.info("Vestige SE stopped");
        } catch (Exception e) {
            LOGGER.error("Unable to stop Vestige SE", e);
        }
    }

    public void start(final PublicVestigeSystem vestigeSystem, final PrivateVestigeSecurityManager vestigeSecurityManager, final PrivateVestigePolicy vestigePolicy) throws Exception {
        if (workerThread != null) {
            return;
        }
        workerThread = vestigeExecutor.createWorker("se-worker", true, 0);
        try {
            defaultApplicationManager.powerOn(vestigePlatform, vestigeSystem, vestigeSecurityManager, vestigePolicy, applicationDescriptorFactory);
            if (sshServer != null) {
                sshServer.start();
            }
            if (webServer != null) {
                webServer.start();
            }
        } catch (Exception e) {
            stop();
            throw e;
        }
    }

    public void stop() throws Exception {
        if (workerThread == null) {
            return;
        }
        if (webServer != null) {
            webServer.stop();
        }
        if (sshServer != null) {
            sshServer.stop();
        }
        defaultApplicationManager.shutdown();
        try {
            File parentFile = resolverFile.getParentFile();
            if (!parentFile.isDirectory()) {
                parentFile.mkdirs();
            }
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(resolverFile));
            try {
                objectOutputStream.writeObject(defaultApplicationManager);
            } finally {
                objectOutputStream.close();
            }
        } catch (Exception e) {
            LOGGER.warn("Unable to save application manager", e);
        }
        workerThread.interrupt();
        workerThread.join();
        workerThread = null;
    }

    public static void vestigeMain(final VestigeExecutor vestigeExecutor, final VestigePlatform vestigePlatform, final String[] args) {
        try {
            if (args.length != 2) {
                throw new IllegalArgumentException("expected two argument (vestige home, vestige base)");
            }
            final Thread currentThread = Thread.currentThread();
            // logback can use system stream directly
            giveDirectStreamAccessToLogback();

            Runtime.getRuntime().addShutdownHook(new Thread("se-shutdown") {
                @Override
                public void run() {
                    currentThread.interrupt();
                    try {
                        currentThread.join();
                    } catch (InterruptedException e) {
                        LOGGER.error("Shutdown thread interrupted", e);
                    }
                }
            });

            String home = args[0];
            String base = args[1];
            File homeFile = new File(home).getCanonicalFile();
            File baseFile = new File(base).getCanonicalFile();
            if (!baseFile.isDirectory()) {
                if (!baseFile.mkdirs()) {
                    LOGGER.error("Unable to create vestige base");
                }
            }
            LOGGER.info("Use {} for home file", homeFile);
            final StandardEditionVestige standardEditionVestige = new StandardEditionVestige(homeFile, baseFile, vestigeExecutor, vestigePlatform);
            standardEditionVestige.run();

        } catch (Throwable e) {
            LOGGER.error("Uncatched throwable", e);
        }
    }

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

    public static void main(final String[] args) throws Exception {
        VestigeExecutor vestigeExecutor = new VestigeExecutor();
        VestigePlatform vestigePlatform = new DefaultVestigePlatform(vestigeExecutor);
        vestigeMain(vestigeExecutor, vestigePlatform, args);
    }


}
