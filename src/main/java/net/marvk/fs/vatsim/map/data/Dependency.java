package net.marvk.fs.vatsim.map.data;

import javafx.beans.property.ReadOnlyStringProperty;

public class Dependency {
    private final ReadOnlyStringProperty licenseName;
    private final ReadOnlyStringProperty projectName;
    private final ReadOnlyStringProperty groupId;
    private final ReadOnlyStringProperty artifactId;
    private final ReadOnlyStringProperty version;
    private final ReadOnlyStringProperty projectUrl;

    public Dependency(
            final String licenseName,
            final String projectName,
            final String groupId,
            final String artifactId,
            final String version,
            final String projectUrl
    ) {
        this.licenseName = new ImmutableStringProperty(licenseName);
        this.projectName = new ImmutableStringProperty(projectName);
        this.groupId = new ImmutableStringProperty(groupId);
        this.artifactId = new ImmutableStringProperty(artifactId);
        this.version = new ImmutableStringProperty(version);
        this.projectUrl = new ImmutableStringProperty(projectUrl);
    }

    public String getLicenseName() {
        return licenseName.get();
    }

    public ReadOnlyStringProperty licenseNameProperty() {
        return licenseName;
    }

    public String getProjectName() {
        return projectName.get();
    }

    public ReadOnlyStringProperty projectNameProperty() {
        return projectName;
    }

    public String getGroupId() {
        return groupId.get();
    }

    public ReadOnlyStringProperty groupIdProperty() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId.get();
    }

    public ReadOnlyStringProperty artifactIdProperty() {
        return artifactId;
    }

    public String getVersion() {
        return version.get();
    }

    public ReadOnlyStringProperty versionProperty() {
        return version;
    }

    public String getProjectUrl() {
        return projectUrl.get();
    }

    public ReadOnlyStringProperty projectUrlProperty() {
        return projectUrl;
    }
}
