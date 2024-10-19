package io.jenkins.plugins;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.io.File;
import java.nio.file.Files;
import java.util.logging.Level;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.UploadableArtifact;
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
    public void smokeUploadTest() throws Throwable {
        jj.startJenkins();
        jj.runRemotely(SmokeTests::smokeTest);
    }

    /**
     * Smoke test for the plugin
     * @param r the Jenkins rule
     * @throws Exception if the test fails
     */
    public static void smokeTest(JenkinsRule r) throws Exception {

        WireMockServer wireMock = new WireMockServer(8181);
        wireMock.start();

        File testFile = Files.createTempFile("text", ".txt").toFile();

        // PUT to upload artifact
        wireMock.stubFor(
                WireMock.put(WireMock.urlMatching("/my-generic-repo/.*")).willReturn(WireMock.okJson("{}")));

        // Test upload
        testUpload("my-generic-repo", wireMock.baseUrl(), testFile);
    }

    /**
     * Test uploading a file to an Artifactory server
     * @param url the URL of the Artifactory server
     * @param testFile the file to upload
     * @throws Exception if the file cannot be uploaded
     */
    private static void testUpload(String repo, String url, File testFile) throws Exception {
        try (Artifactory artifactory = ArtifactoryClientBuilder.create()
                .setUrl(url)
                .setUsername("fake")
                .setPassword("fake")
                .build()) {
            UploadableArtifact artifact = artifactory.repository(repo).upload("text.txt", testFile);
            artifact.withSize(Files.size(testFile.toPath()));
            artifact.withListener(
                    (bytesRead, totalBytes) -> LOGGER.info(String.format("Uploaded %d/%d", bytesRead, totalBytes)));
            artifact.doUpload();
        }
    }
}
