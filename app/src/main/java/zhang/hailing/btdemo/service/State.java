package zhang.hailing.btdemo.service;

public enum State {
    STATE_IDLE("idle"),
    STATE_LISTENING("waiting for connecting"),
    STATE_CONNECTED("connected"),
    STATE_CONNECTING("connecting"),
    STATE_CONNECTION_FAILED("connection failed"),;

    private String description;

    State(String description) {
        this.description = description;
    }


    @Override
    public String toString() {
        return description;
    }
}
