package edu.lmu.cs.networking;

/**
 * Created by habar on 04.12.2016.
 */
public enum Block {

    BRICK(true), IMMORTAL(false), E_BRICK(true);


    boolean destructive;

    Block(boolean destructive) {
        this.destructive = destructive;
    }

    public boolean isDestructive() {
        return destructive;
    }
}
