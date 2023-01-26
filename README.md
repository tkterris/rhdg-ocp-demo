
# Data Grid Operator Demo

This project demonstrates some core functionality of the Red Hat Data Grid operator in OpenShift. 

## Overview

### Scope

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

### Design

This demo consists of three parts:
- RHDG server code, made available to RHDG via an HTTPD node
- An RHDG cluster
  - Configurations for setting up the cluster via both the RHDG Operator and Helm are provided
- A simple Spring Boot client application that interacts with the RHDG cluster

## Running the demo

### Prerequisites

- Access to an OpenShift cluster (for local testing, [OpenShift Local](https://developers.redhat.com/products/openshift-local/overview) is recommended)
- The Data Grid Operator is installed
- Helm is installed and the chart repository at <https://charts.openshift.io/> has been added: `helm repo add openshift-helm-charts https://charts.openshift.io/`
  - As of writing, the OpenShift Helm chart repository has not been updated to the latest version of the Infinispan Helm chart. Until the repository is updated, 
    run the following commands to install the chart locally:
```
git clone https://github.com/infinispan/infinispan-helm-charts.git
export PATH_TO_HELM=$(pwd)/infinispan-helm-charts
```

### Setup

Populate environment variables (replacing as appropriate) and create the project:
```
export OCP_SERVER_URL=https://api.crc.testing:6443
export DEVUSER=developer
export DEVPASSWORD=developer

oc login -u $DEVUSER -p $DEVPASSWORD $OCP_SERVER_URL
oc new-project rhdg-ocp-demo
```

### Deployment steps

- Build and deploy the client application, either within or outside 
of OCP:
```
## In OCP:
oc apply -f ocp-yaml/client-application.yaml
```
```
## Outside of OCP:
mvn spring-boot:run 
```
- Create an HTTPD server to provide the server JAR:
```
mvn clean install
oc new-app httpd:latest~https://github.com/sclorg/httpd-ex.git
HTTPD_POD=""
while [ -z "$HTTPD_POD" ]
do
    sleep 1s
    HTTPD_POD=$(oc get po -l deployment=httpd-ex \
        -o custom-columns=NAME:metadata.name --no-headers)
done
oc wait --for=condition=ready --timeout=2m pod/$HTTPD_POD
oc cp --no-preserve=true server/target/rhdg-ocp-demo-server*.jar \
    $HTTPD_POD:/opt/app-root/src/server.jar
```
- Create the Infinispan cluster, either using the Operator or Helm:
```
## Via the Data Grid Operator:

oc apply -f ocp-yaml/operator-resources.yaml
```
```
## Via Helm:

# Create the secret used for RHDG credentials
oc apply -f ocp-yaml/helm-secret.yaml
# Install the Helm chart
# TODO: change `$PATH_TO_HELM` to `openshift-helm-charts/redhat-data-grid` 
# once the OCP helm repo is updated to the latest version
helm install infinispan-cluster $PATH_TO_HELM --values ocp-yaml/helm-chart.yaml
```

### Testing

The client application deployed on OpenShift will have a route exposed, or if running locally will be accessible at <http://localhost:8080>. The 
swagger UI is available at `/swagger-ui.html`. 

- Cache connection information can be viewed in the `InfinispanService.java` file in the `client` project
- Basic cache connectivity and functionality can be tested using the POST, GET, and DELETE HTTP methods on the `/infinispan/{key}` endpoint
- If a GET is performed on a key that hasn't been stored, the custom cache loader is used to generate a random value
- A simple remote task can be executed via the `/infinispan/removeTask/{key}` endpoint

To test with a user that fails authentication, change `INFINISPAN_USER` and `INFINISPAN_PASSWORD` in the deployment environment variables to 
use the `invalid.user` and `invalid.password` value from the secret. To test a user that authenticates but is unauthorized, use
`unauthorized.user` and `unauthorized.password`.

Cache nodes can be stopped, started, and scaled by changing the `replicas` parameter of the Data Grid operator or upgrading the Helm release 
with a new `deploy.replicas` parameter. The RHDG admin console can be accessed via an exposed nodePort service at <http://api.crc.testing:31222> 
(if using CRC), with the username `authorized` and password `Authorized-password!` (the same credentials used by the client application to 
connect to RHDG). 

### Cleanup

Resources in the project created for the demo can be deleted with these commands:

```
# Delete the client application:
oc delete all,secret -l app=client
```
```
# Delete the HTTPD server for server.jar:
oc delete all -l app=httpd-ex
```
```
# Delete the Infinispan cluster installed via the Operator:
oc delete all,secret,infinispan,cache -l app=infinispan-operator
```
```
# Delete the Infinispan cluster installed via Helm:
helm uninstall infinispan-cluster
oc delete secret -l app=infinispan-helm
```

The entire project can be deleted with:

```
oc delete project rhdg-ocp-demo
```

## Additional Info

### Debugging

- Infinispan cluster failing to start, due to insufficient memory.
  - Increase memory config: `crc config set memory 20000`
- DNS lookups for routes don't work in browsers with browser-specific DNS (e.g. Brave)
  - Change browser DNS server to the system-provided DNS server (which contains entries for routes)
- PVC creation failing due to CRC issue preventing PVs from being recycled in OpenShift Local
  - Set the following label: `oc label  --overwrite ns openshift-infra  pod-security.kubernetes.io/enforce=privileged`
  - Manually recycle failed PVs using `oc patch pv/$PV_NAME --type json -p '[{ "op": "remove", "path": "/spec/claimRef" }]'`

### Production Considerations

There are a number of design decisions that were made so that this POC would be simple and self-contained, which wouldn't necessarily be appropriate for a production application. These include:

- The application and configuration is all added to openshift via "oc apply", rather than a CI/CD pipeline 
- The server JAR artifact should be stored in an artifact repository (e.g. JFrog), rather than an HTTPD container
- The Cache configuration would likely be part of the client application source code, and updated in the RHDG cluster as part of the client CI/CD deployment
- Cache authentication is configured via Secrets, but Production environments might use a centralized permission repository (e.g. LDAP)

### Accessing RHDG outside of OpenShift

This demo includes instructions on how to connect to RHDG from outside of OCP. This does introduce a few design considerations:

- There are a number ingress methods for connecting to the RHDG cluster, based on design requirements:
  - [NodePorts](https://docs.openshift.com/container-platform/4.12/networking/configuring_ingress_cluster_traffic/configuring-ingress-cluster-traffic-nodeport.html) (used for this demo)
  - [LoadBalancers](https://docs.openshift.com/container-platform/4.12/networking/configuring_ingress_cluster_traffic/configuring-ingress-cluster-traffic-load-balancer.html)
  - [Routes](https://docs.openshift.com/container-platform/4.12/networking/routes/route-configuration.html)
    - Note that Hot Rod connections over routes MUST use passthrough encryption, otherwise the connection will be broken with "invalid 
      magic byte" errors. This is because Hot Rod is not an HTTP protocol, and Routes expect unencrypted connections to be HTTP. See 
      [this solution article](https://access.redhat.com/solutions/6134351) for details.
- Connections to RHDG from outside of OCP must be made with BASIC client intelligence, meaning requests will simply be load balanced 
rather than sent to the entry's owner node. This can have a few performance and functional impacts:
  - Cache loaders may be called multiple times for each cache entry
  - There will be an additional network hop when a request is forwarded from a non-owner node to the entry owner
  - Bulk operations (like keySet and size) may return incorrect results when using a cache loader that doesn't implement the BULK_READ 
    methods (such as the CustomStore loader in this demo)

### Links

Application client code derived from <https://github.com/ngecom/openshiftSpringBoot>

[OpenShift Local](https://developers.redhat.com/products/openshift-local/overview)

[RHDG Operator Guide](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3/html/data_grid_operator_guide/index)

[RHDG Operator custom code deployment](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3/guide/1cfa1bfa-697d-4fda-9e0a-8c3e2b99f815)

[Building and deploying Data Grid clusters with Helm](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.3/html-single/building_and_deploying_data_grid_clusters_with_helm/index)

[Latest Infinispan Helm Chart](https://github.com/infinispan/infinispan-helm-charts)

