
Increase CRC memory if needed: crc config set memory 20000

Install Data Grid operator

Create Infinispan Cluster with operator
- service type = data grid, disable encryption, enable auth
    authorization:
      enabled: true
      role: admin, ALL
      role: restricted-admin, ALL
    endpointAuthentication: true
- create secret using generated credentials
INFINISPAN_USERNAME, INFINISPAN_PASSWORD

Create two caches with operator. "test-cache" where roles is "admin", "restricted-cache" where roles is "restricted"
- template: 
     <distributed-cache mode="SYNC"     statistics="true"><security><authorization     roles="admin"/></security> <persistence> <store class="com.redhat.rhdg.demo.server.loader.CustomStore" segmented="false"/> </persistence></distributed-cache>

Deploy application
- add created secret in deployment "environments"
- route with TLS edge

Auth test:
- set CACHE_NAME property in deployent to "restricted-cache"
- confirm 500s returned

Deploy code:
- oc login -u kubeadmin https://api.crc.testing:6443
- oc apply -f server/datagrid-libs.yaml
- oc apply -f server/datagrid-libs-pod.yaml
- oc wait --for=condition=ready --timeout=2m pod/datagrid-libs-pod
- oc cp --no-preserve=true server/target/rhdg-ocp-demo-server-1.0-SNAPSHOT.jar datagrid-libs-pod:/tmp/libs/
- oc delete pod datagrid-libs-pod
In infinispan CR:
spec:
  dependencies:
    volumeClaimName: datagrid-libs

DNS doesn't work with Brave, use firefox

