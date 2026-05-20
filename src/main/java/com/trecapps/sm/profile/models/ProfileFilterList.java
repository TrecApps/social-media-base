package com.trecapps.sm.profile.models;

import com.trecapps.sm.common.models.SocialMediaEventType;
import com.trecapps.sm.profile.dto.PostFilterRequest;
import com.trecapps.sm.profile.pipeline.PresentProbabilityDefaults;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Document("Profile-Filter-${trecapps.sm.name}")
public class ProfileFilterList {

    @MongoId
    UUID id;

    List<ProfileFilter> filterList = new ArrayList<>();


    private static final double PROBABILITY_MODIFIER = 1.25;

    public double getProbability(UUID id, SocialMediaEventType type){

            for(ProfileFilter filter: filterList){
                if(id.equals(filter.from) && type.equals(filter.type))
                    return filter.getProbability();
            }

        return PresentProbabilityDefaults.getDefaultByType(type);
    }

    public void updateFilter(PostFilterRequest request){
        for(ProfileFilter filter: filterList){
            if(request.getFrom().equals(filter.from) && request.getType().equals(filter.type))
            {
                double prob = filter.getProbability();
                filter.setProbability(request.isDecrease() ? prob / PROBABILITY_MODIFIER : prob * PROBABILITY_MODIFIER);
                return;
            }
        }

        ProfileFilter newFilter = new ProfileFilter();
        newFilter.setFrom(request.getFrom());
        newFilter.setType(request.getType());
        double prob = PresentProbabilityDefaults.getDefaultByType(request.getType());
        newFilter.setProbability(request.isDecrease() ? prob / PROBABILITY_MODIFIER : prob * PROBABILITY_MODIFIER);
        filterList.add(newFilter);
    }

    public void removeFilter(PostFilterRequest request){
        for(int c = 0; c < filterList.size(); c++){
            ProfileFilter filter = filterList.get(c);
            if(request.getFrom().equals(filter.from) && request.getType().equals(filter.type))
            {
                filterList.remove(c);
                return;
            }
        }
    }
}
