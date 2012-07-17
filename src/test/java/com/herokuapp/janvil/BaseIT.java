package com.herokuapp.janvil;

import com.google.common.io.Files;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
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
    protected String appName;
    protected EventSubscription printAllEvents;

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

        appName = System.getenv("HEROKU_APP_NAME");

        config = new Config(System.getenv("HEROKU_API_KEY"))
                .setProtocol(Config.Protocol.HTTP)
                .setConsumersUserAgent(getClass().getSimpleName() + "." + method.getName());

        printAllEvents = new EventSubscription()
                .subscribe(EnumSet.allOf(DeployEvent.class),
                        new EventSubscription.Subscriber() {
                            public void handle(DeployEvent event, Object data) {
                                System.out.println(event + ":" + data);
                            }
                        });
    }

}
