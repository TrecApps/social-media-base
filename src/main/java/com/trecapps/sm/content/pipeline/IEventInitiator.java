package com.trecapps.sm.content.pipeline;

import com.trecapps.sm.common.models.SocialMediaEvent;
import reactor.core.publisher.Mono;

//@Service
public interface IEventInitiator {
    Mono<Void> sendEvent(SocialMediaEvent var1);
}
