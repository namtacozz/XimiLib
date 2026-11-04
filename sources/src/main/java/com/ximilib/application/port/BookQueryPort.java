package com.ximilib.application.port;

import com.ximilib.application.dto.BookSearchCriteria;
import com.ximilib.application.dto.BookSummaryDto;
import com.ximilib.application.dto.PagedResult;
import java.util.List;

public interface BookQueryPort {

    PagedResult<BookSummaryDto> searchBooks(BookSearchCriteria criteria, int page, int pageSize);

    List<BookSummaryDto> getDeletedBooks();
}
