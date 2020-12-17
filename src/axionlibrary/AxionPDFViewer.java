/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axionlibrary;

import APV.AxionPDFFile;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

/**
 *
 * @author thusitha
 */

public class AxionPDFViewer extends javax.swing.JFrame {
    
    /**
     * Creates new form AxionPDFViewer
     */
    private final int MAX_PAGES_LOAD=20;
    private final int MIN_PAGES_LOAD=15;
    private final int MIN_PAGES_TODEL=30;
    private int size = 0;
    private final int SLEEP=2000;
    public boolean runnable = true;
    
    
    Image img1=null;
    Image img2=null;
    int currentPage=0;
    int previousPage=-1;
    boolean SlideSlipOn=false;
    PageQueue queue=null;
    AxionPDFFile file=null;
    private int pp=-1;
    String path;
    
    
    
    public AxionPDFViewer(String path) {
        initComponents();
        this.path=path;
        file =new AxionPDFFile(path);
        setTitle(file.getTitle());
        size = file.getLength();
        System.out.println("size "+size);
        queue  = new PageQueue();
        backLoader.setPriority(Thread.NORM_PRIORITY-1);
        backLoader.setName("backLoader");
        backLoader.start();
        jumpPage(0);
        setLocationRelativeTo(null);
        webLabel1.setText("/ "+size);
        webPanel3.setVisible(false);
    }
    
    public AxionPDFViewer() {
        initComponents();
        setTitle("Axion PDF Viewer");
        setLocationRelativeTo(null);
        webLabel1.setText("/ 0");
        webPanel3.setVisible(false);
    }
    
    
    Thread backLoader = new Thread(new Runnable() {
        @Override
        public void run() {
            loop1:
                while(runnable){
                    try {

    //                    System.out.println("resumed");
                        long startTime = System.currentTimeMillis();
                        int tmpPage=currentPage;
                        int il = 0;
                        int ir = 0;
                        if (currentPage != previousPage || queue.getMin() < currentPage-MIN_PAGES_LOAD || queue.getMax() > currentPage+MIN_PAGES_LOAD) {//if currentPage changed
        //                    System.out.println("loading "+(tmpPage-MIN_PAGES_LOAD)+" to "+(tmpPage+MIN_PAGES_LOAD));
                            for (il = tmpPage, ir = tmpPage; il > tmpPage-MIN_PAGES_LOAD || ir < tmpPage+MIN_PAGES_LOAD; il--, ir++) {
        //                        if(tmpPage!=currentPage){
        //                            continue loop1;
        //                        }
        //                        System.out.println(" exists(ir):"+queue.exists(ir)+" ir>=0:"+(ir>=0)+" ir<="+size+":"+(ir<=size));
        //                        System.out.println(" exists(il):"+queue.exists(il)+" il>=0:"+(il>=0)+" il<="+size+":"+(il<=size));
                                if (!queue.exists(ir) && ir>=0 && ir<size) {
                                    try {
                                        queue.put(ir, file.getPageImage(ir));
                                    } catch (Exception e) {
                                    }
        //                            try {
        //                                backLoader.wait(SLEEP);
        //                            } catch (Exception e) {
        //                            }
                                }
                                if (!queue.exists(il) && il>=0 && il<size) {
                                    try {
                                        queue.put(il, file.getPageImage(il));
                                    } catch (Exception e) {
                                    }
        //                            try {
        //                                backLoader.wait(SLEEP);
        //                            } catch (Exception e) {
        //                            }
                                }
                            }
        //                    System.out.println("loaded "+il+" to "+ir+" queue size:"+queue.length());
                        }

                        if (queue.length()>MIN_PAGES_TODEL) {
    //                        System.out.println("removing <"+il+" and >"+ir );
                            for (int i = 0; i < queue.length(); i++) {
                                if (i<il || i>ir) {

                                    try {
                                        queue.remove(i);
                                    } catch (Exception e) {
                                    }
                                }
        //                        if(tmpPage!=currentPage){
        //                            continue loop1;
        //                        }
                            }
                        }

                        //System.out.println((startTime-System.currentTimeMillis())/1000+" s");

                        try {
    //                        System.out.println("waiting");
                            Thread.sleep(SLEEP);
                        } catch (Exception e1) {
                        }

                    } catch (OutOfMemoryError eE) {
                        openDocument(path);
                    }
                }//while
        }
    });
    
