package com.mensa.zhmensa.navigation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mensa.zhmensa.models.categories.MensaCategory;
import com.mensa.zhmensa.services.Helper;

public class NavigationMenuHeader implements  Comparable<NavigationMenuHeader>{


    final MensaCategory category;
    private final boolean hasChildren;
    private final Integer position;

    NavigationMenuHeader(MensaCategory category, boolean hasChildren, int position) {
        this.category = category;
        this.hasChildren = hasChildren;
        this.position = position;
    }

    @NonNull
    public String getDisplayName() {
        return category.getDisplayName();
    }

    public boolean isStandaloneItem(){
        return !hasChildren;
    }

    @SuppressWarnings("HardCodedStringLiteral")
    @NonNull
    public String toString() {
        return "Cat: " + (category == null ? "null" : category.getDisplayName())  + " - hasChildren: " + hasChildren;
    }

    @Override
    public int compareTo(@Nullable NavigationMenuHeader o) {
        if(o == null)
            return -1;
       return Helper.firstNonNull(position, -1).compareTo(o.position);
    }
}
