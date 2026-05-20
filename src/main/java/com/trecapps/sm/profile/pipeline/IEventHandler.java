package com.trecapps.sm.profile.pipeline;

import com.trecapps.sm.common.models.SocialMediaEvent;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface IEventHandler {
    Mono<Boolean> processEvent(SocialMediaEvent var1);
}
