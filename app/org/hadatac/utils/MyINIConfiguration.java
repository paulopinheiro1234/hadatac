package org.hadatac.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;

public class MyINIConfiguration extends INIConfiguration {
    
    public MyINIConfiguration(String filePath) {
        try {
            read(new InputStreamReader(MyINIConfiguration.class.getClassLoader().getResourceAsStream(filePath)));
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
