/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axionlibrary;

import com.alee.extended.image.WebImage;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 *
 * @author thusitha
 */
public class AxionTools {
    private static IDGenerator idgen = new IDGenerator();
    private static LogPop logpop = new LogPop();
    private static AxionIO axionio = new AxionIO();
    private static AxionObjectConverter objectconvert = new AxionObjectConverter();
    private static AxionDateTime datetime = new AxionDateTime();
    
    private static String textCopy="";
    private static String sysCopy="";
    
    
    
    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
        AxionTools t = new AxionTools();
        axionio.saveObject(new String("my st"),"Data/jas.txt");
        System.out.println(axionio.getObject("Data/jas.txt"));
    }
    
    
    
    public static String openTextFile(String path){
        return axionio.openTextFile(path);
    }
    
    public static void saveTextFile(String text, String path){
        axionio.saveTextFile(text, path+".txt");
    }
    
    public static long getMilies(Date date){
        return datetime.getMilies(date);
    }
    
    public static Date now(){
        return datetime.now();
    }
    
    public static Date setTimeOfDay(Date date, int hour, int mins){
        return datetime.setTimeOfDay(date, hour, mins);
    }
    
    public static int getMonth(Date date){
        return datetime.getMonth(date);
    }
    
    public static int getYear(Date date){
        return datetime.getYear(date);
    }
    
    public static Date getDate(int year, int month, int day){
        return datetime.getDate(year, month, day);
    }
    
    public static Date getYesterday(){
        return datetime.getYesterday();
    }
    
    public static Date getTomorrow(){
        return datetime.getTomorrow();
    }
    
    public static Date getRelativeDateTo(Date date, int daysToCalculate){
        return datetime.getRelativeDateTo(date, daysToCalculate);
    }
    
    public static Date getRelativeDate(int numDays){
        return datetime.getRelativeDate(numDays);
    }
    
    public static void copy(String text){
        textCopy=text;
    }
    
    public static String paste(){
        return textCopy;
    }
    
    public static void selectAll(){
        try {
            Robot robo = new Robot();
            robo.keyPress(KeyEvent.VK_CONTROL);
            robo.keyPress(KeyEvent.VK_A);
            robo.keyRelease(KeyEvent.VK_A);
            robo.keyRelease(KeyEvent.VK_CONTROL);
        } catch (AWTException ex) {
            Logger.getLogger(AxionTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public static byte[] getBytesFromFile(File file) throws IOException{
        return objectconvert.getBytesFromFile(file);
    }
    
    public static byte[] convImageToByte(String path) throws IOException{
        return objectconvert.convImageToBytes(path);
    }
    
    public static byte[] convObJToByte(Object obj) throws IOException {
        return objectconvert.serialize(obj);
    }
    
    public static Object convByteToOBJ(byte[] data) throws IOException, ClassNotFoundException {
        return objectconvert.deserialize(data);
    }
    
    public static String generateID(){
        return idgen.generateID();
    }
    
    public static String generateID(String prefix){
        return idgen.generateID(prefix);
    }
    
    public static void saveObject(Object obj, String path){
        axionio.saveObject(obj, path);
    }
    
    public static void savePrivateObject(Object obj, String path){
        axionio.savePrivateObject(obj, path);
    }
    
    public static Object getObject(String path){
        return axionio.getObject(path);
    }
    
    public static Object getPrivateObject(String path){
        return axionio.getPrivateObject(path);
    }
}
class IDGenerator{
    /*create unique ids*/
    public String generateID(String prefix){
        String s=prefix+System.nanoTime();
        //return s.substring(s.length()-4, s.length());
        return s;
    }
    
    public String generateID(){
        //return String.valueOf(System.nanoTime()).substring(0, String.valueOf(System.nanoTime()).length()-4);
        return String.valueOf(System.nanoTime());
    }
}

class LogPop{
    
    public static final int WARN=1;
    public static final int ERROR=2;
    public static final int OK=3;
    public static final int PRINT=4;
    public static final int INFO=5;
    
    
    
    
    public ImageIcon getIcon(int icon){
        switch (icon) {
            case WARN:
                return new ImageIcon(getClass().getResource("/Icons/warning32.png"));
            case ERROR:
                return new ImageIcon(getClass().getResource("/Icons/cancel3232.png"));
            case OK:
                return new ImageIcon(getClass().getResource("/Icons/ok32.png"));
            case PRINT:
                return new ImageIcon(getClass().getResource("/Icons/print32.png"));
            case INFO:
                return new ImageIcon(getClass().getResource("/Icons/info32.png"));
        }
        return null;
    }
}

class AxionIO{
    
    public void saveObject(Object obj, String path){
        String classpath;
        classpath = System.getProperty("user.dir")+path;
        
        File parent = new File(classpath).getParentFile();
        
        if(!parent.exists()){
            parent.mkdirs();
        }
        
        if(!new File(classpath).exists()){
            try {
                new File(classpath).createNewFile();
            } catch (IOException ex) {
                
            }
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(classpath))){
            oos.writeObject(obj);
            oos.flush();
        } catch (Exception e) {
            
        }
    }
    
    public void savePrivateObject(Object obj, String path){
        
        File parent = new File(path).getParentFile();
        
        if(!parent.exists()){
            parent.mkdirs();
        }
        
        if(!new File(path).exists()){
            try {
                new File(path).createNewFile();
            } catch (IOException ex) {
                
            }
        }
        
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))){
            oos.writeObject(obj);
            oos.flush();
            System.out.println(new File(path).getAbsoluteFile());
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    
    public Object getObject(String path){
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(System.getProperty("user.dir")+path))){
            return ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
    
    public Object getPrivateObject(String path){
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))){
            return ois.readObject();
        } catch (Exception e) {
            return null;
        }
    }
    
    public void saveTextFile(String text, String path){
        FileWriter fw=null;
        try {
            String ntext=text.replaceAll("(?!\\r)\\n", "\r\n");
            File file = new File(path);
            // if file doesnt exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }   fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(ntext);
            bw.close();
        } catch (IOException ex) {
            Logger.getLogger(AxionIO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fw.close();
            } catch (IOException ex) {
                Logger.getLogger(AxionIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    
    public String openTextFile(String path){
        FileReader fr = null;
        StringBuilder text=new StringBuilder();
        try {
            fr = new FileReader(new File(path));
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while(line != null){
              text.append(line + "\n");
              line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(AxionIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AxionIO.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                fr.close();
            } catch (IOException ex) {
                Logger.getLogger(AxionIO.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        //text=text.replaceAll("\r\n", "(?!\\r)\\n");
        //text=text.replaceAll("(?!\\r)\\n", "\r\n");
        return text.toString();
    }
}
class AxionObjectConverter{
    public byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }
    public Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
    
    public byte[] convImageToBytes (String ImageName) throws IOException {
        // open image
        File imgPath = new File(ImageName);
        BufferedImage bufferedImage = ImageIO.read(imgPath);

        // get DataBufferBytes from Raster
        WritableRaster raster = bufferedImage .getRaster();
        DataBufferByte data   = (DataBufferByte) raster.getDataBuffer();

        return data.getData();
    }
    // Returns the contents of the file in a byte array.
    public byte[] getBytesFromFile(File file) throws IOException {        
        // Get the size of the file
        long length = file.length();

        // You cannot create an array using a long type.
        // It needs to be an int type.
        // Before converting to an int type, check
        // to ensure that file is not larger than Integer.MAX_VALUE.
        if (length > Integer.MAX_VALUE) {
            // File is too large
            throw new IOException("File is too large!");
        }

        // Create the byte array to hold the data
        byte[] bytes = new byte[(int)length];

        // Read in the bytes
        int offset = 0;
        int numRead = 0;

        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                   && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }

        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }
        return bytes;
    }
}

class AxionDateTime{

    public Date getRelativeDate(int daysToCalculate){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, daysToCalculate);    
        return cal.getTime();
    }
    
    public Date getRelativeDateTo(Date date, int daysToCalculate){
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, daysToCalculate);    
        return cal.getTime();
    }
    
    public Date getYesterday(){
        return getRelativeDate(-1);
    }
    
    public Date getTomorrow(){
        return getRelativeDate(+1);
    }
    
    public Date getDate(int year, int month, int day){
        Calendar cal = new GregorianCalendar(year, month, day);
        return cal.getTime();
    }
    
    public int getYear(Date date){
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.YEAR);
    }
    
    public int getMonth(Date date){
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal.get(Calendar.MONTH);
    }
    
    public Date setTimeOfDay(Date date, int hour, int mins){
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY,hour);
        cal.set(Calendar.MINUTE,mins);
        return cal.getTime();
    }
    
    public long getMilies(Date date){
        return date.getTime();
    }
    
    public Date now(){
        return new Date();
    }
    
}

