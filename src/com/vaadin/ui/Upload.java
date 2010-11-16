/*
 * @ITMillApache2LicenseForJavaFiles@
 */

package com.vaadin.ui;

import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import com.vaadin.terminal.PaintException;
import com.vaadin.terminal.PaintTarget;
import com.vaadin.terminal.gwt.client.ui.VUpload;
import com.vaadin.terminal.gwt.server.NoInputStreamException;
import com.vaadin.terminal.gwt.server.NoOutputStreamException;
import com.vaadin.ui.ClientWidget.LoadStyle;

/**
 * Component for uploading files from client to server.
 * 
 * <p>
 * The visible component consists of a file name input box and a browse button
 * and an upload submit button to start uploading.
 * 
 * <p>
 * The Upload component needs a java.io.OutputStream to write the uploaded data.
 * You need to implement the Upload.Receiver interface and return the output
 * stream in the receiveUpload() method.
 * 
 * <p>
 * You can get an event regarding starting (StartedEvent), progress
 * (ProgressEvent), and finishing (FinishedEvent) of upload by implementing
 * StartedListener, ProgressListener, and FinishedListener, respectively. The
 * FinishedListener is called for both failed and succeeded uploads. If you wish
 * to separate between these two cases, you can use SucceededListener
 * (SucceededEvenet) and FailedListener (FailedEvent).
 * 
 * <p>
 * The upload component does not itself show upload progress, but you can use
 * the ProgressIndicator for providing progress feedback by implementing
 * ProgressListener and updating the indicator in updateProgress().
 * 
 * <p>
 * Setting upload component immediate initiates the upload as soon as a file is
 * selected, instead of the common pattern of file selection field and upload
 * button.
 * 
 * <p>
 * Note! Because of browser dependent implementations of <input type="file">
 * element, setting size for Upload component is not supported. For some
 * browsers setting size may work to some extend.
 * 
 * @author IT Mill Ltd.
 * @version
 * @VERSION@
 * @since 3.0
 */
@SuppressWarnings("serial")
@ClientWidget(value = VUpload.class, loadStyle = LoadStyle.LAZY)
public class Upload extends AbstractComponent implements Component.Focusable {

    /**
     * Should the field be focused on next repaint?
     */
    private final boolean focus = false;

    /**
     * The tab order number of this field.
     */
    private int tabIndex = 0;

    /**
     * The output of the upload is redirected to this receiver.
     */
    private Receiver receiver;

    private boolean isUploading;

    private long contentLength = -1;

    private int totalBytes;

    private String buttonCaption = "Upload";

    /**
     * ProgressListeners to which information about progress is sent during
     * upload
     */
    private LinkedHashSet<ProgressListener> progressListeners;

    private boolean interrupted = false;

    private boolean notStarted;

    private int nextid;

    /**
     * Creates a new instance of Upload.
     * 
     * The receiver must be set before performing an upload.
     */
    public Upload() {
    }

    public Upload(String caption, Receiver uploadReceiver) {
        setCaption(caption);
        receiver = uploadReceiver;
    }

    /**
     * Invoked when the value of a variable has changed.
     * 
     * @see com.vaadin.ui.AbstractComponent#changeVariables(java.lang.Object,
     *      java.util.Map)
     */
    @Override
    public void changeVariables(Object source, Map<String, Object> variables) {
        if (variables.containsKey("pollForStart")) {
            int id = (Integer) variables.get("pollForStart");
            if (!isUploading && id == nextid) {
                notStarted = true;
                requestRepaint();
            } else {
            }
        }
    }

    /**
     * Paints the content of this component.
     * 
     * @param target
     *            Target to paint the content on.
     * @throws PaintException
     *             if the paint operation failed.
     */
    @Override
    public void paintContent(PaintTarget target) throws PaintException {
        if (notStarted) {
            target.addAttribute("notStarted", true);
            notStarted = false;
            return;
        }
        // The field should be focused
        if (focus) {
            target.addAttribute("focus", true);
        }

        // The tab ordering number
        if (tabIndex >= 0) {
            target.addAttribute("tabindex", tabIndex);
        }

        target.addAttribute("state", isUploading);

        target.addAttribute("buttoncaption", buttonCaption);

        target.addAttribute("nextid", nextid);

        // Post file to this strean variable
        target.addVariable(this, "action", getStreamVariable());

    }

