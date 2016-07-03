import javax.swing.*;
import java.awt.*;
import java.util.Random;


public class MainClass
{
    /**
     * Main method of this application
     */
    public static void main(final String[] arg)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            public void run()
            {
                //Sets up the frame
                JFrame frame = new WindowManager();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);

                frame.setLocationRelativeTo(null);
            }
        });
    }
}