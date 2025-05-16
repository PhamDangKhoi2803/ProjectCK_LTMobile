package ute.nhom27.android.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.widget.SearchView;
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
    private SearchView searchView;
    private FriendMessagesFragment friendMessagesFragment;
    private GroupMessagesFragment groupMessagesFragment;

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
        searchView = view.findViewById(R.id.search_view);

        setupViewPager();
        setupTabLayout();
        setupSearchView();
    }

    private void setupViewPager() {
        friendMessagesFragment = new FriendMessagesFragment();
        groupMessagesFragment = new GroupMessagesFragment();

        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPagerAdapter.addFragment(friendMessagesFragment, "Bạn bè");
        viewPagerAdapter.addFragment(groupMessagesFragment, "Nhóm");
        viewPager.setAdapter(viewPagerAdapter);
    }

    private void setupTabLayout() {
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(viewPagerAdapter.getTitle(position))
        ).attach();
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterMessages(newText);
                return true;
            }
        });
    }

    private void filterMessages(String query) {
        if (friendMessagesFragment != null && friendMessagesFragment.getAdapter() != null) {
            friendMessagesFragment.getAdapter().getFilter().filter(query);
        }

        if (groupMessagesFragment != null && groupMessagesFragment.getAdapter() != null) {
            groupMessagesFragment.getAdapter().getFilter().filter(query);
        }
    }
}