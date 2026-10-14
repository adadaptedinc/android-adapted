# Android Adapted Sample App

The Android Adapted sample app provides app creators with a working example of how to correctly implement the AdAdapted Multiplatform SDK. This app includes examples for utilizing our ad zones, keyword intercept, and tracking functionality.

It is the Jetpack Compose counterpart to [swiftui-adapted](https://github.com/adadaptedinc/swiftui-adapted) and covers the same scenarios.

## What's in the app

Three tabs:

**Lists**: the grocery app itself. Multiple lists persisted with DataStore, plus adding, crossing off, editing and deleting items, quantities, reordering lists, and keyword intercept suggestions above the add field. The open list renders an ad zone under the add field. Add-to-list ads drop their items straight into that list and are badged "From ad". The list menu toggles the `original` recipe zone context and reloads the zone.

**Zone Lab**: SDK test scenarios kept out of the shopping flow.
- *Infinite Scroller*: a paged feed with a zone every eighth row. Each row reports its own visibility (50% of the row inside the list) to `setAdZoneVisibility`. Rows are recycled, so a row scrolled far enough away stops its zone and scrolling back starts a fresh one.
- *Off-Screen Zones*: a recipe page with a zone above and below the content, a picker for the zone context ID, and a button that adds the ingredients to the first list.
- *List Manager Events*: fires `AdAdaptedListManager` events by hand.
- The configuration the SDK was started with.

**Activity**: a live tail of every SDK callback (zone lifecycle, ad events, delivered content), keyword match, and list event the app reported. It can be filtered by category and shared as a plain-text transcript, so the app can be exercised without a debugger attached. Every entry is also written to Logcat under the `AdAdapted` tag.

## How it's put together

A single-activity Compose app: MVVM with `StateFlow` UI state, type-safe Navigation Compose with a back stack per tab, and manual dependency injection through `di/AppContainer.kt`. Only the `adadapted` package touches the SDK. Everything else depends on the small interfaces it implements, so view models are unit tested with fakes (`./gradlew :app:testDebugUnitTest`).

| Concern | Where |
| --- | --- |
| SDK startup and app-wide listeners (`AaSdkEventListener`, `AaSdkAdditContentListener`) | `adadapted/AdAdaptedService.kt` |
| Launch configuration (app ID, environment, feature flags) | `adadapted/AdAdaptedConfig.kt` |
| Compose host for `AaZoneView`, with one listener per zone view | `adadapted/AdZone.kt` |
| Zone callbacks routed to the activity log and to a list | `adadapted/AdZoneHandler.kt` |
| Acknowledging or failing add-to-list content | `adadapted/AddToListOffer.kt` |
| Keyword intercept matching and suggestion tracking | `adadapted/KeywordSuggestionSource.kt` |
| `AdAdaptedListManager` events for list changes | `adadapted/AdAdaptedListEventTracker.kt` |
| List rules (dedupe, renames, merges) and the events they produce, SDK-free | `data/GroceryListEditor.kt` |
| Persistence | `data/DataStoreGroceryRepository.kt` |
| Zone IDs and their creative height | `adadapted/AdZoneCatalog.kt` |

### Zones

The app key (`7D58810X6333241C`) serves three zones: 102110 (lists), 110002 (scroller, off-screen top) and 110003 (off-screen bottom), all authored full width × 100. 102110 and 110002 also serve a different ad for the `original` zone context.

Things worth knowing if you extend this:

- Use **one content listener per zone view**. `AaZoneView.onStop(listener)` unregisters the listener from `AdContentPublisher`, so a shared listener would silently unsubscribe every other zone. `AdZone` handles this for you.
- Every live zone with an ID receives that ID's add-to-list content, so the New List sheet won't accept a Zone Lab zone ID, and the scroller attaches no list.
- `setAdZoneContextId` reports an event every time it's called, so `AdZone` only calls it when the context actually changes.

Official documentation for integrating the SDK libraries can be found at [https://docs.adadapted.com](https://docs.adadapted.com)

### Prerequisites

* Android Studio with recent Android SDK and Kotlin versions installed. (The Kotlin multiplatform plugin is not required to use our multiplatform library.)

### Installing

Simply clone the repository and load it up into Android Studio with the required plugins and updates. The app is supplied already with a test API key that is used for a suite of test ads and keywords to interact with.

### Developing against the SDK source (local composite build)

By default this app pulls the **published** AdAdapted SDK artifact. If you want to
read and edit the SDK source directly from this project, check out the
[`android-sdk`](https://github.com/adadaptedinc/android-sdk) repo as a **sibling
folder** of this one:

```
GitHub/
├── android-adapted/   <- this project
└── android-sdk/       <- SDK source
```

When `../android-sdk` is present, `settings.gradle` automatically wires it in as a
Gradle [composite build](https://docs.gradle.org/current/userguide/composite_builds.html):
the SDK modules show up in the Android Studio project view (fully editable), and
any change is compiled straight into the app on the next build — no
`maven publish` / JitPack round-trip.

**You don't have to clone it yourself** — if the folder is missing, the first
Gradle sync auto-clones it from GitHub for you.

#### Keeping the local SDK up to date

The auto-clone is a **one-time bootstrap, not a sync** — it only runs when the
folder is absent. Once `../android-sdk` exists, Gradle builds against whatever is
checked out there and **never updates it for you**. Two consequences:

- The `com.github.adadaptedinc:android-sdk:5.0.0` version in `app/build.gradle`
  is **ignored** in local mode — the composite build substitutes it for your
  on-disk source regardless of the version string. When the SDK is tagged, say,
  `5.1.0`, you will *not* pick it up until you update the checkout yourself:

  ```
  cd ../android-sdk && git pull
  ```

- To make the version string meaningful again (i.e. consume the published
  artifact), set `adadapted.sdk.local=false` in `local.properties` and bump the
  version in `app/build.gradle`.

Each sync prints the checked-out SDK revision, e.g.
`Building against local SDK source at … (5.0.0)` or `… (5.0.0-4-g1a2b3c-dirty)`,
so a stale checkout is visible at a glance.

Configuration (all optional, per-developer, via the gitignored `local.properties`):

| Property | Default | Purpose |
|----------|---------|---------|
| `adadapted.sdk.local` | `true` | Set to `false` to force the published artifact and ignore any local checkout. |
| `adadapted.sdk.path`  | `../android-sdk` | Point at a different location for the SDK checkout (absolute or relative to this project). |

> Composite builds require a single Android Gradle Plugin version across both
> projects. This app is aligned to the SDK's AGP version; if you bump one, bump
> the other.

> **Gradle JDK:** the current AGP requires the Gradle daemon to run on **JDK 17
> or newer**. If Android Studio reports *"Gradle JVM version incompatible"*, set
> **Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JDK**
> to the embedded JBR (or any JDK 17+) and re-sync.
