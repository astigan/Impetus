package net.astigan.impetus.ui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import net.astigan.impetus.R;
import net.astigan.impetus.entities.DrawerItem;

/**
 * A listview for the DrawerLayout menu that displays an icon and a string.
 */
public class DrawerList extends ListView {

    public DrawerList(Context context) {
        super(context);
        initialise();
    }

    public DrawerList(Context context, AttributeSet attrs) {
        super (context, attrs);
        initialise();
    }

    public DrawerList(Context context, AttributeSet attrs, int defStyle) {
        super (context, attrs, defStyle);
        initialise();
    }

    private void initialise() {

        DrawerItem[] values = new DrawerItem[] {
                new DrawerItem(R.drawable.ic_info_outline_black_36dp, R.string.about),
                new DrawerItem(R.drawable.ic_star_outline_black_36dp, R.string.rate),
                new DrawerItem(R.drawable.ic_share_black_36dp, R.string.share),
                new DrawerItem(R.drawable.ic_remove_red_eye_black_36dp, R.string.privacy),
                new DrawerItem(R.drawable.ic_toc_black_36dp, R.string.license)
        };

        setAdapter(new DrawerLayoutAdapter(getContext(), values));
    }

    private class DrawerLayoutAdapter extends ArrayAdapter<DrawerItem> {

        public DrawerLayoutAdapter(Context context, DrawerItem[] values) {
            super(context, R.layout.drawer_item, R.id.drawer_item_text, values);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            DrawerItem currentItem = getItem(position);

            TextView tv = (TextView) view.findViewById(R.id.drawer_item_text);
            tv.setText(currentItem.getStringId());

            ImageView imageView = (ImageView) view.findViewById(R.id.drawer_item_icon);
            imageView.setImageResource(currentItem.getIcon());

            return view;
        }
    }

}
