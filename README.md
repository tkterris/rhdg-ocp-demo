
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
- The Data Grid Operator is installed
- Helm is installed and the chart repository at <https://charts.openshift.io/> has been added: `helm repo add openshift-helm-charts https://charts.openshift.io/`

### Setup

Populate environment variables (replacing as appropriate) and create the project:
```
export OCP_SERVER_URL=https://api.crc.testing:6443
export DEVUSER=developer
export DEVPASSWORD=developer
export ADMINUSER=kubeadmin
export ADMINPASSWORD=f5tRY-9RxxL-2TEMT-CykXc

oc login -u $DEVUSER -p $DEVPASSWORD $OCP_SERVER_URL
oc new-project rhdg-ocp-demo
```

### Deployment steps

- Start the build and deploy of the client application:
```
oc apply -f ocp-yaml/client-application.yaml
```
- Create the Infinispan cluster, either using the Operator or Helm (note that the Helm chart does not support custom code):
```
## Via the Data Grid Operator:

# Build the server JAR 
mvn clean install

# Copy the server JAR to a PV
# This PV will be mounted on each RHDG pod
oc login -u $ADMINUSER -p $ADMINPASSWORD $OCP_SERVER_URL
oc apply -f ocp-yaml/operator-server-jar.yaml
oc wait --for=condition=ready --timeout=2m pod/server-jar-pod
oc cp --no-preserve=true server/target/rhdg-ocp-demo-server*.jar server-jar-pod:/tmp/libs/
oc login -u $DEVUSER -p $DEVPASSWORD $OCP_SERVER_URL
oc delete pod server-jar-pod

# Create the Infinispan cluster
oc apply -f ocp-yaml/operator-resources.yaml
```
```
## Via Helm:

# Create the secret used for RHDG credentials
oc apply -f ocp-yaml/helm-secret.yaml
# Install the Helm chart
oc login -u $ADMINUSER -p $ADMINPASSWORD $OCP_SERVER_URL
helm install infinispan-cluster openshift-helm-charts/redhat-data-grid \
    --values ocp-yaml/helm-chart.yaml
oc login -u $DEVUSER -p $DEVPASSWORD $OCP_SERVER_URL
```

### Testing

The client application will have a route exposed, with a swagger UI available at `/swagger-ui.html`. 

- Cache connection information can be viewed in the `InfinispanService.java` file in the `client` project
- Basic cache connectivity and functionality can be tested using the POST, GET, and DELETE HTTP methods on the `/infinispan/{key}` endpoint
- If a GET is performed on a key that hasn't been stored, the custom cache loader is used to generate a random value
  - Note: the RHDG Helm chart does not support deploying custom code, so the above will only work when deploying RHDG via the Operator
- A simple remote task can be executed via the `/infinispan/removeTask/{key}` endpoint
  - Note: the RHDG Helm chart does not support deploying custom code, so the above will only work when deploying RHDG via the Operator

To test with a user that fails authentication, change `INFINISPAN_USER` and `INFINISPAN_PASSWORD` in the deployment environment variables to 
use the `invalid.user` and `invalid.password` value from the secret. To test a user that authenticates but is unauthorized, use
`unauthorized.user` and `unauthorized.password`.

Cache nodes can be stopped, started, and scaled by changing the `replicas` parameter of the Data Grid operator or upgrading the Helm release 
with a new `deploy.replicas` parameter. The RHDG admin console can be accessed via an exposed `infinispan-cluster` route, with the username 
`authorized` and password `Authorized-password!` (the same credentials used by the client application to connect to RHDG).

### Cleanup

Resources in the project created for the demo can be deleted with these commands:

```
# Delete the client application:
oc delete all,secret -l app=client
```
```
# Delete the Infinispan cluster installed via the Operator:
oc delete all,secret,pvc,infinispan,cache -l app=infinispan-operator
```
```
# Delete the Infinispan cluster installed via Helm:
oc login -u $ADMINUSER -p $ADMINPASSWORD $OCP_SERVER_URL
helm uninstall infinispan-cluster
oc login -u $DEVUSER -p $DEVPASSWORD $OCP_SERVER_URL
oc delete secret -l app=infinispan-helm
oc delete pvc -l app=infinispan-pod
```

The entire project can be deleted with:

```
oc delete project rhdg-ocp-demo
```

## Additional Info

### Debugging

Some issues were encountered using CRC, here are the workarounds:
- Infinispan cluster failing to start, due to insufficient memory.
  - Increase memory config: `crc config set memory 20000`
- DNS lookups for routes don't work in browsers with browser-specific DNS (e.g. Brave)
  - Change browser DNS server to the system-provided DNS server (which contains entries for routes)
- PVC creation failing due to CRC issue preventing PVs from being recycled in OpenShift Local
  - Set the following label: `oc label  --overwrite ns openshift-infra  pod-security.kubernetes.io/enforce=privileged`
  - Manually recycle failed PVs using `oc patch pv/$PV_NAME --type json -p '[{ "op": "remove", "path": "/spec/claimRef" }]'`

### Links

Application client code derived from <https://github.com/ngecom/openshiftSpringBoot>

[OpenShift Local](https://developers.redhat.com/products/openshift-local/overview)

[RHDG Operator Guide](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3/html/data_grid_operator_guide/index)

[RHDG Operator custom code deployment](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3/guide/1cfa1bfa-697d-4fda-9e0a-8c3e2b99f815)

[Building and deploying Data Grid clusters with Helm](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3/html-single/building_and_deploying_data_grid_clusters_with_helm/index)

