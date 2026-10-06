# NOTES

## Summary of changes
- Fixed AND/OR precedence in the search query (`TaskRepository`, `search_tasks.sql`, and Oracle package) so the archived and status filters are applied correctly.
- Removed the unnecessary `Thread.sleep` delay from `TaskController`.
- Fixed `useTasks` so loading does not remain stuck when the API fails, errors are displayed correctly, and stale responses are ignored.
- Added a 300 ms search debounce and reset the page to 1 when the query or status changes.
- Added validation so invalid status values return 400, and page/pageSize values are clamped.

## What I chose not to change
- Pagination is still performed in memory. I kept the diff small as requested by the README.
- I did not escape `%` and `_` in the search term, add DTOs, or add tests.
- The "pagination off-by-one" seed task is data-related; the existing page calculation was correct.

## Biggest remaining risk
The endpoint loads every matching row and then paginates in Java. This will not scale well with a large table. It should use database-level pagination (`LIMIT/OFFSET` or `Pageable`) together with a count query.

## Tools / AI used
I used Claude to walk through the codebase and help draft the fixes. I reproduced each bug myself before changing it, tested each fix, and wrote the handwritten explanations myself.