import javax.swing.SwingWorker;
import javax.swing.JButton;
import javax.swing.JProgressBar;
import java.util.List;

public class CookieScanWorker extends SwingWorker<List<String>, String> {

    private final CookieScanEngine scanEngine;
    private final CookieTableModel activeCookieModel;
    private final JButton scanButton;
    private final JProgressBar progressBar;

    public CookieScanWorker(CookieScanEngine scanEngine, CookieTableModel activeCookieModel, JButton scanButton, JProgressBar progressBar) {
        this.scanEngine = scanEngine;
        this.activeCookieModel = activeCookieModel;
        this.scanButton = scanButton;
        this.progressBar = progressBar;
    }

    @Override
    protected List<String> doInBackground() {
        publish("Scanning...");
        return scanEngine.getCookiesFromHistory();
    }

    @Override
    protected void process(List<String> chunks) {
        progressBar.setIndeterminate(true);
        progressBar.setString(chunks.get(chunks.size() - 1));
    }

    @Override
    protected void done() {
        try {
            List<String> cookies = get();
            for (String cookie : cookies) {
                activeCookieModel.addCookie(cookie);
            }
            activeCookieModel.cookieList = cookies;
            progressBar.setIndeterminate(false);
            progressBar.setString("Scan Complete");
            scanButton.setEnabled(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
