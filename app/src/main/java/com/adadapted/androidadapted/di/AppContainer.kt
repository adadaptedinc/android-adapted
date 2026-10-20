package com.adadapted.androidadapted.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStoreFile
import com.adadapted.androidadapted.adadapted.ActivityLoggingAdZoneHandler
import com.adadapted.androidadapted.adadapted.ActivityLoggingKeywordSuggestionSource
import com.adadapted.androidadapted.adadapted.AdAdaptedConfig
import com.adadapted.androidadapted.adadapted.AdAdaptedKeywordSuggestionSource
import com.adadapted.androidadapted.adadapted.AdAdaptedListEventTracker
import com.adadapted.androidadapted.adadapted.AdAdaptedService
import com.adadapted.androidadapted.adadapted.AdZoneCatalog
import com.adadapted.androidadapted.adadapted.AdZoneHandlerFactory
import com.adadapted.androidadapted.adadapted.AddToListHandler
import com.adadapted.androidadapted.adadapted.KeywordSuggestionSource
import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog
import com.adadapted.androidadapted.core.activity.InMemoryActivityLog
import com.adadapted.androidadapted.data.ActivityLoggingListEventTracker
import com.adadapted.androidadapted.data.DataStoreGroceryRepository
import com.adadapted.androidadapted.data.GroceryListsSerializer
import com.adadapted.androidadapted.data.GroceryRepository
import com.adadapted.androidadapted.data.ListEventTracker
import com.adadapted.androidadapted.data.StarterLists
import com.adadapted.androidadapted.data.StoredGroceryLists
import com.adadapted.androidadapted.data.model.ItemOrigin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** The app's dependencies, built once per process. Screens receive only what they use. */
interface AppContainer {
    val adAdaptedConfig: AdAdaptedConfig
    val activityLog: ActivityLog
    val groceryRepository: GroceryRepository
    val adZoneHandlerFactory: AdZoneHandlerFactory

    /** Reports straight to the SDK, without logging, for screens that log events their own way. */
    val sdkListEventTracker: ListEventTracker

    /** A new source per screen, since a source remembers the suggestions it last matched. */
    fun createKeywordSuggestionSource(): KeywordSuggestionSource
}

class DefaultAppContainer(context: Context) : AppContainer {

    private val appContext = context.applicationContext

    /** For work that must finish even when the screen that started it goes away. */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    override val adAdaptedConfig: AdAdaptedConfig = AdAdaptedConfig.Default

    override val activityLog: ActivityLog = InMemoryActivityLog()

    override val sdkListEventTracker: ListEventTracker = AdAdaptedListEventTracker()

    private val groceryListsStore: DataStore<StoredGroceryLists> by lazy {
        val serializer = GroceryListsSerializer(StoredGroceryLists(StarterLists.create(AdZoneCatalog.LIST)))
        DataStoreFactory.create(
            serializer = serializer,
            corruptionHandler = ReplaceFileCorruptionHandler { error ->
                activityLog.log(ActivityCategory.ERROR, "Saved lists were unreadable", error.message ?: "Reset to the starter lists")
                serializer.defaultValue
            },
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { appContext.dataStoreFile(GROCERY_LISTS_FILE) },
        )
    }

    override val groceryRepository: GroceryRepository by lazy {
        DataStoreGroceryRepository(
            dataStore = groceryListsStore,
            tracker = ActivityLoggingListEventTracker(sdkListEventTracker, activityLog),
            externalScope = applicationScope,
            zoneRule = AdZoneCatalog.listZoneRule,
            onReadFailed = { error -> activityLog.log(ActivityCategory.ERROR, "Could not read lists", error.message) },
            onWriteFailed = { error -> activityLog.log(ActivityCategory.ERROR, "Could not save lists", error.message) },
        )
    }

    override val adZoneHandlerFactory: AdZoneHandlerFactory = AdZoneHandlerFactory { addToList ->
        ActivityLoggingAdZoneHandler(activityLog, applicationScope, addToList)
    }

    val adAdaptedService: AdAdaptedService by lazy {
        AdAdaptedService(
            config = adAdaptedConfig,
            activityLog = activityLog,
            scope = applicationScope,
            payloadHandler = AddToListHandler { names ->
                groceryRepository.addItemsToDefaultList(names, ItemOrigin.PAYLOAD)
            },
        )
    }

    override fun createKeywordSuggestionSource(): KeywordSuggestionSource =
        ActivityLoggingKeywordSuggestionSource(AdAdaptedKeywordSuggestionSource(), activityLog)

    private companion object {
        const val GROCERY_LISTS_FILE = "grocery_lists.json"
    }
}
