package com.adadapted.androidadapted.data

import com.adadapted.androidadapted.core.activity.ActivityCategory
import com.adadapted.androidadapted.core.activity.ActivityLog

/** Passes events on to [delegate] and records each one in the activity log. */
class ActivityLoggingListEventTracker(
    private val delegate: ListEventTracker,
    private val activityLog: ActivityLog,
) : ListEventTracker {

    override fun track(event: ListEvent) {
        delegate.track(event)
        activityLog.log(ActivityCategory.LIST, event.description, event.listName.ifEmpty { null })
    }
}