    public void resumeLoader(){
        try {
            backLoader.notify();
        } catch (Exception e) {
        }
    }
    
    public void pauseLoader(){
        try {
            if (backLoader.getState().equals(Thread.State.RUNNABLE)) {
                backLoader.wait();
            }
        } catch (Exception e) {
        }
    }
    
    private void updatePages() {
        try {
            webImage1.setIcon(new ImageIcon(img1.getScaledInstance(webImage1.getWidth(), webImage1.getHeight(), Image.SCALE_SMOOTH)));
            webImage2.setIcon(new ImageIcon(img2.getScaledInstance(webImage2.getWidth(), webImage2.getHeight(), Image.SCALE_SMOOTH)));
        } catch (Exception e) {
        }
    }
    
    public void selectNextPage(){
        currentPage+=2;
        jumpPage(currentPage);
    }
    
    public void selectPreviousPage(){
        currentPage-=2;
        jumpPage(currentPage);
    }
    
    boolean waiting=false;
    public void jumpPage(int pageIndex){
        if (!waiting && queue!=null) {
            waiting=true;
            pauseLoader();
            img1=null;
            img2=null;

            int tempPage=currentPage;
            
            if (pageIndex>=size) {
                tempPage=size-1;
            }else if(pageIndex<0){
                tempPage=0;
            }else{
                tempPage=pageIndex;
            }

            if (SlideSlipOn && tempPage%2==0) {//cover oththe
                try {

                    img1=pushNGet(tempPage-1);
                    img2=pushNGet(tempPage);
                    updatePages();
                } catch (Exception e) {
                }
            }else if (SlideSlipOn && tempPage%2!=0) {//cover iratte
                try {
                    img1=pushNGet(tempPage);
                    img2=pushNGet(tempPage+1);
                    updatePages();
                } catch (Exception e) {
                }
            }else if (!SlideSlipOn && tempPage%2==0) {//not cover oththe
                try {
                    img1=pushNGet(tempPage);
                    img2=pushNGet(tempPage+1);
                    updatePages();
                } catch (Exception e) {
                }
            }else if (!SlideSlipOn && tempPage%2!=0) {//not cover iratte
                try {
                    img1=pushNGet(tempPage-1);
                    img2=pushNGet(tempPage);
                    updatePages();
                } catch (Exception e) {
                }
            }
    //        System.out.println(queue.getMin()+":"+currentPage+":"+queue.getMax());
            resumeLoader();
            waiting=false;
        }else{
            try {
                wait(1000);
            } catch (Exception e) {
            }
            waiting=true;
        }
        webTextField1.setText(String.valueOf(currentPage));
        System.out.println("queue.size:"+queue.length());
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                stack.resumePageLoading();
//            }
//        }).start();
    }
    
    public Image pushNGet(int pageIndex){
        if (queue.exists(pageIndex)) {
            return queue.get(pageIndex);
        }
        Image tmpImage=file.getPageImage(pageIndex);
        queue.put(pageIndex, tmpImage);
        return tmpImage;
    }
    
    public void mouseWheelRotate(MouseWheelEvent evt){
        if (evt.getScrollAmount()>=3) {
            if (evt.getWheelRotation()<0) {
                    selectPreviousPage();
            }else if(evt.getWheelRotation()>0){
                    selectNextPage();
            }
        }
    }
    
    public void mouseWheelZoom(MouseWheelEvent evt){
        if (evt.getScrollAmount()>=3) {
            if (evt.getWheelRotation()<0) {
                    zoomIn();
            }else if(evt.getWheelRotation()>0){
                    zoomOut();
            }
        }
    }
    
