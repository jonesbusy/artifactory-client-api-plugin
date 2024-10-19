package io.jenkins.plugins;

import java.io.File;
import java.nio.file.Files;
import org.jfrog.artifactory.client.Artifactory;
import org.jfrog.artifactory.client.ArtifactoryClientBuilder;
import org.jfrog.artifactory.client.UploadableArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestUtils.class);

    /**
     * Test uploading a file to an Artifactory server
     * @param url the URL of the Artifactory server
     * @param testFile the file to upload
     * @throws Exception if the file cannot be uploaded
     */
    public static void testUpload(String repo, String url, File testFile) throws Exception {
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
