package ute.nhom27.android.view.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import ute.nhom27.android.R;
import ute.nhom27.android.adapter.ViewPagerAdapter;

public class MessageListFragment extends Fragment {

    private ViewPager2 viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter viewPagerAdapter;

    public MessageListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        setupViewPager();
        setupTabLayout();
    }

    private void setupViewPager() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerAdapter.addFragment(new FriendMessagesFragment(), "Bạn bè");
        viewPagerAdapter.addFragment(new GroupMessagesFragment(), "Nhóm");
        viewPager.setAdapter(viewPagerAdapter);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager,
            (tab, position) -> tab.setText(viewPagerAdapter.getTitle(position))
        ).attach();
    }
}
