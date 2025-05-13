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

import ute.nhom27.android.BaseActivity;
import ute.nhom27.android.R;
import ute.nhom27.android.SettingsActivity;
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

        if (savedInstanceState == null) {
            loadFragment(new MessageListFragment());
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();

//			if (itemId == R.id.nav_settings) {
//                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//                startActivity(intent);
//                return true;
//            }
            if (itemId == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
            }
            if (itemId == R.id.nav_messages) {
                selectedFragment = new MessageListFragment();
            } else if (itemId == R.id.nav_contacts) {
                selectedFragment = new FriendContainerFragment();
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
                if (itemId == R.id.nav_settings) {
                    onThemeChange();
                }
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