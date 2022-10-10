
Increase CRC memory if needed: crc config set memory 20000

Install Data Grid operator

Create Infinispan Cluster with operator
- service type = data grid, disable encryption, enable auth
- create secret using generated credentials
INFINISPAN_USERNAME, INFINISPAN_PASSWORD

Create "test-cache" cache with operator

Deploy application
- add created secret in deployment "environments"
- route with TLS edge

DNS doesn't work with Brave, use firefox

