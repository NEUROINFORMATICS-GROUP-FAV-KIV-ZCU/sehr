/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package pl.mateusz;

import java.io.File;
import java.util.ArrayList;

/**
 * @author Erasmus Students
 */
public class IOManager 
{
    /**
     * Fields
     */
    private ArrayList<File> files;
    
    /**
     * Method return list of loaded files
     * @return list of loaded files
     */
    public ArrayList<File> getFiles()
    {
        return this.files;
    }
            
    /**
     * Method to loading archetypes files from directory
     * @param directoryPath path to directory, where are an Archetypes
     */
    public void loadFiles(String directoryPath)
    {
        File dir = new File(directoryPath);
        this.files = new ArrayList<File>();
        
        for(File single : dir.listFiles())
        {
            this.files.add(new File(single.getAbsolutePath()));
        }
    }    
}