    /**
     * Interface that must be implemented by the upload receivers to provide the
     * Upload component an output stream to write the uploaded data.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 3.0
     */
    public interface Receiver extends Serializable {
        public OutputStream receiveUpload(String filename, String mimetype);
    }

    /* Upload events */

    private static final Method UPLOAD_FINISHED_METHOD;

    private static final Method UPLOAD_FAILED_METHOD;

    private static final Method UPLOAD_SUCCEEDED_METHOD;

    private static final Method UPLOAD_STARTED_METHOD;

    static {
        try {
            UPLOAD_FINISHED_METHOD = FinishedListener.class.getDeclaredMethod(
                    "uploadFinished", new Class[] { FinishedEvent.class });
            UPLOAD_FAILED_METHOD = FailedListener.class.getDeclaredMethod(
                    "uploadFailed", new Class[] { FailedEvent.class });
            UPLOAD_STARTED_METHOD = StartedListener.class.getDeclaredMethod(
                    "uploadStarted", new Class[] { StartedEvent.class });
            UPLOAD_SUCCEEDED_METHOD = SucceededListener.class
                    .getDeclaredMethod("uploadSucceeded",
                            new Class[] { SucceededEvent.class });
        } catch (final java.lang.NoSuchMethodException e) {
            // This should never happen
            throw new java.lang.RuntimeException(
                    "Internal error finding methods in Upload");
        }
    }

    /**
     * Upload.Received event is sent when the upload receives a file, regardless
     * of whether the reception was successful or failed. If you wish to
     * distinguish between the two cases, use either SucceededEvent or
     * FailedEvent, which are both subclasses of the FinishedEvent.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 3.0
     */
    public static class FinishedEvent extends Component.Event {

        /**
         * Length of the received file.
         */
        private final long length;

        /**
         * MIME type of the received file.
         */
        private final String type;

        /**
         * Received file name.
         */
        private final String filename;

        /**
         * 
         * @param source
         *            the source of the file.
         * @param filename
         *            the received file name.
         * @param MIMEType
         *            the MIME type of the received file.
         * @param length
         *            the length of the received file.
         */
        public FinishedEvent(Upload source, String filename, String MIMEType,
                long length) {
            super(source);
            type = MIMEType;
            this.filename = filename;
            this.length = length;
        }

        /**
         * Uploads where the event occurred.
         * 
         * @return the Source of the event.
         */
        public Upload getUpload() {
            return (Upload) getSource();
        }

        /**
         * Gets the file name.
         * 
         * @return the filename.
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Gets the MIME Type of the file.
         * 
         * @return the MIME type.
         */
        public String getMIMEType() {
            return type;
        }

        /**
         * Gets the length of the file.
         * 
         * @return the length.
         */
        public long getLength() {
            return length;
        }

    }

    /**
     * Upload.Interrupted event is sent when the upload is received, but the
     * reception is interrupted for some reason.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 3.0
     */
    public static class FailedEvent extends FinishedEvent {

        private Exception reason = null;

        /**
         * 
         * @param source
         * @param filename
         * @param MIMEType
         * @param length
         * @param exception
         */
        public FailedEvent(Upload source, String filename, String MIMEType,
                long length, Exception reason) {
            this(source, filename, MIMEType, length);
            this.reason = reason;
        }

        /**
         * 
         * @param source
         * @param filename
         * @param MIMEType
         * @param length
         * @param exception
         */
        public FailedEvent(Upload source, String filename, String MIMEType,
                long length) {
            super(source, filename, MIMEType, length);
        }

        /**
         * Gets the exception that caused the failure.
         * 
         * @return the exception that caused the failure, null if n/a
         */
        public Exception getReason() {
            return reason;
        }

    }

    /**
     * FailedEvent that indicates that an output stream could not be obtained.
     */
    public static class NoOutputStreamEvent extends FailedEvent {

        /**
         * 
         * @param source
         * @param filename
         * @param MIMEType
         * @param length
         */
        public NoOutputStreamEvent(Upload source, String filename,
                String MIMEType, long length) {
            super(source, filename, MIMEType, length);
        }
    }

    /**
     * FailedEvent that indicates that an input stream could not be obtained.
     */
    public static class NoInputStreamEvent extends FailedEvent {

        /**
         * 
         * @param source
         * @param filename
         * @param MIMEType
         * @param length
         */
        public NoInputStreamEvent(Upload source, String filename,
                String MIMEType, long length) {
            super(source, filename, MIMEType, length);
        }

    }

