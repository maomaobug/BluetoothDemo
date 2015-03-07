package zhang.hailing.btdemo.service;

public class BaseEvent {
    public Object what;

    public BaseEvent(Object what) {
        this.what = what;
    }

    @Override
    public String toString() {
        return what.toString();
    }
}
