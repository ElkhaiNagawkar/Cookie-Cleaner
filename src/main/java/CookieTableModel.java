import javax.swing.table.DefaultTableModel;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class CookieTableModel extends DefaultTableModel {

    private final Set<String> cookieNames;
    public List<String> cookieList = new CopyOnWriteArrayList<>();

    public CookieTableModel(String[] columns, int rows, Set<String> sharedSet){
        super(columns, rows);
        this.cookieNames = sharedSet;
    }

    public void addCookie(String cookieName) {
        if (cookieNames.add(cookieName)) {
            addRow(new Object[]{cookieName});
        }
    }

    public void moveCookie(String cookieName) {
        addRow(new Object[]{cookieName});
        cookieList.add(cookieName);
    }

    public void removeCookie(int row){
        String cookieName = getValueAt(row, 0).toString();
        cookieList.remove(cookieName);
        removeRow(row);
    }

    public void removeCookieWithString(CookieTableModel activeCookie, String cookieName){
        for(int i = 0; i < activeCookie.getRowCount(); i++){
            if(activeCookie.getValueAt(i,0).toString().equals(cookieName)){
                activeCookie.removeCookie(i);
                return;
            }
        }
    }
}
