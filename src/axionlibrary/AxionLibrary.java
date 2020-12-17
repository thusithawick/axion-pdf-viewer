/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axionlibrary;

import com.alee.laf.WebLookAndFeel;

/**
 *
 * @author thusitha
 */
public class AxionLibrary {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        WebLookAndFeel.install();
        if (args.length>0) {
            new AxionPDFViewer(args[0]).setVisible(true);
        }else{
            new AxionPDFViewer().setVisible(true);
        }
        //new AxionPDFViewer("E:\\Library\\Encyclopedia\\The Knowledge Encyclopedia - 2013.pdf").setVisible(true);
    }
    
}
