package nz.ac.canterbury.seng302.homehelper.controller.renovations;

import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.dto.LocationQuery;
import nz.ac.canterbury.seng302.homehelper.service.LocationQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * This handles location auto complete functionality. used by the register user, edit user,
 * create renovation and edit renovation pages
 */
@Controller
public class LocationAutocompleteFormController {

    private LocationQueryService locationQueryService;

    @Autowired
    public LocationAutocompleteFormController(LocationQueryService locationQueryService) {
        this.locationQueryService = locationQueryService;
    }

    /**
     * returns a list of suggestion to the page. It uses a location query service to
     * gather suggestions based on the location of the user.
     * @param q is the query string. this is used to make autocomplete suggestions
     * @param session HttpSession of the user
     * @return a list of suggestions formatted as [[street, suburb, city, postcode, country, fullAddress], ...]
     */
    @GetMapping("/suggest")
    @ResponseBody
    public List<List<String>> suggest(@RequestParam String q, HttpSession session) {
        LocationQuery locationQuery = (LocationQuery) session.getAttribute("locationQuery");
        if (locationQuery == null) {
            return List.of(); // or reinitialize based on default
        }
        if (q != locationQuery.getQuery()) {
            locationQuery.setQuery(q);
            locationQueryService.enqueueQuery(locationQuery);
        }
        try {
            List<List<String>> originalSuggestions = locationQuery.getSuggestions();
            List<List<String>> extendedSuggestions = new ArrayList<>();
            for (List<String> components : originalSuggestions) {
                List<String> newComponents = new ArrayList<>(components);
                String fullAddress = locationQueryService.formatLocationComponents(components);
                newComponents.add(fullAddress);
                extendedSuggestions.add(newComponents);
            }
            return extendedSuggestions;
        } catch (NullPointerException e) {
            return List.of();
        }
    }
}
