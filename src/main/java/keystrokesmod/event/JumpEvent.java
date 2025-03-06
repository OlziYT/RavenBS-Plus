package keystrokesmod.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class JumpEvent extends Event {
    private float motionY, yaw;
    private boolean applySprint;

    public JumpEvent(float motionY, float yaw, boolean applySprint) {
        this.motionY = motionY;
        this.yaw = yaw;
        this.applySprint = applySprint;
    }

    public float getMotionY() {
        return motionY;
    }

    public void setMotionY(float motionY) {
        this.motionY = motionY;
    }

    public float getYaw() {
        return yaw;
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public boolean applySprint() {
        return applySprint;
    }

    public void setSprint(boolean applySprint) {
        this.applySprint = applySprint;
    }
}
