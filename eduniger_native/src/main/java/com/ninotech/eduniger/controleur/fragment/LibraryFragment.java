package com.ninotech.eduniger.controleur.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.ninotech.eduniger.R;
import com.ninotech.eduniger.controleur.activity.AccountActivity;
import com.ninotech.eduniger.controleur.activity.BookActivity;
import com.ninotech.eduniger.controleur.activity.ContactActivity;
import com.ninotech.eduniger.controleur.activity.ContainerActivity;
import com.ninotech.eduniger.controleur.activity.FingerPrintActivity;
import com.ninotech.eduniger.controleur.activity.InfosActivity;
import com.ninotech.eduniger.controleur.activity.MainActivity;
import com.ninotech.eduniger.controleur.activity.NotificationActivity;
import com.ninotech.eduniger.controleur.activity.PdfBoxViewerActivity;
import com.ninotech.eduniger.controleur.activity.PlaybackListActivity;
import com.ninotech.eduniger.controleur.activity.SuggestionActivity;
import com.ninotech.eduniger.controleur.activity.ThemeActivity;
import com.ninotech.eduniger.controleur.animation.RoundedTransformation;
import com.ninotech.eduniger.model.data.PlaybackRepository;
import com.ninotech.eduniger.model.data.Server;
import com.ninotech.eduniger.model.net.ApiClient;
import com.ninotech.eduniger.model.net.LoandSyncTask;
import com.ninotech.eduniger.model.table.AudioTable;
import com.ninotech.eduniger.model.table.ElectronicTable;
import com.ninotech.eduniger.model.table.LoandTable;
import com.ninotech.eduniger.model.table.Session;
import com.ninotech.eduniger.model.table.UserTable;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Onglet « Compte » (version 2, maquette octobre 2026).
 *
 * Regroupe sur un seul écran : profil, statistiques de la bibliothèque locale, dernier
 * livre téléchargé (lecture hors ligne), paramètres du compte et déconnexion. Toutes les
 * actions réutilisent les écrans existants (ContainerActivity, AccountActivity,
 * ThemeActivity, InfosActivity...) : aucune logique métier n'est dupliquée ici.
 *
 * Les données viennent uniquement des tables SQLite locales (aucun appel réseau à
 * l'affichage), sauf la synchronisation des emprunts (LoandSyncTask) et l'envoi de la
 * photo de profil, inchangés par rapport à la version précédente.
 */
public class LibraryFragment extends Fragment {

    private static final String TAG = "LibraryFragment";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_IMAGE_GALLERY = 2;

    /** Préférences où LoginActivity mémorise la date de création du compte (cf. saveMemberSince). */
    public static final String PREFS_ACCOUNT = "account_prefs";
    public static final String KEY_MEMBER_SINCE = "member_since";

    // Identifiants « id » attendus par ContainerActivity (cf. ElectronicAdapter).
    private static final int CONTAINER_DOWNLOADS = 1;
    private static final int CONTAINER_AUDIO = 2;
    private static final int CONTAINER_LOANS = 3;
    private static final int CONTAINER_CATEGORIES = 4;
    private static final int CONTAINER_AUTHORS = 5;

    private static final String GUIDE_URL =
            "https://www.youtube.com/playlist?list=PL9OgjL2isuO_lWGCR9rem2qKig6m8CPzK";

    private Session mSession;
    private UserTable mUserTable;
    private File mImageFile;

    private View mRoot;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ImageView mAvatarImageView;
    private TextView mUsernameTextView;
    private TextView mEmailTextView;
    private TextView mRoleTextView;
    private TextView mMemberSinceTextView;
    private TextView mCategoriesShortcut;
    private TextView mAuthorsShortcut;
    private View mRecentCard;
    private TextView mRecentEmptyTextView;
    private ImageView mRecentCoverImageView;
    private TextView mRecentTitleTextView;
    private TextView mRecentMetaTextView;

    /** Dernier livre PDF téléchargé et complet, ou null s'il n'y en a pas. */
    private RecentBook mRecentBook;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRoot = inflater.inflate(R.layout.fragment_library, container, false);

        mSession = new Session(getContext());
        mUserTable = new UserTable(getContext());

        initializeViews(mRoot);
        setupStaticActions(mRoot);
        setupSettings(mRoot);
        setupSwipeRefresh();
        loadData();

        // Réconcilie le cache local des emprunts (LoandTable) avec le serveur :
        // sinon le compteur « Empruntés » ne reflète que ce qu'une notification push
        // avait déjà synchronisé, jamais les emprunts rendus depuis.
        syncLoans();

        return mRoot;
    }

    /**
     * Le fragment est créé une fois puis masqué/affiché par MainActivity (hide/show) :
     * on rafraîchit les compteurs à chaque retour sur l'onglet, car un téléchargement ou
     * un emprunt a pu avoir lieu entre-temps sur un autre écran.
     */
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && mRoot != null) {
            loadData();
        }
    }

    // ==================== Vues ====================

    private void initializeViews(View view) {
        mSwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_library);
        mAvatarImageView = view.findViewById(R.id.image_view_fragment_library_avatar);
        mUsernameTextView = view.findViewById(R.id.text_view_fragment_library_username);
        mEmailTextView = view.findViewById(R.id.text_view_fragment_library_email);
        mRoleTextView = view.findViewById(R.id.text_view_fragment_library_role);
        mMemberSinceTextView = view.findViewById(R.id.text_view_fragment_library_member_since);
        mCategoriesShortcut = view.findViewById(R.id.text_view_fragment_library_shortcut_categories);
        mAuthorsShortcut = view.findViewById(R.id.text_view_fragment_library_shortcut_authors);
        mRecentCard = view.findViewById(R.id.relative_layout_fragment_library_recent);
        mRecentEmptyTextView = view.findViewById(R.id.text_view_fragment_library_recent_empty);
        mRecentCoverImageView = view.findViewById(R.id.image_view_fragment_library_recent_cover);
        mRecentTitleTextView = view.findViewById(R.id.text_view_fragment_library_recent_title);
        mRecentMetaTextView = view.findViewById(R.id.text_view_fragment_library_recent_meta);

        // Les lignes de paramètres ont un fond « ripple » rectangulaire : on découpe la
        // carte selon ses coins arrondis pour que l'effet ne déborde pas.
        View settingsCard = view.findViewById(R.id.linear_layout_fragment_library_settings);
        settingsCard.setClipToOutline(true);

        TextView versionTextView = view.findViewById(R.id.text_view_fragment_library_version);
        versionTextView.setText(getString(R.string.account_version, getString(R.string.app_version)));
    }

    private void setupStaticActions(View view) {
        view.findViewById(R.id.image_button_fragment_library_edit_profile)
                .setOnClickListener(v -> open(AccountActivity.class));

        mAvatarImageView.setOnClickListener(v -> openGallery());
        view.findViewById(R.id.image_button_fragment_library_change_photo)
                .setOnClickListener(v -> openGallery());

        // Statistiques : chaque carte ouvre la liste correspondante (mêmes écrans que
        // l'ancienne liste « bibliothèque » de cet onglet).
        bindStat(view, R.id.include_fragment_library_stat_downloads,
                R.drawable.ic_account_download, R.string.account_stat_downloads,
                v -> openContainer(CONTAINER_DOWNLOADS));
        bindStat(view, R.id.include_fragment_library_stat_audio,
                R.drawable.ic_account_headphones, R.string.account_stat_audio,
                v -> openContainer(CONTAINER_AUDIO));
        bindStat(view, R.id.include_fragment_library_stat_loans,
                R.drawable.ic_account_book, R.string.account_stat_loans,
                v -> openContainer(CONTAINER_LOANS));
        bindStat(view, R.id.include_fragment_library_stat_favorites,
                R.drawable.ic_account_heart, R.string.account_stat_favorites,
                v -> openPlaybackList(PlaybackListActivity.MODE_FAVORITES));

        mCategoriesShortcut.setOnClickListener(v -> openContainer(CONTAINER_CATEGORIES));
        mAuthorsShortcut.setOnClickListener(v -> openContainer(CONTAINER_AUTHORS));
        view.findViewById(R.id.text_view_fragment_library_shortcut_history)
                .setOnClickListener(v -> openPlaybackList(PlaybackListActivity.MODE_HISTORY));

        view.findViewById(R.id.text_view_fragment_library_see_all)
                .setOnClickListener(v -> openContainer(CONTAINER_DOWNLOADS));
        view.findViewById(R.id.linear_layout_fragment_library_read_offline)
                .setOnClickListener(v -> readRecentOffline());
        view.findViewById(R.id.image_button_fragment_library_recent_more)
                .setOnClickListener(this::showRecentMenu);
        mRecentCard.setOnClickListener(v -> readRecentOffline());

        view.findViewById(R.id.linear_layout_fragment_library_logout)
                .setOnClickListener(v -> confirmLogout());
    }

    private void bindStat(View root, int includeId, int iconRes, int labelRes,
                          View.OnClickListener listener) {
        View stat = root.findViewById(includeId);
        ImageView icon = stat.findViewById(R.id.image_view_item_account_stat_icon);
        TextView label = stat.findViewById(R.id.text_view_item_account_stat_label);
        icon.setImageResource(iconRes);
        label.setText(labelRes);
        stat.setOnClickListener(listener);
    }

    private void setStatValue(int includeId, int value) {
        View stat = mRoot.findViewById(includeId);
        TextView valueView = stat.findViewById(R.id.text_view_item_account_stat_value);
        TextView label = stat.findViewById(R.id.text_view_item_account_stat_label);
        valueView.setText(String.valueOf(value));
        stat.setContentDescription(value + " " + label.getText());
    }

    // ==================== Paramètres du compte ====================

    private void setupSettings(View view) {
        bindSettingRow(view, R.id.include_fragment_library_setting_personal,
                R.drawable.ic_account_user, R.string.account_setting_personal,
                getString(R.string.account_setting_personal_desc),
                v -> open(AccountActivity.class));
        bindSettingRow(view, R.id.include_fragment_library_setting_security,
                R.drawable.ic_account_lock, R.string.account_setting_security,
                getString(R.string.account_setting_security_desc),
                v -> showSecurityChoices());
        bindSettingRow(view, R.id.include_fragment_library_setting_notifications,
                R.drawable.ic_account_bell, R.string.account_setting_notifications,
                getString(R.string.account_setting_notifications_desc),
                v -> open(NotificationActivity.class));
        bindSettingRow(view, R.id.include_fragment_library_setting_appearance,
                R.drawable.ic_account_theme, R.string.account_setting_appearance,
                getString(R.string.account_setting_appearance_desc),
                v -> open(ThemeActivity.class));
        // Description du stockage calculée dans loadData() (taille réelle des fichiers).
        bindSettingRow(view, R.id.include_fragment_library_setting_storage,
                R.drawable.ic_account_storage, R.string.account_setting_storage,
                null,
                v -> openContainer(CONTAINER_DOWNLOADS));
        bindSettingRow(view, R.id.include_fragment_library_setting_help,
                R.drawable.ic_account_help, R.string.account_setting_help,
                getString(R.string.account_setting_help_desc),
                v -> showHelpChoices());
        bindSettingRow(view, R.id.include_fragment_library_setting_about,
                R.drawable.ic_account_info, R.string.account_setting_about,
                getString(R.string.account_setting_about_desc, getString(R.string.app_version)),
                v -> open(InfosActivity.class));
    }

    private void bindSettingRow(View root, int includeId, int iconRes, int titleRes,
                                @Nullable String description, View.OnClickListener listener) {
        View row = root.findViewById(includeId);
        ImageView icon = row.findViewById(R.id.image_view_item_account_setting_icon);
        TextView title = row.findViewById(R.id.text_view_item_account_setting_title);
        TextView desc = row.findViewById(R.id.text_view_item_account_setting_description);
        icon.setImageResource(iconRes);
        title.setText(titleRes);
        if (description != null) {
            desc.setText(description);
        }
        row.setOnClickListener(listener);
    }

    private void setSettingDescription(int includeId, String description) {
        View row = mRoot.findViewById(includeId);
        TextView desc = row.findViewById(R.id.text_view_item_account_setting_description);
        desc.setText(description);
    }

    private void showSecurityChoices() {
        String[] items = {
                getString(R.string.account_security_change_password),
                getString(R.string.account_security_fingerprint)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.account_setting_security)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        // AccountActivity propose déjà « Modifier le mot de passe »
                        // (UpdateSyn.php, authentifié par le jeton).
                        open(AccountActivity.class);
                    } else {
                        open(FingerPrintActivity.class);
                    }
                })
                .show();
    }

    private void showHelpChoices() {
        String[] items = {
                getString(R.string.account_help_guide),
                getString(R.string.account_help_contact),
                getString(R.string.account_help_report)
        };
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.account_setting_help)
                .setItems(items, (dialog, which) -> {
                    if (which == 0) {
                        Intent guide = new Intent(Intent.ACTION_VIEW, Uri.parse(GUIDE_URL));
                        if (guide.resolveActivity(requireContext().getPackageManager()) != null) {
                            startActivity(guide);
                        }
                    } else if (which == 1) {
                        open(ContactActivity.class);
                    } else {
                        open(SuggestionActivity.class);
                    }
                })
                .show();
    }

    private void confirmLogout() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.account_logout_confirm_title)
                .setMessage(R.string.account_logout_confirm_message)
                .setNegativeButton(R.string.account_cancel, null)
                .setPositiveButton(R.string.account_logout_confirm_yes, (dialog, which) -> {
                    // Même déconnexion que le menu de la barre d'outils (révocation côté
                    // serveur + nettoyage de la session locale).
                    Activity activity = getActivity();
                    if (activity instanceof MainActivity) {
                        ((MainActivity) activity).logout();
                    }
                })
                .show();
    }

    // ==================== Données ====================

    private void setupSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.purple_200);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            loadData();
            syncLoans();
            mSwipeRefreshLayout.setRefreshing(false);
        });
    }

    private void syncLoans() {
        new LoandSyncTask(getContext(), () -> {
            if (isAdded()) loadData();
        }).execute();
    }

    private void loadData() {
        Context context = getContext();
        if (context == null) return;

        String idNumber = mSession.getIdNumber();
        loadProfile(idNumber);

        ElectronicTable electronicTable = new ElectronicTable(context);
        AudioTable audioTable = new AudioTable(context);
        LoandTable loandTable = new LoandTable(context);
        PlaybackRepository playbackRepository = new PlaybackRepository(context);

        int electronicCount = electronicTable.getNbrElectronic(idNumber);
        int audioCount = audioTable.getNbrAudio(idNumber);

        setStatValue(R.id.include_fragment_library_stat_downloads, electronicCount);
        setStatValue(R.id.include_fragment_library_stat_audio, audioCount);
        setStatValue(R.id.include_fragment_library_stat_loans, loandTable.getNbrLoand(idNumber));
        setStatValue(R.id.include_fragment_library_stat_favorites, playbackRepository.getNbrFavorites());

        int categories = electronicTable.getNbrCategory(idNumber);
        int authors = electronicTable.getNbrAuthor(idNumber);
        mCategoriesShortcut.setText(getString(R.string.account_shortcut_categories, categories));
        mAuthorsShortcut.setText(getString(R.string.account_shortcut_authors, authors));

        long usedBytes = loadRecentAndStorage(electronicTable, audioTable, idNumber);
        setSettingDescription(R.id.include_fragment_library_setting_storage,
                getString(R.string.account_setting_storage_desc,
                        Formatter.formatShortFileSize(context, usedBytes),
                        electronicCount + audioCount));
    }

    private void loadProfile(String idNumber) {
        Cursor userCursor = null;
        try {
            userCursor = mUserTable.getData(idNumber);
            if (userCursor == null || !userCursor.moveToFirst()) return;

            // Colonnes UserTable : 0 idUser, 1 nom, 2 prénom, 3 e-mail, 6 photo, 7 isAdmin.
            mUsernameTextView.setText(userCursor.getString(2) + " " + userCursor.getString(1));
            mEmailTextView.setText(userCursor.getString(3));

            String isAdmin = userCursor.getString(7);
            boolean admin = "true".equalsIgnoreCase(isAdmin) || "1".equals(isAdmin);
            mRoleTextView.setText(admin ? R.string.account_role_admin : R.string.account_role_user);

            byte[] photoBytes = userCursor.getBlob(6);
            if (photoBytes != null) {
                Glide.with(this)
                        .load(photoBytes)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mAvatarImageView);
            } else {
                Glide.with(this)
                        .load(R.drawable.user)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mAvatarImageView);
            }
        } catch (Exception e) {
            Log.e(TAG, "Lecture du profil local impossible", e);
        } finally {
            if (userCursor != null) userCursor.close();
        }

        String memberSince = formatMemberSince(requireContext()
                .getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE)
                .getString(KEY_MEMBER_SINCE, null));
        if (memberSince != null) {
            mMemberSinceTextView.setText(getString(R.string.account_member_since, memberSince));
            mMemberSinceTextView.setVisibility(View.VISIBLE);
        } else {
            mMemberSinceTextView.setVisibility(View.GONE);
        }
    }

    /**
     * Remplit la carte « Téléchargements récents » avec le dernier PDF complet et calcule
     * l'espace occupé par les livres hors ligne (PDF + audio).
     *
     * @return nombre d'octets occupés sur l'appareil.
     */
    private long loadRecentAndStorage(ElectronicTable electronicTable, AudioTable audioTable,
                                      String idNumber) {
        long usedBytes = 0;
        mRecentBook = null;

        // Colonnes Electronic : 2 idBook, 5 couverture, 6 fichier PDF, 8 titre,
        // 11 dateDownload, 12 statut, 13 taille. Triées par date décroissante.
        Cursor cursor = null;
        try {
            cursor = electronicTable.getData(idNumber);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String status = cursor.getString(12);
                    boolean completed = status == null
                            || ElectronicTable.STATUS_COMPLETED.equals(status);
                    if (!completed) continue;

                    String pdfPath = cursor.getString(6);
                    long size = cursor.getLong(13);
                    if (size <= 0 && pdfPath != null) {
                        size = new File(pdfPath).length();
                    }
                    usedBytes += Math.max(size, 0);

                    if (mRecentBook == null) {
                        mRecentBook = new RecentBook(cursor.getString(2), cursor.getString(8),
                                cursor.getString(5), pdfPath, cursor.getString(11), size);
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lecture des téléchargements impossible", e);
        } finally {
            if (cursor != null) cursor.close();
        }

        // Colonne Audio 6 : chemin du fichier audio local.
        Cursor audioCursor = null;
        try {
            audioCursor = audioTable.getData(idNumber);
            if (audioCursor != null && audioCursor.moveToFirst()) {
                do {
                    String audioPath = audioCursor.getString(6);
                    if (audioPath != null) usedBytes += new File(audioPath).length();
                } while (audioCursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e(TAG, "Lecture des livres audio impossible", e);
        } finally {
            if (audioCursor != null) audioCursor.close();
        }

        bindRecentBook();
        return usedBytes;
    }

    private void bindRecentBook() {
        if (mRecentBook == null) {
            mRecentCard.setVisibility(View.GONE);
            mRecentEmptyTextView.setVisibility(View.VISIBLE);
            return;
        }
        mRecentEmptyTextView.setVisibility(View.GONE);
        mRecentCard.setVisibility(View.VISIBLE);

        mRecentTitleTextView.setText(mRecentBook.title);
        String size = Formatter.formatShortFileSize(requireContext(), Math.max(mRecentBook.sizeBytes, 0));
        String date = formatDownloadDate(mRecentBook.downloadDate);
        mRecentMetaTextView.setText(date != null
                ? getString(R.string.account_recent_meta, size, date)
                : size);

        if (mRecentBook.coverPath != null) {
            Picasso.get()
                    .load(new File(mRecentBook.coverPath))
                    .placeholder(R.drawable.img_default_book)
                    .error(R.drawable.img_default_book)
                    .transform(new RoundedTransformation(16, 0))
                    .resize(198, 282)
                    .centerCrop()
                    .into(mRecentCoverImageView);
        } else {
            mRecentCoverImageView.setImageResource(R.drawable.img_default_book);
        }
    }

    private void readRecentOffline() {
        if (mRecentBook == null) return;
        if (mRecentBook.pdfPath == null || !new File(mRecentBook.pdfPath).exists()) {
            Toast.makeText(getContext(), R.string.account_file_missing, Toast.LENGTH_LONG).show();
            openBook(mRecentBook.idBook);
            return;
        }
        Intent intent = new Intent(getContext(), PdfBoxViewerActivity.class);
        intent.putExtra("PDF_PATH", mRecentBook.pdfPath);
        intent.putExtra("PDF_TITLE", mRecentBook.title);
        startActivity(intent);
    }

    private void showRecentMenu(View anchor) {
        if (mRecentBook == null) return;
        PopupMenu menu = new PopupMenu(requireContext(), anchor);
        menu.getMenu().add(0, 1, 0, R.string.account_menu_open_book);
        menu.getMenu().add(0, 2, 1, R.string.account_menu_downloads);
        menu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == 1) {
                openBook(mRecentBook.idBook);
            } else {
                openContainer(CONTAINER_DOWNLOADS);
            }
            return true;
        });
        menu.show();
    }

    // ==================== Navigation ====================

    private void open(Class<? extends Activity> target) {
        startActivity(new Intent(getContext(), target));
    }

    private void openContainer(int id) {
        Intent intent = new Intent(getContext(), ContainerActivity.class);
        intent.putExtra("id", id);
        startActivity(intent);
    }

    private void openPlaybackList(String mode) {
        Intent intent = new Intent(getContext(), PlaybackListActivity.class);
        intent.putExtra(PlaybackListActivity.EXTRA_MODE, mode);
        startActivity(intent);
    }

    private void openBook(String idBook) {
        Intent intent = new Intent(getContext(), BookActivity.class);
        intent.putExtra("intent_adapter_book_id", idBook);
        startActivity(intent);
    }

    /**
     * Mémorise la date de création du compte renvoyée par l'API (champ created_at de
     * l'utilisateur) pour afficher « Membre depuis … » sans appel réseau.
     * Appelée après une connexion ou une inscription réussie.
     */
    public static void saveMemberSince(Context context, @Nullable JSONObject user) {
        if (context == null || user == null) return;
        String createdAt = user.optString("created_at", "");
        if (createdAt.isEmpty() || "null".equals(createdAt)) return;
        context.getSharedPreferences(PREFS_ACCOUNT, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_MEMBER_SINCE, createdAt)
                .apply();
    }

    // ==================== Dates ====================

    /** "2026-04-15T17:16:42.000000Z" (Laravel) ou "2026-04-15 17:16:42" -> "avril 2026". */
    @Nullable
    static String formatMemberSince(@Nullable String isoDate) {
        if (isoDate == null || isoDate.length() < 7) return null;
        try {
            Date date = new SimpleDateFormat("yyyy-MM", Locale.US).parse(isoDate.substring(0, 7));
            return date == null ? null
                    : new SimpleDateFormat("MMMM yyyy", Locale.FRENCH).format(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /** "2026-10-05 14:02:11" (ElectronicTable.dateDownload) -> "5 oct. 2026". */
    @Nullable
    private static String formatDownloadDate(@Nullable String rawDate) {
        if (rawDate == null || rawDate.length() < 10) return null;
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(rawDate.substring(0, 10));
            return date == null ? null
                    : new SimpleDateFormat("d MMM yyyy", Locale.FRENCH).format(date);
        } catch (ParseException e) {
            return null;
        }
    }

    /** Dernier téléchargement affiché dans la carte « Téléchargements récents ». */
    private static final class RecentBook {
        final String idBook;
        final String title;
        final String coverPath;
        final String pdfPath;
        final String downloadDate;
        final long sizeBytes;

        RecentBook(String idBook, String title, String coverPath, String pdfPath,
                   String downloadDate, long sizeBytes) {
            this.idBook = idBook;
            this.title = title;
            this.coverPath = coverPath;
            this.pdfPath = pdfPath;
            this.downloadDate = downloadDate;
            this.sizeBytes = sizeBytes;
        }
    }

    // ==================== Galerie / Caméra (inchangé) ====================

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_GALLERY
                && resultCode == Activity.RESULT_OK
                && data != null) {
            try {
                Bitmap imageBitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), data.getData());
                mImageFile = convertBitmapToFile(imageBitmap);
                byte[] compressedImageBytes = compressImage(imageBitmap);

                Glide.with(this)
                        .load(compressedImageBytes)
                        .apply(RequestOptions.circleCropTransform())
                        .into(mAvatarImageView);

                mUserTable.setPhoto(compressedImageBytes);

                if (mImageFile != null) {
                    uploadImage(mImageFile);
                }
            } catch (Exception e) {
                Log.e(TAG, "Chargement de l'image impossible", e);
                Toast.makeText(getContext(),
                        "Erreur lors du chargement de l'image." + e.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_IMAGE_CAPTURE
                && resultCode == Activity.RESULT_OK
                && data != null && data.getExtras() != null) {
            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            byte[] compressedImageBytes = compressImage(imageBitmap);

            Glide.with(this)
                    .load(compressedImageBytes)
                    .apply(RequestOptions.circleCropTransform())
                    .into(mAvatarImageView);
        }
    }

    private byte[] compressImage(Bitmap imageBitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, REQUEST_IMAGE_GALLERY);
    }

    private void uploadImage(File imageFile) {
        String serverUrl = Server.getUrlApi(getContext()) + "uploadImg.php";
        OkHttpClient client = ApiClient.getInstance(requireContext());

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), imageFile))
                .build();

        Request request = new Request.Builder()
                .url(serverUrl)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Échec de l'upload de la photo", e);
                Activity activity = getActivity();
                if (activity == null) return;
                activity.runOnUiThread(() ->
                        Toast.makeText(activity,
                                "Échec de l'upload : " + e.getMessage(),
                                Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                final boolean success = response.isSuccessful();
                final String detail = response.toString();
                response.close();
                Activity activity = getActivity();
                if (activity == null) return;
                activity.runOnUiThread(() ->
                        Toast.makeText(activity,
                                success ? "Image téléversée avec succès"
                                        : "Erreur lors de l'upload " + detail,
                                Toast.LENGTH_SHORT).show());
            }
        });
    }

    private File convertBitmapToFile(Bitmap bitmap) throws IOException {
        File file = new File(requireActivity().getCacheDir(), mSession.getIdNumber() + ".png");
        file.createNewFile();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
        byte[] bitmapData = bos.toByteArray();

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(bitmapData);
            fos.flush();
        }
        return file;
    }
}
