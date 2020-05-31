# Spring Boot URL Shortener #
This project demonstrates the use of Spring Boot 2.0 and Redis to build a URL shortener api. Also popularly known as 'tinyurl'.

## Prerequisites
* Redis running locally
  * host: localhost
  * port: 6379

## Running Redis using Docker

Use the following command to start a Redis server in the background.
* You don't need to create a volume to run Redis locally. When you kill the container, values are not persisted.
```
docker run -d --name=redis -p 6379:6379 redis
```

** For production, please create a volume to store Redis data. Create a volume
```
docker volume create redis-urlshortner-volume
```

Then create the docker container
```
docker run -d -v redis-urlshortner-volume:/data --name redis-urlshortner -p 6379:6379 redis
```

## Execution locally

```
java -jar spring-url-shortner.jar
```

## Docker setup

### Create Docker Image
`docker build -t phanibushan/url-shortner .`

Alternatively you can use docker-compose to spin up both services and bind them

### Run docker container
Set the following environment variables when running your docker image

`docker run --container_name url-shortner -e spring_redis_host='<your_redis_host>' -e server_port='<your_server_port>' -p <your_server_port>:<your_server_port> phanibushan/url-shortner:latest`

Alternatively use the docker-compose to spin up both services and bind them

## API Details

Swagger documentation available at `/swagger-ui.html#`

#### Create Short URL:
`http://localhost/`

Request body:
```JSON
{
    "url": "https://www.gmail.com"
}
```
Response body:
```text
http://localhost/4063db89
```

### Redirect to Original URL:
`http://localhost/{id}`

This redirects to original url