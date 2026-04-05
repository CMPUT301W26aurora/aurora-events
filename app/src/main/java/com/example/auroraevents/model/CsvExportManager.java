package com.example.auroraevents.model;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Handles building, writing, and sharing a CSV export of the entrant list.
 *
 * Callers should construct an instance with a {@link Context} and then invoke
 * {@link #export(List, List)} whenever an export is requested. The class is
 * intentionally framework-agnostic (no Fragment/Activity dependency) so it can
 * be unit-tested without Robolectric if needed.
 */
public class CsvExportManager {

    private static final String TAG = "CsvExportManager";

    private final Context context;

    public CsvExportManager(Context context) {
        this.context = context.getApplicationContext();
    }

    // ── Public API ─────────────────────────────────────────────────────────

    /**
     * Exports the entrant list to a CSV file, saves it to the public Downloads
     * folder, and opens the Android Share Sheet.
     *
     * If {@code selectedStatuses} is empty the full {@code masterUiList} is
     * exported (matching the "show all" behaviour of the filter). If no entrant
     * data has loaded yet a toast is shown and the method returns early.
     *
     * @param masterUiList     The complete (unfiltered) list of wrapped users.
     * @param selectedStatuses Status labels currently toggled in the filter dialog.
     *                         An empty list means "export all".
     */
    public void export(List<UserAdapterWrapper> masterUiList, List<String> selectedStatuses) {
        if (masterUiList == null || masterUiList.isEmpty()) {
            Toast.makeText(context, "No entrant data to export.", Toast.LENGTH_SHORT).show();
            return;
        }

        List<UserAdapterWrapper> toExport = applyFilter(masterUiList, selectedStatuses);

        if (toExport.isEmpty()) {
            Toast.makeText(context, "No entrants match the selected filters.", Toast.LENGTH_SHORT).show();
            return;
        }

        String csvContent = buildCsvContent(toExport);
        String fileName   = "entrants_" +
                new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) +
                ".csv";

        try {
            Uri fileUri = writeCsvToDownloads(fileName, csvContent);
            shareCsvFile(fileUri, fileName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to write CSV file", e);
            Toast.makeText(context, "Export failed. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Returns the subset of {@code masterUiList} whose status is contained in
     * {@code selectedStatuses}, or the full list when {@code selectedStatuses} is empty.
     */
    private List<UserAdapterWrapper> applyFilter(List<UserAdapterWrapper> masterUiList,
                                                 List<String> selectedStatuses) {
        if (selectedStatuses.isEmpty()) {
            return new ArrayList<>(masterUiList);
        }

        List<UserAdapterWrapper> filtered = new ArrayList<>();
        for (UserAdapterWrapper wrapper : masterUiList) {
            if (selectedStatuses.contains(wrapper.getStatus())) {
                filtered.add(wrapper);
            }
        }
        return filtered;
    }

    /**
     * Builds the CSV string from the given list of wrapped users.
     *
     * Columns: {@code Name, Email, Phone, Status}. The header row is always
     * included. Values containing commas, quotes, or newlines are wrapped in
     * double-quotes with any internal quotes doubled (RFC 4180).
     *
     * @param wrappers The list of users to serialise.
     * @return A complete CSV string including the header row.
     */
    private String buildCsvContent(List<UserAdapterWrapper> wrappers) {
        StringBuilder sb = new StringBuilder();
        sb.append("Name,Email,Phone,Status\n");

        for (UserAdapterWrapper wrapper : wrappers) {
            User user = wrapper.getUser();
            sb.append(escapeCsvField(user.getName())).append(",");
            sb.append(escapeCsvField(user.getEmail())).append(",");
            String phone = user.getPhoneNumber();
            sb.append(escapeCsvField(phone != null ? phone : "")).append(",");
            sb.append(escapeCsvField(wrapper.getStatus())).append("\n");
        }

        return sb.toString();
    }

    /**
     * Escapes a single CSV field per RFC 4180.
     *
     * If the value contains a comma, double-quote, or newline it is wrapped in
     * double-quotes and any embedded double-quotes are doubled.
     *
     * @param value The raw field value. {@code null} is treated as empty string.
     * @return The escaped field string, ready to embed directly in a CSV row.
     */
    private String escapeCsvField(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Writes the CSV content to the public Downloads folder using {@link MediaStore}
     * and returns a content {@link Uri} pointing to the saved file.
     *
     * @param fileName   Target file name, e.g. {@code "entrants_20260101_120000.csv"}.
     * @param csvContent Full CSV string to write.
     * @return A content {@link Uri} for the saved file.
     * @throws IOException If the file could not be created or written.
     */
    private Uri writeCsvToDownloads(String fileName, String csvContent) throws IOException {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
        values.put(MediaStore.Downloads.MIME_TYPE, "text/csv");
        values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri collection = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            collection = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
        }
        Uri fileUri = context.getContentResolver().insert(collection, values);

        if (fileUri == null) throw new IOException("MediaStore insert returned null URI");

        try (OutputStream out = context.getContentResolver().openOutputStream(fileUri)) {
            if (out == null) throw new IOException("Could not open output stream for URI");
            out.write(csvContent.getBytes(StandardCharsets.UTF_8));
        }

        Log.d(TAG, "CSV saved to Downloads: " + fileName);
        return fileUri;
    }

    /**
     * Opens the Android Share Sheet for the given CSV file URI.
     * Also shows a toast confirming the file was saved to Downloads.
     *
     * @param fileUri  Content URI of the saved CSV file.
     * @param fileName File name shown in the confirmation toast.
     */
    private void shareCsvFile(Uri fileUri, String fileName) {
        Toast.makeText(context,
                "Saved to Downloads: " + fileName, Toast.LENGTH_LONG).show();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/csv");
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(shareIntent, "Share entrant list");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooser);
    }
}