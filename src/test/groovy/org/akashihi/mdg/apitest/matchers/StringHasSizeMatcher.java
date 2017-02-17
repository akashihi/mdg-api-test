package org.akashihi.mdg.apitest.matchers;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Check, whether string have specified length
 */
public class StringHasSizeMatcher extends TypeSafeMatcher<String> {
    private final int expectedLength;

    public StringHasSizeMatcher(final int l) {
        this.expectedLength = l;
    }

    @Override
    protected boolean matchesSafely(String item) {
        if (item == null) {
            return expectedLength == 0;
        }
        return item.length() == expectedLength;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("String should have length of  ");
        description.appendValue(expectedLength);
    }

    public static StringHasSizeMatcher
    stringHasSize(final int parameters) {
        return new StringHasSizeMatcher(parameters);
    }
}
