package com.thebrokenrail.combustible.api.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.GetSite;
import com.thebrokenrail.combustible.api.method.Login;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.HttpUrl;

public class ConnectionTest {
    private static final HttpUrl TEST_INSTANCE = HttpUrl.parse("https://voyager.lemmy.ml/");

    @Test
    public void basicTest() throws InterruptedException {
        // Connect
        Connection connection = new Connection(TEST_INSTANCE, null);

        // Should Be Logged Out
        assertFalse(connection.hasToken());

        // Test GetSite
        AtomicBoolean isSuccess = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        GetSite method = new GetSite();
        connection.send(method, getSiteResponse -> {
            // Success
            isSuccess.set(true);
            latch.countDown();
        }, () -> {
            // Error
            isSuccess.set(false);
            latch.countDown();
        });
        latch.await();
        assertTrue(isSuccess.get());
    }

    @Test
    public void apiErrorTest() throws InterruptedException {
        // Connect
        Connection connection = new Connection(TEST_INSTANCE, null);

        // Should Be Logged Out
        assertFalse(connection.hasToken());

        // Test Login With Invalid Info
        AtomicBoolean isSuccess = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        Login method = new Login();
        method.username_or_email = "TheTestingRail";
        method.password = "ThisIsNotMyPassword";
        connection.send(method, loginResponse -> {
            // Unexpected Success
            isSuccess.set(false);
            latch.countDown();
        }, () -> {
            // Expected Failure
            isSuccess.set(true);
            latch.countDown();
        });
        latch.await();
        assertTrue(isSuccess.get());
    }

    @Test
    public void httpErrorTest() throws InterruptedException {
        // Connect
        Connection connection = new Connection(TEST_INSTANCE, null);

        // Should Be Logged Out
        assertFalse(connection.hasToken());

        // Test Login With Invalid Info
        AtomicBoolean isSuccess = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        GetSite method = new GetSite() {
            @Override
            public String getPath() {
                return "not/a/real/path";
            }
        };
        connection.send(method, getSiteResponse -> {
            // Unexpected Success
            isSuccess.set(false);
            latch.countDown();
        }, () -> {
            // Expected Failure
            isSuccess.set(true);
            latch.countDown();
        });
        latch.await();
        assertTrue(isSuccess.get());
    }

    @Test
    public void networkErrorTest() throws InterruptedException {
        // Connect
        Connection connection = new Connection(TEST_INSTANCE, null);

        // Should Be Logged Out
        assertFalse(connection.hasToken());

        // Break Network Access
        String proxyKey = "socksProxyHost";
        String oldProxy = System.getProperty(proxyKey);
        System.setProperty(proxyKey, "127.0.0.1");

        // Test Login With Invalid Info
        AtomicBoolean isSuccess = new AtomicBoolean();
        CountDownLatch latch = new CountDownLatch(1);
        GetSite method = new GetSite();
        connection.send(method, getSiteResponse -> {
            // Unexpected Success
            isSuccess.set(false);
            latch.countDown();
        }, () -> {
            // Expected Failure
            isSuccess.set(true);
            latch.countDown();
        });
        latch.await();

        // Restore Network Access
        if (oldProxy == null) {
            System.clearProperty(proxyKey);
        } else {
            System.setProperty(proxyKey, oldProxy);
        }

        // Check Test Success
        assertTrue(isSuccess.get());
    }

    @Test
    public void callbackHelperTest() throws InterruptedException {
        // Connect
        Connection connection = new Connection(TEST_INSTANCE, null);

        // Should Be Logged Out
        assertFalse(connection.hasToken());

        // Setup Callback Helper
        CountDownLatch latch = new CountDownLatch(1);
        Runnable[] callback = new Runnable[1];
        connection.setCallbackHelper(runnable -> {
            callback[0] = runnable;
            latch.countDown();
        });

        // Test GetSite
        Boolean[] isSuccess = new Boolean[1];
        GetSite method = new GetSite();
        connection.send(method, getSiteResponse -> {
            // Success
            isSuccess[0] = true;
        }, () -> {
            // Error
            isSuccess[0] = false;
        });
        latch.await();
        assertNull(isSuccess[0]);
        assertNotNull(callback[0]);
        callback[0].run();
        assertTrue(isSuccess[0]);
    }
}
