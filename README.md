
# Data Grid Operator Demo

This project demonstrates some core functionality of the Red Hat Data Grid operator in OpenShift. 

## Scope

The following features are explored in this application:

- Server Configuration
  - Creating new caches
  - Enabling RBAC / security
  - Deploying custom code
  - Configuring custom cache loaders
- Cache functionality
  - Connecting to the RHDG cluster
  - Basic cache operations (gets and puts)
  - Executing remote tasks
- Cluster management
  - Node discovery and clustering
  - Starting and stopping RHDG nodes
  - Scaling the cluster

## Running the demo

### Prerequisites

- Access to an OpenShift cluster (for local testing, [OpenShift Local](https://developers.redhat.com/products/openshift-local/overview) is recommended)
- Red Hat Data Grid Operator is installed

### Deployment steps

- Build the server JAR, which will be deployed to the RHDG cluster
```
cd server
mvn clean install
```
- Populate environment variables for later commands (replace the below values)
```
export DEVUSER=developer
export DEVPASSWORD=developer
export ADMINUSER=kubeadmin
export ADMINPASSWORD=f5tRY-9RxxL-2TEMT-CykXc
export PROJECT=rhdg-demo
```
- Create the project and load the compiled server JAR onto an OpenShift PVC
```
oc login -u $DEVUSER -p $DEVPASSWORD https://api.crc.testing:6443
oc new-project $PROJECT
oc login -u $ADMINUSER -p $ADMINPASSWORD https://api.crc.testing:6443
# The following steps (and the PVC reference in ocp-yaml/datagrid-cluster.yaml) are used to deploy custom code
oc apply -f ocp-yaml/datagrid-libs.yaml
oc apply -f ocp-yaml/datagrid-libs-pod.yaml
oc wait --for=condition=ready --timeout=2m pod/datagrid-libs-pod
oc cp --no-preserve=true server/target/rhdg-ocp-demo-server-1.0-SNAPSHOT.jar datagrid-libs-pod:/tmp/libs/
oc delete pod datagrid-libs-pod
```
- Create the project and the Infinispan cluster
```
oc login -u $DEVUSER -p $DEVPASSWORD https://api.crc.testing:6443
oc apply -f ocp-yaml/datagrid-cluster.yaml
oc apply -f ocp-yaml/datagrid-cache.yaml # creates new caches, configures custom cache loaders
oc apply -f ocp-yaml/datagrid-cache-restricted.yaml
```
- Create a new Secret using the value from `infinispan-generated-secret`
```
INFINISPAN_USERNAME=developer
INFINISPAN_PASSWORD=<value from infinispan-generated-secret>
```
- Create a new deployment using "Import from Git"
  - Repo URL: `https://github.com/tkterris/rhdg-ocp-demo.git`
  - Context dir: `/client/`
  - Environment variables:
    - `CACHE_NAME=test-cache`
    - `INFINISPAN_SERNAME` and `INFINISPAN_PASSWORD` from the Secret created in the previous steps

### Testing

The client application will have a route exposed, with a swagger UI available at `$ROUTE_URL/swagger-ui.html`. 

- Basic cache connectivity and functionality can be tested using the POST, GET, and DELETE HTTP methods on the `/infinispan/{key}` endpoint
- If a GET is performed on a key that hasn't been stored, the custom cache loader is used to generate a random value
- Cache connection information can be viewed in the `InfinispanService.java` file in the `client` project
- A simple remote task can be executed via the `/infinispan/removeTask/{key}` endpoint

RBAC can be tested by changing the `CACHE_NAME` environment variable to `restricted-cache`. This cache exists on the RHDG cluster, but the 
`developer` user does not have permission to access it. The expected behavior is an error when cache operations are attempted.

Cache nodes can be stopped, started, and scaled by changing the `replicas` parameter of the Data Grid operator.

## Additional Info

### Debugging

Some issues were encountered using CRC, here are the workarounds:
- Infinispan cluster failing to start, due to insufficient memory.
  - Increase memory config: `crc config set memory 20000`
- DNS lookups for routes don't work in Chromium browsers
  - Use Firefox
- PVC creation failing due to CRC issue preventing PVs from being recycled in OpenShift Local
  - Set the following label: `oc label  --overwrite ns openshift-infra  pod-security.kubernetes.io/enforce=privileged`
  - Manually recycle failed PVs using `oc patch pv/$PV_NAME --type json -p '[{ "op": "remove", "path": "/spec/claimRef" }]'`

### Links

Application client code derived from <https://github.com/ngecom/openshiftSpringBoot>

[OpenShift Local](https://developers.redhat.com/products/openshift-local/overview)

[RHDG Operator Guide](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3/html/data_grid_operator_guide/index)

