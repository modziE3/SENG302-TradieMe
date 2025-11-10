package nz.ac.canterbury.seng302.homehelper.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.homehelper.entity.dto.LocationQuery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

/**
 * Service class for Location Querys.
 * This class interacts with LocationIQ API and IP API.
 */
@Service
public class LocationQueryService {

    private final Queue<LocationQuery> queryQueue = new LinkedList<>();
    private final double CHRISTCHURCH_LAT = -43.5321;
    private final double CHRISTCHURCH_LON = 172.6362;
    private final double BOUNDING_BOX_OFFSET = 1;

    @Value("${location.iq.api.key}")
    private String apiKey;

    /**
     * Puts a users query at the back of the queue
     * @param query query containing users region and latest query
     */
    public void enqueueQuery(LocationQuery query) {
        queryQueue.remove(query);
        queryQueue.add(query);
    }

    /**
     * updates the query queue. this runs at fixed rate of 1 query per
     * second.
     */
    @Scheduled(fixedRate = 1000)
    public void updateQueue() {
        LocationQuery query = queryQueue.poll();
        if (query != null) {
            query.setSuggestions(getStructuredLocationSuggestions(query.getQuery(), query.getViewbox()));
        }
    }

    /**
     * Location IQ gatherer. This directly interacts with locationIQ. this function is
     * very gorey.
     * @param query string to make autocomplete suggestions fort.
     * @param viewbox region to bound by.
     * @param bounded should the query be location bounded.
     * @return the suggestions.
     */
    public String buildLocationIqUrl(String query, String viewbox, boolean bounded) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("https://us1.locationiq.com/v1/autocomplete.php")
                .queryParam("key", apiKey)
                .queryParam("q", query)
                .queryParam("format", "json")
                .queryParam("normalizecity", 1)
                .queryParam("limit", 5)
                .queryParam("dedupe", 1)
                .queryParam("addressdetails", 1);
        if (bounded && viewbox != null && !viewbox.isEmpty()) {
            builder.queryParam("bounded", 1)
                    .queryParam("viewbox", viewbox);
        }
        return builder.build().toUriString();
    }

    /**
     * used to create a rest template that interacts with an api
     * @param url the link/url of api used.
     * @return a rest template.
     */
    public String fetchResponse(String url) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.getForObject(url, String.class);
    }

    /**
     * Uses the users Ip to get their region. This is used to make localized suggestions.
     * @param ip the users ip. this will be used to find their location.
     * @return string that represents a box of lat lon coordinates
     */
    public String getBoundingBoxFromIp(String ip) {
        try {
            String geoUrl = "https://ipapi.co/" + ip + "/json/";
            RestTemplate restTemplate = new RestTemplate();
            Map<String, Object> geoData = restTemplate.getForObject(geoUrl, Map.class);
            double lat = Double.parseDouble(geoData.get("latitude").toString());
            double lon = Double.parseDouble(geoData.get("longitude").toString());
            return String.format("%f,%f,%f,%f",
                    lon - BOUNDING_BOX_OFFSET,
                    lat - BOUNDING_BOX_OFFSET,
                    lon + BOUNDING_BOX_OFFSET,
                    lat + BOUNDING_BOX_OFFSET);
        } catch (Exception e) {
            return String.format("%f,%f,%f,%f",
                    CHRISTCHURCH_LON - BOUNDING_BOX_OFFSET,
                    CHRISTCHURCH_LAT - BOUNDING_BOX_OFFSET,
                    CHRISTCHURCH_LON + BOUNDING_BOX_OFFSET,
                    CHRISTCHURCH_LAT + BOUNDING_BOX_OFFSET);
        }
    }

    /**
     * Gets structured location suggestions as list of components:
     * [street address, suburb, city, postcode, country]
     * this function was made with help of chatgpt
     * @param query the search string
     * @param viewbox optional bounding box
     * @return list of lists of address components formatted as [[street, suburb, city, postcode, country, fullAddress], ...]
     */
    public List<List<String>> getStructuredLocationSuggestions(String query, String viewbox) {
        String boundedUrl = buildLocationIqUrl(query, viewbox, true);
        try {
            String response;
            try {
                response = fetchResponse(boundedUrl);
            } catch (Exception e) {
                // Fallback to unbounded search
                String unboundedUrl = buildLocationIqUrl(query, null, false);
                response = fetchResponse(unboundedUrl);
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);
            List<List<String>> structuredSuggestions = new ArrayList<>();

            for (JsonNode suggestion : root) {
                JsonNode address = suggestion.path("address");
                String houseNumber = getFirstNonEmpty(address,
                        "house_number", "housenumber");
                String road = getFirstNonEmpty(address,
                        "road", "pedestrian", "path", "footway", "residential", "street", "address29");
                String suburb = getFirstNonEmpty(address,
                        "suburb", "neighbourhood", "quarter", "borough", "locality");
                String city = getFirstNonEmpty(address,
                        "city", "town", "village", "municipality", "hamlet", "locality", "county", "state_district");
                String postcode = getFirstNonEmpty(address,
                        "postcode", "postal_code");
                String country = getFirstNonEmpty(address,
                        "country", "country_name");
                String streetAddress = (houseNumber + " " + road).trim();

                // Coordinates
                String lat = suggestion.path("lat").asText("");
                String lon = suggestion.path("lon").asText("");

                boolean hasStructuredAddress = !streetAddress.isEmpty() || !suburb.isEmpty() || !city.isEmpty();
                if (!hasStructuredAddress) {
                    String displayName = suggestion.path("display_name").asText("");
                    if (!displayName.isEmpty() && !lat.isEmpty() && !lon.isEmpty()) {
                        List<String> components = Arrays.asList(
                                displayName, "", "", "", "", lat, lon
                        );
                        structuredSuggestions.add(components);
                        continue;
                    }
                }

                List<String> components = Arrays.asList(
                        streetAddress,
                        suburb,
                        city,
                        postcode,
                        country,
                        lat,
                        lon
                );

                structuredSuggestions.add(components);
            }
            return structuredSuggestions;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String getFirstNonEmpty(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = node.path(field).asText("");
            if (!value.isEmpty()) {
                return value;
            }
        }
        return "";
    }


    /**
     * Formats a list of address components into a readable address string.
     * Skips any empty components.
     * @param components [street, suburb, city, postcode, country]
     * @return formatted address string
     */
    public String formatLocationComponents(List<String> components) {
        return components.stream()
                .limit(components.size() - 2)
                .filter(s -> s != null && !s.isBlank())
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    /**
     * Creates new Location Query and sets it as the value of the location query attribute for an HTTP session
     * @param request HTTP request
     * @param session HTTP session
     */
    public void setLocationQuerySessionAttribute(HttpServletRequest request, HttpSession session) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        } else {
            ipAddress = ipAddress.split(",")[0];
        }
        LocationQuery query = new LocationQuery("", getBoundingBoxFromIp(ipAddress), null);
        session.setAttribute("locationQuery", query);
    }
}

