package com.canonical.rockcraft.builder;

import com.canonical.rockcraft.util.POMUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class POMUtilTest {
    @TempDir
    private File tempDir;

    @Test
    public void testAddRepository() throws IOException, ParserConfigurationException, TransformerException, SAXException {
        String project = "<project><repositories></repositories></project>";
        Path pom = Files.createTempFile(tempDir.toPath(), "temp","pom.xml");
        Files.write(pom, project.getBytes() );
        POMUtil.addRepositoryToPom(pom.toFile(), "repo-id", "repo-name", "repo-url");
        String data = new String(Files.readAllBytes(pom));
        assertTrue(data.contains("<repository>"));
        assertTrue(data.contains("<name>repo-name</name>"));
        assertTrue(data.contains("<id>repo-id</id>"));
        assertTrue(data.contains("<pluginRepository>"));
        // we added only 1 plugin repository
        assertEquals(data.lastIndexOf("<pluginRepositories>"), data.indexOf("<pluginRepositories>"));
        // we have only 1 repository
        assertEquals(data.lastIndexOf("<repositories>"), data.indexOf("<repositories>"));
    }
}
