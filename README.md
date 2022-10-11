
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
- template: <distributed-cache mode="SYNC"     statistics="true"><security><authorization     roles="admin"/></security></distributed-cache>

Deploy application
- add created secret in deployment "environments"
- route with TLS edge

Auth test:
- set CACHE_NAME property in deployent to "restricted-cache"
- confirm 500s returned

DNS doesn't work with Brave, use firefox

