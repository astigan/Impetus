package net.astigan.impetus.entities;

/**
 * Model representing a menu item in the DrawerLayout
 */
public class DrawerItem {

    private final int icon;
    private final int stringId;

    public DrawerItem(int icon, int stringId) {
        this.icon = icon;
        this.stringId = stringId;
    }

    public int getIcon() {
        return icon;
    }

    public int getStringId() {
        return stringId;
    }
}
