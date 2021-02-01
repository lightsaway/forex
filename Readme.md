# Forex test app

## Running and quering locally


### With docker compose
```shell script
sbt docker:publishLocal
docker-compose up -d
curl 'localhost:8080/rates?from=NZD&to=USD'
```

### Without docker compose
You need to configure one frame url, this can be done via ONE_FRAME_URL env var passed to container

```shell script
sbt docker:publishLocal
docker run -e ONE_FRAME_URL=http://some-url forex
```