class AxionCollection<A,B>{
    
    public static final int ASCENDING=0;
    public static final int DESCENDING=1;
    
    Object[] a;
    Object[] b;

    public AxionCollection() {
        this.a= new Object[0];
        this.b= new Object[0];
    }
    
    public void put(A key, B val){
        Object[] tempa = new Object[a.length+1];
        Object[] tempb = new Object[b.length+1];
        
        for (int i = 0; i < a.length; i++) {
            tempa[i]=a[i];
            tempb[i]=b[i];
        }
        
        tempa[a.length]=key;
        tempb[b.length]=val;
        
        a=tempa;
        b=tempb;
    }
    
    public void putUniq(A key, B val){
        Object[] tempa = new Object[a.length+1];
        Object[] tempb = new Object[b.length+1];
        
        boolean exists = false;
        for (int i = 0; i < a.length; i++) {
            tempa[i]=a[i];
            tempb[i]=b[i];
            if (key.equals(a[i])) {
                exists=true;
            }
        }
        if(!exists){
            tempa[a.length]=key;
            tempb[b.length]=val;
            a=tempa;
            b=tempb;
        }
    }
    
    public B get(A key){
        B b1=null;
        for (int i = 0; i < a.length; i++) {
            try {
                if(key.equals(a[i])){
                    return (B) b[i];
                }
            } catch (Exception e) {
            }
        }
        return b1;
    }
    
