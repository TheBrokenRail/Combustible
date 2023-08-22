package com.thebrokenrail.combustible.util;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedActivity;
import com.thebrokenrail.combustible.api.Connection;
import com.thebrokenrail.combustible.api.method.CommunityView;
import com.thebrokenrail.combustible.api.method.FollowCommunity;
import com.thebrokenrail.combustible.api.method.SubscribedType;
import com.thebrokenrail.combustible.util.markdown.Markdown;
import com.thebrokenrail.combustible.widget.PossiblyOutlinedButton;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple dialog for showing information.
 */
public class InfoDialog {
    /**
     * Information dialog manager.
     */
    public static class Manager {
        private final AppCompatActivity context;
        private final Connection connection;

        private final Map<String, InfoDialog> dialogs = new HashMap<>();

        public Manager(AppCompatActivity context, Connection connection) {
            this.context = context;
            this.connection = connection;
        }

        /**
         * Create dialog.
         * @param key A unique key
         * @return The new dialog
         */
        public InfoDialog create(String key) {
            key = "info_" + key;
            InfoDialog dialog = new InfoDialog(context, connection, key);
            dialogs.put(key, dialog);
            return dialog;
        }
    }

    public static class Fragment extends BottomSheetDialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
            return new BottomSheetDialog(requireContext(), getTheme()) {
                @Override
                protected void onCreate(Bundle savedInstanceState) {
                    super.onCreate(savedInstanceState);
                    // No Status On Dark Mode
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                        Window window = getWindow();
                        boolean isDarkMode = Util.isDarkMode(requireContext());
                        if (window != null && isDarkMode) {
                            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                            window.setStatusBarColor(Color.TRANSPARENT);
                        }
                    }
                }
            };
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Check
            InfoDialog info = getInfo();
            assert info != null;
            if (!info.isSetup()) {
                dismissAllowingStateLoss();
                return null;
            }

            // Create Layout
            inflater = LayoutInflater.from(requireActivity());
            @SuppressLint("InflateParams") View root = inflater.inflate(R.layout.dialog_info, null);
            PossiblyOutlinedButton subscribeButton = root.findViewById(R.id.community_subscribe);
            TextView infoView = root.findViewById(R.id.community_info);

            // Subscribe
            boolean canSubscribe = info.connection.hasToken() && info.community != null;
            subscribeButton.setEnabled(canSubscribe);
            if (info.community != null) {
                if (canSubscribe) {
                    subscribeButton.setButtonOnClickListener(v -> {
                        // Send
                        FollowCommunity method = new FollowCommunity();
                        method.follow = info.community.subscribed == SubscribedType.NotSubscribed;
                        method.community_id = info.community.community.id;
                        info.connection.send(method, communityResponse -> {
                            info.community.subscribed = communityResponse.community_view.subscribed;
                            info.updateSubscribed.accept(info.community.subscribed);
                            updateSubscriptionText(subscribeButton);
                        }, () -> Util.unknownError(requireContext()));
                    });
                }
                updateSubscriptionText(subscribeButton);
            } else {
                subscribeButton.setVisibility(View.GONE);
            }

            // Info
            if (info.text != null) {
                Markdown markdown = new Markdown(requireContext());
                markdown.set(infoView, info.text);
            } else {
                infoView.setVisibility(View.GONE);

                // Remove Bottom Margin From Subscribe Button
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) subscribeButton.getLayoutParams();
                layoutParams.setMargins(0, 0, 0, 0);
                subscribeButton.requestLayout();
            }

            // Edge-To-Edge
            ViewCompat.setOnApplyWindowInsetsListener(root, (v, windowInsets) -> {
                Insets insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
                root.findViewById(R.id.community_dialog_root).setPadding(0, 0, 0, insets.bottom);
                return windowInsets;
            });

            // Return
            return root;
        }

        private void updateSubscriptionText(PossiblyOutlinedButton button) {
            InfoDialog info = getInfo();
            button.setText(getResources().getStringArray(R.array.subscribed_type)[info.community.subscribed.ordinal()]);
            button.setOutlined(info.community.subscribed != SubscribedType.NotSubscribed);
        }

        private InfoDialog getInfo() {
            FragmentActivity activity = requireActivity();
            InfoDialog info = ((FeedActivity) activity).infoManager.dialogs.get(getTag());
            assert info != null;
            return info;
        }
    }

    private final AppCompatActivity context;
    private final Connection connection;
    private final String key;

    private String text = "";
    private CommunityView community = null;
    private Consumer<SubscribedType> updateSubscribed = null;

    private InfoDialog(AppCompatActivity context, Connection connection, String key) {
        this.context = context;
        this.connection = connection;
        this.key = key;
    }

    /**
     * Set dialog information from a {@link CommunityView}.
     * @param community The community
     * @param updateSubscribed Callback that is executed when the subscription status is updated
     */
    public void set(CommunityView community, Consumer<SubscribedType> updateSubscribed) {
        setInternal(community.community.description);
        this.community = community;
        this.updateSubscribed = updateSubscribed;
    }

    /**
     * Set dialog information from raw text;
     * @param text The text
     */
    public void set(String text) {
        setInternal(text);
        this.community = null;
        this.updateSubscribed = null;
    }

    private void setInternal(String text) {
        if (text != null && text.trim().length() == 0) {
            text = null;
        }
        this.text = text;
    }

    /**
     * Check if this dialog is setup.
     * @return True if it is setup, false otherwise
     */
    public boolean isSetup() {
        return text != null;
    }

    /**
     * Show dialog.
     */
    public void show() {
        new Fragment().show(context.getSupportFragmentManager(), key);
    }
}
