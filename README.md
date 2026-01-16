# quarkus-playground

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./gradlew build -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./gradlew build -Dquarkus.native.enabled=true
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/quarkus-playground-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Kubernetes Deployment

The project includes a [Helm](https://helm.sh/) chart in the `helm/quarkus-playground/` directory for deploying the application to a Kubernetes cluster.

### Directory Structure

```
helm/
└── quarkus-playground/
    ├── Chart.yaml              # Helm chart metadata
    ├── values.yaml             # Default configuration values
    └── templates/
        ├── _helpers.tpl        # Template helpers
        ├── namespace.yaml      # Namespace template
        ├── deployment.yaml     # Deployment template
        └── service.yaml        # Service template
```

### Resources

The deployment creates the following resources:

- **Namespace**: `playground-namespace` - isolates application resources
- **Service**: `playground-quarkus-playground-service` - NodePort service exposing port 8080
- **Deployment**: `playground-quarkus-playground-deployment` - single replica deployment

### Configuration

Key values in `values.yaml`:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `namespace.create` | Create namespace | `true` |
| `namespace.name` | Namespace name | `playground-namespace` |
| `replicaCount` | Number of replicas | `1` |
| `image.repository` | Image repository | `quarkus/quarkus-playground` |
| `image.tag` | Image tag | `1.0` |
| `image.pullPolicy` | Image pull policy | `Always` |
| `service.type` | Service type | `NodePort` |
| `service.port` | Service port | `8080` |
| `resources.requests.memory` | Memory request | `128Mi` |
| `resources.requests.cpu` | CPU request | `100m` |
| `resources.limits.memory` | Memory limit | `256Mi` |
| `resources.limits.cpu` | CPU limit | `500m` |

### Building the Docker Image

Before deploying, build the JVM Docker image:

```shell script
./gradlew build
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-playground:1.0 .
```

### Deploying to Kubernetes

Preview the generated manifests:

```shell script
helm template my-release helm/quarkus-playground/
```

Install the Helm chart:

```shell script
helm install my-release helm/quarkus-playground/
```

Upgrade an existing release:

```shell script
helm upgrade my-release helm/quarkus-playground/
```

Override values during installation:

```shell script
helm install my-release helm/quarkus-playground/ --set replicaCount=3 --set image.tag=2.0
```

### Accessing the Application

Once deployed, access the application via the NodePort service:

```shell script
# Get the NodePort assigned to the service
kubectl get svc playground-quarkus-playground-service -n playground-namespace

# Access the application (replace <NODE_IP> and <NODE_PORT> with actual values)
curl http://<NODE_IP>:<NODE_PORT>/hello
```

### Cleanup

To remove all deployed resources:

```shell script
helm uninstall my-release
```

## Related Guides

- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
