package com.invisible.silentinstall.download;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation of the EventController.
 */
public final class DownloadEventController {
	
	private List<IDownloadEventsListener> mDownloadListeners;
	
	/**
	 * Holder for singleton implementation.
	 */
	private static class EventControllerHolder {
		private static final DownloadEventController INSTANCE = new DownloadEventController();
	}
	
	/**
	 * Get the unique instance of the Controller.
	 * @return The instance of the Controller
	 */
	public static DownloadEventController getInstance() {
		return EventControllerHolder.INSTANCE;
	}
	
	/**
	 * Private Constructor.
	 */
	private DownloadEventController() {
		mDownloadListeners = new ArrayList<IDownloadEventsListener>();
	}
	
	/**
	 * Add a listener for download events.
	 * @param listener The listener to add.
	 */
	public synchronized void addDownloadListener(IDownloadEventsListener listener) {
		
		if (!mDownloadListeners.contains(listener)) {
			mDownloadListeners.add(listener);
		}
	}
	
	/**
	 * Remove a listener for download events.
	 * @param listener The listener to remove.
	 */
	public synchronized void removeDownloadListener(IDownloadEventsListener listener) {
		mDownloadListeners.remove(listener);
	}
	
	/**
	 * Trigger a download event.
	 * @param event The event.
	 * @param data Additional data.
	 */
	public synchronized void fireDownloadEvent(int event, Object data) {
		Iterator<IDownloadEventsListener> iter = mDownloadListeners.iterator();
		while (iter.hasNext()) {
			iter.next().onDownloadEvent(event, data);
		}
	}

}
