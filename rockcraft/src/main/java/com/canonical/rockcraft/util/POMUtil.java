/*
 * Copyright 2025 Canonical Ltd.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.canonical.rockcraft.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class POMUtil {
    /**
     * Adds repository to POM file. Does not check if repository already exists
     * @param pomFile - file to update
     * @param repoId - repository id
     * @param repoName - repository name
     * @param repoUrl - repository URL
     * @throws ParserConfigurationException - failed to configure xml parser
     * @throws IOException - failed to read/write the file
     * @throws SAXException - failed to parse the file
     * @throws TransformerException - failed to save the false
     */
    public static void addRepositoryToPom(File pomFile,
                                          String repoId,
                                          String repoName,
                                          String repoUrl)
            throws ParserConfigurationException, IOException, SAXException, TransformerException {

        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(pomFile);

        Node projectNode = doc.getElementsByTagName("project").item(0);
        if (projectNode == null) {
            throw new IllegalArgumentException("Invalid POM file: 'project' element not found.");
        }

        Node repositoriesNode = null;
        Node pluginRepositoriesNode = null;
        NodeList projectChildren = projectNode.getChildNodes();
        for (int i = 0; i < projectChildren.getLength(); i++) {
            Node child = projectChildren.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("repositories")) {
                repositoriesNode = child;
            }
            if (child.getNodeType() == Node.ELEMENT_NODE && child.getNodeName().equals("pluginRepositories")) {
                pluginRepositoriesNode = child;
            }

        }

        if (pluginRepositoriesNode == null) {
            pluginRepositoriesNode = doc.createElement("pluginRepositories");
            projectNode.appendChild(pluginRepositoriesNode);
        }
        if (repositoriesNode == null) {
            repositoriesNode = doc.createElement("repositories");
            projectNode.appendChild(repositoriesNode);
        }

        Element repositoryElement = doc.createElement("repository");
        appendRepositoryDefinition(repoId, repoName, repoUrl, doc, repositoryElement);
        repositoriesNode.appendChild(repositoryElement);

        Element pluginRepositoryElement = doc.createElement("pluginRepository");
        appendRepositoryDefinition(repoId, repoName, repoUrl, doc, pluginRepositoryElement);
        pluginRepositoriesNode.appendChild(pluginRepositoryElement);


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(pomFile);
        transformer.transform(source, result);
    }

    private static void appendRepositoryDefinition(String repoId, String repoName, String repoUrl, Document doc, Element repositoryElement) {
        Element idElement = doc.createElement("id");
        idElement.setTextContent(repoId);
        repositoryElement.appendChild(idElement);

        Element nameElement = doc.createElement("name");
        nameElement.setTextContent(repoName);
        repositoryElement.appendChild(nameElement);

        Element urlElement = doc.createElement("url");
        urlElement.setTextContent(repoUrl);
        repositoryElement.appendChild(urlElement);
    }
}
