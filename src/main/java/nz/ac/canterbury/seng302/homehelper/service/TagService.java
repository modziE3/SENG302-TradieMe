package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

/**
 * Service class for tags
 */
@Service
public class TagService {
    private final TagRepository tagRepository;
    private final ValidationService validationService;
    private final ModerationService moderationService;

    public static final String TAG_NAME_TOO_LONG = "The tags must be less than 15 characters long";
    public static final String TAG_NAME_NO_LETTERS = "The tag must contain at least one letter";
    public static final String TAG_NAME_INVALID_CHARS = "The tag must not contain any & or ; characters";
    public static final String TAG_ALREADY_EXISTS = "There is already a tag with that name";
    public static final String FIVE_TAGS_ALREADY_EXIST = "You cannot add more than 5 tags";
    public static final String TAG_NAME_CONTAINS_PROFANITY = "Tag is not following the system language standards";

    @Autowired
    public TagService(TagRepository tagRepository, ValidationService validationService, ModerationService moderationService) {
        this.tagRepository = tagRepository;
        this.validationService = validationService;
        this.moderationService = moderationService;
    }

    /**
     * Stores a new tag entity
     * @param tag tag being stored
     */
    public void addTag(Tag tag) {
        tagRepository.save(tag);
    }

    /**
     * Gets all tags from database storage that match a name
     * @param name name to find tags by
     * @return list of tags with
     */
    public Tag getTagByName(String name) {
        return tagRepository.findByName(name);
    }

    /**
     * Gets all tags in storage
     * @return list of all tags in storage
     */
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    /**
     * Validates the name of a new tag.
     * If invalid, the correct error message is returned. Otherwise, an empty string is returned
     * @param record renovation record the tag will be added to
     * @param tagName tag name being validated
     * @return string containing any error messages
     * @throws IOException exception thrown by moderation service when checking profanity
     */
    public String validateTag(RenovationRecord record, String tagName) throws IOException {
        String error = "";

        if (!validationService.tagNameContainsLetters(tagName)) {
            error = TAG_NAME_NO_LETTERS;
        } else if (!validationService.correctTagNameLength(tagName)) {
            error = TAG_NAME_TOO_LONG;
        } else if (validationService.tagNameContainsInvalidCharacters(tagName)) {
            error = TAG_NAME_INVALID_CHARS;
        } else if (!validationService.checkTagsLessThanFive(record)) {
            error = FIVE_TAGS_ALREADY_EXIST;
        } else if (validationService.checkRenovationContainsTag(record, tagName)) {
            error = TAG_ALREADY_EXISTS;
        } else if (moderationService.isProfanity(tagName)) {
            error = TAG_NAME_CONTAINS_PROFANITY;
        }

        return error;
    }

    /**
     * Validates a new search tag being added to the string of current search tags
     * If invalid, the correct error message is returned. Otherwise, an empty string is returned
     * @param currentSearchTags String of search tags currently in the search bar
     * @param newSearchTag New tag wanting to be added
     * @return String containing any error messages
     */
    public String validateSearchTag(String currentSearchTags, String newSearchTag) {
        String error = "";

        List<String> tagList = List.of(currentSearchTags.split(";"));
        if (tagList.size() == 5) {
            error = FIVE_TAGS_ALREADY_EXIST;
        } else if (tagList.contains(newSearchTag)) {
            error = TAG_ALREADY_EXISTS;
        }

        return error;
    }

    /**
     * Adds a new search tag to the string of search tags in the search bar
     * @param currentSearchTags string of current search tags
     * @param newSearchTag new search tag being added
     * @return new string of search tags with new search tag added
     */
    public String addNewSearchTag(String currentSearchTags, String newSearchTag) {
        if (currentSearchTags == null || currentSearchTags.isEmpty()) {
            currentSearchTags = newSearchTag;
        } else {
            currentSearchTags = currentSearchTags.concat(";" + newSearchTag);
        }
        return currentSearchTags;
    }

    /**
     * Deletes a tag from the repository
     * @param tag A tag object which is no longer linked to any records
     */
    public void deleteTag(Tag tag) {
        tagRepository.delete(tag);
    }
}
