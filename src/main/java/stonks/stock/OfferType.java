package stonks.stock;

public enum OfferType {
    
    BUY_OFFER("Buy Offer", "§a"),
    SELL_OFFER("Sell Offer", "§c");

    public final String offerUIName;
    public final String preferedColor;
    
    private OfferType(String offerUIName, String preferedColor) {
        this.offerUIName = offerUIName;
        this.preferedColor = preferedColor;
    }
    
}
