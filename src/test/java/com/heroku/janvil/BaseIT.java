package com.heroku.janvil;

import com.google.common.io.Files;
import com.heroku.api.App;
import com.heroku.api.HerokuAPI;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.EnumSet;
import java.util.UUID;

import static org.testng.Assert.assertTrue;

/**
 * @author Ryan Brainard
 */
public abstract class BaseIT {

    protected File dir;
    protected File emptyFile;
    protected File staticContentsFile;
    protected File randomContentsFile;
    protected File subdir;
    protected File subdirFile;
    protected Config config;
    protected HerokuAPI herokuApi;
    protected EventSubscription<Janvil.Event> printAllEvents;

    protected interface AppRunnable {
        void run(App app) throws Exception;
    }

    protected interface AppsRunnable {
        void run(App[] apps)  throws Exception;
    }

    protected void withApp(final AppRunnable appRunnable) throws Exception {
        withApps(1, new AppsRunnable() {
            public void run(App[] apps) throws Exception {
                appRunnable.run(apps[0]);
            }
        });
    }

    protected void withApps(int qty, AppsRunnable appsRunnable) throws Exception {
        final App[] apps = new App[qty];
        try {
            for (int i = 0; i < apps.length; i++) {
                apps[i] = herokuApi.createApp();
                System.out.println("Created app: " + apps[i].getName());
            }
            appsRunnable.run(apps);
        } finally {
            for (App app : apps) {
                if (app == null) continue;
                herokuApi.destroyApp(app.getName());
                System.out.println("Destroyed app: " + app.getName());
            }
        }
    }

    @BeforeMethod
    protected void setUp(Method method) throws Exception {
        dir = Files.createTempDir();

        emptyFile = new File(dir, "empty.file");
        assertTrue(emptyFile.createNewFile());

        staticContentsFile = new File(dir, "static.file");
        assertTrue(staticContentsFile.createNewFile());
        final PrintWriter staticWriter = new PrintWriter(staticContentsFile);
        staticWriter.append("STATIC");
        staticWriter.close();

        randomContentsFile = new File(dir, "random.file");
        assertTrue(randomContentsFile.createNewFile());
        final PrintWriter randomWriter = new PrintWriter(randomContentsFile);
        randomWriter.append(UUID.randomUUID().toString());
        randomWriter.close();

        subdir = new File(dir, "subdir");
        assertTrue(subdir.mkdir());

        subdirFile = new File(subdir, "subdir.file");
        assertTrue(subdirFile.createNewFile());

        final EnumSet<Janvil.Event> debugLevelOnly = EnumSet.of(Janvil.Event.HTTP_LOGGING_BYTE);
        final EnumSet<Janvil.Event> infoLevelOnly = EnumSet.complementOf(debugLevelOnly);

        printAllEvents = new EventSubscription<Janvil.Event>(Janvil.Event.class)
                .subscribe(debugLevelOnly,
                        new EventSubscription.Subscriber<Janvil.Event>() {
                            public void handle(Janvil.Event event, Object data) {
                                if (event == Janvil.Event.HTTP_LOGGING_BYTE) {
                                    final Integer codePoint = (Integer) data;

                                    final char[] chars;
                                    if (codePoint == Character.MIN_CODE_POINT) {
                                        chars = "NUL".toCharArray();
                                    } else if (codePoint > Character.MAX_CODE_POINT) {
                                        chars = "?".toCharArray();
                                    } else {
                                        chars = Character.toChars(codePoint);
                                    }

                                    System.out.print(chars);
                                    return;
                                }

                                System.out.println(new Date() + ":" + event + ":" + data);
                            }
                        });

        config = new Config(AbstractApi.getPropOrEnv("heroku.apiKey", "HEROKU_API_KEY"))
                .setHerokuUser(method.getName() + "@" + getClass().getSimpleName())
                .setEventSubscription(printAllEvents);

        herokuApi = new HerokuAPI(config.getApiKey());
    }


}
