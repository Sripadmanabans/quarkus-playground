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

The project includes Kubernetes manifests in the `k8s/` directory for deploying the application to a Kubernetes cluster using [Kustomize](https://kustomize.io/).

### Directory Structure

```
k8s/
├── kustomization.yaml  # Kustomize configuration
├── namespace.yaml      # Namespace definition
├── deployment.yaml     # Deployment definition
└── service.yaml        # Service definition
```

### Resources

The deployment creates the following resources:

- **Namespace**: `playground-namespace` - isolates application resources
- **Service**: `playground-service` - NodePort service exposing port 8080
- **Deployment**: `playground-deployment` - single replica deployment

The `kustomization.yaml` uses:
- `namePrefix: playground-` to prefix all resource names
- `namespace` to set the target namespace for all resources
- `labels` to apply common labels across all resources

### Building the Docker Image

Before deploying, build the native Docker image:

```shell script
./gradlew build -Dquarkus.native.enabled=true -Dquarkus.native.container-build=true
docker build -f src/main/docker/Dockerfile.native-micro -t quarkus/quarkus-playground:1.0 .
```

### Deploying to Kubernetes

Preview the generated manifests:

```shell script
kubectl kustomize k8s/
```

Apply the Kubernetes manifests:

```shell script
kubectl apply -k k8s/
```

### Accessing the Application

Once deployed, access the application via the NodePort service:

```shell script
# Get the NodePort assigned to the service
kubectl get svc playground-service -n playground-namespace

# Access the application (replace <NODE_IP> and <NODE_PORT> with actual values)
curl http://<NODE_IP>:<NODE_PORT>/hello
```

### Cleanup

To remove all deployed resources:

```shell script
kubectl delete -k k8s/
```

## Related Guides

- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
