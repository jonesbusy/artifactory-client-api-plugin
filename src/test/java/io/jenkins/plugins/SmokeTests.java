package io.jenkins.plugins;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.File;
import java.nio.file.Files;
import java.util.logging.Level;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.RealJenkinsRule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmokeTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmokeTests.class);

    @Rule
    public RealJenkinsRule jj = new RealJenkinsRule().withLogger(SmokeTests.class, Level.FINEST);

    @Test
    public void test() throws Throwable {
        jj.startJenkins();
        jj.runRemotely(SmokeTests::smokeTest);
    }

    public static void smokeTest(JenkinsRule r) throws Throwable {

        WireMockServer wireMock = new WireMockServer(8181);
        wireMock.start();

        File testFile = Files.createTempFile("text", ".txt").toFile();

        // PUT to upload artifact
        wireMock.stubFor(
                WireMock.put(WireMock.urlMatching("/my-generic-repo/.*")).willReturn(WireMock.okJson("{}")));

        // Test upload
        TestUtils.testUpload("my-generic-repo", wireMock.baseUrl(), testFile);
    }
}