    public int length(){
        return a.length;
    }
    
    public int size(){
        return a.length;
    }
    
    public A[] getKeys(){
        return (A[]) a;
    }
    
    public A getKey(B val){
        A a1=null;
        for (int i = 0; i < b.length; i++) {
            if (val.equals(b[i])) {
                return (A) a[i];
            }
        }
        return null;
    }
    
    
    public A[] getValues(){
        return (A[]) a;
    }
    
    public B getByArrayIndex(int i){
        return (B) b[i];
    }
    
    public void remove(A key){
        int exists = 0;
        for (int i = 0; i < a.length; i++) {
            if(key.equals(a[i])){
                exists++;
            }
        }
        if (exists>0) {
            Object[] tempa = new Object[a.length-exists];
            Object[] tempb = new Object[b.length-exists];
            
            for (int i = 0, j=0; i < a.length; i++) {
                if(key.equals(a[i])){
                    
                }else if (!key.equals(a[i])) {
                    tempa[j]=a[i];
                    tempb[j]=b[i];
                    j++;
                }
            }
            a=tempa;
            b=tempb;
        }
    }
    
    public void removeAll(){
        a=new Object[0];
        b=new Object[0];
    }
    
    public void clean(){
        for (int i = 0; i < a.length; i++) {
            a[i]=null;
            b[i]=null;
        }
    }
    
    public void set(A key, B value){
        for (int i = 0; i < a.length; i++) {
            if(key.equals(a[i])){
                b[i]=value;
            }
        }
    }
    
    public void display(){
        for (int i = 0; i < a.length; i++) {
            System.out.println(a[i]+","+b[i]);
        }
    }
    
    public boolean findByKey(A key){
        for (int i = 0; i < a.length; i++) {
            if(key.equals(a[i])){
                return true;
            }
        }
        return false;
    }
    
    private boolean findDuplicates(Object[] ob, Object key){
        for (int i = 0; i < ob.length; i++) {
            if(key.equals(ob[i])){
                return true;
            }
        }
        return false;
    }
    
    public boolean findByVal(B val){
        for (int i = 0; i < b.length; i++) {
            if(val.equals(b[i])){
                return true;
            }
        }
        return false;
    }
    
    public void removeDuplicatesByKey(){
        HashSet<Object> set = new HashSet<>();
        final int len = a.length;
        for(int i = 0; i < len; i++){
            set.add(a[i]);
        }

        Object[] keylist = new Object[set.size()];
        Object[] vallist = new Object[set.size()];
        
        int i = 0;
        for (Iterator<Object> it = set.iterator(); it.hasNext();) {
            keylist[i] = it.next();
            vallist[i] = get((A) keylist[i]);
            i++;
        }
        a=keylist;
        b=vallist;
    }
    
    public void removeDuplicatesByVal(){
        HashSet<Object> set = new HashSet<>();
        final int len = b.length;
        for(int i = 0; i < len; i++){
            set.add(b[i]);
        }

        Object[] keylist = new Object[set.size()];
        Object[] vallist = new Object[set.size()];
        
        int i = 0;
        for (Iterator<Object> it = set.iterator(); it.hasNext();) {
            vallist[i] = it.next();
            keylist[i] = getKey((B) vallist[i]);
            i++;
        }
        a=keylist;
        b=vallist;
    }
    
    public static void main(String[] args) {
        AxionCollection<Integer,String> c = new AxionCollection<Integer, String>();
        c.put(1, "apple");
        c.put(1, "mango");
        c.put(2, "orange");
        c.put(3, "orange");
        c.put(2, "cherry");
        c.put(4, "apple");
        c.put(5, "strawberry");
        System.out.println(c.length());
        
        System.out.println("preview");
        c.display();
        System.out.println("remove Dup by key");
        c.removeDuplicatesByKey();
        c.display();
        System.out.println("remove Dup by val");
        c.removeDuplicatesByVal();
        c.display();
    }
    
    
    
}