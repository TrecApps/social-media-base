package com.trecapps.sm.profile.dto;

import com.trecapps.sm.profile.models.FeatureShow;
import lombok.Data;

@Data
public class Favorite {

    String brandId;
    String brandName;
    String type;

    FeatureShow show;
}
