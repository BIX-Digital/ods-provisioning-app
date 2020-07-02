package org.opendevstack.provision.config;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.PostConstruct;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "jenkinspipeline")
@Configuration
public class JenkinsPipelineProperties {

  private final Map<String, Quickstarter> quickstarter = new HashMap<>();
  private final Map<String, Quickstarter> adminjobs = new HashMap<>();
  public static final String CREATE_PROJECTS = "create-projects";
  public static final String DELETE_PROJECTS = "delete-projects";
  public static final String QUICKSTARTER_DELETE_COMPONENTS = "delete-components";
  public static final List<String> adminJobs =
      Arrays.asList(CREATE_PROJECTS, DELETE_PROJECTS, QUICKSTARTER_DELETE_COMPONENTS);

  @PostConstruct
  public void setNameInAllQuickstarters() {
    initQuickstarter(this.quickstarter, QuickstarterType.component);
    initQuickstarter(this.adminjobs, QuickstarterType.adminjob);
    this.adminjobs
        .entrySet()
        .forEach(entry -> entry.getValue().setType(QuickstarterType.component));
  }

  private void initQuickstarter(Map<String, Quickstarter> quickstarter, QuickstarterType type) {
    quickstarter
        .entrySet()
        .forEach(
            entry -> {
              Quickstarter qs = entry.getValue();
              qs.setName(entry.getKey());
              qs.setType(type);
            });
  }

  public Map<String, Quickstarter> getQuickstarter() {
    return quickstarter;
  }

  public Map<String, Quickstarter> getAdminjobs() {
    return adminjobs;
  }

  public void setComponentQuickstarter(Map<String, Quickstarter> componentQuickstarter) {
    this.quickstarter.putAll(componentQuickstarter);
  }

  public void addQuickstarter(Quickstarter quickstarter) {
    getMapForType(quickstarter.getType()).put(quickstarter.getName(), quickstarter);
  }

  public Quickstarter getCreateProjectQuickstarter() {
    return getAdminjob(CREATE_PROJECTS);
  }

  public Quickstarter getDeleteProjectsQuickstarter() {
    return getAdminjob(DELETE_PROJECTS);
  }

  public Quickstarter getDeleteComponentsQuickstarter() {
    return getAdminjob(QUICKSTARTER_DELETE_COMPONENTS);
  }

  private Quickstarter getAdminjob(String name) {
    return getQuickstarter(name, QuickstarterType.adminjob);
  }

  public boolean isDeleteComponentJob(String jobId) {
    return QUICKSTARTER_DELETE_COMPONENTS.equals(jobId);
  }

  public boolean isAdminjob(String jobId) {
    return adminJobs.contains(jobId);
  }

  private Quickstarter getQuickstarter(String name, QuickstarterType type) {
    return getMapForType(type).values().stream()
        .filter(q -> q.getName().equals(name))
        .findFirst()
        .orElseThrow(exceptionSupplier(name, type));
  }

  private Map<String, Quickstarter> getMapForType(QuickstarterType type) {
    return type.equals(QuickstarterType.component) ? this.quickstarter : this.adminjobs;
  }

  private Supplier<RuntimeException> exceptionSupplier(String name, QuickstarterType type) {
    return () ->
        new RuntimeException(
            format(
                "Invalid configuration: %s-quickstarter with name '%s' is not found in configuration",
                type, name));
  }
}
