/**
 * Created by PixelPlayer Studios Development Team
 * Date: 2024-08-11
 * Time: 21:16
 */

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.command.PullImageResultCallback;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DockerService {
    private DockerClient dockerClient;
    private String containerName;
    private String serviceType;
    private List<String> environment;
    private List<String> ports;
    private boolean autoRestart;
    private String imageName;

    /**
     * Constructor to initialize the DockerService instance with a DockerClient.
     * @param dockerClient The DockerClient instance used to communicate with the Docker daemon.
     */
    public DockerService(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    /**
     * Getter for the container name.
     * @return The name of the container.
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Setter for the container name.
     * @param containerName The name of the container to be set.
     */
    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    /**
     * Getter for the service type.
     * @return The type of the service (e.g., "nginx").
     */
    public String getServiceType() {
        return serviceType;
    }

    /**
     * Setter for the service type.
     * @param serviceType The type of the service to be set (e.g., "nginx").
     */
    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    /**
     * Getter for the environment variables.
     * @return A list of environment variables set for the container.
     */
    public List<String> getEnvironment() {
        return environment;
    }

    /**
     * Setter for the environment variables.
     * @param environment A list of environment variables to be set for the container.
     */
    public void setEnvironment(List<String> environment) {
        this.environment = environment;
    }

    /**
     * Getter for the ports.
     * @return A list of port mappings set for the container.
     */
    public List<String> getPorts() {
        return ports;
    }

    /**
     * Setter for the ports.
     * @param ports A list of port mappings to be set for the container (e.g., "8080:80").
     */
    public void setPorts(List<String> ports) {
        this.ports = ports;
    }

    /**
     * Getter for the auto-restart option.
     * @return True if the container is set to automatically restart, false otherwise.
     */
    public boolean isAutoRestart() {
        return autoRestart;
    }

    /**
     * Setter for the auto-restart option.
     * @param autoRestart True to enable automatic restart of the container, false to disable.
     */
    public void setAutoRestart(boolean autoRestart) {
        this.autoRestart = autoRestart;
    }

    /**
     * Getter for the image name.
     * @return The name of the Docker image used for the container.
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Setter for the image name.
     * @param imageName The name of the Docker image to be used for the container.
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Creates a new container based on the provided configuration.
     * @return The ID of the created container.
     */
    public String createContainer() {
        HostConfig hostConfig = new HostConfig();

        // Configure ports
        if (ports != null && !ports.isEmpty()) {
            Ports portBindings = new Ports();
            for (String port : ports) {
                String[] split = port.split(":");
                portBindings.bind(ExposedPort.tcp(Integer.parseInt(split[1])), Ports.Binding.bindPort(Integer.parseInt(split[0])));
            }
            hostConfig.withPortBindings(portBindings);
        }

        // Configure auto-restart
        if (autoRestart) {
            RestartPolicy restartPolicy = RestartPolicy.alwaysRestart();
            hostConfig.withRestartPolicy(restartPolicy);
        }

        CreateContainerResponse container = dockerClient.createContainerCmd(imageName)
                .withName(containerName)
                .withEnv(environment)
                .withHostConfig(hostConfig)
                .exec();

        return container.getId();
    }

    /**
     * Starts the specified container.
     * @param containerId The ID of the container to be started.
     */
    public void startContainer(String containerId) {
        dockerClient.startContainerCmd(containerId).exec();
    }

    /**
     * Stops the specified container.
     * @param containerId The ID of the container to be stopped.
     */
    public void stopContainer(String containerId) {
        dockerClient.stopContainerCmd(containerId).exec();
    }

    /**
     * Restarts the specified container.
     * @param containerId The ID of the container to be restarted.
     */
    public void restartContainer(String containerId) {
        dockerClient.restartContainerCmd(containerId).exec();
    }

    /**
     * Pauses the specified container.
     * @param containerId The ID of the container to be paused.
     */
    public void pauseContainer(String containerId) {
        dockerClient.pauseContainerCmd(containerId).exec();
    }

    /**
     * Unpauses the specified container.
     * @param containerId The ID of the container to be unpaused.
     */
    public void unpauseContainer(String containerId) {
        dockerClient.unpauseContainerCmd(containerId).exec();
    }

    /**
     * Removes the specified container.
     * @param containerId The ID of the container to be removed.
     */
    public void removeContainer(String containerId) {
        dockerClient.removeContainerCmd(containerId).exec();
    }

    /**
     * Retrieves the logs of the specified container.
     *
     * @param containerId The ID of the container whose logs are to be retrieved.
     * @return An InputStream containing the container's logs.
     */
    public InputStream getContainerLogs(String containerId) {
        return dockerClient.logContainerCmd(containerId)
                .withStdOut(true)
                .withStdErr(true)
                .exec();
    }

    /**
     * Retrieves the status of the specified container.
     *
     * @param containerId The ID of the container whose status is to be retrieved.
     * @return The status of the container (e.g., "running", "exited").
     */
    public String getContainerStatus(String containerId) {
        Container container = dockerClient.listContainersCmd()
                .withIdFilter(List.of(containerId))
                .exec()
                .stream()
                .findFirst()
                .orElse(null);

        return (container != null) ? container.getState() : "Container not found";
    }

    /**
     * Retrieves a list of container IDs.
     *
     * @param showAll If true, all containers are listed, including stopped ones. If false, only running containers are listed.
     * @return A list of container IDs.
     */
    public List<String> listContainers(boolean showAll) {
        return dockerClient.listContainersCmd()
                .withShowAll(showAll)
                .exec()
                .stream()
                .map(Container::getId)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves statistics for the specified container.
     *
     * @param containerId The ID of the container whose statistics are to be retrieved.
     * @return A Statistics object containing the container's statistics.
     */
    public Statistics getContainerStats(String containerId) {
        return dockerClient.statsCmd(containerId).exec(new ResultCallback.Adapter<Statistics>() {
        }).awaitResult();
    }

    /**
     * Pulls a Docker image from the registry.
     * @param imageName The name of the Docker image to be pulled (e.g., "nginx").
     */
    public void pullImage(String imageName) {
        dockerClient.pullImageCmd(imageName)
                .exec(new PullImageResultCallback())
                .awaitCompletion();
    }

    /**
     * Retrieves a list of image IDs.
     * @return A list of image IDs available on the Docker host.
     */
    public List<String> listImages() {
        return dockerClient.listImagesCmd()
                .exec()
                .stream()
                .map(Image::getId)
                .collect(Collectors.toList());
    }

    /**
     * Removes a Docker image.
     * @param imageId The ID of the Docker image to be removed.
     */
    public void removeImage(String imageId) {
        dockerClient.removeImageCmd(imageId).exec();
    }

    /**
     * Creates a Docker volume with the specified name.
     * @param volumeName The name of the Docker volume to be created.
     */
    public void createVolume(String volumeName) {
        dockerClient.createVolumeCmd()
                .withName(volumeName)
                .exec();
    }

    /**
     * Retrieves a list of Docker volume names.
     * @return A list of volume names available on the Docker host.
     */
    public List<String> listVolumes() {
        return dockerClient.listVolumesCmd()
                .exec()
                .getVolumes()
                .stream()
                .map(Volume::getName)
                .collect(Collectors.toList());
    }

    /**
     * Removes a Docker volume.
     * @param volumeName The name of the Docker volume to be removed.
     */
    public void removeVolume(String volumeName) {
        dockerClient.removeVolumeCmd(volumeName).exec();
    }
}
