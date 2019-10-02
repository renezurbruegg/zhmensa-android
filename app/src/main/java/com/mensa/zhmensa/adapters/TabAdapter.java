package com.mensa.zhmensa.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.component.fragments.MensaTab;
import com.mensa.zhmensa.models.Mensa;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter that manages tabs in tab-layout
 */
public class TabAdapter extends FragmentStatePagerAdapter {


    @SuppressWarnings("HardCodedStringLiteral")
    private static final String MENSA_ID = "mensaId";
    private final List<MensaTab.MenuTabContentFragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private final List<Pair<Mensa.MenuCategory, String>> posToLabels = new ArrayList<>();
    private final List<Mensa.MenuCategory> catList = new ArrayList<>();
    public TabAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    public void addFragment(MensaTab.MenuTabContentFragment fragment, Mensa.MenuCategory category, String mensaId, Context ctx) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(Helper.getLabelForMealType(category, ctx));
        posToLabels.add(new Pair(category, mensaId));
        catList.add(category);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
/*
        for (MensaTab.MenuTabContentFragment frag : mFragmentList) {
                Log.d("TabAdapter.ndsc", "MensaWeekdayTabFragment " + frag.getId() + " mensa " + frag.getArguments().getString(MENSA_ID) );
                frag.notifyDatasetChanged();
        }*/
    }

    /**
     * Returns a custom layout (Day and Date) for a mensa menu tab
     * @param position
     * @param inflater
     * @return
     */
    @NonNull
    public View getTabView(int position, LayoutInflater inflater, Context ctx) {
        @SuppressLint("InflateParams")
        View view = inflater.inflate(R.layout.custom_mealtype_tab, null);
        updateCustomView(view, position, ctx);
        return view;
    }

    /*
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }
*/
    @Override
    public int
    getCount() {
        return mFragmentList.size();
    }

    public void updateCustomView(View view, int position, Context ctx) {
        if(position >= posToLabels.size() || view == null)
            return;
        Mensa.MenuCategory mealType = posToLabels.get(position).first;
        String mensaId = posToLabels.get(position).second;

        Mensa m = MensaManager.getMensaForId(mensaId);
        String label = null;

        if(m != null) {
            label = m.getMealTypeTime(mealType);
        }
        // View Binding
        ((TextView) view.findViewById(R.id.tab_mealtype_name)).setText(Helper.getLabelForMealType(mealType, ctx));

        if(label != null) {
            ((TextView) view.findViewById(R.id.tab_mealtime_label)).setText(label);
            view.findViewById(R.id.tab_mealtime_label).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.tab_mealtime_label).setVisibility(View.GONE);
        }
    }

    public boolean hasFragmentForCategory(Mensa.MenuCategory category) {
        return catList.contains(category);
    }
}