    /**
     * Upload.Success event is sent when the upload is received successfully.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 3.0
     */
    public static class SucceededEvent extends FinishedEvent {

        /**
         * 
         * @param source
         * @param filename
         * @param MIMEType
         * @param length
         */
        public SucceededEvent(Upload source, String filename, String MIMEType,
                long length) {
            super(source, filename, MIMEType, length);
        }

    }

    /**
     * Upload.Started event is sent when the upload is started to received.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 5.0
     */
    public static class StartedEvent extends Component.Event {

        private final String filename;
        private final String type;
        /**
         * Length of the received file.
         */
        private final long length;

        /**
         * 
         * @param source
         * @param filename
         * @param MIMEType
         * @param length
         */
        public StartedEvent(Upload source, String filename, String MIMEType,
                long contentLength) {
            super(source);
            this.filename = filename;
            type = MIMEType;
            length = contentLength;
        }

        /**
         * Uploads where the event occurred.
         * 
         * @return the Source of the event.
         */
        public Upload getUpload() {
            return (Upload) getSource();
        }

        /**
         * Gets the file name.
         * 
         * @return the filename.
         */
        public String getFilename() {
            return filename;
        }

        /**
         * Gets the MIME Type of the file.
         * 
         * @return the MIME type.
         */
        public String getMIMEType() {
            return type;
        }

        /**
         * @return the length of the file that is being uploaded
         */
        public long getContentLength() {
            return length;
        }

    }

    /**
     * Receives the events when the upload starts.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 5.0
     */
    public interface StartedListener extends Serializable {

        /**
         * Upload has started.
         * 
         * @param event
         *            the Upload started event.
         */
        public void uploadStarted(StartedEvent event);
    }

    /**
     * Receives the events when the uploads are ready.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 3.0
     */
    public interface FinishedListener extends Serializable {

        /**
         * Upload has finished.
         * 
         * @param event
         *            the Upload finished event.
         */
        public void uploadFinished(FinishedEvent event);
    }

    /**
     * Receives events when the uploads are finished, but unsuccessful.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 3.0
     */
    public interface FailedListener extends Serializable {

        /**
         * Upload has finished unsuccessfully.
         * 
         * @param event
         *            the Upload failed event.
         */
        public void uploadFailed(FailedEvent event);
    }

    /**
     * Receives events when the uploads are successfully finished.
     * 
     * @author IT Mill Ltd.
     * @version
     * @VERSION@
     * @since 3.0
     */
    public interface SucceededListener extends Serializable {

        /**
         * Upload successfull..
         * 
         * @param event
         *            the Upload successfull event.
         */
        public void uploadSucceeded(SucceededEvent event);
    }

    /**
     * Adds the upload started event listener.
     * 
     * @param listener
     *            the Listener to be added.
     */
    public void addListener(StartedListener listener) {
        addListener(StartedEvent.class, listener, UPLOAD_STARTED_METHOD);
    }

    /**
     * Removes the upload started event listener.
     * 
     * @param listener
     *            the Listener to be removed.
     */
    public void removeListener(StartedListener listener) {
        removeListener(StartedEvent.class, listener, UPLOAD_STARTED_METHOD);
    }

    /**
     * Adds the upload received event listener.
     * 
     * @param listener
     *            the Listener to be added.
     */
    public void addListener(FinishedListener listener) {
        addListener(FinishedEvent.class, listener, UPLOAD_FINISHED_METHOD);
    }

    /**
     * Removes the upload received event listener.
     * 
     * @param listener
     *            the Listener to be removed.
     */
    public void removeListener(FinishedListener listener) {
        removeListener(FinishedEvent.class, listener, UPLOAD_FINISHED_METHOD);
    }

    /**
     * Adds the upload interrupted event listener.
     * 
     * @param listener
     *            the Listener to be added.
     */
    public void addListener(FailedListener listener) {
        addListener(FailedEvent.class, listener, UPLOAD_FAILED_METHOD);
    }

    /**
     * Removes the upload interrupted event listener.
     * 
     * @param listener
     *            the Listener to be removed.
     */
    public void removeListener(FailedListener listener) {
        removeListener(FailedEvent.class, listener, UPLOAD_FAILED_METHOD);
    }

    /**
     * Adds the upload success event listener.
     * 
     * @param listener
     *            the Listener to be added.
     */
    public void addListener(SucceededListener listener) {
        addListener(SucceededEvent.class, listener, UPLOAD_SUCCEEDED_METHOD);
    }

