package com.hedvig.botService.config;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SwitchableInsurers {
  public static final Set<String> SWITCHABLE_INSURERS = Set.of(
    "Folksam",
    "Trygg-Hansa",
    "ICA",
    "Tre Kronor"
  );
}



