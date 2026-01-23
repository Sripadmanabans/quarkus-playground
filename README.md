# quarkus-playground

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Features

- **REST API** with Kotlin coroutines support
- **MongoDB** integration using the reactive client
- **Redis Cluster** integration using the reactive Quarkus Redis client
- **Kubernetes deployment** with Helm charts, Percona MongoDB Operator, and Bitnami Redis Cluster

## API Endpoints

### Greeting

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/hello?name={name}` | Returns a greeting message |

### Notes (MongoDB CRUD)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/notes` | List all notes |
| GET | `/notes/{id}` | Get a note by ID |
| POST | `/notes` | Create a new note |
| PUT | `/notes/{id}` | Update an existing note |
| DELETE | `/notes/{id}` | Delete a note |

**Request/Response format:**

```json5
// POST/PUT request body
{
  "title": "My Note",
  "content": "Note content here"
}
```
```json5
// Response
{
  "id": "507f1f77bcf86cd799439011",
  "title": "My Note",
  "content": "Note content here"
}
```

### Increment (Redis CRUD)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/increment` | List all keys |
| GET | `/increment/{key}` | Get value for a key |
| POST | `/increment` | Create a new key with initial value |
| PUT | `/increment/{key}` | Increment a key by a value |
| DELETE | `/increment/{key}` | Delete a key |

**Request/Response format:**

```json5
// POST request body
{
  "key": "my-counter",
  "value": 0
}
```
```json5
// GET response
{
  "key": "my-counter",
  "value": 42
}
```

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./gradlew quarkusDev
```

> **_NOTE:_** In dev mode, Quarkus Dev Services automatically starts MongoDB and Redis containers. No manual setup required!

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it's not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

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

The project includes a [Helm](https://helm.sh/) chart in the `helm/quarkus-playground/` directory for deploying the application to a Kubernetes cluster. MongoDB is deployed separately using the [Percona Operator for MongoDB](https://docs.percona.com/percona-operator-for-mongodb/).

### Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│ Kubernetes Cluster                                                          │
│                                                                             │
│  ┌─────────────────┐     ┌────────────────────────────────────────────────┐│
│  │ psmdb namespace │     │ playground namespace                           ││
│  │                 │     │                                                ││
│  │ ┌─────────────┐ │     │  ┌──────────┐      ┌─────────────┐            ││
│  │ │  Percona    │ │     │  │          │─────▶│   MongoDB   │            ││
│  │ │  Operator   │─┼─────┼─▶│ Quarkus  │      │  (Percona)  │            ││
│  │ └─────────────┘ │     │  │   App    │      └─────────────┘            ││
│  └─────────────────┘     │  │          │      ┌─────────────┐            ││
│                          │  │          │─────▶│   Redis     │            ││
│                          │  └──────────┘      │  (Bitnami)  │            ││
│                          │                    └─────────────┘            ││
│                          └────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────────────────────┘
```

The Percona Operator (installed in `psmdb` namespace) manages the MongoDB cluster deployed in the `playground` namespace. The Bitnami Redis Cluster is deployed as a Helm subchart. The Quarkus application connects to MongoDB using credentials automatically generated by Percona, and to Redis Cluster via the headless service.

### Directory Structure

```
helm/
└── quarkus-playground/
    ├── Chart.yaml                       # Helm chart metadata
    ├── Chart.lock                       # Dependency lock file
    ├── values.yaml                      # Default configuration values
    ├── mongo-values.yaml                # Percona MongoDB values (production)
    ├── mongo-local-values.yaml          # Percona MongoDB values (local/dev)
    ├── redis-values.yaml                # Bitnami Redis Cluster values
    ├── charts/                          # Packaged dependency charts
    │   ├── psmdb-db-1.21.2.tgz         # Percona MongoDB chart dependency
    │   └── redis-cluster-13.0.4.tgz    # Bitnami Redis Cluster chart dependency
    └── templates/
        ├── _helpers.tpl                 # Template helpers
        ├── namespace.yaml               # Namespace template
        ├── deployment.yaml              # Deployment template
        └── service.yaml                 # Service template
.helmignore                              # Files to exclude from Helm packaging
```

### Prerequisites

**1. Add the Percona Helm repository:**

```shell script
helm repo add percona https://percona.github.io/percona-helm-charts/
helm repo update
```

**2. Create the namespace** for the application and MongoDB:

```shell script
kubectl create namespace playground
```

**3. Install the Percona MongoDB Operator:**

```shell script
helm install psmdb-operator percona/psmdb-operator \
  --namespace psmdb \
  --create-namespace
```

### Configuration

Key values in `values.yaml`:

| Parameter | Description | Default |
|-----------|-------------|---------|
| `namespace.create` | Create namespace | `false` |
| `namespace.name` | Namespace name | `playground` |
| `quarkus.replicaCount` | Number of app replicas | `1` |
| `quarkus.image.repository` | Image repository | `quarkus/quarkus-playground` |
| `quarkus.image.tag` | Image tag | `1.0` |
| `quarkus.image.pullPolicy` | Image pull policy | `IfNotPresent` |
| `quarkus.service.type` | Service type | `NodePort` |
| `quarkus.service.port` | Service port | `8080` |
| `quarkus.resources.requests.memory` | Memory request | `128Mi` |
| `quarkus.resources.requests.cpu` | CPU request | `100m` |
| `quarkus.resources.limits.memory` | Memory limit | `256Mi` |
| `quarkus.resources.limits.cpu` | CPU limit | `500m` |

