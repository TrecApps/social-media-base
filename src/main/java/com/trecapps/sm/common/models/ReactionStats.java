package com.trecapps.sm.common.models;

import lombok.Data;

import java.math.BigInteger;
import java.util.Map;

@Data
public class ReactionStats {

    String yourReaction;
    Map<String, Long> reactions;
    BigInteger id;
}
