package Client;

public enum Commands {
    PRESS_MOUSE_PRIMARY(-1),
    RELEASE_MOUSE_PRIMARY(-2),
    PRESS_MOUSE_SECONDARY(-3),
    RELEASE_MOUSE_SECONDARY(-4),
    PRESS_KEY(-5),
    RELEASE_KEY(-6),
    MOVE_MOUSE(-7),
    DRAG_MOUSE(-8),

    SCROLL_MOUSE(-9);


    private int abbrev;

    Commands(int abbrev) {
        this.abbrev = abbrev;
    }

    public int getAbbrev() {
        return abbrev;
    }
}