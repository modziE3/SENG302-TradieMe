package nz.ac.canterbury.seng302.homehelper.service;

import nz.ac.canterbury.seng302.homehelper.entity.RenovationRecord;
import nz.ac.canterbury.seng302.homehelper.entity.Tag;
import nz.ac.canterbury.seng302.homehelper.entity.dto.TradieRating;
import nz.ac.canterbury.seng302.homehelper.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.time.LocalDate;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidationService {

    private static final int MAX_TRADIE_RATING = 5;
    private static final int MIN_TRADIE_RATING = 1;


    public ValidationService() {}

    /**
     * Takes a password string and checks that it is not weak i.e. checks it is longer than 8 characters,
     * contains a capital letter, contains a lower case letter, and a special character.
     * @param password the password string to be checked.
     * @return true if the password passes all checks, false if the password is weak.
     */
    public boolean checkPassword(String password, User user) {
        boolean containCapital = false;
        boolean containLower = false;
        boolean containNumber = false;
        boolean containSpecial = false;
        char character;

        if (user.getCreatedTimestamp() == null) {
            if (password.contains(LocalDate.now().toString())) {
                return false;
            }
        } else {
            if (password.contains(user.getCreatedTimestamp().toString())) {
                return false;
            }
        }
        if (!user.getEmail().isEmpty() && password.contains(user.getEmail()) ||
                (!user.getFirstName().isEmpty()) && password.contains(user.getFirstName()) ||
                (!user.getLastName().isEmpty() && password.contains(user.getLastName()))) {
            return false;
        }

        if (password.length() < 8) {
            return false;
        }
        for (int i = 0; i < password.length(); i++) {
            character = password.charAt(i);
            if (Character.isDigit(character)) {
                containNumber = true;
            } else if (Character.isUpperCase(character)) {
                containCapital = true;
            } else if (Character.isLowerCase(character)) {
                containLower = true;
            } else if (!Character.isSpaceChar(character)) {
                containSpecial = true;
            }
        }
        return containCapital && containLower && containNumber && containSpecial;
    }

    /**
     * Checks to see if two strings match
     * @param oldPassword the first string to compare.
     * @param newPassword the second string to compare.
     * @return true if the two strings are the same.
     */
    public boolean passwordMatch(String oldPassword, String newPassword) {
        return Objects.equals(oldPassword, newPassword);
    }

    /**
     * Checks if the name is only letters, '-', ''', or ' ' characters.
     * @param name the string to be checked.
     * @return true if the name meets the checks, false if the name contains any other characters.
     */
    public boolean checkName(String name){
        char character;
        for (int i = 0; i < name.length(); i++) {
            character = name.charAt(i);
            if (!Character.isAlphabetic(character) && character != '-' && character != '\'' && !Character.isSpaceChar(character)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the name of a record includes any characters it's not supposed to have
     * @param name String of the name of a renovation record
     */
    public boolean containsNonAlphaNumeric(String name) {
        Pattern p = Pattern.compile("[^\\p{L}\\p{N} .,'-]"); //ChatGPT used to find how to make regular expression
        return p.matcher(name).find();
    }

    /**
     * Checks that a given name string is 64 characters long or less.
     * @param name the string to be checked.
     * @return true if the name is 64 or fewer characters long, false if the string is longer than 64 characters.
     */
    public boolean correctNameLength(String name) {
        return name.length() <= 64;
    }

    /**
     *
     * @param streetAddress the string to be checked
     * @return true if the street address is 512 characters or fewer. False if the string is longer than 512 character.
     */
    public boolean correctStreetAddressLength(String streetAddress) {
        if (streetAddress != null) {
            return streetAddress.length() <= 512;
        }
        return true;
    }

    /**
     *
     * @param suburb the string to be checked
     * @return true if the suburb is 64 characters or fewer. False if the string is longer than 64 character.
     */
    public boolean correctSuburbLength(String suburb) {
        if (suburb != null) {
            return suburb.length() <= 512;
        }
        return true;
    }

    /**
     *
     * @param city the string to be checked
     * @return true if the city is 64 characters or fewer. False if the string is longer than 64 character.
     */
    public boolean correctCityLength(String city) {
        if (city != null) {
            return city.length() <= 512;
        }
        return true;
    }

    /**
     *
     * @param postcode the string to be checked
     * @return true if the postcode is 64 characters or fewer. False if the string is longer than 64 character.
     */
    public boolean correctPostcodeLength(String postcode) {
        if (postcode != null) {
            return postcode.length() <= 512;
        }
        return true;
    }

    /**
     *
     * @param country the string to be checked
     * @return true if the country is 64 characters or fewer. False if the string is longer than 64 character.
     */
    public boolean correctCountryLength(String country) {
        if (country != null) {
            return country.length() <= 512;
        }
        return true;
    }

    /**
     * Checks that a given description string is 512 characters long or less
     * @param description the string to be checked
     * @return true if the description is 512 characters long or less, false if the string is longer than 512 characters
     */
    public boolean correctDescriptionLength(String description) {
        return description.codePointCount(0, description.length()) <= 512;
    }

    /**
     * Checks if a given string is empty.
     * @param string the string to be checked.
     * @return true if the string is empty, false otherwise.
     */
    public boolean stringEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }

    /**
     * Checks to make sure that the email is of the form 'jane@example.com'.
     * @param email the string to check
     * @return true if the email string is of the correct form, false otherwise.
     */
    public boolean checkEmailForm(String email) {
        String emailRegex = "^(?!.*\\.\\.)[\\p{L}\\p{N}\\p{M}་!#$%&'*+/=?^_`{|}~.-]+@[\\p{L}\\p{N}\\p{M}་-]+(?:\\.[\\p{L}\\p{N}\\p{M}་-]+)*\\.[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Checks if a given file is a valid file type for a user image
     * @param file a file submitted by the user
     * @return true if image is valid type and false if not
     */
    public boolean checkImageType(MultipartFile file) {
        List<String> validTypes = new ArrayList<>();
        validTypes.add("image/jpeg");
        validTypes.add("image/png");
        validTypes.add("image/svg+xml");
        String fileType = file.getContentType();
        return validTypes.contains(fileType);
    }

    /**
     * Checks if a given file is larger than 10MB
     * @param file a file submitted by the user
     * @return true if the image is smaller than 10MB and false if not
     */
    public boolean checkImageSize(MultipartFile file) {
        return file.getSize() <= 10000000;
    }

    /**
     * Checks if the location information provided has at least a street address
     * @param locationInfo List of location information, containing street address, suburb, city, postcode and country
     * @return True if there is at least a street address and false if not
     */
    public boolean correctLocationInfo(List<String> locationInfo) {
        if (locationInfo.getFirst() == null) {
            for (String location : locationInfo.subList(1, locationInfo.size())) {
                if (location != null) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks the street address for a location such that it contains only
     * letters, hyphen, apostrophe, number, space, dot
     * @param streetAddress the street address string to be checked.
     * @return true if the string contains only accepted characters, false otherwise.
     */
    public boolean checkStreetAddress(String streetAddress) {
        String addressRegex = "^[\\p{L}\\p{N}\\s.'-]*$";
        Pattern pattern = Pattern.compile(addressRegex);
        Matcher matcher = pattern.matcher(streetAddress);
        return matcher.matches();
    }

    /**
     * Checks the suburb for a location such that it contains only
     * letters, hyphen, apostrophe, number, space
     * @param suburb the suburb string to be checked.
     * @return true if the string contains only accepted characters, false otherwise.
     */
    public boolean checkSuburb(String suburb) {
        String suburbRegex = "^[\\p{L}\\p{N}\\s'-]*$";
        Pattern pattern = Pattern.compile(suburbRegex);
        Matcher matcher = pattern.matcher(suburb);
        return matcher.matches();
    }

    /**
     * Checks the city for a location such that it contains only
     * letter, hyphen, apostrophe, space
     * @param city the city string to be checked.
     * @return true if the string contains only accepted characters, false otherwise.
     */
    public boolean checkCity(String city) {
        String cityRegex = "^[\\p{L}\\s'-]*$";
        Pattern pattern = Pattern.compile(cityRegex);
        Matcher matcher = pattern.matcher(city);
        return matcher.matches();
    }

    /**
     * Checks the post code for a location such that it only contains
     * letters, number, single space
     * @param postCode the post code string to be checked.
     * @return true if the string contains only accepted characters, false otherwise.
     */
    public boolean checkPostCode(String postCode) {
        String postCodeRegex = "^[\\p{L}\\p{N}]* ?[\\p{L}\\p{N}]*$";
        Pattern pattern = Pattern.compile(postCodeRegex);
        Matcher matcher = pattern.matcher(postCode);
        return matcher.matches();
    }

    /**
     * Checks the country of a location such that it only contains
     * letters, hyphen, apostrophe, single space
     * @param country the country string to be checked.
     * @return true if the string contains only accepted characters, false otherwise.
     */
    public boolean checkCountry(String country) {
        String countryRegex = "^[\\p{L}-']* ?[\\p{L}-']*$";
        Pattern pattern = Pattern.compile(countryRegex);
        Matcher matcher = pattern.matcher(country);
        return matcher.matches();
    }

    /**
     * Checks a location to see if the components are considered valid.
     * @param locationInfo the location to check.
     * @return A list of the components that are not considered valid.
     */
    public List<String> checkLocation(List<String> locationInfo) {
        List<String> errorList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            switch (i) {
               case 0:
                   if (locationInfo.get(i) != null) {
                       if (!correctStreetAddressLength(locationInfo.get(i))) {
                           errorList.add("STREET_ADDRESS_OVER_512_CHARS");
                       } else if (!checkStreetAddress(locationInfo.get(i))) {
                           errorList.add("ADDRESS");
                       }
                   }
                   break;
               case 1:
                   if (locationInfo.get(i) != null && !checkSuburb(locationInfo.get(i))) {
                       errorList.add("SUBURB");
                   }
                   if (locationInfo.get(i) != null && !correctSuburbLength(locationInfo.get(i))) {
                       errorList.add("SUBURB_OVER_64_CHARS");
                   }
                   break;
               case 2:
                   if (locationInfo.get(i) != null && !checkCity(locationInfo.get(i))) {
                       errorList.add("CITY");
                   }
                   if (locationInfo.get(i) != null && !correctCityLength(locationInfo.get(i))) {
                       errorList.add("CITY_OVER_64_CHARS");
                   }
                   break;
               case 3:
                   if (locationInfo.get(i) != null && !checkPostCode(locationInfo.get(i))) {
                       errorList.add("POSTCODE");
                   }
                   if (locationInfo.get(i) != null && !correctPostcodeLength(locationInfo.get(i))) {
                       errorList.add("POSTCODE_OVER_64_CHARS");
                   }
                   break;
               case 4:
                   if (locationInfo.get(i) != null && !checkCountry(locationInfo.get(i))) {
                       errorList.add("COUNTRY");
                   }
                   if (locationInfo.get(i) != null && !correctCountryLength(locationInfo.get(i))) {
                       errorList.add("COUNTRY_OVER_64_CHARS");
                   }
                   break;
            }
        }


        String street = locationInfo.getFirst();
        String city = locationInfo.get(2);
        String postcode = locationInfo.get(3);
        String country = locationInfo.get(4);

        if ((street != null && !street.isEmpty())){
            if (city == null) errorList.add("CITY_EMPTY");
            if (postcode == null) errorList.add("POSTCODE_EMPTY");
            if (country == null) errorList.add("COUNTRY_EMPTY");
        }

        return errorList;
    }


    /**
     * Checks if a tag name contains at least one letter
     * @param tagName tag name being checked
     * @return true if tag name contains at least one letter, false otherwise
     */
    public Boolean tagNameContainsLetters(String tagName) {
        String tagRegex = "^.*\\p{L}.*$";
        Pattern pattern = Pattern.compile(tagRegex);
        Matcher matcher = pattern.matcher(tagName);
        return matcher.matches();
    }

    /**
     * Checks if a tag name is at most 15 characters
     * @param tagName tag name being checked
     * @return true if tag name length at most 15, false otherwise
     */
    public Boolean correctTagNameLength(String tagName) {
        return tagName.length() <= 15;
    }

    /**
     * Checks if a tag name contains invalid characters
     * @param tagName tag name being checked
     * @return true if tag name contains invalid characters, false otherwise
     */
    public Boolean tagNameContainsInvalidCharacters(String tagName) {
        return tagName.contains("&") || tagName.contains(";");
    }

    /**
     * Checks to see if a record has less than 5 tags currently.
     * Used to check if another tag can be added to a renovation record.
     * @param record the renovation record to check tags.
     * @return true if the record has less than 5 tags, false otherwise.
     */
    public Boolean checkTagsLessThanFive(RenovationRecord record) {
        return record.getTags().size() < 5;
    }

    /**
     * Checks if a renovation record already contains a tag
     * @param record the renovation to check
     * @param tagName the tag name to check
     * @return true if the renovation record contains the tag, false otherwise.
     */
    public Boolean checkRenovationContainsTag(RenovationRecord record, String tagName) {
        return record.getTags().stream().map(Tag::getName).toList().contains(tagName);
    }

    /**
     * Checks if a page number falls inside a list of page numbers
     * @param pageList list of page numbers
     * @param pageNumber page number being validation
     * @return true if page number in page list, false otherwise
     */
    public Boolean pageNumberIsInPageList(List<Integer> pageList, Integer pageNumber) {
        return (pageList.getFirst() <= pageNumber) && (pageNumber <= pageList.getLast());
    }

    /**
     * Checks if the expense cost matches the form of a floating point number
     * @param expenseCost string of the expense cost
     * @return true if cost matches floating point number form, false otherwise
     */
    public Boolean correctExpenseCostFormat(String expenseCost) {
        String costRegex = "^([0-9]*[.])?[0-9]+$";
        Pattern costPattern = Pattern.compile(costRegex);
        Matcher costMatcher = costPattern.matcher(expenseCost);
        String zeroRegex = "^(0*[.])?0+$";
        Pattern zeroPattern = Pattern.compile(zeroRegex);
        Matcher zeroMatcher = zeroPattern.matcher(expenseCost);
        return costMatcher.matches() && !zeroMatcher.matches();
    }

    /**
     * Checks if the expense cost is at most 15 characters long
     * @param expenseCost string of the expense cost
     * @return true if expense cost length is at most 15, false otherwise
     */
    public Boolean correctExpenseCostLength(String expenseCost) {
        return expenseCost.length() <= 15;
    }

    /**
     * Checks if a date string is in the right date format
     * @param date date string being checked
     * @return true if date string matches format, false otherwise
     */
    public Boolean correctDateFormat(String date) {
        return Pattern.matches("[0-9]{2}/[0-9]{2}/[0-9]{4}", date);
    }

    /**
     * Checks if a date string is before today's date
     * @param dateString date string being checked
     * @return true if date string is in the past, false otherwise
     * @throws ParseException thrown if date is not a valid date
     */
    public Boolean dateInThePast(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        Date date = dateFormat.parse(dateString);
        String todayDateString = dateFormat.format(new Date());
        Date todayDate = dateFormat.parse(todayDateString);
        return date.before(todayDate);
    }

    /**
     * Checks if a date string is after today's date
     * @param dateString date string being checked
     * @return true if date string is in the future, false otherwise
     * @throws ParseException thrown if date is not a valid date
     */
    public Boolean dateInTheFuture(String dateString) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        Date date = dateFormat.parse(dateString);
        String todayDateString = dateFormat.format(new Date());
        Date todayDate = dateFormat.parse(todayDateString);
        return date.after(todayDate);
    }

    /**
     * Checks if a date string is after a specified date
     * @param dateString1 the date that is being checked
     * @param dateString2 the date that the other date has to be after
     * @return a Boolean of if the date is after the second date
     * @throws ParseException thrown if date is not a valid date
     */
    public Boolean dateAfterAnotherDate(String dateString1, String dateString2) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false);
        Date date1 = dateFormat.parse(dateString1);
        Date date2 = dateFormat.parse(dateString2);
        return date1.after(date2);
    }

    /**
     * checks if a string is a valid phone number
     * @param phoneNumber the string being checked
     * @return a boolean that is true if the phone number is valid
     */
    public Boolean checkValidPhoneNumber(String phoneNumber) {
        String phoneRegex = "^(\\+\\d{1,3}\\s?)?(\\(?\\d{2,4}\\)?[\\s.-]?)?\\d{3,4}[\\s.-]?\\d{3,4}$";
        Pattern phonePattern = Pattern.compile(phoneRegex);
        Matcher phoneMatcher = phonePattern.matcher(phoneNumber);
        return phoneMatcher.matches();
    }

    /**
     * checks if a string is a positive number
     * @param number the string being checked
     * @return a boolean that is true if the number is positive
     */
    public Boolean checkPositiveNumber(String number) {
        String numberRegex = "^[+]?((([1-9]\\d*)(\\.\\d+)?)|(0*\\.0*[1-9]\\d*))$";
        Pattern numberPattern = Pattern.compile(numberRegex);
        Matcher numberMatcher = numberPattern.matcher(number);
        return numberMatcher.matches();
    }

    /**
     * checks if the user provided ratings are greater than zero and filters out the ratings are not.
     * @param tradieRatings the user's tradie ratings
     * @return the tradie ratings that are going to be saved.
     */
    public List<TradieRating> checkTradieRatings(List<TradieRating> tradieRatings) {
        List<TradieRating> validTradieRatings = new ArrayList<>();
        for (TradieRating tradieRating : tradieRatings) {
            if (tradieRating.getRating() >= MIN_TRADIE_RATING && tradieRating.getRating() <= MAX_TRADIE_RATING) {
                validTradieRatings.add(tradieRating);
            }
        }
        return validTradieRatings;
    }
}
