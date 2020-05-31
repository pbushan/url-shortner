package com.ultimatesoftware.urlshortner.controllers;

import com.google.common.hash.Hashing;
import com.ultimatesoftware.urlshortner.models.Url;
import com.ultimatesoftware.urlshortner.models.Error;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@RestController
@Api(value = "UrlShortnerController", description = "In-house operations to shorten urls and provide redirects")
public class UrlShortnerController {

    //@ApiOperation(value = "Get Gatling assertion SLA by correlation Id")
    @Autowired
    private RedisTemplate<String, Url> redisTemplate;

    @Value("${redis.ttl}")
    private long ttl;

    @Value("${server.port}")
    private int port;


/**
 * Returns the original URL.
 */

  @ApiOperation(value = "Get the original URL by Id")
  @RequestMapping(value = "/api/get/originalurl/{id}", method = RequestMethod.GET)
  @ResponseBody
  public ResponseEntity getUrl(@PathVariable String id) {

    // get from redis
    Url url = redisTemplate.opsForValue().get(id);

    if (url == null) {
      Error error = new Error("id", id, "No such key exists");
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    return ResponseEntity.ok(url);
  }

/**
 * Redirects to the original URL.
 */
    @ApiOperation(value = "Redirects to the original URL by Id if value found in redis store")
    @RequestMapping(value = "/r/{id}", method = RequestMethod.GET)
    public ResponseEntity<Object> redirect(@PathVariable String id) throws URISyntaxException {

        Url url = redisTemplate.opsForValue().get(id);
        if (url == null) {
            Error error = new Error("id", id, "No such key exists");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        URI originalUri = new URI(url.getUrl());
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setLocation(originalUri);
        return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
    }

    /**
     * Returns a short URL.
     */
    @ApiOperation(value = "Returns a short URL and stores it in redis store")
    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity postUrl(@RequestBody @NotNull Url url) {

        UrlValidator validator = new UrlValidator(
                new String[]{"http", "https"}
        );

        // if invalid url, return error
        if (!validator.isValid(url.getUrl())) {
            Error error = new Error("url", url.getUrl(), "Invalid URL");
            return ResponseEntity.badRequest().body(error);
        }

        String id = Hashing.murmur3_32().hashString(url.getUrl(), Charset.defaultCharset()).toString();
        url.setId(id);
        url.setCreated(LocalDateTime.now());

        //store in redis
        redisTemplate.opsForValue().set(url.getId(), url, ttl, TimeUnit.SECONDS);
        String address = InetAddress.getLoopbackAddress().getHostName();
        if(port!=80) {
            url.setUrl("http://"+ address + ":" + port + "/" + id);
        }
        else {
            url.setUrl("http://"+ address + "/" + id);
        }
        return ResponseEntity.ok(url);
    }

    /**
     * Returns all key-value pairs stored in Redis Store.
     */

    @ApiOperation(value = "Returns all key-value pairs stored in Redis Store")
    @RequestMapping(value = "/api/get/all", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity getAll() {

        List<Url> urlList = new ArrayList<>();
        Set<String> keys = redisTemplate.keys("*");
        for(String key : keys) {
            urlList.add(redisTemplate.opsForValue().get(key));
        }
        if (urlList.isEmpty()) {
            Error error = new Error("keys", null, "No values exist in the Redis DB");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        return ResponseEntity.ok(urlList);
    }

}
