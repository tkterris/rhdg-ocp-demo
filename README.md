
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

### Setup

Build the project locally (creating the JAR which will be deployed to the RHDG cluster), populate environment variables (replacing as appropriate), and create the project:
```
export OCP_SERVER_URL=https://api.crc.testing:6443
export DEVUSER=developer
export DEVPASSWORD=developer
export ADMINUSER=kubeadmin
export ADMINPASSWORD=f5tRY-9RxxL-2TEMT-CykXc

oc login -u $DEVUSER -p $DEVPASSWORD $OCP_SERVER_URL
oc new-project rhdg-ocp-demo

mvn clean install
```

### Deployment steps

- Load the compiled server JAR onto an OpenShift PVC:
```
# The following steps (and the PVC reference in ocp-yaml/infinispan-cluster.yaml) are used to deploy custom code
oc login -u $ADMINUSER -p $ADMINPASSWORD $OCP_SERVER_URL
oc apply -f ocp-yaml/infinispan-libs.yaml
oc apply -f ocp-yaml/infinispan-libs-pod.yaml
oc wait --for=condition=ready --timeout=2m pod/infinispan-libs-pod
oc cp --no-preserve=true server/target/*.jar infinispan-libs-pod:/tmp/libs/
oc delete pod infinispan-libs-pod
```
- Create the Infinispan cluster and deploy the client application:
```
oc login -u $DEVUSER -p $DEVPASSWORD $OCP_SERVER_URL
oc apply -f ocp-yaml/infinispan-cluster.yaml
oc apply -f ocp-yaml/client-application.yaml
```

### Testing

The client application will have a route exposed, with a swagger UI available at `$ROUTE_URL/swagger-ui.html`. 

- Basic cache connectivity and functionality can be tested using the POST, GET, and DELETE HTTP methods on the `/infinispan/{key}` endpoint
- If a GET is performed on a key that hasn't been stored, the custom cache loader is used to generate a random value
- Cache connection information can be viewed in the `InfinispanService.java` file in the `client` project
- A simple remote task can be executed via the `/infinispan/removeTask/{key}` endpoint

To test with a user that fails authentication, change `INFINISPAN_USER` and `INFINISPAN_PASSWORD` in the deployment environment variables to 
use the `invalid.user` and `invalid.password` value from the secret. To test a user that authenticates but is unauthorized, use
`unauthorized.user` and `unauthorized.password`.

Cache nodes can be stopped, started, and scaled by changing the `replicas` parameter of the Data Grid operator.

### Cleanup

All resources in the project created for the demo can be deleted with:

```
oc delete all,secret,infinispan,cache -l app=rhdg-ocp-demo
```

The project can be deleted with:

```
oc delete project rhdg-ocp-demo
```

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

