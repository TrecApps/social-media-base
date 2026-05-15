package com.trecapps.sm.profile.service;

import com.trecapps.auth.common.models.TcBrands;
import com.trecapps.auth.common.models.TcUser;
import com.trecapps.sm.common.functionality.ObjectResponseException;
import com.trecapps.sm.common.functionality.ProfileFunctionality;
import com.trecapps.sm.common.models.ResponseObj;
import com.trecapps.sm.profile.dto.Favorite;
import com.trecapps.sm.profile.dto.PostProfile;
import com.trecapps.sm.profile.dto.ProfileSearchResult;
import com.trecapps.sm.profile.dto.SkillPost;
import com.trecapps.sm.profile.models.Education;
import com.trecapps.sm.profile.models.Profile;
import com.trecapps.sm.profile.models.Skill;
import com.trecapps.sm.profile.models.WorkExpHolder;
import com.trecapps.sm.profile.repos.ProfileRepoMongo;
import jakarta.annotation.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class MongoProfileService implements ProfileService {

    @Autowired
    ProfileRepoMongo profileRepo;

    @Value("${trecapps.sm.support-recruiters:false}")
    boolean supportRecruiters;


    @Override
    public Mono<ResponseObj> createProfile(TcUser user, @Nullable TcBrands brand, PostProfile post) {

        return profileRepo
                .findById(ProfileFunctionality.getProfileId(user, brand))
                .doOnNext((Profile profile) -> {throw new ObjectResponseException(HttpStatus.CONFLICT, "Profile already exists!");})
                .thenReturn(new Profile())
                .flatMap((Profile profile) -> {
                    profile.setId(ProfileFunctionality.getProfileId(user, brand));
                    profile.setAboutMe(post.getAboutMe());
                    profile.setAboutMeShort(post.getAboutMeShort());
                    profile.setPronouns(post.getPronouns());
                    profile.setPronounVisibility(post.getPronounVisibility());

                    profile.setTitle(brand == null ? user.getDisplayName() : brand.getName());

                    return profileRepo.save(profile);
                })
                .map((Profile profile) -> ResponseObj.getInstanceCREATED("Success", profile.getId()));
    }

    @Override
    public Mono<List<ProfileSearchResult>> searchProfiles(String userId, String query, int page, int size) {

        return Mono.just(userId)
                .flatMap((String user) -> {

                    // ToDo - look up block table for any profiles blocking this user


                    return Mono.just(new ArrayList<String> ());
                })
                .flatMap((List<String> blockers) -> {

                    return profileRepo.findProfileByQuery(query, blockers, PageRequest.of(page, size))
                            .map((Profile profile) -> {
                                ProfileSearchResult result = new ProfileSearchResult();
                                result.setId(profile.getId());
                                result.setDisplayName(profile.getTitle());
                                result.setShortAboutMe(profile.getAboutMeShort());
                                return result;
                            }).collectList();


                });
    }

    @Override
    public Mono<Profile> getProfile(TcUser userId, @Nullable String brandId, String profileId) {
        return Mono.just(userId.getId())
                .flatMap((String user) -> {

                    // ToDo - look up block table for any profiles blocking this user


                    return Mono.just(new ArrayList<String> ());
                })
                .doOnNext((List<String> blockers) -> {
                    if(blockers.contains(profileId)){

                        // ToDo - set up mechanism where target profile can be alerted about this search (whether a threshold is established or not)


                        // If the searcher if the profile owner is a danger to said owner, hopefully we can convince them that profile does not exist
                        throw new ObjectResponseException(HttpStatus.NOT_FOUND, "");

                    }
                })
                .flatMap((List<String> b) -> {
                    return profileRepo.findById(profileId);
                })
                .doOnNext((Profile profile) -> {
                    // If requester is retrieving own profile, allow everything to be returned
                    if(profile.getId().equals(ProfileFunctionality.getProfileId(userId.getId(), brandId)))
                        return;

                    // prepare to filter data

                    // ToDo - get information about connections between requester and profile

                    profile.filterData(supportRecruiters && userId.getAuthRoles().contains("RECRUITER"), false, false);
                });
    }

    @Override
    public Mono<ResponseObj> updateFavorites(String userId, @Nullable String brandId, List<Favorite> favorites) {
        return profileRepo.findById(ProfileFunctionality.getProfileId(userId, brandId))
                .doOnNext((Profile profile) -> {
                    // ToDo - inspect favorites


                    // End ToDo

                    profile.setFavorites(favorites);
                })
                .flatMap((Profile profile) -> profileRepo.save(profile))
                .map((Profile p) -> ResponseObj.getInstanceOK("Updated"))
                .switchIfEmpty(Mono.just(ResponseObj.getInstanceNOTFOUND("Profile Not Found!")))
                ;

    }

    @Override
    public Mono<ResponseObj> setEducation(String userId, @Nullable String brandId, @Nullable String eduId, Education education) {
        return profileRepo.findById(ProfileFunctionality.getProfileId(userId, brandId))
                .doOnNext((Profile profile) -> {
                    // ToDo - inspect education


                    // End ToDo
                    List<Education> educations = profile.getEducation();
                    if(eduId == null){
                        // This is a POST Request
                        educations.add(education);
                    } else {
                        // This is a PUT Request
                        try{
                            int location = Integer.parseInt(eduId);
                            if(location < 0 || location >= educations.size())
                                throw new ObjectResponseException(HttpStatus.BAD_REQUEST,
                                        String.format("Education Id needs to be between 0 and the index of the last entry (in your case, %d)", educations.size() - 1));
                            educations.set(location, education);
                        } catch (NumberFormatException ignore){
                            throw new ObjectResponseException(HttpStatus.BAD_REQUEST, "Education Id is expected to be a number");
                        }
                    }
                })
                .flatMap((Profile profile) -> profileRepo.save(profile))
                .map((Profile p) -> ResponseObj.getInstanceOK("Updated"))
                .switchIfEmpty(Mono.just(ResponseObj.getInstanceNOTFOUND("Profile Not Found!")))
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                // ToDo - handle Blank Exceptions

        ;
    }

    @Override
    public Mono<ResponseObj> setWorkExperience(String userId, @Nullable String brandId, @Nullable String perspective, WorkExpHolder experience) {
        return profileRepo.findById(ProfileFunctionality.getProfileId(userId, brandId))
                .doOnNext((Profile profile) -> {
                    // ToDo - inspect education


                    // End ToDo
                    List<WorkExpHolder> experiences = profile.getWorkExperiences();
                    if(perspective == null){
                        // This is a POST Request
                        experiences.add(experience);
                    } else {
                        // This is a PUT Request

                        int c = 0;
                        for(; c < experiences.size(); c++){
                            WorkExpHolder tempHolder = experiences.get(c);
                            if(tempHolder.getPerspective().contains(perspective)){
                                break;
                            }
                        }

                        experiences.set(c, experience);
                    }
                })
                .flatMap((Profile profile) -> profileRepo.save(profile))
                .map((Profile p) -> ResponseObj.getInstanceOK("Updated"))
                .switchIfEmpty(Mono.just(ResponseObj.getInstanceNOTFOUND("Profile Not Found!")))
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                // ToDo - handle Blank Exceptions

                ;
    }

    @Override
    public Mono<ResponseObj> setSkill(String userId, @Nullable String brandId, String name, SkillPost skillPost) {

        return profileRepo.findById(ProfileFunctionality.getProfileId(userId, brandId))
                .flatMap((Profile profile) -> {
                    byte level = skillPost.getLevel();
                    if(level < 1 || level > 10)
                        throw new ObjectResponseException(HttpStatus.BAD_REQUEST, "Skill Level needs to be between 1 and 10 (inclusive)");


                    int index = -1;
                    List<Skill> skills = profile.getSkills();
                    for(int c = 0; c < skills.size(); c++){
                        if(skills.get(c).getName().equals(name)){
                            index = c;
                        }
                    }

                    Skill skill = new Skill();
                    skill.setName(name);
                    skill.setDetails(skillPost.getDetail());
                    skill.setLevel(skillPost.getLevel());
                    if(index == -1)
                        skills.add(skill);
                    else skills.set(index, skill);

                    return profileRepo.save(profile);
                })
                .map((Profile p) -> ResponseObj.getInstanceOK("Updated"))
                .switchIfEmpty(Mono.just(ResponseObj.getInstanceNOTFOUND("Profile Not Found!")))
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                // ToDo - handle Blank Exceptions

                ;
    }

    @Override
    public Mono<ResponseObj> removeEducation(String userId, @Nullable String brandId, String eduId) {
        return profileRepo.findById(ProfileFunctionality.getProfileId(userId, brandId))
                .flatMap((Profile profile) -> {
                    List<Education> educations = profile.getEducation();
                    try{
                        int location = Integer.parseInt(eduId);
                        if(location < 0 || location >= educations.size())
                            throw new ObjectResponseException(HttpStatus.BAD_REQUEST,
                                    String.format("Education Id needs to be between 0 and the index of the last entry (in your case, %d)", educations.size() - 1));
                        educations.remove(location);
                    } catch (NumberFormatException ignore){
                        throw new ObjectResponseException(HttpStatus.BAD_REQUEST, "Education Id is expected to be a number");
                    }
                    return profileRepo.save(profile);
                })
                .map((Profile p) -> ResponseObj.getInstanceOK("Removed"))
                .switchIfEmpty(Mono.just(ResponseObj.getInstanceNOTFOUND("Profile Not Found!")))
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                // ToDo - handle Blank Exceptions

                ;
    }

    @Override
    public Mono<ResponseObj> removeWorkExperience(String userId, @Nullable String brandId, String perspective) {
        return profileRepo.findById(ProfileFunctionality.getProfileId(userId, brandId))
                .flatMap((Profile profile) -> {
                    List<WorkExpHolder> experiences = profile.getWorkExperiences();

                    boolean looking = true;
                    for(int c = 0; looking && c < experiences.size(); c++){
                        WorkExpHolder holder = experiences.get(c);
                        if(holder.getPerspective().contains(perspective)){
                            experiences.remove(c);
                            looking = false;
                        }
                    }

                    if(looking)
                        throw new ObjectResponseException(HttpStatus.NOT_FOUND, String.format(
                                "Work Experience %s not found", perspective
                        ));
                    return profileRepo.save(profile);
                })
                .map((Profile p) -> ResponseObj.getInstanceOK("Removed"))
                .switchIfEmpty(Mono.just(ResponseObj.getInstanceNOTFOUND("Profile Not Found!")))
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                // ToDo - handle Blank Exceptions

                ;
    }

    @Override
    public Mono<ResponseObj> removeSkill(String userId, @Nullable String brandId, List<String> names) {
        return profileRepo.findById(ProfileFunctionality.getProfileId(userId, brandId))
                .flatMap((Profile profile) -> {
                    List<Skill> skills = profile.getSkills();
                    int removed = 0;
                    for(int c = 0; c < skills.size(); c++){
                        Skill holder = skills.get(c);
                        if(names.contains(holder.getName())){
                            skills.remove(c--);
                            removed++;
                        }
                    }

                    if(removed == 0)
                        throw new ObjectResponseException(HttpStatus.NOT_FOUND, "No Skills found!");
                    int finalRemoved = removed;
                    return profileRepo.save(profile)
                            .map((Profile p) -> ResponseObj.getInstanceOK(String.format("Removed %d Skills", finalRemoved)));
                })

                .switchIfEmpty(Mono.just(ResponseObj.getInstanceNOTFOUND("Profile Not Found!")))
                .onErrorResume(ObjectResponseException.class, (ObjectResponseException e) -> Mono.just(e.toResponseObj()))
                // ToDo - handle Blank Exceptions

                ;
    }
}
