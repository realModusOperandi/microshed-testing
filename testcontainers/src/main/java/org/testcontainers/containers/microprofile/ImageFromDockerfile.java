package org.testcontainers.containers.microprofile;

import java.nio.file.Path;
import java.util.Optional;

import com.github.dockerjava.api.command.BuildImageCmd;

public class ImageFromDockerfile extends org.testcontainers.images.builder.ImageFromDockerfile {

    private Optional<Path> baseDir = Optional.empty();

    public ImageFromDockerfile() {
        super();
    }

    public ImageFromDockerfile(String dockerImageName) {
        super(dockerImageName);
    }

    public ImageFromDockerfile(String dockerImageName, boolean deleteOnExit) {
        super(dockerImageName, deleteOnExit);
    }

    @Override
    protected void configure(BuildImageCmd buildImageCmd) {
        baseDir.ifPresent(p -> buildImageCmd.withBaseDirectory(p.toFile()));
        super.configure(buildImageCmd);
    }

    public ImageFromDockerfile withBaseDirectory(Path baseDir) {
        this.baseDir = Optional.of(baseDir);
        return this;
    }

}