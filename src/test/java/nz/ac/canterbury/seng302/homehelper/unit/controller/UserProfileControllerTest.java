package nz.ac.canterbury.seng302.homehelper.unit.controller;

import nz.ac.canterbury.seng302.homehelper.controller.account.UserProfileController;
import nz.ac.canterbury.seng302.homehelper.entity.dto.MapMarker;
import nz.ac.canterbury.seng302.homehelper.entity.dto.MapPosition;
import nz.ac.canterbury.seng302.homehelper.service.JobService;
import nz.ac.canterbury.seng302.homehelper.service.RenovationRecordService;
import nz.ac.canterbury.seng302.homehelper.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserProfileControllerTest {

    @Mock private UserService userService;
    @Mock private RenovationRecordService renovationRecordService;
    @Mock private JobService jobService;

    @InjectMocks
    private UserProfileController userProfileController;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void getTradiesPortfolioJobMarkers_TradieExistsAndHasLocatedJobs_StatusIsOKAndJobsAreInDtos() {
        int length = 5;
        List<MapMarker> markers = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            markers.add(
                    new MapMarker("job"+i, Long.parseLong(""+i), new MapPosition(0.0f, 0.0f))
            );
        }
        when(userService.getPortfolioMapMarkers(1L)).thenReturn(markers);

        ResponseEntity<?> response = userProfileController.getTradiesPortfolioJobMarkers(1L);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals(markers, response.getBody());
    }

    @Test
    public void getTradiesPortfolioJobMarkers_TradieDoesNotExist_StatusIsBadAndErrorIsReturned() {

        when(userService.getPortfolioMapMarkers(1L)).thenThrow(new NullPointerException("User not found"));

        ResponseEntity<?> response = userProfileController.getTradiesPortfolioJobMarkers(1L);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertTrue(Objects.requireNonNull(response.getBody()).toString().contains("User not found"));
    }

}
