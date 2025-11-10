package nz.ac.canterbury.seng302.homehelper.unit.service;

import nz.ac.canterbury.seng302.homehelper.service.PaginationUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class PaginationUtilTest {

    static List<String> items = new ArrayList<>();

    @BeforeAll
    static void setUp() {
        for (int i = 0; i<100; i++) {
            items.add("item" + i);
        }
    }
    @Test
    public void getPage_ThereAreEnoughItems_PageIsFull() {
        int pageSize = 10;
        List<String> page = PaginationUtil.getPage(items, 2, pageSize);
        Assertions.assertTrue(page.size() == pageSize);
    }
    @Test
    public void getPage_ThereAreNotEnoughItems_PageIsPartiallyFull() {
        int pageSize = 200;
        List<String> page = PaginationUtil.getPage(items, 1, pageSize);
        Assertions.assertEquals(page.size(), items.size());
    }
    @Test
    public void getLastPageNumber_ThereAreLotsOfItems_CorrectLastPageNumber() {
        int pageSize = 5;
        int lastPageNum = PaginationUtil.getLastPageNumber(items.size(), pageSize);
        Assertions.assertEquals(lastPageNum, 20);
    }
    @Test
    public void getPageNumbers_ThereAreLotsOfItems_CorrectPageNumberList() {
        int pageSize = 5;
        List<Integer> pageNums = PaginationUtil.getPageNumbers(items.size(), pageSize);
        for (Integer i = 1; i<=pageNums.size(); i++) {
            Assertions.assertEquals(pageNums.get(i-1), i);
        }
    }
}
