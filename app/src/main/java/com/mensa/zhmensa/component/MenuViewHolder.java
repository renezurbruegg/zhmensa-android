package com.mensa.zhmensa.component;

import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.mensa.zhmensa.R;
import com.mensa.zhmensa.models.menu.FavoriteMenu;
import com.mensa.zhmensa.models.menu.IMenu;
import com.mensa.zhmensa.services.Helper;
import com.mensa.zhmensa.services.MensaManager;

import java.util.Collections;

import kotlin.jvm.functions.Function2;

/**
 * Simple implementation for a Mensa Menu view.
 * This class is used in the MenuCardAdapter that calls the bind function, to load the values of a menu into a card.
 */
public class MenuViewHolder extends RecyclerView.ViewHolder {

    @SuppressWarnings("HardCodedStringLiteral")
    private static final String DUMMY = "dummy";

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    /**
     * Binds the menu to a view.
     * @param viewHolder to view to bind to menu to
     * @param menu to menu
     * @param mensaId
     */
    public static void bind(final MenuViewHolder viewHolder, final IMenu menu, @NonNull final Context ctx, final String mensaId) {
        ((TextView) viewHolder.itemView.findViewById(R.id.card_title)).setText(menu.getName());
        ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setText(menu.getPrices());
        ((TextView) viewHolder.itemView.findViewById(R.id.card_content)).setText(menu.getDescription());
        ((TextView) viewHolder.itemView.findViewById(R.id.allergene)).setText(menu.getAllergene(ctx));

        final ImageButton favBtn = viewHolder.itemView.findViewById(R.id.bookmark_button);


        final ImageButton shareBtn = viewHolder.itemView.findViewById(R.id.share_button);
        final LinearLayout showMoreLayout = viewHolder.itemView.findViewById(R.id.showmore_layout);
        final ImageButton showMoreBtn = viewHolder.itemView.findViewById(R.id.showmore_button);
        final ImageButton hideMenuBtn = viewHolder.itemView.findViewById(R.id.hide_button);
        final TextView vegiView =  viewHolder.itemView.findViewById(R.id.vegi_badge);

        vegiView.setVisibility(menu.isVegi() ? View.VISIBLE : View.INVISIBLE);

        if(Helper.firstNonNull(menu.getMeta(),"").equals(DUMMY)) {
            ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setGravity(View.TEXT_ALIGNMENT_CENTER);
            shareBtn.setVisibility(View.INVISIBLE);
            showMoreBtn.setVisibility(View.INVISIBLE);
            favBtn.setVisibility(View.INVISIBLE);
            hideMenuBtn.setVisibility(View.INVISIBLE);

            return;
        } else {
            ((TextView) viewHolder.itemView.findViewById(R.id.price_text)).setGravity(View.TEXT_ALIGNMENT_TEXT_START);
            shareBtn.setVisibility(View.VISIBLE);
            showMoreBtn.setVisibility(View.VISIBLE);
            favBtn.setVisibility(View.VISIBLE);
            hideMenuBtn.setVisibility( (menu instanceof FavoriteMenu) ? View.INVISIBLE : View.VISIBLE);
        }


        if(!menu.hasAllergene()) {
            showMoreBtn.setVisibility(View.INVISIBLE);
        }


        favBtn.setImageResource( menu.isFavorite() ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp);

        favBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MensaManager.toggleMenuFav(menu, ctx);
                //menu.setFavorite(!menu.isFavorite());
                favBtn.setImageResource( menu.isFavorite() ? R.drawable.ic_favorite_black_24dp : R.drawable.ic_favorite_border_black_24dp);
            }
        });


        hideMenuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MensaManager.hideMenu(menu, mensaId, ctx);
                Snackbar.make(view, String.format(ctx.getString(R.string.menu_deleted) , menu.getName()), Snackbar.LENGTH_LONG)
                        .setAction(ctx.getString(R.string.undo), new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                MensaManager.showMenu(menu, mensaId, ctx);
                            }
                        })
                        .show();
            }
        });
        final PopupMenu dropDownMenu = new PopupMenu(ctx, shareBtn);

        final Menu ddMenu = dropDownMenu.getMenu();

        dropDownMenu.getMenuInflater().inflate(R.menu.drop_down_share, ddMenu);
        dropDownMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.action_share_to_device:
                        Intent i = new Intent(android.content.Intent.ACTION_SEND);
                        i.setType("text/plain");
                        i.putExtra(android.content.Intent.EXTRA_TEXT, menu.getSharableString());
                        ctx.startActivity(Intent.createChooser(i, ctx.getString(R.string.share)));
                        return true;

                    case R.id.action_add_to_poll:

                        String mMensaId = mensaId;
                        if(menu instanceof FavoriteMenu){
                            mMensaId = ((FavoriteMenu) menu).getMensaId();
                        }


                        MensaManager.getPollManagger(ctx).showAddMenusToPollDialog(Collections.singletonList(menu), mMensaId, ctx, new Function2<String, Boolean, Void>() {
                            @Override
                            public Void invoke(String s, Boolean error) {
                                if(error){
                                    Snackbar.make(viewHolder.itemView, s, Snackbar.LENGTH_SHORT).show();
                                    return null;
                                }
                              Snackbar.make(viewHolder.itemView, ctx.getString(R.string.added_menu), Snackbar.LENGTH_SHORT).show();
                              return null;
                            }
                        });
                        // item ID 1 was clicked
                        return true;
                }
                return false;
            }
        });


        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropDownMenu.show();
               /* Intent i = new Intent(android.content.Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(android.content.Intent.EXTRA_TEXT, menu.getSharableString());
                ctx.startActivity(Intent.createChooser(i, "Share"));*/
            }
        });

        showMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean hide = (showMoreLayout.getVisibility() == View.VISIBLE);
                showMoreLayout.setVisibility(hide ? View.GONE : View.VISIBLE);
                showMoreBtn.setImageResource( hide ? R.drawable.ic_keyboard_arrow_down_black_24dp : R.drawable.ic_keyboard_arrow_up_black_24dp);
            }
        });


    }
}
