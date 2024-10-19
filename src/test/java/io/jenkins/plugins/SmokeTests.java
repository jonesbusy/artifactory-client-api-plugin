package io.jenkins.plugins;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import java.io.File;
import java.nio.file.Files;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.UploadableArtifact;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WireMockTest
public class SmokeTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(SmokeTests.class);

    @Test
    public void smokeTest(WireMockRuntimeInfo wmRuntimeInfo) throws Exception {

        // WireMock stub
        WireMock wireMock = wmRuntimeInfo.getWireMock();

        File testFile = Files.createTempFile("text", ".txt").toFile();

        // PUT to upload artifact
        wireMock.register(
                WireMock.put(WireMock.urlMatching("/my-generic-repo/.*")).willReturn(WireMock.okJson("{}")));

        try (Artifactory artifactory = ArtifactoryClientBuilder.create()
                .setUrl(wmRuntimeInfo.getHttpBaseUrl())
                .setUsername("fake")
                .setPassword("fake")
                .build()) {
            UploadableArtifact artifact =
                    artifactory.repository("my-generic-repo").upload("text.txt", testFile);
            artifact.withSize(Files.size(testFile.toPath()));
            artifact.withListener(
                    (bytesRead, totalBytes) -> LOGGER.info(String.format("Uploaded %d/%d", bytesRead, totalBytes)));
            artifact.doUpload();
        }
    }
}
