package ute.nhom27.android.view;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;

import android.widget.Button;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.zegocloud.uikit.prebuilt.call.ZegoUIKitPrebuiltCallService;
import com.zegocloud.uikit.prebuilt.call.invite.ZegoUIKitPrebuiltCallInvitationConfig;

import ute.nhom27.android.BaseActivity;
import ute.nhom27.android.R;
import ute.nhom27.android.SettingsActivity;
import ute.nhom27.android.utils.CloudinaryUtils;
import ute.nhom27.android.utils.SharedPrefManager;
import ute.nhom27.android.view.fragment.AIChatFragment;
import ute.nhom27.android.view.fragment.CallHistoryFragment;
import ute.nhom27.android.view.fragment.FriendContainerFragment;
import ute.nhom27.android.view.fragment.FriendListFragment;
import ute.nhom27.android.view.fragment.MessageListFragment;
import ute.nhom27.android.view.fragment.SettingsFragment;
import ute.nhom27.android.view.fragment.ThemeChange;


public class MainActivity extends BaseActivity implements ThemeChange{

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        CloudinaryUtils.init(this);

        if (savedInstanceState == null) {
            loadFragment(new MessageListFragment());
        }

        SharedPrefManager prefManager = new SharedPrefManager(this);

        long appID = 1041973249;   // yourAppID
        String appSign = "ee8dace42b86b5d0be495cf3a6c8d5f0d78023701fa225a58f56bf00a628fb70";  // yourAppSign
        String userID = prefManager.getUser().getUsername(); // yourUserID, userID should only contain numbers, English characters, and '_'.

        ZegoUIKitPrebuiltCallInvitationConfig callInvitationConfig = new ZegoUIKitPrebuiltCallInvitationConfig();

        ZegoUIKitPrebuiltCallService.init(getApplication(), appID, appSign, userID, userID,callInvitationConfig);


        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }
            if (itemId == R.id.nav_messages) {
                selectedFragment = new MessageListFragment();
            } else if (itemId == R.id.nav_contacts) {
                selectedFragment = new FriendContainerFragment();
            } else if (itemId == R.id.nav_more) {
                selectedFragment = new CallHistoryFragment();
            }



            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                if (itemId == R.id.nav_settings) {
                    onThemeChange();
                }
                return true;
            }
            if (itemId == R.id.nav_diary) {
                // Chuyển đến AIChatFragment khi click vào menu Nhật ký
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new AIChatFragment())
                        .addToBackStack(null)
                        .commit();
                return true;
            }
            return false;
        });

    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    @Override
    public void onThemeChange() {
        applyTheme();
    }
}