package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.service.LocationQueryService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
public class LocationQueryServiceTest {

    private LocationQueryService locationQueryService;

    @BeforeEach
    void setUp() {
        locationQueryService = new LocationQueryService();
    }

    @Test
    public void formatLocationComponents_AllComponentsPresent_BaseString() {
        List<String> locationComponents = List.of("street", "suburb", "city", "postcode", "country");
        Assertions.assertEquals(locationQueryService.formatLocationComponents(locationComponents),
                "street, suburb, city");
    }

    @Test
    public void formatLocationComponents_MissingComponents_ReducedString() {
        List<String> locationComponents = List.of("street", "", "city", "", "country");
        Assertions.assertEquals(locationQueryService.formatLocationComponents(locationComponents),
                "street, city");
    }

    @Test
    public void getBoundingBoxFromIp_UnusableIp_ChristchurchFallback() {
        String ip = "0.0.0.0";
        Assertions.assertEquals(locationQueryService.getBoundingBoxFromIp(ip).split(",")[0], "171.636200");
        Assertions.assertEquals(locationQueryService.getBoundingBoxFromIp(ip).split(",")[1], "-44.532100");
    }

    @Test
    public void getStructuredLocationSuggestions_NoMatchedSuggestions_EmptyList() {
        String query = "asdasdsfsrgkjb jhfbajhfdbahjbdhjabdjhadghj asdasd"; //nonsense that won't match any address
        String viewbox = locationQueryService.getBoundingBoxFromIp("0.0.0.0");
        Assertions.assertEquals(locationQueryService.getStructuredLocationSuggestions(query, viewbox).size(), 0);
    }
}
