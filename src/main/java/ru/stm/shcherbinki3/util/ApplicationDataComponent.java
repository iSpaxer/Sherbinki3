package ru.stm.shcherbinki3.util;

import lombok.Getter;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Getter
@Component
public class ApplicationDataComponent {

    private final List<Map<String, String>> technologies;

    @Value("${app.version}")
    private String version;

    public ApplicationDataComponent() {
        this.technologies = initTechnologies();
    }

    public String glueEndpoint(String path) {
        return "/api/v" + version + path;
    }

    public String[] glueEndpoints(String... paths) {
        return Arrays.stream(paths)
                .filter(Objects::nonNull)
                .map(path -> path.trim().replaceAll("/+$", ""))
                .map(path -> "/api/v" + version + path)
                .toArray(String[]::new);
    }

    public String[] glueEndpoints(List<String> paths) {
        return paths.stream()
                .filter(Objects::nonNull)
                .map(path -> path.trim().replaceAll("/+$", ""))
                .map(path -> "/api/v" + version + path)
                .toArray(String[]::new);
    }


    private List<Map<String, String>> initTechnologies() {
        List<Map<String, String>> techList = new ArrayList<>();

        try {
            File pomFile = new File("pom.xml");
            SAXBuilder builder = new SAXBuilder();
            Document document = builder.build(pomFile);
            Element root = document.getRootElement();
            Namespace ns = root.getNamespace();

            Map<String, String> propertiesMap = new HashMap<>();
            Element propertiesElement = root.getChild("properties", ns);
            if (propertiesElement != null) {
                for (Element prop : propertiesElement.getChildren()) {
                    propertiesMap.put(prop.getName(), prop.getTextTrim());
                }
            }

            Element dependencies = root.getChild("dependencies", ns);
            if (dependencies != null) {
                for (Element dependency : dependencies.getChildren("dependency", ns)) {

                    String groupId = dependency.getChildText("groupId", ns);
                    String artifactId = dependency.getChildText("artifactId", ns);
                    String version = dependency.getChildText("version", ns);

                    Map<String, String> tech = new HashMap<>();
                    tech.put("groupId", groupId);
                    tech.put("artifactId", artifactId);

                    if (version != null && version.startsWith("${") && version.endsWith("}")) {
                        tech.put("version",
                                 propertiesMap.getOrDefault(
                                         version.substring(2, version.length() - 1), "unknown")
                        );
                    } else {
                        tech.put("version", propertiesMap.get("spring-boot.version"));
                    }
                    techList.add(tech);
                }
            }

        } catch (IOException | JDOMException e) {
            throw new RuntimeException("Error with pom.xml");
        }

        return techList;
    }
}
