package com.example.eventnotificationdemo.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter.SseEventBuilder;

@RestController
@RequestMapping(path = "/events")
public class EventController {

  public final List<SseEmitter> emitters = new CopyOnWriteArrayList<>();

  @Async
  @CrossOrigin
  @GetMapping(path = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe() throws IOException {

    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    emitter.send(SseEmitter.event().name("SUBSCRIBE"));
    emitters.add(emitter);
    return emitter;

  }

  @GetMapping(path = "/send/{text}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public void sendEventToSubscribers(@PathVariable String text) {

    emitters.forEach(e ->
        {
          SseEventBuilder event = SseEmitter.event()
              .data(text, MediaType.APPLICATION_JSON)
              .id(String.valueOf(UUID.randomUUID()))
              .name("TEST-EVENT");
          try {
            e.send(event);
          } catch (IOException ex) {
            emitters.remove(e);
            throw new RuntimeException(ex);
          }
        }

    );

  }
}
