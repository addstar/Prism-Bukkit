package me.botsko.prism.actions;

public class PlayerAction extends GenericAction {

    private String extraInfo;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getNiceName() {
        if (extraInfo != null && !extraInfo.isEmpty()) {
            return switch (getAction().getName()) {
                case "player-join" -> "from " + extraInfo;
                case "xp-pickup" -> extraInfo + " xp";
                case "bucket-fill" -> "a " + extraInfo + " bucket";
                default -> extraInfo;
            };
        }

        return "";
    }

    @Override
    public boolean hasExtraData() {
        return extraInfo != null;
    }

    @Override
    public String serialize() {
        return extraInfo;
    }

    @Override
    public void deserialize(String data) {
        extraInfo = data;
    }
}