    /**
     * Removes the upload success event listener.
     * 
     * @param listener
     *            the Listener to be removed.
     */
    public void removeListener(SucceededListener listener) {
        removeListener(SucceededEvent.class, listener, UPLOAD_SUCCEEDED_METHOD);
    }

    /**
     * Adds the upload success event listener.
     * 
     * @param listener
     *            the Listener to be added.
     */
    public void addListener(ProgressListener listener) {
        if (progressListeners == null) {
            progressListeners = new LinkedHashSet<ProgressListener>();
        }
        progressListeners.add(listener);
    }

    /**
     * Removes the upload success event listener.
     * 
     * @param listener
     *            the Listener to be removed.
     */
    public void removeListener(ProgressListener listener) {
        if (progressListeners != null) {
            progressListeners.remove(listener);
        }
    }

    /**
     * Emit upload received event.
     * 
     * @param filename
     * @param MIMEType
     * @param length
     */
    protected void fireStarted(String filename, String MIMEType) {
        fireEvent(new Upload.StartedEvent(this, filename, MIMEType,
                contentLength));
    }

    /**
     * Emits the upload failed event.
     * 
     * @param filename
     * @param MIMEType
     * @param length
     */
    protected void fireUploadInterrupted(String filename, String MIMEType,
            long length) {
        fireEvent(new Upload.FailedEvent(this, filename, MIMEType, length));
    }

    protected void fireNoInputStream(String filename, String MIMEType,
            long length) {
        fireEvent(new Upload.NoInputStreamEvent(this, filename, MIMEType,
                length));
    }

    protected void fireNoOutputStream(String filename, String MIMEType,
            long length) {
        fireEvent(new Upload.NoOutputStreamEvent(this, filename, MIMEType,
                length));
    }

    protected void fireUploadInterrupted(String filename, String MIMEType,
            long length, Exception e) {
        fireEvent(new Upload.FailedEvent(this, filename, MIMEType, length, e));
    }

    /**
     * Emits the upload success event.
     * 
     * @param filename
     * @param MIMEType
     * @param length
     * 
     */
    protected void fireUploadSuccess(String filename, String MIMEType,
            long length) {
        fireEvent(new Upload.SucceededEvent(this, filename, MIMEType, length));
    }

    /**
     * Emits the progress event.
     * 
     * @param totalBytes
     *            bytes received so far
     * @param contentLength
     *            actual size of the file being uploaded, if known
     * 
     */
    protected void fireUpdateProgress(long totalBytes, long contentLength) {
        // this is implemented differently than other listeners to maintain
        // backwards compatibility
        if (progressListeners != null) {
            for (Iterator<ProgressListener> it = progressListeners.iterator(); it
                    .hasNext();) {
                ProgressListener l = it.next();
                l.updateProgress(totalBytes, contentLength);
            }
        }
    }

    /**
     * Returns the current receiver.
     * 
     * @return the StreamVariable.
     */
    public Receiver getReceiver() {
        return receiver;
    }

