package li.klass.fhem.data.provider;

public interface FHEMDataProvider {
    String xmllist();
    void executeCommand(String command);
}
