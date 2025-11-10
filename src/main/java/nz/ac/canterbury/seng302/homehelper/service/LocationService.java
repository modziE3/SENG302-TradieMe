package nz.ac.canterbury.seng302.homehelper.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationService {
    private final ValidationService validationService;

    public static final String STREET_ADDRESS_EMPTY = "Location must contain a street address";
    public static final String STREET_ADDRESS_INVALID = "Street address contains invalid characters";
    public static final String SUBURB_INVALID = "Suburb contains invalid characters";
    public static final String CITY_INVALID = "City contains invalid characters";
    public static final String POSTCODE_INVALID = "Postcode contains invalid characters";
    public static final String COUNTRY_INVALID = "Country contains invalid characters";
    public static final String CITY_EMPTY = "City must not be empty if location supplied";
    public static final String POSTCODE_EMPTY = "Postcode must not be empty if location supplied";
    public static final String COUNTRY_EMPTY = "Country must not be empty if location supplied";
    public static final String STREET_ADDRESS_OVER_512_CHARS = "Street address must be 512 characters long or less";
    public static final String SUBURB_OVER_64_CHARS = "Suburb must be 64 characters long or less";
    public static final String CITY_OVER_64_CHARS = "City must be 64 characters long or less";
    public static final String POSTCODE_OVER_64_CHARS = "State must be 64 characters long or less";
    public static final String COUNTRY_OVER_64_CHARS = "Country must be 64 characters long or less";


    @Autowired
    public LocationService(ValidationService validationService) {
        this.validationService = validationService;
    }

    /**
     * Gets all location errors for a given location.
     * Uses the checks for the individual location components as well as check for if the street address is empty.
     * @param locationInfo the location to check
     * @return A string containing the location errors. The string will be empty if there are no errors present with the location.
     */
    public String locationErrors(List<String> locationInfo) {
        List<String> errors = new ArrayList<>();

        if (!validationService.correctLocationInfo(locationInfo)) {
            errors.add("INVALID-LOCATION");
        }
        errors.addAll(validationService.checkLocation(locationInfo));

        if (!errors.isEmpty()) {
            return (String.join(" ", errors));
        } else {
            return "";
        }
    }


    /**
     * Adds the location error messages to a given model for a html template.
     * @param errorMessages The string containing the error messages.
     * @param model The model that the error messages will be added to.
     */
    public void setLocationErrorMessages(String errorMessages, Model model) {
        if (errorMessages.contains("INVALID-LOCATION")) {
            model.addAttribute("addressErrorMessage", STREET_ADDRESS_EMPTY);
        } else if (errorMessages.contains("STREET_ADDRESS_OVER_512_CHARS")) {
            model.addAttribute("addressErrorMessage", STREET_ADDRESS_OVER_512_CHARS);
        } else if (errorMessages.contains("ADDRESS")) {
            model.addAttribute("addressErrorMessage", STREET_ADDRESS_INVALID);
        }
        if (errorMessages.contains("SUBURB_OVER_64_CHARS")) {
            model.addAttribute("suburbErrorMessage", SUBURB_OVER_64_CHARS);
        } else if (errorMessages.contains("SUBURB")) {
            model.addAttribute("suburbErrorMessage", SUBURB_INVALID);
        }
        if (errorMessages.contains("CITY_EMPTY")) {
            model.addAttribute("cityErrorMessage", CITY_EMPTY);
        } else if (errorMessages.contains("CITY_OVER_64_CHARS")) {
            model.addAttribute("cityErrorMessage", CITY_OVER_64_CHARS);
        } else if (errorMessages.contains("CITY")) {
            model.addAttribute("cityErrorMessage", CITY_INVALID);
        }
        if (errorMessages.contains("POSTCODE_EMPTY")) {
            model.addAttribute("postcodeErrorMessage", POSTCODE_EMPTY);
        } else if (errorMessages.contains("POSTCODE_OVER_64_CHARS")) {
            model.addAttribute("postcodeErrorMessage", POSTCODE_OVER_64_CHARS);
        } else if (errorMessages.contains("POSTCODE")) {
            model.addAttribute("postcodeErrorMessage", POSTCODE_INVALID);
        }
        if (errorMessages.contains("COUNTRY_EMPTY")) {
            model.addAttribute("countryErrorMessage", COUNTRY_EMPTY);
        } else if (errorMessages.contains("COUNTRY_OVER_64_CHARS")) {
            model.addAttribute("countryErrorMessage", COUNTRY_OVER_64_CHARS);
        } else if (errorMessages.contains("COUNTRY")) {
            model.addAttribute("countryErrorMessage", COUNTRY_INVALID);
        }
    }

    /**
     * Goes through a string of all detected errors from a form with location support
     * and assigns an error message from the string to be displayed for each
     * field of the form
     * @param allErrorMessages String of all errors detected from the form with location support
     * @return List of error messages (one for each input field of the form) that will
     * be displayed on the form
     */
    public List<String> getErrorMessages(String allErrorMessages) {
        String streetAddressErrorMessage = "";
        if (allErrorMessages.contains(STREET_ADDRESS_EMPTY)) {
            streetAddressErrorMessage = STREET_ADDRESS_EMPTY;
        }
        if (allErrorMessages.contains(STREET_ADDRESS_INVALID)) {
            streetAddressErrorMessage = STREET_ADDRESS_INVALID;
        }

        String suburbErrorMessage = "";
        if (allErrorMessages.contains(SUBURB_INVALID)) {
            suburbErrorMessage = SUBURB_INVALID;
        }

        String cityErrorMessage = "";
        if (allErrorMessages.contains(CITY_EMPTY)) {
            cityErrorMessage = CITY_EMPTY;
        }
        if (allErrorMessages.contains(CITY_INVALID)) {
            cityErrorMessage = CITY_INVALID;
        }

        String postcodeErrorMessage = "";
        if (allErrorMessages.contains(POSTCODE_EMPTY)) {
            postcodeErrorMessage = POSTCODE_EMPTY;
        }
        if (allErrorMessages.contains(POSTCODE_INVALID)) {
            postcodeErrorMessage = POSTCODE_INVALID;
        }

        String countryErrorMessage = "";
        if (allErrorMessages.contains(COUNTRY_EMPTY)) {
            countryErrorMessage = COUNTRY_EMPTY;
        }
        if (allErrorMessages.contains(COUNTRY_INVALID)) {
            countryErrorMessage = COUNTRY_INVALID;
        }

        return List.of(streetAddressErrorMessage, suburbErrorMessage, cityErrorMessage, postcodeErrorMessage, countryErrorMessage);
    }
}