> **Note:** The application automatically reads MongoDB credentials from the Percona-generated secret (`<clusterName>-mongo-secrets`).

### Building the Docker Image

Before deploying, build the JVM Docker image:

```shell script
./gradlew build
docker build -f src/main/docker/Dockerfile.jvm -t quarkus/quarkus-playground:1.0 .
```

### Deploying to Kubernetes

**Ensure MongoDB is running first** (see Prerequisites above).

#### Preview the generated manifests:

```shell script
helm template my-release helm/quarkus-playground/ \
  --values helm/quarkus-playground/values.yaml \
  --values helm/quarkus-playground/mongo-values(|-local).yaml
```

#### Install the Helm chart:

```shell script
helm install my-release helm/quarkus-playground/ \
  --namespace playground \
  --values helm/quarkus-playground/values.yaml \
  --values helm/quarkus-playground/mongo-values(|-local).yaml
```

#### Upgrade an existing release:

```shell script
helm upgrade my-release helm/quarkus-playground/ \
  --namespace playground \
  --values helm/quarkus-playground/values.yaml \
  --values helm/quarkus-playground/mongo-values(|-local).yaml
```

#### Override values during installation:

```shell script
helm install my-release helm/quarkus-playground/ \
  --namespace playground \
  --values helm/quarkus-playground/values.yaml \
  --values helm/quarkus-playground/mongo-values(|-local).yaml
  --set quarkus.replicaCount=3 \
  --set quarkus.image.tag=2.0
```

### Accessing the Application

Once deployed, access the application via the NodePort service:

```shell script
# Get the NodePort assigned to the service
kubectl get svc -n playground

# Access the application (replace <NODE_IP> and <NODE_PORT> with actual values)
curl http://<NODE_IP>:<NODE_PORT>/hello
curl http://<NODE_IP>:<NODE_PORT>/notes
curl http://<NODE_IP>:<NODE_PORT>/increment
```

### Cleanup

To remove all deployed resources:

**1. Remove the Quarkus application:**

```shell script
helm uninstall my-release --namespace playground
```

**2. Delete the MongoDB cluster:**

```shell script
kubectl delete psmdb {my-release}-mongo -n playground
```

**3. Clean up remaining resources** (PVCs and Secrets are retained by default to prevent data loss):

```shell script
# List and delete PVCs
kubectl get pvc -n playground
kubectl delete pvc -l app.kubernetes.io/instance={my-release}-mongo -n playground

# List and delete Secrets
kubectl get secrets -n playground
kubectl delete secret -l app.kubernetes.io/instance={my-release}-mongo -n playground
```

**4. Delete the namespace** (optional):

```shell script
kubectl delete namespace playground
```

**5. Remove the Percona Operator** (optional, if no other MongoDB clusters depend on it):

```shell script
helm uninstall psmdb-operator --namespace psmdb
kubectl delete namespace psmdb
```

> **Note:** See the [Percona documentation](https://docs.percona.com/percona-operator-for-mongodb/delete.html) for detailed cleanup instructions.

### Percona MongoDB Values Files

Two configuration files are provided for deploying MongoDB:

| File | Use Case              | Replicas | Sharding |
|------|-----------------------|----------|----------|
| `mongo-local-values.yaml` | Local                 | 1 | Disabled |
| `mongo-values.yaml` | Development | 3 | Enabled |

**Local values** use relaxed settings (`unsafeFlags.replsetSize: true`) suitable for single-node development clusters.

**Development values** configure a 3-node replica set with sharding enabled for high availability and horizontal scaling.

> **Note:** The Helm chart includes MongoDB and Redis Cluster as dependencies (defined in `Chart.yaml`). The dependency charts are packaged in the `charts/` directory and locked in `Chart.lock`. To update dependencies, run `helm dependency update helm/quarkus-playground/`.

### Redis Cluster Configuration

Redis is deployed using the Bitnami Redis Cluster Helm chart as a subchart dependency. The configuration is defined in `redis-values.yaml`.

| Feature | Configuration |
|---------|---------------|
| Cluster Mode | 6 nodes (3 masters + 3 replicas) |
| Authentication | Disabled (for local development) |
| Persistence | Enabled with 8Gi PVC per node |
| Client Type | Cluster |

The Quarkus application connects to Redis via the headless service at `<release>-redis-cluster-headless.<namespace>.svc.cluster.local:6379`.

## Future Enhancements

The MongoDB setup supports additional features:

- **Backups**: Enable in `mongo-values.yaml` and configure storage (S3, GCS, Azure)
- **Monitoring**: Enable PMM (Percona Monitoring and Management) integration
- **Failover Testing**: The 3-node replica set supports automatic failover

## Related Guides

- Kotlin ([guide](https://quarkus.io/guides/kotlin)): Write your services in Kotlin
- MongoDB ([guide](https://quarkus.io/guides/mongodb)): Connect to MongoDB datastores
- Redis ([guide](https://quarkus.io/guides/redis)): Connect to Redis datastores
- Percona Operator ([docs](https://docs.percona.com/percona-operator-for-mongodb/)): MongoDB operator for Kubernetes
- Bitnami Redis Cluster ([chart](https://github.com/bitnami/charts/tree/main/bitnami/redis-cluster)): Redis Cluster Helm chart
