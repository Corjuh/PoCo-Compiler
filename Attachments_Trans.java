import javax.swing.JOptionPane;

public class Attachments_Trans {
	public static int ShowDialog(String s) {
	int option = JOptionPane.showConfirmDialog(null, s, "Warning", 0);
	return option;
    }
}