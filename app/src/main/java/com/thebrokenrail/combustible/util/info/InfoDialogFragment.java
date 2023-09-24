package com.thebrokenrail.combustible.util.info;

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
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.FragmentActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.thebrokenrail.combustible.R;
import com.thebrokenrail.combustible.activity.feed.FeedActivity;
import com.thebrokenrail.combustible.api.method.FollowCommunity;
import com.thebrokenrail.combustible.api.method.SubscribedType;
import com.thebrokenrail.combustible.util.EdgeToEdge;
import com.thebrokenrail.combustible.util.Util;
import com.thebrokenrail.combustible.util.markdown.Markdown;
import com.thebrokenrail.combustible.widget.PossiblyOutlinedButton;

/**
 * Fragment for information dialog.
 */
public class InfoDialogFragment extends BottomSheetDialogFragment {
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

        // Return
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        InfoDialog info = getInfo();
        assert info != null;
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
                        updateSubscriptionText(subscribeButton);
                    }, () -> Util.unknownError(requireContext()));
                });
            }
            updateSubscriptionText(subscribeButton);
        } else {
            subscribeButton.setVisibility(View.GONE);
        }

        // Info
        Markdown markdown = new Markdown(requireContext());
        markdown.set(infoView, info.text);

        // Edge-To-Edge
        NestedScrollView scroll = root.findViewById(R.id.community_dialog_root);
        EdgeToEdge.setupScroll(scroll);
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
