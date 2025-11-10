package nz.ac.canterbury.seng302.homehelper.service;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used for generating pagination values to be passed into the model for a page. This class was written
 * with the help of chatgpt, but it did write most of it.
 */
public class PaginationUtil {

    /**
     * Finds the items on a page given a page number and returns those items as a list.
     * @param allItems This is the list of all the items that you want to be paginated.
     * @param pageNumber This is the number of the page that you want.
     * @param pageSize This is the number of elements you want in each page.
     * @return a list of {@code pageSize} items of type {@code T}, this list represents the items on the page with number {@code pageNumber}
     * @param <T> this is an object that is being paginated. an example is that this function is used to paginate a
     *           list of Quotes, in this scenario the class {@code Quote} is {@code T}
     */
    public static <T> List<T> getPage(List<T> allItems, int pageNumber, int pageSize) {
        int totalItems = allItems.size();
        int fromIndex = (pageNumber - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        if (fromIndex >= totalItems || fromIndex < 0) {
            return List.of();
        }
        return allItems.subList(fromIndex, toIndex);
    }

    /**
     * Finds the number of the last page.
     * @param totalItems the total number of items being paginated.
     * @param pageSize the number of elements allowed on each page.
     * @return a number representing the page number of the last page.
     */
    public static int getLastPageNumber(int totalItems, int pageSize) {
        return (int) Math.ceil((double) totalItems / pageSize);
    }

    /**
     * This is used only in frontend. It gives a list of page numbers in order.
     * @param totalItems the total number of items being paginated.
     * @param pageSize the number of elements allowed on each page.
     * @return a list of numbers upto the biggest page number.
     */
    public static List<Integer> getPageNumbers(int totalItems, int pageSize) {
        List<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= getLastPageNumber(totalItems, pageSize); i++) {
            nums.add(i);
        }
        return nums;
    }
}