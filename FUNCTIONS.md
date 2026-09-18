# What Every Function Does

One line per method. Getters/setters are skipped (every field in `model/` and `dto/` has one of each) - only the methods with actual logic are listed.

## model/

**Fridge.java**
- No logic methods - just fields, constructors, getters/setters.

**FoodItem.java**
- `isExpired()` - true if the best-before date already passed.
- `getDaysLeft()` - how many days until the best-before date (negative if already passed).
- `isUrgent()` - true if the item is still available and must be taken today or tomorrow.

## repository/

**FridgeRepository.java**
- `findAllByOrderByNameAsc()` - every fridge, sorted by name.
- `findByActiveTrueOrderByNameAsc()` - only fridges currently in service.

**FoodItemRepository.java**
- `findByFridgeIdOrderByExpiryDateAsc()` - everything inside one fridge, soonest-expiring first.
- `findByStatusOrderByExpiryDateAsc()` - all items with a given status (e.g. all AVAILABLE items).
- `countByStatus()` - how many items have a given status, network-wide.
- `countByFridgeIdAndStatus()` - how many items with a given status are in one fridge.
- `findByStatusAndExpiryDateBefore()` - items still marked AVAILABLE whose date has already passed (used by the expiry sweep).
- `findByStatusAndExpiryDateLessThanEqualOrderByExpiryDateAsc()` - AVAILABLE items expiring within N days.
- `sumWeightByStatus()` - total kilograms across the whole network for a given status.
- `sumWeightByFridgeAndStatus()` - total kilograms inside one fridge for a given status.
- `sumWeightGroupedByCategory()` - rescued kilograms, grouped by food category.

## service/

**FridgeService.java**
- `findAll()` - every fridge.
- `findActive()` - only active fridges.
- `findById()` - one fridge, or throws 404 if the id doesn't exist.
- `summarise()` - builds a `FridgeSummary` (fridge + how full it currently is).
- `create()` - saves a brand new fridge.
- `update()` - overwrites an existing fridge's details.
- `delete()` - removes a fridge, but refuses if it still holds available food.

**FoodItemService.java**
- `findById()` - one item, or throws 404.
- `findByFridge()` - everything inside one fridge (checks the fridge exists first).
- `findByStatus()` - all items with a given status.
- `findExpiringSoon()` - AVAILABLE items due within N days.
- `donate()` - Rule 1 & 2: checks the fridge is active and has space, and the food isn't already expired, then saves it.
- `update()` - edits an item, but only while it's still AVAILABLE.
- `claim()` - Rule 3: marks an item as claimed, refusing if it's already claimed, gone, or expired.
- `removeFromFridge()` - a volunteer manually pulls an item out (e.g. damaged).
- `delete()` - permanently deletes an item's record.

**StatsService.java**
- `buildStats()` - assembles all the dashboard numbers into one `NetworkStats` object.
- `rescuedByCategory()` - turns the raw per-category query results into `CategoryStat` objects with a percentage share.

**ExpirySweepService.java**
- `scheduledSweep()` - runs every minute; calls `sweep()` and just logs if anything goes wrong.
- `sweep()` - finds every AVAILABLE item past its date and flips it to EXPIRED.

**StatsExporter.java** (abstract)
- `export()` *(abstract)* - each subclass turns the stats into its own text format.
- `contentType()` *(abstract)* - the HTTP content type for that format.
- `fileExtension()` *(abstract)* - the file extension for that format.
- `fileName()` - builds the download file name using `fileExtension()`; shared by every subclass.
- `forFormat()` *(static)* - picks a `CsvStatsExporter` or `TextStatsExporter` based on the requested format.

**CsvStatsExporter.java**
- `export()` - writes the stats as comma-separated rows (metrics, then one row per category).

**TextStatsExporter.java**
- `export()` - writes the stats as a human-readable multi-line report.

## controller/

**FridgeController.java**
- `list()` - `GET /api/fridges` - all fridges, or only active ones with `?activeOnly=true`.
- `getOne()` - `GET /api/fridges/{id}` - one fridge.
- `summary()` - `GET /api/fridges/{id}/summary` - fridge plus its filling level.
- `create()` - `POST /api/fridges` - registers a new fridge.
- `update()` - `PUT /api/fridges/{id}` - edits a fridge.
- `delete()` - `DELETE /api/fridges/{id}` - removes a fridge.
- `items()` - `GET /api/fridges/{id}/items` - everything inside that fridge.
- `donate()` - `POST /api/fridges/{id}/items` - donates food into that fridge.

**FoodItemController.java**
- `list()` - `GET /api/items?status=` - items filtered by status.
- `expiringSoon()` - `GET /api/items/expiring?days=` - items due soon.
- `getOne()` - `GET /api/items/{id}` - one item.
- `update()` - `PUT /api/items/{id}` - edits an item.
- `claim()` - `POST /api/items/{id}/claim` - takes the food home.
- `remove()` - `POST /api/items/{id}/remove` - volunteer pulls it out.
- `delete()` - `DELETE /api/items/{id}` - deletes the record.

**StatsController.java**
- `stats()` - `GET /api/stats` - the dashboard numbers as JSON.
- `export()` - `GET /api/stats/export?format=` - downloads the stats as CSV or plain text.

## exception/

**GlobalExceptionHandler.java**
- `handleNotFound()` - catches `ResourceNotFoundException` → 404.
- `handleBusinessRule()` - catches `BusinessRuleException` → 409.
- `handleValidation()` - catches failed `@Valid` checks → 400 with a list of field errors.
- `handleUnreadable()` - catches broken/unparsable JSON → 400.
- `handleTypeMismatch()` - catches a wrong-type URL parameter → 400.
- `handleUnexpected()` - catches anything else → 500, logs the real error, shows only a generic message.
- `build()` - private helper that assembles the actual `ApiError` response every handler above uses.

**ResourceNotFoundException.java**
- `of()` *(static)* - shorthand for building the "X with id Y was not found" message.

## config/

**DataSeeder.java**
- `run()` - runs once on startup (via `CommandLineRunner`); does nothing if the database already has data, otherwise creates 4 demo fridges and 15 demo items.
- `available()` / `claimed()` / `expired()` - private helpers that build a `FoodItem` already in that specific status, used only while seeding.

## static/js/app.js (frontend)

- `api()` - wraps `fetch()`; turns any backend error into a plain JS `Error` with the clean message from the JSON body.
- `showBanner()` - displays the green/red message banner at the top of the page.
- `formatDate()` / `today()` - small date-formatting helpers.
- `setupTabs()` - wires up the Dashboard / Fridges / Donate Food tab buttons.
- `loadDashboard()` - fetches `/api/stats` and `/api/items/expiring`, fills in the stat cards and tables.
- `loadFridges()` - fetches `/api/fridges` (+ each one's `/summary`), renders the fridge cards, and fills the Donate Food dropdown.
- `viewFridgeItems()` - fetches `/api/fridges/{id}/items` and renders the items table with Claim/Remove buttons.
- `claimItem()` - prompts for a name, then calls the claim endpoint.
- `removeItem()` - calls the remove endpoint.
- `deleteFridge()` - calls the delete-fridge endpoint.
- `refreshCurrentFridge()` - re-renders whichever fridge's items are currently on screen, after a change.
- `setupForms()` - wires up the "Add fridge" and "Donate food" form submissions.
