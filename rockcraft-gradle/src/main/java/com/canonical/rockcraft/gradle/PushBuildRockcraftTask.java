package com.canonical.rockcraft.gradle;

import com.canonical.rockcraft.builder.BuildRockcraftOptions;
import com.canonical.rockcraft.builder.RockBuilder;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import javax.inject.Inject;
import java.io.IOException;

/**
 * This task pushes rock image to the local docker
 */
public class PushBuildRockcraftTask extends DefaultTask {

    private BuildRockcraftOptions options;

    /**
     * Constructs PushRockcraftTask
     *
     * @param options - rockcraft options
     */
    @Inject
    public PushBuildRockcraftTask(BuildRockcraftOptions options) {
        this.options = options;
    }

    /**
     * The task action
     *
     * @throws IOException          - IO error while writing <i>rockcraft.yaml</i>
     * @throws InterruptedException - <i>rockcraft</i> process was aborted
     */
    @TaskAction
    public void pushRock() throws IOException, InterruptedException {
        RockBuilder.pushRock(RockSettingsFactory.createBuildRockProjectSettings(getProject()), options);
    }
}
