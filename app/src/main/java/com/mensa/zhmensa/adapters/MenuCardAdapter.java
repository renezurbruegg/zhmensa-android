package com.mensa.zhmensa.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SortedList;

import com.mensa.zhmensa.R;
import com.mensa.zhmensa.component.MenuViewHolder;
import com.mensa.zhmensa.filters.MenuFilter;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

import java.util.List;


/**
 * Adapter class that holds all menu views for a Mensa.
 * Is used inside the recycler view to display different menus.
 */
public class MenuCardAdapter extends RecyclerView.Adapter<MenuViewHolder> {


    private Context context;
    private final SortedList<IMenu> menus;
    private final String mensaId;
    private final MenuFilter filter;

    public MenuCardAdapter(@Nullable List<IMenu> menus, String mensaId, MenuFilter menuFilter, Context ctx) {
        this.filter = menuFilter;

        this.menus = new SortedList<>(IMenu.class, new ComparableSortedListAdapterCallback(this));

        if (menus != null) {
            for(IMenu menu : menus) {
                if(menuFilter == null || menuFilter.apply(menu))
                    this.menus.add(menu);
            }
        }

        if (this.menus.size() == 0) {
            this.menus.addAll(MensaManager.getPlaceholderForEmptyMenu(mensaId, ctx));
        }

        //noinspection HardCodedStringLiteral
        this.mensaId = Helper.firstNonNull(mensaId, "unknown Mensa");
    }

    public void setItems(@NonNull List<IMenu> items, Context ctx){
        this.menus.clear();

        for (IMenu item: items) {
            if(filter == null || filter.apply(item))
                this.menus.add(item);
        }

        if(this.menus.size() == 0){
            this.menus.addAll(MensaManager.getPlaceholderForEmptyMenu(mensaId, ctx));
        }
    }


    @NonNull
    @Override
    public MenuViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_card, parent, false);
        return new MenuViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MenuViewHolder holder, int position) {
        IMenu menu = menus.get(position);
        MenuViewHolder.bind(holder, menu, context, mensaId);
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }


    public SortedList<IMenu> getItems() {
        return menus;
    }
}
