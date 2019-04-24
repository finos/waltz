package com.khartec.waltz.service.user_contribution;

import com.khartec.waltz.model.tally.OrderedTally;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserContributionUtilities {



    public static List<OrderedTally<String>> getOrderedListOf10(List<OrderedTally<String>> list, String userId){

        Optional<OrderedTally<String>> tallyForUserId = list
                .stream()
                .filter(d -> d.id().equals(userId))
                .findFirst();

        int userRank = tallyForUserId.map(t -> t.index()).orElse(0);

        List<OrderedTally<String>> subsectionOrderedList = findWindow(list, userRank, 10);

        return subsectionOrderedList;
    }

    public static <T> List<T> findWindow(List<T> list, int windowFocus, int windowSize) {

        int maxRange = list.size();
        int minRange = 1;

        int range;
        int upperBound;
        int lowerBound;

        if (windowSize%2 == 0){
            range = windowSize/2;
            upperBound = windowFocus + range;
            lowerBound = windowFocus - (range - 1);
        } else {
            range = (windowSize - 1)/2;
            upperBound = windowFocus + range;
            lowerBound = windowFocus - range;
        }

        if (lowerBound <= minRange) {
            lowerBound = minRange;
            upperBound = windowSize;
        } else if (upperBound >= maxRange) {
            lowerBound = maxRange - windowSize + 1;
            upperBound = maxRange;
        }

        int finalUpperBound = upperBound;
        int finalLowerBound = lowerBound;

        List<T> orderedList = list
                .stream()
                .filter(d -> list.indexOf(d) >= (finalLowerBound - 1)
                && list.indexOf(d) <= (finalUpperBound - 1))
                .collect(Collectors.toList());

        return orderedList;
    }

}

