package com.fadutube.app;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.ContentUris;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int BLUE = Color.rgb(23, 105, 224);
    private static final int NAVY = Color.rgb(11, 61, 145);
    private static final int AUDIO_PERMISSION_REQUEST = 41;
    private EditText urlInput;
    private LinearLayout songList;
    private TextView nowPlaying;
    private Button playPause;
    private MediaPlayer player;
    private final List<Track> tracks = new ArrayList<>();
    private int currentTrack = -1;

    private static class Track {
        final long id;
        final String title;
        final String artist;
        final Uri uri;
        Track(long id, String title, String artist, Uri uri) {
            this.id = id; this.title = title; this.artist = artist; this.uri = uri;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(Color.rgb(245, 248, 255));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 248, 255));

        ScrollView scroll = new ScrollView(this);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(24), dp(22), dp(24), dp(20));
        scroll.addView(page);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        TextView brand = new TextView(this);
        brand.setText("♪  fadutube");
        brand.setTextSize(30);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setTextColor(NAVY);
        page.addView(brand);

        TextView tagline = new TextView(this);
        tagline.setText("Tu música, sin vueltas.");
        tagline.setTextSize(16);
        tagline.setTextColor(Color.rgb(91, 106, 130));
        addWithTopMargin(page, tagline, 6);

        TextView downloadTitle = new TextView(this);
        downloadTitle.setText("Descargar audio");
        downloadTitle.setTextSize(21);
        downloadTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        downloadTitle.setTextColor(Color.rgb(25, 39, 62));
        addWithTopMargin(page, downloadTitle, 30);

        TextView hint = new TextView(this);
        hint.setText("Pegá el enlace directo de un archivo de audio que tengas permiso para descargar.");
        hint.setTextSize(14);
        hint.setTextColor(Color.rgb(91, 106, 130));
        addWithTopMargin(page, hint, 8);

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setTextSize(15);
        urlInput.setHint("https://sitio.com/tema.mp3");
        urlInput.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_URI);
        urlInput.setPadding(dp(15), dp(13), dp(15), dp(13));
        urlInput.setBackgroundColor(Color.WHITE);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(-1, dp(54));
        inputParams.topMargin = dp(14);
        page.addView(urlInput, inputParams);

        Button download = makeButton("Descargar audio");
        addWithTopMargin(page, download, 10);
        download.setOnClickListener(v -> startDownload());

        TextView downloadNote = new TextView(this);
        downloadNote.setText("Los archivos se guardan en Música. No se descargan contenidos desde YouTube.");
        downloadNote.setTextSize(12);
        downloadNote.setTextColor(Color.rgb(112, 124, 145));
        downloadNote.setGravity(Gravity.CENTER);
        addWithTopMargin(page, downloadNote, 8);

        TextView libraryTitle = new TextView(this);
        libraryTitle.setText("Mi música");
        libraryTitle.setTextSize(21);
        libraryTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        libraryTitle.setTextColor(Color.rgb(25, 39, 62));
        addWithTopMargin(page, libraryTitle, 28);

        TextView libraryHint = new TextView(this);
        libraryHint.setText("Reproducí los archivos de audio guardados en tu dispositivo.");
        libraryHint.setTextSize(14);
        libraryHint.setTextColor(Color.rgb(91, 106, 130));
        addWithTopMargin(page, libraryHint, 6);

        Button refresh = makeButton("Actualizar biblioteca");
        addWithTopMargin(page, refresh, 10);
        refresh.setOnClickListener(v -> loadLibrary());

        songList = new LinearLayout(this);
        songList.setOrientation(LinearLayout.VERTICAL);
        addWithTopMargin(page, songList, 8);

        LinearLayout playerBar = new LinearLayout(this);
        playerBar.setOrientation(LinearLayout.HORIZONTAL);
        playerBar.setGravity(Gravity.CENTER_VERTICAL);
        playerBar.setPadding(dp(16), dp(8), dp(16), dp(8));
        playerBar.setBackgroundColor(Color.WHITE);
        nowPlaying = new TextView(this);
        nowPlaying.setText("Elegí una canción de tu biblioteca");
        nowPlaying.setTextSize(13);
        nowPlaying.setTextColor(Color.rgb(48, 62, 85));
        nowPlaying.setMaxLines(2);
        playerBar.addView(nowPlaying, new LinearLayout.LayoutParams(0, -2, 1));
        playPause = makeButton("▶");
        playPause.setTextSize(18);
        LinearLayout.LayoutParams playParams = new LinearLayout.LayoutParams(dp(58), dp(48));
        playParams.leftMargin = dp(10);
        playerBar.addView(playPause, playParams);
        playPause.setOnClickListener(v -> togglePlayback());
        root.addView(playerBar, new LinearLayout.LayoutParams(-1, -2));

        setContentView(root);
        loadLibrary();
    }

    private Button makeButton(String text) {
        Button button = new Button(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(15);
        button.setAllCaps(false);
        button.setBackgroundTintList(ColorStateList.valueOf(BLUE));
        return button;
    }

    private void addWithTopMargin(LinearLayout parent, View child, int marginDp) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(marginDp);
        parent.addView(child, params);
    }

    private void loadLibrary() {
        if (!hasAudioPermission()) {
            requestAudioPermission();
            return;
        }
        tracks.clear();
        songList.removeAllViews();
        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST};
        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        try (Cursor cursor = getContentResolver().query(collection, projection, selection, null,
                MediaStore.Audio.Media.TITLE + " COLLATE NOCASE ASC")) {
            if (cursor != null) {
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                int titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                int artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                while (cursor.moveToNext()) {
                    long id = cursor.getLong(idCol);
                    String title = cursor.getString(titleCol);
                    String artist = cursor.getString(artistCol);
                    Uri uri = ContentUris.withAppendedId(collection, id);
                    tracks.add(new Track(id, title == null ? "Audio" : title,
                            artist == null || artist.equals("<unknown>") ? "Artista desconocido" : artist, uri));
                }
            }
        } catch (Exception ignored) { }
        if (tracks.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Todavía no hay canciones en la biblioteca del dispositivo.");
            empty.setTextSize(14);
            empty.setTextColor(Color.rgb(112, 124, 145));
            addWithTopMargin(songList, empty, 8);
            return;
        }
        for (int i = 0; i < tracks.size(); i++) {
            final int index = i;
            Track track = tracks.get(i);
            TextView row = new TextView(this);
            row.setText("♫  " + track.title + "\n     " + track.artist);
            row.setTextSize(15);
            row.setTextColor(Color.rgb(32, 48, 73));
            row.setPadding(dp(14), dp(12), dp(14), dp(12));
            row.setBackgroundColor(Color.WHITE);
            LinearLayout.LayoutParams rowParams = new LinearLayout.LayoutParams(-1, -2);
            rowParams.topMargin = dp(6);
            songList.addView(row, rowParams);
            row.setOnClickListener(v -> playTrack(index));
        }
    }

    private boolean hasAudioPermission() {
        if (Build.VERSION.SDK_INT >= 33) return checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        return Build.VERSION.SDK_INT < 23 || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        String permission = Build.VERSION.SDK_INT >= 33 ? Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (Build.VERSION.SDK_INT >= 23) requestPermissions(new String[]{permission}, AUDIO_PERMISSION_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) loadLibrary();
        else if (requestCode == AUDIO_PERMISSION_REQUEST) Toast.makeText(this, "Dale permiso para mostrar tu música local.", Toast.LENGTH_LONG).show();
    }

    private void playTrack(int index) {
        if (index < 0 || index >= tracks.size()) return;
        releasePlayer();
        currentTrack = index;
        Track track = tracks.get(index);
        nowPlaying.setText(track.title + " — " + track.artist);
        playPause.setText("…");
        try {
            player = new MediaPlayer();
            player.setDataSource(this, track.uri);
            player.setOnPreparedListener(mp -> { mp.start(); playPause.setText("Ⅱ"); });
            player.setOnCompletionListener(mp -> { playPause.setText("▶"); });
            player.setOnErrorListener((mp, what, extra) -> {
                Toast.makeText(this, "No se pudo reproducir ese archivo.", Toast.LENGTH_SHORT).show();
                playPause.setText("▶");
                releasePlayer();
                return true;
            });
            player.prepareAsync();
        } catch (Exception e) {
            Toast.makeText(this, "No se pudo abrir esa canción.", Toast.LENGTH_SHORT).show();
            playPause.setText("▶");
            releasePlayer();
        }
    }

    private void togglePlayback() {
        if (player == null) {
            if (currentTrack >= 0) playTrack(currentTrack);
            else if (!tracks.isEmpty()) playTrack(0);
            else loadLibrary();
            return;
        }
        try {
            if (player.isPlaying()) { player.pause(); playPause.setText("▶"); }
            else { player.start(); playPause.setText("Ⅱ"); }
        } catch (IllegalStateException ignored) { }
    }

    private void startDownload() {
        String raw = urlInput.getText().toString().trim();
        if (raw.isEmpty()) { urlInput.setError("Pegá un enlace primero"); return; }
        try {
            Uri uri = Uri.parse(raw);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || !scheme.equalsIgnoreCase("https") || host == null) throw new IllegalArgumentException();
            String normalizedHost = host.toLowerCase(Locale.ROOT);
            if (normalizedHost.equals("youtube.com") || normalizedHost.endsWith(".youtube.com") ||
                    normalizedHost.equals("youtu.be") || normalizedHost.endsWith(".googlevideo.com")) {
                Toast.makeText(this, "Usá enlaces directos autorizados, no enlaces de YouTube.", Toast.LENGTH_LONG).show();
                return;
            }
            String path = uri.getPath() == null ? "" : uri.getPath().toLowerCase(Locale.ROOT);
            if (!(path.endsWith(".mp3") || path.endsWith(".m4a") || path.endsWith(".aac") || path.endsWith(".ogg") ||
                    path.endsWith(".wav") || path.endsWith(".flac") || path.endsWith(".opus"))) {
                Toast.makeText(this, "El enlace debe apuntar directamente a un archivo de audio.", Toast.LENGTH_LONG).show();
                return;
            }
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(urlInput.getWindowToken(), 0);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle(fileName(path));
            request.setDescription("Descargando con FaduTube");
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, fileName(path));
            DownloadManager manager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            manager.enqueue(request);
            Toast.makeText(this, "Descarga iniciada; actualizá la biblioteca al terminar.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "No pude validar ese enlace. Revisá que sea HTTPS y esté bien escrito.", Toast.LENGTH_LONG).show();
        }
    }

    private String fileName(String path) {
        String name = path.substring(path.lastIndexOf('/') + 1);
        return name.isEmpty() ? "audio-descargado" : name;
    }

    private void releasePlayer() {
        if (player != null) {
            try { player.stop(); } catch (Exception ignored) { }
            player.release();
            player = null;
        }
    }

    @Override protected void onDestroy() { releasePlayer(); super.onDestroy(); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