    public void keyPressing(KeyEvent evt){
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_LEFT:
                selectPreviousPage();
                break;
            case KeyEvent.VK_RIGHT:
                selectNextPage();
                break;
            case KeyEvent.VK_DOWN:
                selectNextPage();
                break;
            case KeyEvent.VK_UP:
                selectPreviousPage();
                break;
            case KeyEvent.VK_ENTER:
                resizeWindow();
                break;
            case KeyEvent.VK_PLUS:
                zoomIn();
                break;
            case KeyEvent.VK_MINUS:
                zoomOut();
                break;
            case KeyEvent.VK_ESCAPE:
                minimize();
                break;
            case KeyEvent.VK_NUMPAD0:
                zoomFactor=0;
                zoomIn();
                break;
            case KeyEvent.VK_NUMPAD1:
                zoomFactor=6;
                zoomIn();
                break;
            case KeyEvent.VK_NUMPAD2:
                zoomFactor=12;
                zoomIn();
                break;
            case KeyEvent.VK_NUMPAD3:
                currentPage=size-1;
                jumpPage(currentPage);
                break;
            case KeyEvent.VK_NUMPAD4:
                zoomOut();
                break;
            case KeyEvent.VK_NUMPAD5:
                zoomIn();
                break;
            case KeyEvent.VK_NUMPAD9:
                currentPage=0;
                jumpPage(currentPage);
                break;
            default:
        }
    }
    
    
    
    /////////////////////
    /*mouse drag scroll*/
    /////////////////////
    
    int x=0; int y=0;
    public void dragMouse(MouseEvent evt){
        int tempx=evt.getXOnScreen();
        int tempy=evt.getYOnScreen();
        jScrollPane1.getVerticalScrollBar().setValue(jScrollPane1.getVerticalScrollBar().getValue()+y-tempy);
        jScrollPane1.getHorizontalScrollBar().setValue(jScrollPane1.getHorizontalScrollBar().getValue()+x-tempx);
        x=evt.getXOnScreen();
        y=evt.getYOnScreen();
    }
    
    public void PressedMouse(MouseEvent evt){
        x=evt.getXOnScreen();
        y=evt.getYOnScreen();
        webImage1.setCursor(new Cursor(Cursor.HAND_CURSOR));
        webImage2.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public void releaseMouse(){
        webImage1.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        webImage2.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    
    
    ////////////////////////
    /*zoom control methods*/
    ////////////////////////
    
    int zoomFactor=0;
    int zoomValue=100;
    final int maxfactor=12;
    
    public void zoomIn(){
        zoomFactor+=1;
        if (zoomFactor>maxfactor) {
            zoomFactor=maxfactor;
        }
        webPanel4.setPreferredSize(new Dimension(jScrollPane1.getWidth()+zoomFactor*zoomValue, jScrollPane1.getHeight()+zoomFactor*zoomValue));
        webPanel4.setSize(new Dimension(jScrollPane1.getWidth()+zoomFactor*zoomValue, jScrollPane1.getHeight()+zoomFactor*zoomValue));
        webPanel2.setPreferredSize(new Dimension(jScrollPane1.getWidth()+zoomFactor*zoomValue, jScrollPane1.getHeight()+zoomFactor*zoomValue));
        webPanel2.setSize(new Dimension(jScrollPane1.getWidth()+zoomFactor*zoomValue, jScrollPane1.getHeight()+zoomFactor*zoomValue));
        webPanel2.revalidate();
    }
    public void zoomOut(){
        zoomFactor-=1;
        if (zoomFactor<-maxfactor) {
            zoomFactor=-maxfactor;
        }
        if (zoomFactor<0) {
            webPanel4.setPreferredSize(new Dimension(jScrollPane1.getWidth()+(zoomFactor*zoomValue), jScrollPane1.getHeight()+(zoomFactor*zoomValue)));
            webPanel4.setSize(new Dimension(jScrollPane1.getWidth()+(zoomFactor*zoomValue), jScrollPane1.getHeight()+(zoomFactor*zoomValue)));
            webPanel2.setPreferredSize(new Dimension(jScrollPane1.getWidth()+(zoomFactor*zoomValue), jScrollPane1.getHeight()+(zoomFactor*zoomValue)));
            webPanel2.setSize(new Dimension(jScrollPane1.getWidth()+(zoomFactor*zoomValue), jScrollPane1.getHeight()+(zoomFactor*zoomValue)));
            webPanel2.revalidate();
        }else{
            webPanel2.setPreferredSize(new Dimension(jScrollPane1.getWidth()+(zoomFactor*zoomValue), jScrollPane1.getHeight()+(zoomFactor*zoomValue)));
            webPanel2.setSize(new Dimension(jScrollPane1.getWidth()+(zoomFactor*zoomValue), jScrollPane1.getHeight()+(zoomFactor*zoomValue)));
            webPanel2.setLocation(webPanel4.getLocation());
            webPanel2.revalidate();
        }
    }
    
    
    
    
    boolean resize=true;
    private void resizeWindow() {
        if (resize) {
            JFrame frame = this;
            frame.dispose();
            frame.setVisible(false);
            frame.setExtendedState(MAXIMIZED_BOTH);
            frame.setUndecorated(true);
            frame.setVisible(true);
            webPanel2.requestFocusInWindow();
            resize=false;
        }else{
            JFrame frame = this;
            frame.dispose();
            frame.setVisible(false);
            frame.setUndecorated(false);
            frame.setExtendedState(0);
            frame.setSize(720, 400);
            frame.setPreferredSize(new Dimension(720, 400));
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            webPanel2.requestFocusInWindow();
            resize=true;
        }
    }
    
    public void close(){
    }
    
    public void minimize(){
        
        JFrame frame = this;
        frame.dispose();
        frame.setVisible(false);
        frame.setUndecorated(false);
        frame.setSize(720, 400);
        frame.setPreferredSize(new Dimension(720, 400));
        frame.setExtendedState(ICONIFIED);
        frame.setVisible(true);
        webPanel2.requestFocusInWindow();
        resize=true;
    }
    
    public void openDocument(String path){
        pauseLoader();
        this.path=path;
        runnable=false;
        try {
            if (backLoader!=null) {
                backLoader.join();
            }
        } catch (Exception e) {
        }
        resumeLoader();
        try {
            if (file!=null) {
                file.closeFile();
            }
        } catch (IOException ex) {
            
        } catch (InterruptedException ex) {
            
        }
        file = new AxionPDFFile(path);
        setTitle(file.getTitle());
        size = file.getLength();
        System.out.println("size "+size);
        if(queue!=null){
            queue.clean();
        }
        queue  = new PageQueue();
        backLoader.setPriority(Thread.NORM_PRIORITY-1);
        runnable=true;
        backLoader=new Thread(new Runnable() {
            @Override
            public void run() {
                loop1:
                while(runnable){
                    try {

    //                    System.out.println("resumed");
                        long startTime = System.currentTimeMillis();
                        int tmpPage=currentPage;
                        int il = 0;
                        int ir = 0;
                        if (currentPage != previousPage || queue.getMin() < currentPage-MIN_PAGES_LOAD || queue.getMax() > currentPage+MIN_PAGES_LOAD) {//if currentPage changed
        //                    System.out.println("loading "+(tmpPage-MIN_PAGES_LOAD)+" to "+(tmpPage+MIN_PAGES_LOAD));
                            for (il = tmpPage, ir = tmpPage; il > tmpPage-MIN_PAGES_LOAD || ir < tmpPage+MIN_PAGES_LOAD; il--, ir++) {
        //                        if(tmpPage!=currentPage){
        //                            continue loop1;
        //                        }
        //                        System.out.println(" exists(ir):"+queue.exists(ir)+" ir>=0:"+(ir>=0)+" ir<="+size+":"+(ir<=size));
        //                        System.out.println(" exists(il):"+queue.exists(il)+" il>=0:"+(il>=0)+" il<="+size+":"+(il<=size));
                                if (!queue.exists(ir) && ir>=0 && ir<size) {
                                    try {
                                        queue.put(ir, file.getPageImage(ir));
                                    } catch (Exception e) {
                                    }
        //                            try {
        //                                backLoader.wait(SLEEP);
        //                            } catch (Exception e) {
        //                            }
                                }
                                if (!queue.exists(il) && il>=0 && il<size) {
                                    try {
                                        queue.put(il, file.getPageImage(il));
                                    } catch (Exception e) {
                                    }
        //                            try {
        //                                backLoader.wait(SLEEP);
        //                            } catch (Exception e) {
        //                            }
                                }
                            }
        //                    System.out.println("loaded "+il+" to "+ir+" queue size:"+queue.length());
                        }

                        if (queue.length()>MIN_PAGES_TODEL) {
    //                        System.out.println("removing <"+il+" and >"+ir );
                            for (int i = 0; i < queue.length(); i++) {
                                if (i<il || i>ir) {

                                    try {
                                        queue.remove(i);
                                    } catch (Exception e) {
                                    }
                                }
        //                        if(tmpPage!=currentPage){
        //                            continue loop1;
        //                        }
                            }
                        }

                        //System.out.println((startTime-System.currentTimeMillis())/1000+" s");

                        try {
    //                        System.out.println("waiting");
                            Thread.sleep(SLEEP);
                        } catch (Exception e1) {
                        }

                    } catch (OutOfMemoryError eE) {
                        openDocument(path);
                    }
                }//while
            }
        });
        backLoader.setPriority(Thread.NORM_PRIORITY-1);
        backLoader.setName("backLoader");
        backLoader.start();
        jumpPage(currentPage);
        setLocationRelativeTo(null);
        webLabel1.setText("/ "+size);
        
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        webPanel1 = new com.alee.laf.panel.WebPanel();
        webPanel3 = new com.alee.laf.panel.WebPanel();
        webButton1 = new com.alee.laf.button.WebButton();
        webButton2 = new com.alee.laf.button.WebButton();
        webButton3 = new com.alee.laf.button.WebButton();
        webButton4 = new com.alee.laf.button.WebButton();
        webButton5 = new com.alee.laf.button.WebButton();
        webSlider1 = new com.alee.laf.slider.WebSlider();
        webButton6 = new com.alee.laf.button.WebButton();
        webToggleButton1 = new com.alee.laf.button.WebToggleButton();
        webButton7 = new com.alee.laf.button.WebButton();
        webTextField1 = new com.alee.laf.text.WebTextField();
        webLabel1 = new com.alee.laf.label.WebLabel();
        webButton8 = new com.alee.laf.button.WebButton();
        webButton9 = new com.alee.laf.button.WebButton();
        webButton10 = new com.alee.laf.button.WebButton();
        webButton11 = new com.alee.laf.button.WebButton();
        webPanel5 = new com.alee.laf.panel.WebPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        webPanel4 = new com.alee.laf.panel.WebPanel();
        webPanel2 = new com.alee.laf.panel.WebPanel();
        webImage1 = new com.alee.extended.image.WebImage();
        webImage2 = new com.alee.extended.image.WebImage();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                formMouseEntered(evt);
            }
        });

        webPanel1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                webPanel1ComponentResized(evt);
            }
        });

        webPanel3.setBackground(new java.awt.Color(51, 51, 51));
        webPanel3.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        webButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/back.png"))); // NOI18N
        webButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton1ActionPerformed(evt);
            }
        });

        webButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/forward.png"))); // NOI18N
        webButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton2ActionPerformed(evt);
            }
        });

        webButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/zoom_in.png"))); // NOI18N
        webButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton3ActionPerformed(evt);
            }
        });

        webButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/zoom_out.png"))); // NOI18N
        webButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton4ActionPerformed(evt);
            }
        });

        webButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/maximize.png"))); // NOI18N
        webButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton5ActionPerformed(evt);
            }
        });

        webSlider1.setBackground(new java.awt.Color(242, 242, 242));
        webSlider1.setMaximum(12);
        webSlider1.setMinimum(-12);
        webSlider1.setPaintLabels(true);
        webSlider1.setSnapToTicks(true);
        webSlider1.setToolTipText("");
        webSlider1.setValue(0);
        webSlider1.setThumbBgBottom(new java.awt.Color(242, 242, 242));
        webSlider1.setThumbBgTop(new java.awt.Color(242, 242, 242));
        webSlider1.setTrackBgBottom(new java.awt.Color(242, 242, 242));
        webSlider1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                webSlider1StateChanged(evt);
            }
        });

        webButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/exit.png"))); // NOI18N
        webButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton6ActionPerformed(evt);
            }
        });

        webToggleButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/sildslip.png"))); // NOI18N
        webToggleButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webToggleButton1ActionPerformed(evt);
            }
        });

        webButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/iconified.png"))); // NOI18N

        webTextField1.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        webTextField1.setText("1");
        webTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webTextField1ActionPerformed(evt);
            }
        });

        webLabel1.setForeground(new java.awt.Color(255, 255, 255));
        webLabel1.setText(" / 1 ");

        webButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/list.png"))); // NOI18N

        webButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/open.png"))); // NOI18N
        webButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton9ActionPerformed(evt);
            }
        });

        webButton10.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/first.png"))); // NOI18N
        webButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton10ActionPerformed(evt);
            }
        });

        webButton11.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Icons16/last.png"))); // NOI18N
        webButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                webButton11ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout webPanel3Layout = new javax.swing.GroupLayout(webPanel3);
        webPanel3.setLayout(webPanel3Layout);
        webPanel3Layout.setHorizontalGroup(
            webPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(webButton8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(webButton9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(webButton10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(webButton11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10, 10, 10)
                .addComponent(webSlider1, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(webLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 48, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(webToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(webButton3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(webButton4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(webButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(webButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(webButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(webButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(webButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        webPanel3Layout.setVerticalGroup(
            webPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(webButton5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(webButton6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(webButton7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(webButton4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webButton8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webButton9, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webButton10, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webButton3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(webPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(webTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(webLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(webButton2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webToggleButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webButton1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webButton11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webSlider1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        webPanel5.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                webPanel5KeyPressed(evt);
            }
        });

        jScrollPane1.setBorder(null);
        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

        webPanel2.setBackground(new java.awt.Color(101, 101, 95));
        webPanel2.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webPanel2MouseWheelMoved(evt);
            }
        });
        webPanel2.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                webPanel2ComponentResized(evt);
            }
        });
        webPanel2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                webPanel2KeyPressed(evt);
            }
        });
        webPanel2.setLayout(new java.awt.GridLayout(1, 2));

        webImage1.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                webImage1MouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                webImage1MouseMoved(evt);
            }
        });
        webImage1.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webImage1MouseWheelMoved(evt);
            }
        });
        webImage1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                webImage1MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                webImage1MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                webImage1MouseReleased(evt);
            }
        });
        webImage1.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                webImage1ComponentResized(evt);
            }
        });
        webPanel2.add(webImage1);

        webImage2.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                webImage2MouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                webImage2MouseMoved(evt);
            }
        });
        webImage2.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                webImage2MouseWheelMoved(evt);
            }
        });
        webImage2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                webImage2MouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                webImage2MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                webImage2MouseReleased(evt);
            }
        });
        webPanel2.add(webImage2);

        javax.swing.GroupLayout webPanel4Layout = new javax.swing.GroupLayout(webPanel4);
        webPanel4.setLayout(webPanel4Layout);
        webPanel4Layout.setHorizontalGroup(
            webPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webPanel4Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(webPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 780, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );
        webPanel4Layout.setVerticalGroup(
            webPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webPanel4Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(webPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        jScrollPane1.setViewportView(webPanel4);

        webPanel5.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        javax.swing.GroupLayout webPanel1Layout = new javax.swing.GroupLayout(webPanel1);
        webPanel1.setLayout(webPanel1Layout);
        webPanel1Layout.setHorizontalGroup(
            webPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(webPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(webPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        webPanel1Layout.setVerticalGroup(
            webPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(webPanel1Layout.createSequentialGroup()
                .addComponent(webPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(webPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, 365, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(webPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(webPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void webButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton2ActionPerformed
        // TODO add your handling code here:
        currentPage+=2;
        jumpPage(currentPage);
    }//GEN-LAST:event_webButton2ActionPerformed

    private void webButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton1ActionPerformed
        // TODO add your handling code here:
        currentPage-=2;
        jumpPage(currentPage);
    }//GEN-LAST:event_webButton1ActionPerformed

    private void webPanel2ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_webPanel2ComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_webPanel2ComponentResized

    private void webPanel1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_webPanel1ComponentResized
        // TODO add your handling code here:
    }//GEN-LAST:event_webPanel1ComponentResized

    private void webImage1ComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_webImage1ComponentResized
        // TODO add your handling code here:
        updatePages();
    }//GEN-LAST:event_webImage1ComponentResized
    
    private void webButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton3ActionPerformed
        // TODO add your handling code here:
        zoomIn();
    }//GEN-LAST:event_webButton3ActionPerformed

    private void webButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton4ActionPerformed
        // TODO add your handling code here:
        zoomOut();
    }//GEN-LAST:event_webButton4ActionPerformed

    private void webButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton5ActionPerformed
        // TODO add your handling code here:
        resizeWindow();
    }//GEN-LAST:event_webButton5ActionPerformed
    
    private void webImage1MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage1MouseDragged
        // TODO add your handling code here:
        dragMouse(evt);
    }//GEN-LAST:event_webImage1MouseDragged
    
    private void webImage1MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage1MousePressed
        // TODO add your handling code here:
        PressedMouse(evt);
    }//GEN-LAST:event_webImage1MousePressed

    private void webImage1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage1MouseClicked
        // TODO add your handling code here:
        if (evt.getButton()==2) {
            resizeWindow();
        }
    }//GEN-LAST:event_webImage1MouseClicked

    private void webImage1MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage1MouseReleased
        // TODO add your handling code here:
        releaseMouse();
    }//GEN-LAST:event_webImage1MouseReleased

    private void webImage2MousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage2MousePressed
        // TODO add your handling code here:
        PressedMouse(evt);
    }//GEN-LAST:event_webImage2MousePressed

    private void webImage2MouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage2MouseDragged
        // TODO add your handling code here:
        dragMouse(evt);
    }//GEN-LAST:event_webImage2MouseDragged

    private void webImage2MouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage2MouseReleased
        // TODO add your handling code here:
        releaseMouse();
    }//GEN-LAST:event_webImage2MouseReleased

    private void webSlider1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_webSlider1StateChanged
        // TODO add your handling code here:
        if (webSlider1.getValue()==0) {
            webPanel4.setPreferredSize(new Dimension(jScrollPane1.getWidth(), jScrollPane1.getHeight()));
            webPanel4.setSize(new Dimension(jScrollPane1.getWidth(), jScrollPane1.getHeight()));
            webPanel2.setPreferredSize(new Dimension(jScrollPane1.getWidth(), jScrollPane1.getHeight()));
            webPanel2.setSize(new Dimension(jScrollPane1.getWidth(), jScrollPane1.getHeight()));
            webPanel2.revalidate();
        }else if(webSlider1.getValue()>0){
            zoomIn();
        }else if(webSlider1.getValue()<0){
            zoomOut();
        }
        webPanel2.requestFocusInWindow();
    }//GEN-LAST:event_webSlider1StateChanged

    private void webButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton6ActionPerformed
        // TODO add your handling code here:
        dispose();
    }//GEN-LAST:event_webButton6ActionPerformed

    private void webPanel5KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_webPanel5KeyPressed
        // TODO add your handling code here:
    }//GEN-LAST:event_webPanel5KeyPressed

    private void webPanel2KeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_webPanel2KeyPressed
        // TODO add your handling code here:
        keyPressing(evt);
    }//GEN-LAST:event_webPanel2KeyPressed

    private void webImage1MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_webImage1MouseWheelMoved
        // TODO add your handling code here:
        mouseWheelZoom(evt);
    }//GEN-LAST:event_webImage1MouseWheelMoved

    private void webPanel2MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_webPanel2MouseWheelMoved
        // TODO add your handling code here:
        mouseWheelRotate(evt);
    }//GEN-LAST:event_webPanel2MouseWheelMoved

    private void webImage2MouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_webImage2MouseWheelMoved
        // TODO add your handling code here:
        mouseWheelZoom(evt);
    }//GEN-LAST:event_webImage2MouseWheelMoved

    private void webImage2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage2MouseClicked
        // TODO add your handling code here:
        if (evt.getButton()==2) {
            resizeWindow();
        }
    }//GEN-LAST:event_webImage2MouseClicked

    private void webToggleButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webToggleButton1ActionPerformed
        // TODO add your handling code here:
        if (webToggleButton1.isSelected()) {
            SlideSlipOn=true;
            currentPage--;
        }else{
            SlideSlipOn=false;
            currentPage++;
        }
        webPanel2.requestFocusInWindow();
    }//GEN-LAST:event_webToggleButton1ActionPerformed

    private void webTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webTextField1ActionPerformed
        // TODO add your handling code here:
        currentPage=Integer.parseInt(webTextField1.getText());
        jumpPage(currentPage);
    }//GEN-LAST:event_webTextField1ActionPerformed

    private void webButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton10ActionPerformed
        // TODO add your handling code here:
        currentPage=0;
        jumpPage(currentPage);
    }//GEN-LAST:event_webButton10ActionPerformed

    private void webButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton11ActionPerformed
        // TODO add your handling code here:
        currentPage=size-1;
        jumpPage(currentPage);
    }//GEN-LAST:event_webButton11ActionPerformed

    private void webButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_webButton9ActionPerformed
        // TODO add your handling code here:
        new OpenPDFFile(this).setVisible(true);
    }//GEN-LAST:event_webButton9ActionPerformed

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        // TODO add your handling code here:
    }//GEN-LAST:event_formMouseMoved

    private void formMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
        // TODO add your handling code here:
    }//GEN-LAST:event_formMouseEntered

    private void webImage1MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage1MouseMoved
        // TODO add your handling code here:
        if (evt.getY()<=50) {
            webPanel3.setVisible(true);
        }else{
            webPanel3.setVisible(false);
        }
    }//GEN-LAST:event_webImage1MouseMoved

    private void webImage2MouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_webImage2MouseMoved
        // TODO add your handling code here:
        if (evt.getY()<=50) {
            webPanel3.setVisible(true);
        }else{
            webPanel3.setVisible(false);
        }
    }//GEN-LAST:event_webImage2MouseMoved

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AxionPDFViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AxionPDFViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AxionPDFViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AxionPDFViewer.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AxionPDFViewer("").setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private com.alee.laf.button.WebButton webButton1;
    private com.alee.laf.button.WebButton webButton10;
    private com.alee.laf.button.WebButton webButton11;
    private com.alee.laf.button.WebButton webButton2;
    private com.alee.laf.button.WebButton webButton3;
    private com.alee.laf.button.WebButton webButton4;
    private com.alee.laf.button.WebButton webButton5;
    private com.alee.laf.button.WebButton webButton6;
    private com.alee.laf.button.WebButton webButton7;
    private com.alee.laf.button.WebButton webButton8;
    private com.alee.laf.button.WebButton webButton9;
    private com.alee.extended.image.WebImage webImage1;
    private com.alee.extended.image.WebImage webImage2;
    private com.alee.laf.label.WebLabel webLabel1;
    private com.alee.laf.panel.WebPanel webPanel1;
    public com.alee.laf.panel.WebPanel webPanel2;
    private com.alee.laf.panel.WebPanel webPanel3;
    private com.alee.laf.panel.WebPanel webPanel4;
    private com.alee.laf.panel.WebPanel webPanel5;
    private com.alee.laf.slider.WebSlider webSlider1;
    private com.alee.laf.text.WebTextField webTextField1;
    private com.alee.laf.button.WebToggleButton webToggleButton1;
    // End of variables declaration//GEN-END:variables
    

}
