/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package axionlibrary;

import java.awt.Image;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author thusitha
 */
public class PageQueue{
    
    Map map = new TreeMap<Integer, Image>();
    
    public void put(int key, Image image){
        map.put(key, image);
        //System.out.println(key+" put "+image!=null?true:false);
    }
    
    public void remove(int key){
        map.remove(key);
    }
    
    public Image get(int key){
        return (Image) map.get(key);
    }
    
    public Integer[] getKeys(){
        return (Integer[]) map.keySet().toArray();
    }
    
    public int length(){
        return map.size();
    }
    
    public Integer getMin(){
        if (map.isEmpty()) {
            return 0;
        }
        return (Integer) map.keySet().toArray()[0];
    }
    
    public Integer getMax(){
        if (map.isEmpty()) {
            return 0;
        }
        return (Integer) map.keySet().toArray()[map.size()-1];
    }
    
    public boolean exists(int key){
        try {
            return map.containsKey(key);
        } catch (Exception e) {
            return false;
        }
    }
    
    public static void main(String[] args) {
        PageQueue p=new PageQueue();
        p.put(12, null);
        p.put(11, null);
        p.put(15, null);
        p.put(14, null);
        p.put(13, null);
        p.put(16, null);
        System.out.println();
        System.out.println("max "+p.getMax());
        System.out.println("min "+p.getMin());
    }
    
    public void putUniq(int key, Image image){
        if (!map.containsKey(key)) {
            map.put(key, image);
        }
    }
    public void clean(){
        map.clear();
    }
}