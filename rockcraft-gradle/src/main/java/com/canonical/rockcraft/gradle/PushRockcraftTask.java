package com.canonical.rockcraft.gradle;

import com.canonical.rockcraft.builder.RockBuilder;
import com.canonical.rockcraft.builder.RockcraftOptions;
import org.gradle.api.tasks.TaskAction;
import org.gradle.work.DisableCachingByDefault;

import javax.inject.Inject;
import java.io.IOException;

/**
 * This task pushes rock image to the local docker
 */
@DisableCachingByDefault(because = "This task runs an external tool that builds an OCI container. It needs to perform its own up-to-date checks.")
public class PushRockcraftTask extends AbstractRockcraftTask {

    /**
     * Constructs PushRockcraftTask
     *
     * @param options - rockcraft options
     */
    @Inject
    public PushRockcraftTask(RockcraftOptions options) {
        super(options);
    }

    /**
     * The task action
     *
     * @throws IOException          - IO error while writing <i>rockcraft.yaml</i>
     * @throws InterruptedException - <i>rockcraft</i> process was aborted
     */
    @TaskAction
    public void pushRock() throws IOException, InterruptedException {
        RockBuilder.pushRock(RockSettingsFactory.createRockProjectSettings(getProject()), getOptions());
    }
}
