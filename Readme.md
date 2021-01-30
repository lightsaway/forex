# Forex test app

## Running and quering locally

```shell script
sbt docker:publishLocal
docker-compose up -d
curl 'localhost:8080/rates?from=NZD&to=USD'
```