    /**
     * Sets the receiver.
     * 
     * @param receiver
     *            the receiver to set.
     */
    public void setReceiver(Receiver receiver) {
        this.receiver = receiver;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void focus() {
        super.focus();
    }

    /**
     * Gets the Tabulator index of this Focusable component.
     * 
     * @see com.vaadin.ui.Component.Focusable#getTabIndex()
     */
    public int getTabIndex() {
        return tabIndex;
    }

    /**
     * Sets the Tabulator index of this Focusable component.
     * 
     * @see com.vaadin.ui.Component.Focusable#setTabIndex(int)
     */
    public void setTabIndex(int tabIndex) {
        this.tabIndex = tabIndex;
    }

    /**
     * Go into upload state. This is to prevent double uploading on same
     * component.
     * 
     * Warning: this is an internal method used by the framework and should not
     * be used by user of the Upload component. Using it results in the Upload
     * component going in wrong state and not working. It is currently public
     * because it is used by another class.
     */
    public void startUpload() {
        if (isUploading) {
            throw new IllegalStateException("uploading already started");
        }
        isUploading = true;
        nextid++;
    }

    /**
     * Interrupts the upload currently being received. The interruption will be
     * done by the receiving tread so this method will return immediately and
     * the actual interrupt will happen a bit later.
     */
    public void interruptUpload() {
        if (isUploading) {
            interrupted = true;
        }
    }

    /**
     * Go into state where new uploading can begin.
     * 
     * Warning: this is an internal method used by the framework and should not
     * be used by user of the Upload component.
     */
    private void endUpload() {
        isUploading = false;
        contentLength = -1;
        interrupted = false;
        requestRepaint();
    }

    public boolean isUploading() {
        return isUploading;
    }

    /**
     * Gets read bytes of the file currently being uploaded.
     * 
     * @return bytes
     */
    public long getBytesRead() {
        return totalBytes;
    }

    /**
     * Returns size of file currently being uploaded. Value sane only during
     * upload.
     * 
     * @return size in bytes
     */
    public long getUploadSize() {
        return contentLength;
    }

    /**
     * This method is deprecated, use addListener(ProgressListener) instead.
     * 
     * @deprecated Use addListener(ProgressListener) instead.
     * @param progressListener
     */
    @Deprecated
    public void setProgressListener(ProgressListener progressListener) {
        addListener(progressListener);
    }

    /**
     * This method is deprecated.
     * 
     * @deprecated Replaced with addListener/removeListener
     * @return listener
     * 
     */
    @Deprecated
    public ProgressListener getProgressListener() {
        if (progressListeners == null || progressListeners.isEmpty()) {
            return null;
        } else {
            return progressListeners.iterator().next();
        }
    }

    /**
     * ProgressListener receives events to track progress of upload.
     */
    public interface ProgressListener extends Serializable {
        /**
         * Updates progress to listener
         * 
         * @param readBytes
         *            bytes transferred
         * @param contentLength
         *            total size of file currently being uploaded, -1 if unknown
         */
        public void updateProgress(long readBytes, long contentLength);
    }

    /**
     * @return String to be rendered into button that fires uploading
     */
    public String getButtonCaption() {
        return buttonCaption;
    }

    /**
     * In addition to the actual file chooser, upload components have button
     * that starts actual upload progress. This method is used to set text in
     * that button.
     * 
     * <p>
     * <strong>Note</strong> the string given is set as is to the button. HTML
     * formatting is not stripped. Be sure to properly validate your value
     * according to your needs.
     * 
     * @param buttonCaption
     *            text for upload components button.
     */
    public void setButtonCaption(String buttonCaption) {
        this.buttonCaption = buttonCaption;
    }

    /*
     * Handle to terminal via Upload monitors and controls the upload during it
     * is being streamed.
     */
    private com.vaadin.terminal.StreamVariable streamVariable;

    protected com.vaadin.terminal.StreamVariable getStreamVariable() {
        if (streamVariable == null) {
            streamVariable = new com.vaadin.terminal.StreamVariable() {
                private StreamingStartEvent lastStartedEvent;

                public boolean listenProgress() {
                    return (progressListeners != null && !progressListeners
                            .isEmpty());
                }

                public void onProgress(StreamingProgressEvent event) {
                    fireUpdateProgress(event.getBytesReceived(),
                            event.getContentLength());
                }

                public boolean isInterrupted() {
                    return interrupted;
                }

                public OutputStream getOutputStream() {
                    OutputStream receiveUpload = receiver.receiveUpload(
                            lastStartedEvent.getFileName(),
                            lastStartedEvent.getMimeType());
                    lastStartedEvent = null;
                    return receiveUpload;
                }

                public void streamingStarted(StreamingStartEvent event) {
                    startUpload();
                    contentLength = event.getContentLength();
                    fireStarted(event.getFileName(), event.getMimeType());
                    lastStartedEvent = event;
                }

                public void streamingFinished(StreamingEndEvent event) {
                    fireUploadSuccess(event.getFileName(), event.getMimeType(),
                            event.getContentLength());
                    endUpload();
                    requestRepaint();
                }

                public void streamingFailed(StreamingErrorEvent event) {
                    Exception exception = event.getException();
                    if (exception instanceof NoInputStreamException) {
                        fireNoInputStream(event.getFileName(),
                                event.getMimeType(), 0);
                    } else if (exception instanceof NoOutputStreamException) {
                        fireNoOutputStream(event.getFileName(),
                                event.getMimeType(), 0);
                    } else {
                        fireUploadInterrupted(event.getFileName(),
                                event.getMimeType(), 0, exception);
                    }
                    endUpload();
                }
            };
        }
        return streamVariable;
    }

}
