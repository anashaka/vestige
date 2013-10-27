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

package com.googlecode.vestige.application;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.Permission;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.googlecode.vestige.application.util.FileUtils;
import com.googlecode.vestige.core.VestigeClassLoader;
import com.googlecode.vestige.platform.AttachedVestigeClassLoader;
import com.googlecode.vestige.platform.ClassLoaderConfiguration;
import com.googlecode.vestige.platform.VestigePlatform;
import com.googlecode.vestige.platform.system.PrivateVestigePolicy;
import com.googlecode.vestige.platform.system.PrivateVestigeSecurityManager;
import com.googlecode.vestige.platform.system.PublicVestigeSystem;

/**
 * @author Gael Lalire
 */
public class DefaultApplicationManager implements ApplicationManager, Serializable {

    public static final String VESTIGE_APP_NAME = "vestige.appName";

    private static final long serialVersionUID = -738251991775204424L;

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultApplicationManager.class);

    private static final Set<Permission> SYSTEM_RESOURCES_PERMISSIONS;

    static {
        Set<Permission> resourcesPermissions = new HashSet<Permission>();
        try {
            resourcesPermissions.add(ClassLoader.getSystemResource("java/lang/Object.class").openConnection().getPermission());
            for (URL url : ((URLClassLoader) ClassLoader.getSystemClassLoader()).getURLs()) {
                resourcesPermissions.add(url.openConnection().getPermission());
            }
        } catch (Exception e) {
            LOGGER.debug("Unable to add permission to system classloader", e);
        }
        String extdir = System.getProperty("java.ext.dirs");
        if (extdir != null) {
            StringTokenizer extensionsTok = new StringTokenizer(extdir, File.pathSeparator);
            while (extensionsTok.hasMoreTokens()) {
                String path = new File(extensionsTok.nextToken()).getPath();
                resourcesPermissions.add(new FilePermission(path, "read"));
                resourcesPermissions.add(new FilePermission(path + File.separator + "-", "read"));
            }
        }
        String libdir = System.getProperty("java.library.path");
        if (libdir != null) {
            StringTokenizer extensionsTok = new StringTokenizer(libdir, File.pathSeparator);
            while (extensionsTok.hasMoreTokens()) {
                String path = new File(extensionsTok.nextToken()).getPath();
                resourcesPermissions.add(new FilePermission(path, "read"));
                resourcesPermissions.add(new FilePermission(path + File.separator + "*", "read"));
            }
        }
        SYSTEM_RESOURCES_PERMISSIONS = Collections.unmodifiableSet(resourcesPermissions);
    }

    private transient VestigePlatform vestigePlatform;

    private transient PublicVestigeSystem publicVestigeSystem;

    private transient PrivateVestigePolicy vestigePolicy;

    private transient PrivateVestigeSecurityManager vestigeSecurityManager;

    private Map<String, Map<String, Map<List<Integer>, ApplicationContext>>> applicationContextByVersionByNameByRepo = new TreeMap<String, Map<String, Map<List<Integer>, ApplicationContext>>>();

    private Map<String, URL> urlByRepo = new TreeMap<String, URL>();

    private transient ApplicationDescriptorFactory applicationDescriptorFactory;

    private transient ThreadGroupDestroyer threadGroupDestroyer;

    private File repoFile;

    public DefaultApplicationManager(final File repoFile) {
        this.repoFile = repoFile;
    }

    public void createRepository(final String name, final URL url) throws ApplicationException {
        URL old = urlByRepo.put(name, url);
        if (old != null) {
            urlByRepo.put(name, old);
            throw new ApplicationException("repository already exists");
        }
        applicationContextByVersionByNameByRepo.put(name, new HashMap<String, Map<List<Integer>, ApplicationContext>>());
        LOGGER.info("Repository {} whith url {} created", name, url);
    }

    public void removeRepository(final String name) throws ApplicationException {
        Map<String, Map<List<Integer>, ApplicationContext>> applicationContextByVersionByName = applicationContextByVersionByNameByRepo
                .get(name);
        if (applicationContextByVersionByName == null) {
            throw new ApplicationException("repository do not exists");
        }
        if (!applicationContextByVersionByName.isEmpty()) {
            throw new ApplicationException("uninstall applications before deleting repo");
        }
        applicationContextByVersionByNameByRepo.remove(name);
        urlByRepo.remove(name);
        LOGGER.info("Repository {} removed", name);
    }

    public URL getRepositoryURL(final String repoName) {
        return urlByRepo.get(repoName);
    }

    public Set<String> getRepositoriesName() {
        return urlByRepo.keySet();
    }

    public ApplicationContext getApplication(final String repoName, final String appName, final List<Integer> version)
            throws ApplicationException {
        Map<String, Map<List<Integer>, ApplicationContext>> applicationContextByVersionByName = applicationContextByVersionByNameByRepo
                .get(repoName);
        if (applicationContextByVersionByName == null) {
            throw new ApplicationException("Repository " + repoName + " does not exists");
        }
        Map<List<Integer>, ApplicationContext> applicationContextByVersion = applicationContextByVersionByName.get(appName);
        if (applicationContextByVersion == null) {
            throw new ApplicationException("Application " + appName + " is not installed");
        }
        ApplicationContext applicationContext = applicationContextByVersion.get(version);
        if (applicationContext == null) {
            throw new ApplicationException("Version " + VersionUtils.toString(version) + " of " + appName + " is not installed");
        }
        return applicationContext;
    }

    public boolean hasApplication(final String repoName, final String appName, final List<Integer> version)
            throws ApplicationException {
        URL context = urlByRepo.get(repoName);
        URL url;
        try {
            url = new URL(context, appName + "/" + appName + "-" + VersionUtils.toString(version) + ".xml");
        } catch (MalformedURLException e) {
            throw new ApplicationException("url is invalid", e);
        }
        try {
            URLConnection openConnection = url.openConnection();
            openConnection.connect();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void installApplicationContext(final String repoName, final String appName, final List<Integer> version,
            final ApplicationContext applicationContext) throws ApplicationException {
        Map<String, Map<List<Integer>, ApplicationContext>> applicationByVersionByName = applicationContextByVersionByNameByRepo
                .get(repoName);
        Map<List<Integer>, ApplicationContext> applicationByVersion = applicationByVersionByName.get(appName);
        if (applicationByVersion == null) {
            applicationByVersion = new TreeMap<List<Integer>, ApplicationContext>(VersionUtils.VERSION_COMPARATOR);
            applicationByVersionByName.put(appName, applicationByVersion);
        } else {
            ApplicationContext oldApplicationContext = applicationByVersion.get(version);
            if (oldApplicationContext != null) {
                throw new ApplicationException("application already installed");
            }
        }
        applicationByVersion.put(version, applicationContext);
    }

    public ApplicationContext createApplicationContext(final String repoName, final String appName, final List<Integer> version)
            throws ApplicationException {
        URL context = urlByRepo.get(repoName);
        if (context == null) {
            throw new ApplicationException("repo not found");
        }

        ApplicationDescriptor applicationDescriptor = applicationDescriptorFactory.createApplicationDescriptor(context, repoName, appName, version);

        File file = new File(repoFile, repoName + File.separator
                + appName + File.separator + VersionUtils.toString(version));
        file.mkdirs();

        Set<List<Integer>> supportedMigrationVersion = applicationDescriptor.getSupportedMigrationVersions();
        Set<List<Integer>> uninterruptedMigrationVersion = applicationDescriptor.getUninterruptedMigrationVersions();
        if (!supportedMigrationVersion.containsAll(uninterruptedMigrationVersion)) {
            throw new ApplicationException("some migration are uninterrupted but not supported");
        }

        ApplicationContext applicationContext = new ApplicationContext();

        applicationContext.setInstallerClassName(applicationDescriptor.getInstallerClassName());
        applicationContext.setInstallerResolve(applicationDescriptor.getInstallerClassLoaderConfiguration());
        applicationContext.setClassName(applicationDescriptor.getLauncherClassName());
        applicationContext.setPrivateSystem(applicationDescriptor.isLauncherPrivateSystem());
        applicationContext.setResolve(applicationDescriptor.getLauncherClassLoaderConfiguration());
        applicationContext.setPermissions(applicationDescriptor.getPermissions());

        applicationContext.setHome(file);
        applicationContext.setSupportedMigrationVersion(supportedMigrationVersion);
        applicationContext.setUninterruptedMigrationVersion(uninterruptedMigrationVersion);
        applicationContext.setName(repoName + "-" + appName + "-" + VersionUtils.toString(version));

        return applicationContext;
    }

    public void checkAutoMigrateLevel(final String repoName, final String appName, final List<Integer> version, final int level,
            final Collection<List<Integer>> ignoredVersions) throws ApplicationException {
        Map<List<Integer>, ApplicationContext> map = applicationContextByVersionByNameByRepo.get(repoName).get(appName);
        if (map == null) {
            // no other version
            return;
        }
        forloop: for (Entry<List<Integer>, ApplicationContext> otherEntry : map.entrySet()) {
            List<Integer> otherVersion = otherEntry.getKey();
            if (ignoredVersions.contains(otherVersion)) {
                continue;
            }
            for (int i = 0; i < 3; i++) {
                if (!otherVersion.get(i).equals(version.get(i))) {
                    if (otherVersion.get(i).intValue() < version.get(i).intValue()) {
                        // before version
                        if (otherEntry.getValue().getAutoMigrateLevel() > 2 - i) {
                            throw new ApplicationException("Version " + VersionUtils.toString(otherVersion)
                                    + " must have an automigrate level lesser or equals than " + (2 - i));
                        }
                    } else {
                        // after version
                        if (level > 2 - i) {
                            throw new ApplicationException("Version " + VersionUtils.toString(version)
                                    + " must have an automigrate level lesser or equals than " + (2 - i));
                        }
                    }
                    continue forloop;
                }
            }
            throw new ApplicationException("Version is already installed ");
        }
    }

    private static final Set<List<Integer>> EMPTY_VERSION_SET = Collections.emptySet();

    public void install(final String repoName, final String appName, final List<Integer> version) throws ApplicationException {
        ApplicationContext applicationContext = createApplicationContext(repoName, appName, version);

        // check if it broke an automigrate level
        checkAutoMigrateLevel(repoName, appName, version, 0, EMPTY_VERSION_SET);

        // install
        try {
            ClassLoaderConfiguration installerResolve = applicationContext.getInstallerResolve();
            if (installerResolve != null) {
                int installerAttach = vestigePlatform.attach(installerResolve);
                try {
                    vestigePlatform.start(installerAttach);
                    VestigeClassLoader<?> installerClassLoader = vestigePlatform.getClassLoader(installerAttach);
                    Method installerMethod = installerClassLoader.loadClass(applicationContext.getInstallerClassName())
                            .getMethod("install", File.class, List.class);
                    Thread currentThread = Thread.currentThread();
                    ClassLoader contextClassLoader = currentThread.getContextClassLoader();
                    currentThread.setContextClassLoader(installerClassLoader);
                    try {
                        installerMethod.invoke(null, applicationContext.getHome(), version);
                    } finally {
                        currentThread.setContextClassLoader(contextClassLoader);
                    }
                } finally {
                    vestigePlatform.detach(installerAttach);
                }
            }
        } catch (Exception e) {
            throw new ApplicationException("fail to install", e);
        }

        installApplicationContext(repoName, appName, version, applicationContext);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Application {} version {} on repository {} installed",
                    new Object[] {appName, VersionUtils.toString(version), repoName});
        }
    }

    public void uninstall(final String repoName, final String appName, final List<Integer> version) throws ApplicationException {
        ApplicationContext applicationContext = getApplication(repoName, appName, version);
        if (applicationContext.isStarted()) {
            throw new ApplicationException("Application is started");
        }
        File home = applicationContext.getHome();
        try {
            ClassLoaderConfiguration installerResolve = applicationContext.getInstallerResolve();
            if (installerResolve != null) {
                int installerAttach = vestigePlatform.attach(installerResolve);
                try {
                    vestigePlatform.start(installerAttach);
                    VestigeClassLoader<?> installerClassLoader = vestigePlatform.getClassLoader(installerAttach);
                    Method installerMethod = installerClassLoader.loadClass(applicationContext.getInstallerClassName())
                            .getMethod("uninstall", File.class, List.class);
                    Thread currentThread = Thread.currentThread();
                    ClassLoader contextClassLoader = currentThread.getContextClassLoader();
                    currentThread.setContextClassLoader(installerClassLoader);
                    try {
                        installerMethod.invoke(null, home, version);
                    } finally {
                        currentThread.setContextClassLoader(contextClassLoader);
                    }
                } finally {
                    vestigePlatform.detach(installerAttach);
                }
            }
        } catch (Exception e) {
            throw new ApplicationException("fail to uninstall", e);
        }

        uninstallApplicationContext(repoName, appName, version, applicationContext);

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Application {} version {} on repository {} uninstalled",
                    new Object[] {appName, VersionUtils.toString(version), repoName});
        }
    }

    public void uninstallApplicationContext(final String repoName, final String appName, final List<Integer> version, final ApplicationContext applicationContext) throws ApplicationException {
        File home = applicationContext.getHome();
        Map<String, Map<List<Integer>, ApplicationContext>> appByVersionByName = applicationContextByVersionByNameByRepo
                .get(repoName);
        Map<List<Integer>, ApplicationContext> appByVersion = appByVersionByName.get(appName);
        if (appByVersion.size() == 1) {
            home = home.getParentFile();
        }
        if (home.exists()) {
            try {
                FileUtils.forceDelete(home);
            } catch (IOException e) {
                throw new ApplicationException("Unable to remove application directory", e);
            }
        }
        if (appByVersion.size() == 1) {
            appByVersionByName.remove(appName);
        } else {
            appByVersion.remove(version);
        }
    }

    public void migrate(final String repoName, final String appName, final List<Integer> fromVersion,
            final List<Integer> toVersion) throws ApplicationException {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("migrating {} {} from {} to {} ", new Object[] {repoName, appName, VersionUtils.toString(fromVersion),
                    VersionUtils.toString(toVersion)});
        }
        final ApplicationContext fromApplicationContext = getApplication(repoName, appName, fromVersion);
        ApplicationContext toApplicationContext = createApplicationContext(repoName, appName, toVersion);

        int level = fromApplicationContext.getAutoMigrateLevel();
        // migration target inherits autoMigrateLevel
        toApplicationContext.setAutoMigrateLevel(level);
        checkAutoMigrateLevel(repoName, appName, toVersion, level, Collections.singleton(fromVersion));

        Integer compare = VersionUtils.compare(fromVersion, toVersion);

        ClassLoaderConfiguration installerResolve;
        RuntimeApplicationContext runtimeApplicationContext = fromApplicationContext.getRuntimeApplicationContext();
        // && runtimeApplicationContext != null should be redondant
        if (fromApplicationContext.isStarted() && runtimeApplicationContext != null) {
            if (compare == null) {
                if (fromApplicationContext.getUninterruptedMigrationVersion().contains(toVersion)) {
                    // use fromVersion to perform the migration
                    installerResolve = fromApplicationContext.getInstallerResolve();
                } else {
                    if (!toApplicationContext.getUninterruptedMigrationVersion().contains(fromVersion)) {
                        throw new ApplicationException("None of " + VersionUtils.toString(fromVersion) + " and "
                                + VersionUtils.toString(toVersion) + " can uninterrupted migrate the other");
                    }
                    // use toVersion to perform the migration
                    installerResolve = toApplicationContext.getInstallerResolve();
                }
            } else {
                if (compare.intValue() < 0) {
                    // fromVersion before toVersion
                    if (toApplicationContext.getUninterruptedMigrationVersion().contains(fromVersion)) {
                        // use toVersion to perform the migration
                        installerResolve = toApplicationContext.getInstallerResolve();
                    } else {
                        throw new ApplicationException(VersionUtils.toString(toVersion)
                                + " does not support uninterrupted migrate from " + VersionUtils.toString(fromVersion));
                    }
                } else {
                    // toVersion before fromVersion
                    if (fromApplicationContext.getUninterruptedMigrationVersion().contains(toVersion)) {
                        // use fromVersion to perform the migration
                        installerResolve = fromApplicationContext.getInstallerResolve();
                    } else {
                        throw new ApplicationException(VersionUtils.toString(fromVersion)
                                + " does not support uninterrupted migrate from " + VersionUtils.toString(toVersion));
                    }
                }
            }
            try {
                final Object runMutex = new Object();
                RuntimeApplicationContext toRuntimeApplicationContext = toApplicationContext.getRuntimeApplicationContext();
                Object constructorMutex = null;
                if (toRuntimeApplicationContext == null) {
                    constructorMutex = new Object();
                } else {
                    toRuntimeApplicationContext.setRunAllowed(false);
                }
                start(toApplicationContext, runMutex, constructorMutex);
                if (constructorMutex != null) {
                    synchronized (constructorMutex) {
                        toRuntimeApplicationContext = toApplicationContext.getRuntimeApplicationContext();
                        while (toRuntimeApplicationContext == null && toApplicationContext.getThread() != null) {
                            constructorMutex.wait();
                            toRuntimeApplicationContext = toApplicationContext.getRuntimeApplicationContext();
                        }
                    }
                }
                if (toRuntimeApplicationContext == null) {
                    // fail on object construction
                    throw new ApplicationException("Cannot create application instance");
                }

                final RuntimeApplicationContext notNullToRuntimeApplicationContext = toRuntimeApplicationContext;

                if (installerResolve != null) {
                    Runnable notifyRunMutex = new Runnable() {

                        @Override
                        public void run() {
                            synchronized (runMutex) {
                                notNullToRuntimeApplicationContext.setRunAllowed(true);
                                runMutex.notify();
                            }
                        }
                    };
                    int installerAttach = vestigePlatform.attach(installerResolve);
                    try {
                        vestigePlatform.start(installerAttach);
                        VestigeClassLoader<?> installerClassLoader = vestigePlatform.getClassLoader(installerAttach);

                        Method installerMethod = installerClassLoader.loadClass(fromApplicationContext.getInstallerClassName())
                                .getMethod("uninterruptedMigrate", File.class, List.class, Object.class, File.class,
                                        List.class, Object.class, Runnable.class);
                        Thread currentThread = Thread.currentThread();
                        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
                        currentThread.setContextClassLoader(installerClassLoader);
                        try {
                            installerMethod.invoke(null, fromApplicationContext.getHome(), fromVersion, runtimeApplicationContext.getRunnable(), toApplicationContext.getHome(),
                                    toVersion, toApplicationContext.getRuntimeApplicationContext().getRunnable(), notifyRunMutex);
                        } finally {
                            currentThread.setContextClassLoader(contextClassLoader);
                        }
                    } finally {
                        vestigePlatform.detach(installerAttach);
                        notifyRunMutex.run();
                    }
                }

                stop(fromApplicationContext);

            } catch (Exception e) {
                throw new ApplicationException("fail to uninterrupted migrate", e);
            }
        } else {
            if (compare == null) {
                if (fromApplicationContext.getSupportedMigrationVersion().contains(toVersion)) {
                    // use fromVersion to perform the migration
                    installerResolve = fromApplicationContext.getInstallerResolve();
                } else {
                    if (!toApplicationContext.getSupportedMigrationVersion().contains(fromVersion)) {
                        throw new ApplicationException("None of " + VersionUtils.toString(fromVersion) + " and "
                                + VersionUtils.toString(toVersion) + " can migrate the other");
                    }
                    // use toVersion to perform the migration
                    installerResolve = toApplicationContext.getInstallerResolve();
                }
            } else {
                if (compare.intValue() < 0) {
                    // fromVersion before toVersion
                    if (toApplicationContext.getSupportedMigrationVersion().contains(fromVersion)) {
                        // use toVersion to perform the migration
                        installerResolve = toApplicationContext.getInstallerResolve();
                    } else {
                        throw new ApplicationException(VersionUtils.toString(toVersion) + " does not support migrate from "
                                + VersionUtils.toString(fromVersion));
                    }
                } else {
                    // toVersion before fromVersion
                    if (fromApplicationContext.getSupportedMigrationVersion().contains(toVersion)) {
                        // use fromVersion to perform the migration
                        installerResolve = fromApplicationContext.getInstallerResolve();
                    } else {
                        throw new ApplicationException(VersionUtils.toString(fromVersion) + " does not support migrate from "
                                + VersionUtils.toString(toVersion));
                    }
                }
            }
            try {
                if (installerResolve != null) {
                    int installerAttach = vestigePlatform.attach(installerResolve);
                    try {
                        vestigePlatform.start(installerAttach);
                        VestigeClassLoader<?> installerClassLoader = vestigePlatform.getClassLoader(installerAttach);

                        Method installerMethod = installerClassLoader.loadClass(fromApplicationContext.getInstallerClassName())
                                .getMethod("migrate", File.class, List.class, File.class, List.class);
                        Thread currentThread = Thread.currentThread();
                        ClassLoader contextClassLoader = currentThread.getContextClassLoader();
                        currentThread.setContextClassLoader(installerClassLoader);
                        try {
                            installerMethod.invoke(null, fromApplicationContext.getHome(), fromVersion,
                                    toApplicationContext.getHome(), toVersion);
                        } finally {
                            currentThread.setContextClassLoader(contextClassLoader);
                        }
                    } finally {
                        vestigePlatform.detach(installerAttach);
                    }
                }
            } catch (Exception e) {
                throw new ApplicationException("fail to migrate", e);
            }
        }
        installApplicationContext(repoName, appName, toVersion, toApplicationContext);
        // uninstall from-version
        uninstallApplicationContext(repoName, appName, fromVersion, fromApplicationContext);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Application {} version {} on repository {} migrated to version {}",
                    new Object[] {appName, VersionUtils.toString(fromVersion), repoName, VersionUtils.toString(toVersion)});
        }

    }

    public boolean isStarted(final String repoName, final String appName, final List<Integer> version)
            throws ApplicationException {
        final ApplicationContext applicationContext = getApplication(repoName, appName, version);
        return applicationContext.isStarted();
    }

    public ClassLoaderConfiguration getClassLoaders(final String repoName, final String appName, final List<Integer> version) throws ApplicationException {
        final ApplicationContext applicationContext = getApplication(repoName, appName, version);
        return applicationContext.getResolve();
    }


    public void start(final String repoName, final String appName, final List<Integer> version) throws ApplicationException {
        final ApplicationContext applicationContext = getApplication(repoName, appName, version);
        if (applicationContext.isStarted()) {
            throw new ApplicationException("already started");
        }
        start(applicationContext);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Application {} version {} on repository {} started",
                    new Object[] {appName, VersionUtils.toString(version), repoName});
        }
    }

    public void stop(final String repoName, final String appName, final List<Integer> version) throws ApplicationException {
        ApplicationContext applicationContext = getApplication(repoName, appName, version);
        stop(applicationContext);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Application {} version {} on repository {} stopped",
                    new Object[] {appName, VersionUtils.toString(version), repoName});
        }
    }

    public Set<String> getApplicationsName(final String repo) {
        Map<String, Map<List<Integer>, ApplicationContext>> applicationContextByVersionByName = applicationContextByVersionByNameByRepo
                .get(repo);
        if (applicationContextByVersionByName == null) {
            return Collections.emptySet();
        }
        return applicationContextByVersionByName.keySet();
    }

    public Set<List<Integer>> getVersions(final String repo, final String appName) {
        Map<String, Map<List<Integer>, ApplicationContext>> applicationContextByVersionByName = applicationContextByVersionByNameByRepo
                .get(repo);
        if (applicationContextByVersionByName == null) {
            return Collections.emptySet();
        }
        Map<List<Integer>, ApplicationContext> applicationContextByVersion = applicationContextByVersionByName.get(appName);
        if (applicationContextByVersion == null) {
            return Collections.emptySet();
        }
        return applicationContextByVersion.keySet();
    }

    public void start(final ApplicationContext applicationContext) throws ApplicationException {
        if (applicationContext.getThread() != null) {
            // already started
            return;
        }
        start(applicationContext, null, null);
    }

    public Object callConstructor(final ClassLoader classLoader, final Class<?> loadClass, final File home, final PublicVestigeSystem vestigeSystem) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException, ApplicationException  {
        Class<?> bestItfType = null;
        Constructor<?> bestConstructor = null;
        int level = -1;
        for (Constructor<?> constructor : loadClass.getConstructors()) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            switch (parameterTypes.length) {
            case 0:
                if (level < 0) {
                    level = 0;
                    bestConstructor = constructor;
                }
                break;
            case 1:
                if (level < 1) {
                    if (!parameterTypes[0].equals(File.class)) {
                        break;
                    }
                    level = 1;
                    bestConstructor = constructor;
                }
                break;
            case 2:
                if (vestigeSystem == null) {
                    break;
                }
                if (!parameterTypes[0].equals(File.class)) {
                    break;
                }
                if (!parameterTypes[1].isInterface()) {
                    break;
                }
                if (level == 2) {
                    LOGGER.warn("Two constructors with two args are available", level);
                    break;
                }
                level = 2;
                bestConstructor = constructor;
                bestItfType = parameterTypes[1];
                break;
            default:
            }
        }
        switch (level) {
        case 0:
            return bestConstructor.newInstance();
        case 1:
            return bestConstructor.newInstance(home);
        case 2:
            return bestConstructor.newInstance(home, VestigeSystemInvocationHandler.createProxy(classLoader, bestItfType, vestigeSystem, vestigeSecurityManager != null));
        default:
            throw new ApplicationException("No constructor found");
        }
    }

    public void start(final ApplicationContext applicationContext, final Object runMutex, final Object constructorMutex) throws ApplicationException {
        try {
            final int attach;
            final PublicVestigeSystem vestigeSystem;
            final VestigeClassLoader<AttachedVestigeClassLoader> classLoader;
            final RuntimeApplicationContext previousRuntimeApplicationContext = applicationContext.getRuntimeApplicationContext();
            final ClassLoaderConfiguration resolve = applicationContext.getResolve();
            if (previousRuntimeApplicationContext == null) {
                attach = vestigePlatform.attach(resolve);
                classLoader = vestigePlatform.getClassLoader(attach);
                if (applicationContext.isPrivateSystem()) {
                    vestigeSystem = publicVestigeSystem.createSubSystem();
                } else {
                    vestigeSystem = null;
                }
            } else {
                // reattach to platform
                attach = vestigePlatform.attach(previousRuntimeApplicationContext.getClassLoader());
                vestigeSystem = previousRuntimeApplicationContext.getVestigeSystem();
                classLoader = previousRuntimeApplicationContext.getClassLoader();
            }
            vestigePlatform.start(attach);

            final ThreadGroup threadGroup = new ThreadGroup(applicationContext.getName());
            Thread thread = new Thread(threadGroup, "main") {
                @Override
                public void run() {
                    MDC.put(VESTIGE_APP_NAME, applicationContext.getName());
                    try {
                        if (vestigeSystem != null) {
                            vestigeSystem.setCurrentSystem();
                        }
                        if (vestigeSecurityManager != null) {
                            vestigeSecurityManager.setThreadGroup(threadGroup);
                            Permissions permissions = new Permissions();
                            // getResource for system jar
                            for (Permission permission : SYSTEM_RESOURCES_PERMISSIONS) {
                                permissions.add(permission);
                            }
                            // getResource for app jar
                            for (Permission permission : resolve.getPermissions()) {
                                permissions.add(permission);
                            }
                            // access to home of app
                            permissions.add(new FilePermission(applicationContext.getHome().getPath(), "read,write"));
                            permissions.add(new FilePermission(applicationContext.getHome().getPath() + File.separator + "-", "read,write,delete"));
                            // access to tmp dir
                            String tmpdir = System.getProperty("java.io.tmpdir");
                            if (tmpdir != null) {
                                String path = new File(tmpdir).getPath();
                                permissions.add(new FilePermission(path, "read,write"));
                                permissions.add(new FilePermission(path + File.separator + "-", "read,write,delete"));
                            }
                            // application specific permission
                            for (Permission permission : applicationContext.getPermissions()) {
                                permissions.add(permission);
                            }
                            vestigePolicy.setPermissionCollection(permissions);
                        }
                        final Runnable runnable;
                        RuntimeApplicationContext runtimeApplicationContext;
                        if (previousRuntimeApplicationContext == null) {
                            try {
                                if (vestigeSecurityManager != null) {
                                    runnable = AccessController.doPrivileged(new PrivilegedExceptionAction<Runnable>() {
                                        @Override
                                        public Runnable run() throws Exception {
                                            return (Runnable) callConstructor(classLoader, classLoader.loadClass(applicationContext.getClassName()), applicationContext.getHome(), vestigeSystem);
                                        }
                                    });
                                } else {
                                    runnable = (Runnable) callConstructor(classLoader, classLoader.loadClass(applicationContext.getClassName()), applicationContext.getHome(), vestigeSystem);
                                }
                            } catch (Exception e) {
                                if (vestigeSecurityManager != null) {
                                    vestigePolicy.unsetPermissionCollection();
                                }
                                LOGGER.error("Unable to create instance", e);
                                return;
                            }
                            runtimeApplicationContext = new RuntimeApplicationContext(classLoader, runnable, vestigeSystem, runMutex == null);
                            if (constructorMutex != null) {
                                synchronized (constructorMutex) {
                                    applicationContext.setRuntimeApplicationContext(runtimeApplicationContext);
                                    constructorMutex.notify();
                                }
                            } else {
                                applicationContext.setRuntimeApplicationContext(runtimeApplicationContext);
                            }
                            classLoader.getData().addObject(new SoftReference<RuntimeApplicationContext>(runtimeApplicationContext));
                        } else {
                            runtimeApplicationContext = previousRuntimeApplicationContext;
                            runnable = runtimeApplicationContext.getRunnable();
                        }
                        if (runMutex != null) {
                            synchronized (runMutex) {
                                while (!runtimeApplicationContext.isRunAllowed()) {
                                    try {
                                        runMutex.wait();
                                    } catch (InterruptedException e) {
                                        if (vestigeSecurityManager != null) {
                                            vestigePolicy.unsetPermissionCollection();
                                        }
                                        LOGGER.trace("Application stopped before run method call", e);
                                        return;
                                    }
                                }
                            }
                        }
                        if (vestigeSecurityManager != null) {
                            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                                @Override
                                public Void run() {
                                    runnable.run();
                                    return null;
                                }
                            });
                        } else {
                            runnable.run();
                        }
                    } catch (RuntimeException e) {
                        if (vestigeSecurityManager != null) {
                            vestigePolicy.unsetPermissionCollection();
                        }
                        LOGGER.error("Application runtime exception", e);
                    } finally {
                        if (vestigeSecurityManager != null) {
                            vestigePolicy.unsetPermissionCollection();
                            vestigeSecurityManager.unsetThreadGroup();
                        }
                        vestigePlatform.stop(attach);
                        vestigePlatform.detach(attach);
                        // allow inner start to run
                        if (constructorMutex != null) {
                            synchronized (constructorMutex) {
                                applicationContext.setThread(null);
                                constructorMutex.notify();
                            }
                        } else {
                            applicationContext.setThread(null);
                        }
                        // allow external start to run
                        applicationContext.setStarted(false);
                        if (vestigeSystem != null) {
                            publicVestigeSystem.setCurrentSystem();
                        }
                        MDC.remove(VESTIGE_APP_NAME);
                        threadGroupDestroyer.destroy(Thread.currentThread(), threadGroup);
                    }
                }
            };
            applicationContext.setThread(thread);
            thread.setContextClassLoader(classLoader);
            applicationContext.setStarted(true);
            thread.start();
        } catch (Exception e) {
            throw new ApplicationException("Unable to start", e);
        }
    }

    public void stop(final ApplicationContext applicationContext) throws ApplicationException {
        Thread thread = applicationContext.getThread();
        if (thread == null) {
            // already stopped
            return;
        }
        thread.interrupt();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new ApplicationException("Unable to stop", e);
        }
    }

    /**
     * Stop all applications without modifing its states.
     */
    public void shutdown() {
        for (Entry<String, Map<String, Map<List<Integer>, ApplicationContext>>> entry : applicationContextByVersionByNameByRepo
                .entrySet()) {
            for (Entry<String, Map<List<Integer>, ApplicationContext>> entry2 : entry.getValue().entrySet()) {
                for (Entry<List<Integer>, ApplicationContext> entry3 : entry2.getValue().entrySet()) {
                    ApplicationContext applicationContext = entry3.getValue();
                    if (applicationContext.isStarted()) {
                        try {
                            stop(applicationContext);
                            applicationContext.setStarted(true);
                        } catch (ApplicationException e) {
                            LOGGER.error("Unable to stop", e);
                        }
                    }
                }
            }
        }
        this.vestigePlatform = null;
        this.applicationDescriptorFactory = null;
        threadGroupDestroyer.interrupt();
    }

    public void powerOn(final VestigePlatform vestigePlatform, final PublicVestigeSystem vestigeSystem, final PrivateVestigeSecurityManager vestigeSecurityManager, final PrivateVestigePolicy vestigePolicy, final ApplicationDescriptorFactory applicationDescriptorFactory) {
        this.publicVestigeSystem = vestigeSystem;
        this.vestigeSecurityManager = vestigeSecurityManager;
        this.vestigePolicy = vestigePolicy;
        this.vestigePlatform = vestigePlatform;
        this.applicationDescriptorFactory = applicationDescriptorFactory;
        List<ApplicationContext> autoStartApplicationContext = new ArrayList<ApplicationContext>();
        for (Entry<String, Map<String, Map<List<Integer>, ApplicationContext>>> entry : applicationContextByVersionByNameByRepo
                .entrySet()) {
            for (Entry<String, Map<List<Integer>, ApplicationContext>> entry2 : entry.getValue().entrySet()) {
                for (Entry<List<Integer>, ApplicationContext> entry3 : entry2.getValue().entrySet()) {
                    ApplicationContext applicationContext = entry3.getValue();
                    if (applicationContext.isStarted()) {
                        autoStartApplicationContext.add(applicationContext);
                        applicationContext.setStarted(false);
                    }
                }
            }
        }
        try {
            autoMigrate();
        } catch (ApplicationException e) {
            LOGGER.error("Automigration failed", e);
        }
        for (ApplicationContext applicationContext : autoStartApplicationContext) {
            try {
                start(applicationContext);
            } catch (ApplicationException e) {
                LOGGER.error("Unable to start", e);
            }
        }
        threadGroupDestroyer = new ThreadGroupDestroyer();
        threadGroupDestroyer.start();
    }

    public void autoMigrate() throws ApplicationException {
        List<String> failedMigration = new ArrayList<String>();
        for (Entry<String, Map<String, Map<List<Integer>, ApplicationContext>>> entry : applicationContextByVersionByNameByRepo
                .entrySet()) {
            String repoName = entry.getKey();
            for (Entry<String, Map<List<Integer>, ApplicationContext>> entry2 : entry.getValue().entrySet()) {
                String appName = entry2.getKey();
                // create a new map because migration cause concurent modification
                Map<List<Integer>, ApplicationContext> applicationContextByVersion = new HashMap<List<Integer>, ApplicationContext>(entry2.getValue());
                for (Entry<List<Integer>, ApplicationContext> entry3 : applicationContextByVersion.entrySet()) {
                    List<Integer> version = entry3.getKey();
                    try {
                        ApplicationContext applicationContext = entry3.getValue();
                        int autoMigrateLevel = applicationContext.getAutoMigrateLevel();
                        int majorVersion = version.get(0);
                        int minorVersion = version.get(1);
                        int bugfixVersion = version.get(2);
                        List<Integer> newerVersion = Arrays.asList(majorVersion, minorVersion, bugfixVersion);
                        switch (autoMigrateLevel) {
                        case 3:
                            newerVersion.set(0, majorVersion + 1);
                            newerVersion.set(1, 0);
                            newerVersion.set(2, 0);
                            if (hasApplication(repoName, appName, newerVersion)) {
                                minorVersion = 0;
                                bugfixVersion = 0;
                                do {
                                    majorVersion++;
                                    newerVersion.set(0, majorVersion + 1);
                                } while (hasApplication(repoName, appName, newerVersion));
                            } else {
                                newerVersion.set(2, bugfixVersion);
                            }
                            newerVersion.set(0, majorVersion);
                        case 2:
                            newerVersion.set(1, minorVersion + 1);
                            newerVersion.set(2, 0);
                            if (hasApplication(repoName, appName, newerVersion)) {
                                bugfixVersion = 0;
                                do {
                                    minorVersion++;
                                    newerVersion.set(1, minorVersion + 1);
                                } while (hasApplication(repoName, appName, newerVersion));
                            }
                            newerVersion.set(1, minorVersion);
                        case 1:
                            newerVersion.set(2, bugfixVersion + 1);
                            if (hasApplication(repoName, appName, newerVersion)) {
                                do {
                                    bugfixVersion++;
                                    newerVersion.set(2, bugfixVersion + 1);
                                } while (hasApplication(repoName, appName, newerVersion));
                            }
                            newerVersion.set(2, bugfixVersion);
                        case 0:
                            break;
                        default:
                            throw new ApplicationException("Unexpected autoMigrateLevel" + autoMigrateLevel);
                        }
                        if (!newerVersion.equals(version)) {
                            migrate(repoName, appName, version, newerVersion);
                        }
                    } catch (ApplicationException e) {
                        String fromApplication = repoName + " " + appName + " " + VersionUtils.toString(version);
                        failedMigration.add(fromApplication);
                        LOGGER.warn("Unable to auto-migrate " + fromApplication, e);
                    }
                }
            }
        }
        if (failedMigration.size() != 0) {
            throw new ApplicationException("Following migration failed " + failedMigration);
        }
    }

    public void setAutoMigrateLevel(final String repoName, final String appName, final List<Integer> version, final int level)
            throws ApplicationException {
        if (level < 0 || level > 3) {
            throw new ApplicationException("Level must be an integer between 0 and 3");
        }
        ApplicationContext applicationContext = getApplication(repoName, appName, version);
        if (level != 0) {
            // check if it is the latest version for the choosed level
            Map<List<Integer>, ApplicationContext> map = applicationContextByVersionByNameByRepo.get(repoName).get(appName);
            Set<List<Integer>> versions = map.keySet();
            forloop: for (List<Integer> otherVersion : versions) {
                for (int i = 0; i < 3 - level; i++) {
                    if (!otherVersion.get(i).equals(version.get(i))) {
                        // other dependency cannot be our future
                        continue forloop;
                    }
                }
                for (int i = 3 - level; i < 3; i++) {
                    int otherVersionPart = otherVersion.get(i).intValue();
                    int versionPart = version.get(i).intValue();
                    if (otherVersionPart > versionPart) {
                        // the auto migrate should apply to otherVersion
                        throw new ApplicationException("Version " + VersionUtils.toString(version)
                                + " cannot have an automigrate level because version " + VersionUtils.toString(otherVersion)
                                + " is installed");
                    } else if (otherVersionPart != versionPart) {
                        // other dependency is in our past
                        continue forloop;
                    }
                }
            }
        }
        applicationContext.setAutoMigrateLevel(level);
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Application {} version {} on repository {} auto-migrate-level setted to {}",
                    new Object[] {appName, VersionUtils.toString(version), repoName, level});
        }
    }

    public int getAutoMigrateLevel(final String repoName, final String appName, final List<Integer> version)
            throws ApplicationException {
        ApplicationContext applicationContext = getApplication(repoName, appName, version);
        return applicationContext.getAutoMigrateLevel();
    }

}
