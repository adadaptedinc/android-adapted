# Android Adapted Sample App

The Android Adapted sample app provides app creators with a working example of how to correctly implement the AdAdapted Multiplatform SDK. This app includes examples for utilizing our ad zones, keyword intercept, and tracking functionality.

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
