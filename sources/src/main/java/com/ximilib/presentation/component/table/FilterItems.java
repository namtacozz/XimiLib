package com.ximilib.presentation.component.table;

import com.ximilib.application.dto.BookSearchCriteria;

public final class FilterItems {

    private FilterItems() {}

    public static class CategoryItem {
        public final Integer id;
        public final String name;

        public CategoryItem(Integer id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static class AvailabilityItem {
        public final BookSearchCriteria.AvailabilityFilter filter;
        public final String label;

        public AvailabilityItem(BookSearchCriteria.AvailabilityFilter filter, String label) {
            this.filter = filter;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
