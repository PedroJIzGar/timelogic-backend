package com.timelogic.timeclock.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PunchController {
  @GetMapping("/api/punches/ping")
  public String ping() {
    return "timeclock-service OK";
  }
}
