package bazzar.data;

public enum ProductName {
    FISH(0), BOAR(1), SALT(2);

    public final int value;

    ProductName(int value) {
        this.value = value;
    }
}
