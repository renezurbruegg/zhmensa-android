package com.mensa.zhmensa.models.menu;

import android.content.Context;

import androidx.annotation.Nullable;

/**
 * Implementation for a menu in the favorite tab.
 * Get name returns not only it's name but also appends the mensa name
 */
public class FavoriteMenu extends Menu {
    private final String mensaName;
    private final IMenu menu;
    private final String mensaId;

    public FavoriteMenu(String mensaName, IMenu menu, String mensaId) {
        super(menu.getId(), menu.getName(), menu.getDescription(), menu.getPrices(), "", menu.getMeta());
        this.menu = menu;
        this.mensaId = mensaId;
        this.mensaName = mensaName;
    }

    @Override
    public boolean isVegi() {
        return menu.isVegi();
    }

    @Nullable
    @Override
    public String getAllergene(Context ctx) {
        return menu.getAllergene(ctx);
    }

    @Override
    public boolean hasAllergene() {
        return menu.hasAllergene();
    }

    @Override
    public void setAllergene(@Nullable String allergene) {
        if(menu instanceof Menu)
            ((Menu)menu).setAllergene(allergene);
    }

    public String getName() {
        return mensaName + ": " + super.getName();
    }

    public String getMensaId() {
        return mensaId;
    }
}
